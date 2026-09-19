import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Real DDS integration, cross-checked against exhaustive minimax on small endings. */
class DdsSolverTest {
    static GameController game(Suit strain, long seed) {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) if (suit != Suit.NT) {
            for (Rank rank : Rank.values()) deck.add(new Card(suit, rank));
        }
        Collections.shuffle(deck, new Random(seed));
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            hands.put(position, new ArrayList<>(deck.subList(position.ordinal() * 13, position.ordinal() * 13 + 13)));
        }
        GameController game = new GameController();
        game.startLoadedDeal(Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, hands));
        game.submitBid(new Bid(new Seat(Position.NORTH), 1, strain));
        for (int i = 0; i < 3; i++) game.submitBid(Bid.BidPass(game.getCurrentBidder()));
        return game;
    }

    static void play(GameController game, int cards) {
        for (int i = 0; i < cards; i++) {
            Gameplay play = game.getGameplay();
            Seat seat = play.getCurrentPlayer();
            assertTrue(game.playCard(seat, play.getLegalCards(seat, play.getRemainingHand(seat)).get(0)));
        }
    }

    @ParameterizedTest
    @EnumSource(Suit.class)
    void matchesExhaustiveSearchForAllStrainsAndEveryPartialTrickLength(Suit strain) {
        GameController game = game(strain, 197);
        play(game, 40); // Three tricks left: small enough for an independent exhaustive oracle.
        for (int partial = 0; partial < 4; partial++) {
            Gameplay gameplay = game.getGameplay();
            String before = DdsSolver.encode(gameplay);
            DdsSolver.Analysis analysis = gameplay.analyzeRemainingTricks();
            List<List<Card>> hands = new ArrayList<>();
            for (Position position : Position.values()) {
                hands.add(new ArrayList<>(gameplay.getRemainingHand(new Seat(position)).getCards()));
            }
            Trick current = gameplay.getCurrentTrick();
            List<Card> played = new ArrayList<>();
            Seat seat = current.getLeader();
            for (int i = 0; i < current.getPlays().size(); i++) {
                played.add(current.getPlays().get(seat));
                seat = seat.next();
            }
            int oracle = minimax(hands, current.getLeader().getPosition().ordinal(), played, strain);
            assertEquals(oracle, analysis.declarerTricks(), strain + " partial=" + partial);
            assertEquals(3, analysis.remainingTricks());
            assertEquals(before, DdsSolver.encode(gameplay), "Analysis must not mutate play");
            assertEquals(oracle, analysis.tricksFor(new Seat(Position.SOUTH), gameplay.getContract()));
            assertEquals(3 - oracle, analysis.tricksFor(new Seat(Position.WEST), gameplay.getContract()));
            if (partial < 3) play(game, 1);
        }
    }

    @Test
    void partialTrickClaimsRejectTooManyAndAllocateRequestedNumber() {
        for (int partial = 0; partial < 4; partial++) {
            GameController game = game(Suit.NT, 197);
            play(game, 40 + partial);
            Gameplay gameplay = game.getGameplay();
            Seat actor = gameplay.getCurrentPlayer();
            DdsSolver.Analysis analysis = gameplay.analyzeRemainingTricks();
            int maximum = analysis.tricksFor(actor, gameplay.getContract());
            String before = DdsSolver.encode(gameplay);
            assertFalse(gameplay.claim(actor, maximum + 1, analysis));
            assertEquals(before, DdsSolver.encode(gameplay));
            int awarded = Math.max(0, maximum - 1);
            boolean declaring = actor.equals(gameplay.getContract().getDeclarer())
                    || actor.isPartner(gameplay.getContract().getDeclarer());
            int expected = gameplay.getDeclarerTricks() + (declaring ? awarded : 3 - awarded);
            assertTrue(gameplay.claim(actor, awarded, analysis));
            assertEquals(expected, gameplay.getDeclarerTricks());
            assertEquals(13 - expected, gameplay.getDefenderTricks());
            assertFalse(gameplay.claim(actor, awarded, analysis));
        }
    }

    // Independent, exhaustive search. NS maximizes, EW minimizes NS trick wins.
    // No DDS encoding, pruning, cached Trick winners or production card comparator is used.
    private int minimax(List<List<Card>> hands, int leader, List<Card> trick, Suit trump) {
        if (trick.size() == 4) {
            int winner = 0;
            Suit led = trick.get(0).getSuit();
            for (int i = 1; i < 4; i++) {
                Card next = trick.get(i), best = trick.get(winner);
                int nextClass = next.getSuit() == trump ? 2 : next.getSuit() == led ? 1 : 0;
                int bestClass = best.getSuit() == trump ? 2 : best.getSuit() == led ? 1 : 0;
                if (nextClass > bestClass || (nextClass == bestClass && nextClass > 0
                        && next.getRank().ordinal() > best.getRank().ordinal())) winner = i;
            }
            int seat = (leader + winner) % 4;
            return (seat % 2 == 0 ? 1 : 0) + minimax(hands, seat, new ArrayList<>(), trump);
        }
        int player = (leader + trick.size()) % 4;
        List<Card> hand = hands.get(player);
        if (hand.isEmpty()) return 0;
        Suit led = trick.isEmpty() ? null : trick.get(0).getSuit();
        boolean mustFollow = hand.stream().anyMatch(card -> card.getSuit() == led);
        int best = player % 2 == 0 ? -1 : 14;
        for (int index = 0; index < hand.size(); index++) {
            Card card = hand.get(index);
            if (mustFollow && card.getSuit() != led) continue;
            hand.remove(index);
            trick.add(card);
            int value = minimax(hands, leader, trick, trump);
            trick.remove(trick.size() - 1);
            hand.add(index, card);
            best = player % 2 == 0 ? Math.max(best, value) : Math.min(best, value);
        }
        return best;
    }

    @Test
    void refusesStaleAnalysisAndLeavesRejectedClaimUnchanged() {
        GameController controller = game(Suit.NT, 54);
        play(controller, 44);
        Gameplay gameplay = controller.getGameplay();
        DdsSolver.Analysis analysis = gameplay.analyzeRemainingTricks();
        play(controller, 1);
        String before = DdsSolver.encode(gameplay);
        assertFalse(controller.claim(0, analysis));
        assertEquals(before, DdsSolver.encode(gameplay));
        assertFalse(gameplay.isFinished());
        assertFalse(gameplay.claim(null, 1));
        assertFalse(gameplay.claim(gameplay.getCurrentPlayer().next(), 0));
        assertFalse(gameplay.claim(gameplay.getCurrentPlayer(), 14));
    }

    @Test
    void missingSolverFailsWithoutChangingThePosition() {
        GameController controller = game(Suit.CLUBS, 99);
        String before = DdsSolver.encode(controller.getGameplay());
        String old = System.getProperty("bridgetut.dds");
        try {
            System.setProperty("bridgetut.dds", "definitely-missing-dds-executable");
            assertThrows(IllegalStateException.class, () -> controller.claim(1));
            assertEquals(before, DdsSolver.encode(controller.getGameplay()));
            assertEquals(GamePhase.PLAYING, controller.getPhase());
            assertTrue(controller.concede(), "Voluntary surrender does not require a solver");
        } finally {
            if (old == null) System.clearProperty("bridgetut.dds");
            else System.setProperty("bridgetut.dds", old);
        }
    }
}
