import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void constructorAndGettersPreserveRankAndSuit() {
        Card card = new Card(Suit.SPADES, Rank.ACE);

        assertEquals(Suit.SPADES, card.getSuit());
        assertEquals(Rank.ACE, card.getRank());
        assertEquals("ACE of SPADES", card.toString());
    }

    @Test
    void trumpBeatsTheLeadSuit() {
        Card trump = new Card(Suit.SPADES, Rank.TWO);
        Card lead = new Card(Suit.HEARTS, Rank.ACE);

        assertTrue(trump.compareTo(lead, Suit.SPADES, Suit.HEARTS) > 0);
        assertTrue(lead.compareTo(trump, Suit.SPADES, Suit.HEARTS) < 0);
    }

    @Test
    void leadSuitBeatsAnOffSuitDiscard() {
        Card lead = new Card(Suit.HEARTS, Rank.TWO);
        Card discard = new Card(Suit.CLUBS, Rank.ACE);

        assertTrue(lead.compareTo(discard, Suit.SPADES, Suit.HEARTS) > 0);
        assertTrue(discard.compareTo(lead, Suit.SPADES, Suit.HEARTS) < 0);
    }

    @Test
    void higherCardWinsWhenBothCardsFollowTheLeadSuit() {
        Card ace = new Card(Suit.HEARTS, Rank.ACE);
        Card two = new Card(Suit.HEARTS, Rank.TWO);

        assertTrue(ace.compareTo(two, Suit.CLUBS, Suit.HEARTS) > 0);
        assertTrue(two.compareTo(ace, Suit.CLUBS, Suit.HEARTS) < 0);
    }

    @Test
    void higherTrumpWinsWhenBothCardsAreTrump() {
        Card ace = new Card(Suit.SPADES, Rank.ACE);
        Card two = new Card(Suit.SPADES, Rank.TWO);

        assertTrue(ace.compareTo(two, Suit.SPADES, Suit.HEARTS) > 0);
        assertTrue(two.compareTo(ace, Suit.SPADES, Suit.HEARTS) < 0);
    }
}
