import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AuctionTest {

    @Test // isComplete()
    void emptyAuction() {
        Auction auction = new Auction();
        assertFalse(auction.isComplete());
    }

    @Test // is NotComplete()
    void threeConsecutivePasses() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));
        auction.addBid(Bid.BidPass(new Seat(Position.SOUTH)));
        assertFalse(auction.isComplete());

    }

    @Test // isComplete()
    void twoConsecutivePasses() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));
        assertFalse(auction.isComplete());

    }

    @Test // isComplete()
    void notEnoughConsecutivePasses() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));

        Bid bid = new Bid(new Seat(Position.SOUTH), 2, Suit.HEARTS);
        auction.addBid(bid);

        auction.addBid(Bid.BidPass(new Seat(Position.WEST)));
        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        assertFalse(auction.isComplete());

    }

    @Test // isComplete()
    void enoughConsecutivePasses() {
        Auction auction = new Auction();

        Bid bid = new Bid(new Seat(Position.SOUTH), 3, Suit.CLUBS);
        auction.addBid(bid);

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));
        auction.addBid(Bid.BidPass(new Seat(Position.SOUTH)));

        assertTrue(auction.isComplete());

    }

    @Test // isComplete()
    void noPasses() {
        Auction auction = new Auction();

        Bid bid = new Bid(new Seat(Position.SOUTH), 1, Suit.CLUBS);
        auction.addBid(bid);
        Bid bid1 = new Bid(new Seat(Position.SOUTH), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.SOUTH), 3, Suit.CLUBS);
        auction.addBid(bid2);
        Bid bid3 = new Bid(new Seat(Position.SOUTH), 4, Suit.CLUBS);
        auction.addBid(bid3);
        Bid bid4 = new Bid(new Seat(Position.SOUTH), 3, Suit.SPADES);
        auction.addBid(bid4);
        assertFalse(auction.isComplete());

    }

    @Test // getFinalBid()
    void emptyAuction_FinalBid() {
        Auction auction = new Auction();
        assertNull(auction.getFinalBid());
    }

    @Test // getFinalBid
    void onlyPasses() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));
        auction.addBid(Bid.BidPass(new Seat(Position.SOUTH)));
        assertNull(auction.getFinalBid());

    }

    @Test // getFinaliBid()
    void oneContractBid() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));

        Bid bid = new Bid(new Seat(Position.SOUTH), 2, Suit.HEARTS);
        auction.addBid(bid);

        auction.addBid(Bid.BidPass(new Seat(Position.WEST)));
        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        assertEquals(bid, auction.getFinalBid());

    }

    @Test // getFinaliBid()
    void manyContractBid() {
        Auction auction = new Auction();

        Bid bid = new Bid(new Seat(Position.SOUTH), 1, Suit.CLUBS);
        auction.addBid(bid);
        Bid bid1 = new Bid(new Seat(Position.WEST), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid2);
        Bid bid3 = new Bid(new Seat(Position.EAST), 4, Suit.CLUBS);
        auction.addBid(bid3);
        Bid bid4 = new Bid(new Seat(Position.SOUTH), 5, Suit.SPADES);
        auction.addBid(bid4);
        assertEquals(bid4, auction.getFinalBid());

    }

    @Test // getFinaliBid()
    void ignoreDoubleBid() {
        Auction auction = new Auction();

        Bid bid = new Bid(new Seat(Position.SOUTH), 1, Suit.CLUBS);
        auction.addBid(bid);
        Bid bid1 = new Bid(new Seat(Position.WEST), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid2);
        auction.addBid(Bid.BidDouble(new Seat(Position.EAST)));

        assertEquals(bid2, auction.getFinalBid());
    }

    @Test // getFinaliBid()
    void ignoreReDoubleBid() {
        Auction auction = new Auction();

        Bid bid = new Bid(new Seat(Position.SOUTH), 1, Suit.CLUBS);
        auction.addBid(bid);
        Bid bid1 = new Bid(new Seat(Position.WEST), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid2);
        auction.addBid(Bid.BidRedouble(new Seat(Position.EAST)));

        assertEquals(bid2, auction.getFinalBid());
    }

    @Test // isValidBid()
    void nullBid() {
        Auction auction = new Auction();
        assertFalse(auction.isValidBid(null));
    }

    @Test // inValidBid()
    void passOnEmpty() {
        Auction auction = new Auction();
        Bid pass = Bid.BidPass(new Seat(Position.WEST));
        assertFalse(auction.isValidBid(pass));
        assertEquals(0, auction.getBids().size());
    }

    @Test // isValidBid()
    void invalidPass() {
        Auction auction = new Auction();

        auction.addBid(Bid.BidPass(new Seat(Position.WEST)));
        auction.addBid(Bid.BidPass(new Seat(Position.NORTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));

        Bid pass = Bid.BidPass(new Seat(Position.WEST));
        assertFalse(auction.isValidBid(pass));

    }

    @Test // isValidBid()
    void validDoubleBid1() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid);

        Bid doubleBid = Bid.BidDouble(new Seat(Position.EAST));
        assertTrue(auction.isValidBid(doubleBid));
    }

    @Test // isValidBid()
    void inValidDoubleBid1() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid);

        Bid doubleBid = Bid.BidDouble(new Seat(Position.SOUTH));
        assertFalse(auction.isValidBid(doubleBid));
    }

    @Test // isValidBid()
    void inValidReDoubleBid1() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid);

        Bid redoubleBid = Bid.BidRedouble(new Seat(Position.EAST));
        assertFalse(auction.isValidBid(redoubleBid));
    }

    @Test // isValidBid()
    void validReDoubleBid1() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid);
        Bid doubleBid = Bid.BidDouble(new Seat(Position.EAST));
        auction.addBid(doubleBid);

        Bid redoubleBid = Bid.BidRedouble(new Seat(Position.SOUTH));
        assertTrue(auction.isValidBid(redoubleBid));
    }

    @Test // isValidBid()
    void inValidReDoubleBid2() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid);
        Bid doubleBid = Bid.BidDouble(new Seat(Position.EAST));
        auction.addBid(doubleBid);

        Bid redoubleBid = Bid.BidRedouble(new Seat(Position.WEST));
        assertFalse(auction.isValidBid(redoubleBid));
    }

    @Test // isValidBid()
    void equalBids() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid);
        Bid bid1 = new Bid(new Seat(Position.EAST), 2, Suit.CLUBS);

        assertFalse(auction.isValidBid(bid1));
    }

    @Test // isValid()
    void validisHigher() {
        Auction auction = new Auction();
        Bid bid1 = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.EAST), 4, Suit.CLUBS);

        assertTrue(auction.isValidBid(bid2));
    }

    @Test // isValid()
    void addValidBid() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        assertTrue(auction.addBid(bid));
        assertEquals(1, auction.getBids().size());
    }

    @Test // isInValid()
    void addInValidBid() {
        Auction auction = new Auction();
        Bid validBid = new Bid(new Seat(Position.NORTH), 3, Suit.CLUBS);
        Bid invalidBid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(validBid);
        assertFalse(auction.addBid(invalidBid));
        assertEquals(1, auction.getBids().size());
    }

    @Test // isValid()
    void inValidisHigher() {
        Auction auction = new Auction();
        Bid bid1 = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.EAST), 1, Suit.CLUBS);

        assertFalse(auction.isValidBid(bid2));
    }

    @Test // isValid()
    void validisHigher2() {
        Auction auction = new Auction();
        Bid bid1 = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid1);
        Bid bid2 = new Bid(new Seat(Position.EAST), 2, Suit.NT);

        assertTrue(auction.isValidBid(bid2));
    }

    @Test
    void getFinalBidIgnoreDouble() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid);
        Bid doubleBid = Bid.BidDouble(new Seat(Position.EAST));
        assertTrue(auction.addBid(doubleBid));
        assertEquals(bid, auction.getFinalBid());
    }

    @Test
    void getFinalBidIgnoresRedouble() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 2, Suit.CLUBS);
        auction.addBid(bid);

        Bid doubleBid = Bid.BidDouble(new Seat(Position.EAST));
        auction.addBid(doubleBid);

        Bid redoubleBid = Bid.BidRedouble(new Seat(Position.SOUTH));
        assertTrue(auction.addBid(redoubleBid));

        assertEquals(bid, auction.getFinalBid());
    }

    @Test
    void createContract() {
        Auction auction = new Auction();
        Bid bid = new Bid(new Seat(Position.NORTH), 5, Suit.CLUBS);
        auction.addBid(bid);

        auction.addBid(Bid.BidPass(new Seat(Position.EAST)));
        auction.addBid(Bid.BidPass(new Seat(Position.SOUTH)));
        auction.addBid(Bid.BidPass(new Seat(Position.WEST)));

        Contract contract = auction.createContract();
        assertNotNull(contract);
        assertEquals(5, contract.getLevel());
        assertEquals(Suit.CLUBS, contract.getDenomination());
        assertEquals(new Seat(Position.NORTH), contract.getDeclarer());
    }

}