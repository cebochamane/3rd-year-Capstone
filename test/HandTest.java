import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HandTest {

    @Test
    void newHandStartsEmptyAndCanAcceptCards() {
        Hand hand = new Hand();
        Card card = new Card(Suit.CLUBS, Rank.TEN);

        hand.addCard(card);

        assertEquals(List.of(card), hand.getCards());
    }

    @Test
    void containsSuitFindsPresentSuitsAndRejectsMissingSuits() {
        Hand hand = new Hand();
        hand.addCard(new Card(Suit.HEARTS, Rank.ACE));
        hand.addCard(new Card(Suit.HEARTS, Rank.TWO));
        hand.addCard(new Card(Suit.CLUBS, Rank.KING));

        assertTrue(hand.contains(Suit.HEARTS));
        assertTrue(hand.contains(Suit.CLUBS));
        assertFalse(hand.contains(Suit.SPADES));
    }

    @Test
    void sortPlacesHigherSuitsAndRanksBeforeLowerOnes() {
        Hand hand = new Hand();
        Card clubKing = new Card(Suit.CLUBS, Rank.KING);
        Card spadeTwo = new Card(Suit.SPADES, Rank.TWO);
        Card spadeAce = new Card(Suit.SPADES, Rank.ACE);
        Card heartQueen = new Card(Suit.HEARTS, Rank.QUEEN);
        hand.addCard(clubKing);
        hand.addCard(spadeTwo);
        hand.addCard(spadeAce);
        hand.addCard(heartQueen);
        hand.sort();
        
        assertEquals(List.of(spadeAce, spadeTwo, heartQueen, clubKing), hand.getCards());
    }

    @Test
    void constructorCopiesProvidedCardsWithoutChangingTheSourceList() {
        ArrayList<Card> source = new ArrayList<>();
        Card aceOfSpades = new Card(Suit.SPADES, Rank.ACE);
        Card kingOfHearts = new Card(Suit.HEARTS, Rank.KING);
        source.add(aceOfSpades);
        source.add(kingOfHearts);

        Hand hand = new Hand(source);

        assertEquals(List.of(aceOfSpades, kingOfHearts), hand.getCards());
        assertEquals(List.of(aceOfSpades, kingOfHearts), source);
    }
}
