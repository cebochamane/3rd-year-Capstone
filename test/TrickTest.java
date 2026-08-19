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

    // --- Constructor tests ---

    @Test
    public void testConstructorInitializesEmptyPlays() {
        // TODO: construct a Trick
        Trick trick = new Trick(1, new Seat(Position.NORTH));
        // TODO: assert getPlays() is not null and size() == 0
        assertNotNull(trick.getPlays());
        assertEquals(0, trick.getPlays().size());
    }

    // TODO: if you added getTrickNo()/getLeader() getters (option b above),
    // add a test here asserting they return what was passed to the constructor

    // --- addPlay() branch tests ---

    @Test
    public void testAddPlayRejectsNullSeat() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));
        Card card = new Card(Suit.CLUBS, Rank.ACE);

        trick.addPlay(null, card);

        assertEquals(0, trick.getPlays().size());
    }

    @Test
    public void testAddPlayRejectsNullCard() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        trick.addPlay(new Seat(Position.NORTH), null);
        assertEquals(0, trick.getPlays().size());

    }

    @Test
    public void testAddPlaySingleValidPlay() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));
        Seat north = new Seat(Position.NORTH);
        Card card = new Card(Suit.CLUBS, Rank.ACE);

        trick.addPlay(north, card);

        assertEquals(1, trick.getPlays().size());
        assertEquals(card, trick.getPlays().get(north));
    }

    @Test
    public void testAddPlayRejectsDuplicateSeat() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));
        Seat north = new Seat(Position.NORTH);

        Card card1 = new Card(Suit.CLUBS, Rank.ACE);
        Card card2 = new Card(Suit.CLUBS, Rank.KING);

        trick.addPlay(north, card1);
        trick.addPlay(north, card2);

        assertEquals(1, trick.getPlays().size());
        assertEquals(card1, trick.getPlays().get(north));

    }

    @Test
    public void testAddPlayRejectsFifthPlay() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));
        trick.addPlay(new Seat(Position.NORTH), new Card(Suit.CLUBS, Rank.ACE));
        trick.addPlay(new Seat(Position.EAST), new Card(Suit.CLUBS, Rank.KING));
        trick.addPlay(new Seat(Position.SOUTH), new Card(Suit.CLUBS, Rank.QUEEN));
        trick.addPlay(new Seat(Position.WEST), new Card(Suit.CLUBS, Rank.JACK));

        trick.addPlay(new Seat(Position.NORTH), new Card(Suit.CLUBS, Rank.TEN));
        assertEquals(4, trick.getPlays().size());
    }

    @Test
    public void testAddPlayAllFourSeats() {
        // TODO
        Seat north = new Seat(Position.NORTH);
        Seat east = new Seat(Position.EAST);
        Seat south = new Seat(Position.SOUTH);
        Seat west = new Seat(Position.WEST);

        Trick trick = new Trick(1, north);

        Card northCard = new Card(Suit.CLUBS, Rank.ACE);
        Card eastCard = new Card(Suit.CLUBS, Rank.KING);
        Card southCard = new Card(Suit.CLUBS, Rank.QUEEN);
        Card westCard = new Card(Suit.CLUBS, Rank.JACK);

        trick.addPlay(north, northCard);
        trick.addPlay(east, eastCard);
        trick.addPlay(south, southCard);
        trick.addPlay(west, westCard);

        assertEquals(4, trick.getPlays().size());
        assertEquals(northCard, trick.getPlays().get(north));
        assertEquals(eastCard, trick.getPlays().get(east));
        assertEquals(southCard, trick.getPlays().get(south));
        assertEquals(westCard, trick.getPlays().get(west));
    }

    // --- getPlays() tests ---

    @Test
    public void testGetPlaysEmptyOnNewTrick() {
        // TODO (may overlap with testConstructorInitializesEmptyPlays --
        // decide if you want both, or if one subsumes the other)
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        assertNotNull(trick.getPlays());
        assertEquals(0, trick.getPlays().size());
    }

    @Test
    public void testGetPlaysReflectsAddedPlays() {

        Seat north = new Seat(Position.NORTH);
        Seat east = new Seat(Position.EAST);
        Seat south = new Seat(Position.SOUTH);
        Seat west = new Seat(Position.WEST);

        // TODO: add all 4, assert getPlays() maps each seat to its own correct card
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        Card northCard = new Card(Suit.CLUBS, Rank.ACE);
        Card eastCard = new Card(Suit.CLUBS, Rank.KING);
        Card southCard = new Card(Suit.CLUBS, Rank.QUEEN);
        Card westCard = new Card(Suit.CLUBS, Rank.JACK);

        trick.addPlay(north, northCard);
        trick.addPlay(east, eastCard);
        trick.addPlay(south, southCard);
        trick.addPlay(west, westCard);

        assertEquals(northCard, trick.getPlays().get(north));
        assertEquals(eastCard, trick.getPlays().get(east));
        assertEquals(southCard, trick.getPlays().get(south));
        assertEquals(westCard, trick.getPlays().get(west));
    }

    // --- isComplete() tests ---

    @Test
    public void testIsCompleteFalseWhenEmpty() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        trick.addPlay(new Seat(Position.NORTH), new Card(Suit.CLUBS, Rank.ACE));
        trick.addPlay(new Seat(Position.EAST), new Card(Suit.CLUBS, Rank.KING));
        assertFalse(trick.isComplete());
    }

    @Test
    public void testIsCompleteFalseWithTwoPlays() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        trick.addPlay(new Seat(Position.NORTH), new Card(Suit.CLUBS, Rank.ACE));
        trick.addPlay(new Seat(Position.EAST), new Card(Suit.CLUBS, Rank.KING));
        trick.addPlay(new Seat(Position.SOUTH), new Card(Suit.CLUBS, Rank.QUEEN));
        trick.addPlay(new Seat(Position.WEST), new Card(Suit.CLUBS, Rank.JACK));
        assertTrue(trick.isComplete());
    }

    @Test
    public void testIsCompleteTrueWithFourPlays() {
        // TODO
        Trick trick = new Trick(1, new Seat(Position.NORTH));

        trick.addPlay(new Seat(Position.NORTH), new Card(Suit.CLUBS, Rank.ACE));
        trick.addPlay(new Seat(Position.EAST), new Card(Suit.CLUBS, Rank.KING));
        trick.addPlay(new Seat(Position.SOUTH), new Card(Suit.CLUBS, Rank.QUEEN));
        trick.addPlay(new Seat(Position.WEST), new Card(Suit.CLUBS, Rank.JACK));
        assertTrue(trick.isComplete());
    }

    // --- getWinner / getWinningCard consistency ---

    @Test
    public void testWinningCardMatchesWinnerSeat() {
        // TODO: build any valid complete trick
        // TODO: assert plays.get(trick.getWinner(trump)) equals
        // trick.getWinningCard(trump)
        // NOTE: getPlays() returns the map, so you can do this via the public API
        Seat north = new Seat(Position.NORTH);
        Seat east = new Seat(Position.EAST);
        Seat south = new Seat(Position.SOUTH);
        Seat west = new Seat(Position.WEST);

        Trick trick = new Trick(1, north);

        Card northCard = new Card(Suit.CLUBS, Rank.ACE);
        Card eastCard = new Card(Suit.CLUBS, Rank.KING);
        Card southCard = new Card(Suit.CLUBS, Rank.QUEEN);
        Card westCard = new Card(Suit.CLUBS, Rank.JACK);

        trick.addPlay(north, northCard);
        trick.addPlay(east, eastCard);
        trick.addPlay(south, southCard);
        trick.addPlay(west, westCard);

        Seat winnerSeat = trick.getWinner(Suit.CLUBS);
        Card winningCard = trick.getWinningCard(Suit.CLUBS);

        assertEquals(winningCard, trick.getPlays().get(winnerSeat));
    }

}