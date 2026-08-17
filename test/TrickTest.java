import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TrickTest {

    @Test
    public void testNormalNTTrick() {

        Card aceOfNT = new Card(Suit.NT, Rank.ACE);
        Seat seat1 = new Seat(Position.NORTH);
        Seat seat2 = new Seat(Position.EAST);
        Seat seat3 = new Seat(Position.SOUTH);
        Seat seat4 = new Seat(Position.WEST);

        Trick trick = new Trick(1, seat1);

        trick.addPlay(seat1, aceOfNT);
        trick.addPlay(seat2, new Card(Suit.NT, Rank.KING));
        trick.addPlay(seat3, new Card(Suit.NT, Rank.QUEEN));
        trick.addPlay(seat4, new Card(Suit.NT, Rank.JACK));

        assertEquals(seat1, trick.getWinner(Suit.NT));
        assertEquals(aceOfNT, trick.getWinningCard(Suit.NT));

    }

    @Test
    public void testOneTrumpBeatsLedSuit() {
        Suit trump = Suit.HEARTS;

        Seat leader = new Seat(Position.NORTH);
        Trick trick = new Trick(1, leader);
        trick.addPlay(leader, new Card(Suit.SPADES, Rank.ACE));

        Seat heartPlayer = new Seat(Position.EAST);
        trick.addPlay(heartPlayer, new Card(Suit.HEARTS, Rank.TWO));

        Seat spadePlayer1 = new Seat(Position.SOUTH);
        trick.addPlay(spadePlayer1, new Card(Suit.SPADES, Rank.KING));
        Seat spadePlayer2 = new Seat(Position.WEST);
        trick.addPlay(spadePlayer2, new Card(Suit.SPADES, Rank.QUEEN));

        assertEquals(heartPlayer, trick.getWinner(Suit.HEARTS));
    }

    @Test
    public void testMultipleTrumpsHighestWins() {

        Suit trump = Suit.HEARTS;

        Seat leader = new Seat(Position.NORTH);
        Trick trick = new Trick(1, leader);
        trick.addPlay(leader, new Card(Suit.SPADES, Rank.TEN));

        Seat heartPlayer1 = new Seat(Position.EAST);
        trick.addPlay(heartPlayer1, new Card(Suit.HEARTS, Rank.KING));
        Seat heartPlayer2 = new Seat(Position.SOUTH);
        trick.addPlay(heartPlayer2, new Card(Suit.HEARTS, Rank.QUEEN));

        Seat spadePlayer = new Seat(Position.WEST);
        trick.addPlay(spadePlayer, new Card(Suit.SPADES, Rank.JACK));

        assertEquals(heartPlayer1, trick.getWinner(Suit.HEARTS));

    }

    @Test
    public void testNoTrumpPlayed() {

        Suit trump = Suit.HEARTS;

        Seat seat1 = new Seat(Position.NORTH);
        Seat seat2 = new Seat(Position.EAST);
        Seat seat3 = new Seat(Position.SOUTH);
        Seat seat4 = new Seat(Position.WEST);
        Trick trick = new Trick(1, seat1);
        trick.addPlay(seat1, new Card(Suit.SPADES, Rank.TEN));
        trick.addPlay(seat2, new Card(Suit.SPADES, Rank.JACK));
        trick.addPlay(seat3, new Card(Suit.SPADES, Rank.QUEEN));
        trick.addPlay(seat4, new Card(Suit.SPADES, Rank.KING));

        assertEquals(seat4, trick.getWinner(Suit.HEARTS));
    }

    @Test
    public void testLedSuitIsTrumpSuit() {

        Suit trump = Suit.HEARTS;

        Seat seat1 = new Seat(Position.NORTH);
        Seat seat2 = new Seat(Position.EAST);
        Seat seat3 = new Seat(Position.SOUTH);
        Seat seat4 = new Seat(Position.WEST);
        Trick trick = new Trick(1, seat1);
        trick.addPlay(seat1, new Card(Suit.HEARTS, Rank.TEN));
        trick.addPlay(seat2, new Card(Suit.HEARTS, Rank.JACK));
        trick.addPlay(seat3, new Card(Suit.HEARTS, Rank.QUEEN));
        trick.addPlay(seat4, new Card(Suit.HEARTS, Rank.KING));

        assertEquals(seat4, trick.getWinner(Suit.HEARTS));
    }

    @Test
    public void testVoidPlayerDiscardsNonTrump() {

        Suit trump = Suit.HEARTS;

        Seat leader = new Seat(Position.NORTH);
        Trick trick = new Trick(1, leader);
        trick.addPlay(leader, new Card(Suit.SPADES, Rank.TEN));

        Seat clubPlayer = new Seat(Position.EAST);
        trick.addPlay(clubPlayer, new Card(Suit.CLUBS, Rank.TWO));

        Seat heartPlayer = new Seat(Position.SOUTH);
        trick.addPlay(heartPlayer, new Card(Suit.HEARTS, Rank.KING));
        Seat spadePlayer2 = new Seat(Position.WEST);
        trick.addPlay(spadePlayer2, new Card(Suit.SPADES, Rank.QUEEN));

        assertEquals(heartPlayer, trick.getWinner(Suit.HEARTS));
    }

    @Test
    public void testIncompleteTrickThrows() {

        Seat seat1 = new Seat(Position.NORTH);
        Seat seat2 = new Seat(Position.EAST);
        Trick trick = new Trick(1, seat1);
        trick.addPlay(seat1, new Card(Suit.SPADES, Rank.TEN));
        trick.addPlay(seat2, new Card(Suit.SPADES, Rank.JACK));

        try {
            trick.getWinner(Suit.HEARTS);
            fail("Expected IllegalStateException to be thrown");
        } catch (IllegalStateException e) {
            // Expected exception

        }
    }
}