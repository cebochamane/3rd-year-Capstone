import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DealTest {

    @Test
    void constructorPreservesDealerAndVulnerability() {
        Deal deal = new Deal(new Seat(Position.EAST), Vulnerability.EW);

        assertEquals(Position.EAST, deal.getDealer().getPosition());
        assertEquals(Vulnerability.EW, deal.getVulnerability());
    }

    @Test
    void dealCardsCreatesOneThirteenCardHandForEveryPosition() {
        Deal deal = new Deal(new Seat(Position.NORTH), Vulnerability.NONE);

        deal.dealCards();

        assertEquals(4, deal.getAllHands().size());
        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            assertNotNull(hand, "Expected a hand for " + position);
            assertEquals(13, hand.getCards().size(),
                    "Expected exactly 13 cards for " + position);
        }
    }

    @Test
    void dealCardsDealsEveryStandardCardExactlyOnce() {
        Deal deal = new Deal(new Seat(Position.SOUTH), Vulnerability.ALL);

        deal.dealCards();

        Set<String> dealtCards = new HashSet<>();
        for (Hand hand : deal.getAllHands().values()) {
            for (Card card : hand.getCards()) {
                assertNotEquals(Suit.NT, card.getSuit(),
                        "No-trump is a contract, not a card suit");
                assertTrue(dealtCards.add(cardKey(card)),
                        "Duplicate card dealt: " + card);
            }
        }

        assertEquals(52, dealtCards.size());
        for (Suit suit : Suit.values()) {
            if (suit == Suit.NT) {
                continue;
            }
            for (Rank rank : Rank.values()) {
                assertTrue(dealtCards.contains(suit + "-" + rank),
                        "Missing " + rank + " of " + suit);
            }
        }
    }

    @Test
    void dealingAgainReplacesHandsRatherThanAccumulatingCards() {
        Deal deal = new Deal(new Seat(Position.WEST), Vulnerability.NS);
        deal.dealCards();

        deal.dealCards();

        for (Position position : Position.values()) {
            assertEquals(13, deal.getHand(new Seat(position)).getCards().size());
        }
    }

    private String cardKey(Card card) {
        return card.getSuit() + "-" + card.getRank();
    }
}
