import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import org.junit.jupiter.api.Test;

class SwapCardsTest {
    private final Seat north = new Seat(Position.NORTH);
    private final Seat south = new Seat(Position.SOUTH);

    @Test
    void swappingTransfersExactlyTwoCardsAndPreservesACompleteDeal() {
        GameController controller = new GameController();
        controller.startLoadedDeal(TutorialTestSupport.early("Claim", 9).getDeal());
        Deal deal = controller.getCurrentDeal();
        Card spade = new Card(Suit.SPADES, Rank.ACE);
        Card heart = new Card(Suit.HEARTS, Rank.ACE);
        var northBefore = new HashSet<>(deal.getHand(north).getCards());
        var southBefore = new HashSet<>(deal.getHand(south).getCards());
        assertTrue(controller.canSwapCards());
        assertTrue(controller.swapCards(north, spade, south, heart));
        northBefore.remove(spade);
        northBefore.add(heart);
        southBefore.remove(heart);
        southBefore.add(spade);
        assertEquals(northBefore, new HashSet<>(deal.getHand(north).getCards()));
        assertEquals(southBefore, new HashSet<>(deal.getHand(south).getCards()));
        HashSet<Card> all = new HashSet<>();
        for (Position position : Position.values()) {
            var cards = deal.getHand(new Seat(position)).getCards();
            assertEquals(13, cards.size());
            for (Card card : cards) assertTrue(all.add(card), "Duplicate: " + card);
        }
        assertEquals(52, all.size());
        assertEquals(Position.SOUTH, deal.getDealer().getPosition());
        assertEquals(Vulnerability.NONE, deal.getVulnerability());
        assertTrue(controller.swapCards(north, heart, south, spade), "Repeated swaps before bidding are allowed");
    }

    @Test
    void invalidSwapsDoNotChangeAnyHands() {
        Deal deal = TutorialTestSupport.early("Claim", 9).getDeal();
        String before = deal.getAllHands().toString();
        Card spade = new Card(Suit.SPADES, Rank.ACE);
        Card heart = new Card(Suit.HEARTS, Rank.ACE);
        assertFalse(deal.swapCards(north, spade, north, spade));
        assertFalse(deal.swapCards(north, heart, south, spade));
        assertFalse(deal.swapCards(null, spade, south, heart));
        assertFalse(deal.swapCards(north, null, south, heart));
        assertFalse(deal.swapCards(north, spade, null, heart));
        assertFalse(deal.swapCards(north, spade, south, null));
        assertEquals(before, deal.getAllHands().toString());
    }

    @Test
    void firstCallIncludingPassLocksSwappingUntilANewDeal() {
        GameController controller = new GameController();
        assertFalse(controller.canSwapCards());
        controller.startLoadedDeal(TutorialTestSupport.early("Claim", 9).getDeal());
        assertTrue(controller.canSwapCards());
        controller.submitBid(Bid.BidPass(south));
        String before = controller.getCurrentDeal().getAllHands().toString();
        assertFalse(controller.canSwapCards());
        assertFalse(controller.swapCards(north, new Card(Suit.SPADES, Rank.ACE),
                south, new Card(Suit.HEARTS, Rank.ACE)));
        assertEquals(before, controller.getCurrentDeal().getAllHands().toString());
        controller.startNewGame();
        assertTrue(controller.canSwapCards());
        controller.quitGame();
        assertFalse(controller.canSwapCards());
    }
}
