import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GameplayTest {

    @Test
    void legalLeadFromTheCurrentPlayerIsRecordedInTheFirstTrick() {
        TestGame testGame = newTestGame();
        Card diamondTwo = cardIn(testGame.deal, Position.EAST, Suit.DIAMONDS, Rank.TWO);

        assertEquals(new Seat(Position.EAST), testGame.gameplay.getCurrentLeader());
        assertTrue(testGame.gameplay.playCard(new Seat(Position.EAST), diamondTwo));

        Trick firstTrick = testGame.gameplay.getTricks().get(0);
        assertEquals(1, firstTrick.getPlays().size());
        assertSameCard(diamondTwo, firstTrick.getPlays().get(new Seat(Position.EAST)));
    }

    @Test
    void cardOutsideThePlayersHandIsRejected() {
        TestGame testGame = newTestGame();
        Card spadeAce = cardIn(testGame.deal, Position.WEST, Suit.SPADES, Rank.ACE);

        assertFalse(testGame.gameplay.playCard(new Seat(Position.EAST), spadeAce));
    }

    @Test
    void playerWhoCanFollowSuitCannotDiscardAnotherSuit() {
        TestGame testGame = newTestGame();
        Card diamondTwo = cardIn(testGame.deal, Position.EAST, Suit.DIAMONDS, Rank.TWO);
        Card heartTwo = cardIn(testGame.deal, Position.SOUTH, Suit.HEARTS, Rank.TWO);
        Card diamondKing = cardIn(testGame.deal, Position.SOUTH, Suit.DIAMONDS, Rank.KING);

        assertTrue(testGame.gameplay.playCard(new Seat(Position.EAST), diamondTwo));
        assertFalse(testGame.gameplay.playCard(new Seat(Position.SOUTH), heartTwo));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.SOUTH), diamondKing));
    }

    @Test
    void playerWhoIsVoidInTheLedSuitMayPlayAnotherSuit() {
        TestGame testGame = newTestGame();
        Card diamondTwo = cardIn(testGame.deal, Position.EAST, Suit.DIAMONDS, Rank.TWO);
        Card diamondKing = cardIn(testGame.deal, Position.SOUTH, Suit.DIAMONDS, Rank.KING);
        Card spadeTwo = cardIn(testGame.deal, Position.WEST, Suit.SPADES, Rank.TWO);

        assertTrue(testGame.gameplay.playCard(new Seat(Position.EAST), diamondTwo));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.SOUTH), diamondKing));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.WEST), spadeTwo));
    }

    @Test
    void onlyTheNextSeatClockwiseMayPlayEachCard() {
        TestGame testGame = newTestGame();
        Card diamondTwo = cardIn(testGame.deal, Position.EAST, Suit.DIAMONDS, Rank.TWO);
        Card diamondKing = cardIn(testGame.deal, Position.SOUTH, Suit.DIAMONDS, Rank.KING);
        Card spadeTwo = cardIn(testGame.deal, Position.WEST, Suit.SPADES, Rank.TWO);
        Card clubTwo = cardIn(testGame.deal, Position.NORTH, Suit.CLUBS, Rank.TWO);

        assertFalse(testGame.gameplay.playCard(new Seat(Position.SOUTH), diamondKing));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.EAST), diamondTwo));
        assertFalse(testGame.gameplay.playCard(new Seat(Position.WEST), spadeTwo));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.SOUTH), diamondKing));
        assertFalse(testGame.gameplay.playCard(new Seat(Position.NORTH), clubTwo));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.WEST), spadeTwo));
        assertTrue(testGame.gameplay.playCard(new Seat(Position.NORTH), clubTwo));
    }

    @Test
    void legalPlayRemovesTheCardFromTheGameplayHandOnly() {
        TestGame testGame = newTestGame();
        Card diamondTwo = cardIn(testGame.deal, Position.EAST, Suit.DIAMONDS, Rank.TWO);

        assertTrue(testGame.gameplay.playCard(new Seat(Position.EAST), diamondTwo));

        Hand remainingEastHand = remainingHand(testGame.gameplay, Position.EAST);
        assertEquals(12, remainingEastHand.getCards().size());
        assertFalse(remainingEastHand.contains(diamondTwo));
        assertTrue(testGame.deal.getHand(new Seat(Position.EAST)).contains(diamondTwo));
    }

    private TestGame newTestGame() {
        Deal deal = controlledDeal();
        Contract contract = new Contract(1, Suit.CLUBS, new Seat(Position.NORTH), false, false);
        return new TestGame(deal, new Gameplay(deal, contract));
    }

    private Deal controlledDeal() {
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            hands.put(position, new ArrayList<>());
        }

        int positionIndex = 0;
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                if (suit != Suit.NT) {
                    Position position = Position.values()[positionIndex % Position.values().length];
                    hands.get(position).add(new Card(suit, rank));
                    positionIndex++;
                }
            }
        }

        moveCard(hands, Position.EAST, Position.SOUTH, Suit.DIAMONDS, Rank.KING);
        moveCard(hands, Position.SOUTH, Position.EAST, Suit.HEARTS, Rank.ACE);
        return Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, hands);
    }

    private void moveCard(Map<Position, List<Card>> hands, Position from, Position to,
            Suit suit, Rank rank) {
        List<Card> fromHand = hands.get(from);
        for (int index = 0; index < fromHand.size(); index++) {
            Card card = fromHand.get(index);
            if (card.getSuit() == suit && card.getRank() == rank) {
                hands.get(to).add(fromHand.remove(index));
                return;
            }
        }
        throw new AssertionError("Expected " + rank + " of " + suit + " in " + from + " hand");
    }

    private Card cardIn(Deal deal, Position position, Suit suit, Rank rank) {
        for (Card card : deal.getHand(new Seat(position)).getCards()) {
            if (card.getSuit() == suit && card.getRank() == rank) {
                return card;
            }
        }
        throw new AssertionError("Expected " + rank + " of " + suit + " in " + position + " hand");
    }

    private void assertSameCard(Card expected, Card actual) {
        assertNotNull(actual);
        assertEquals(expected.getSuit(), actual.getSuit());
        assertEquals(expected.getRank(), actual.getRank());
    }

    @SuppressWarnings("unchecked")
    private Hand remainingHand(Gameplay gameplay, Position position) {
        try {
            Field field = Gameplay.class.getDeclaredField("remainingHands");
            field.setAccessible(true);
            Map<Position, Hand> remainingHands = (Map<Position, Hand>) field.get(gameplay);
            return remainingHands.get(position);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Could not inspect the gameplay hand.", exception);
        }
    }

    private static class TestGame {
        private final Deal deal;
        private final Gameplay gameplay;

        private TestGame(Deal deal, Gameplay gameplay) {
            this.deal = deal;
            this.gameplay = gameplay;
        }
    }
}
