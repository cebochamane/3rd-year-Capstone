import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class BidTest {

    @Test
    void createNormalBid() {
        Seat seat = new Seat(Position.EAST);
        int level = 1;
        Suit suit = Suit.HEARTS;
        Bid bid = new Bid(seat, level, suit);

        assertEquals(seat, bid.getBidder());
        assertEquals(level, bid.getLevel());
        assertEquals(suit, bid.getDenomination());
    }

    @Test
    void createPassBid() {
        Seat seat = new Seat(Position.NORTH);
        Bid bid = Bid.BidPass(seat);

        assertEquals(seat, bid.getBidder());
        assertEquals(0, bid.getLevel());
        assertNull(bid.getDenomination());
        assertTrue(bid.isPass());
    }

    @Test
    void createDoubleBid() {
        Seat seat = new Seat(Position.SOUTH);

        Bid bid = Bid.BidDouble(seat);

        assertEquals(seat, bid.getBidder());
        assertEquals(0, bid.getLevel());
        assertNull(bid.getDenomination());
        assertTrue(bid.isDouble());
    }

    @Test
    void createReDoubleBid() {
        Seat seat = new Seat(Position.WEST);

        Bid bid = Bid.BidRedouble(seat);

        assertEquals(seat, bid.getBidder());
        assertEquals(0, bid.getLevel());
        assertNull(bid.getDenomination());
        assertFalse(bid.isDouble());
        assertTrue(bid.isRedouble());
    }

}