import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class GameControllerFlowTest {
    @ParameterizedTest
    @EnumSource(Position.class)
    void dealerStartsAndWrongSeatCannotAdvanceAuction(Position position) {
        GameController controller = new GameController();
        Seat dealer = new Seat(position);
        controller.startRandomDeal(dealer, Vulnerability.NONE);
        assertEquals(dealer, controller.getCurrentBidder());
        assertThrows(IllegalArgumentException.class,
                () -> controller.submitBid(new Bid(dealer.next(), 1, Suit.CLUBS)));
        assertTrue(controller.getAuction().getBids().isEmpty());
        assertEquals(dealer, controller.getCurrentBidder());
        controller.submitBid(new Bid(dealer, 1, Suit.CLUBS));
        assertEquals(dealer.next(), controller.getCurrentBidder());
    }

    @ParameterizedTest
    @EnumSource(Position.class)
    void partnerRaisesButFirstBidderDeclaresAndLeftOpponentLeads(Position position) {
        GameController controller = new GameController();
        Seat dealer = new Seat(position);
        controller.startRandomDeal(dealer, Vulnerability.ALL);
        controller.submitBid(new Bid(dealer, 1, Suit.HEARTS));
        controller.submitBid(Bid.BidPass(dealer.next()));
        controller.submitBid(new Bid(dealer.opposite(), 2, Suit.HEARTS));
        for (int i = 0; i < 3; i++) controller.submitBid(Bid.BidPass(controller.getCurrentBidder()));
        assertEquals(GamePhase.PLAYING, controller.getPhase());
        assertEquals(dealer, controller.getContract().getDeclarer());
        assertEquals(2, controller.getContract().getLevel());
        assertEquals(dealer.next(), controller.getGameplay().getCurrentPlayer());
        assertNull(controller.getCurrentBidder());
        assertFalse(controller.canSwapCards());
        assertThrows(IllegalStateException.class, controller::getScore);
    }

    @ParameterizedTest
    @EnumSource(Position.class)
    void fourOpeningPassesFinishWithoutContractOrGameplay(Position position) {
        GameController controller = new GameController();
        controller.startRandomDeal(new Seat(position), Vulnerability.NONE);
        for (int i = 0; i < 3; i++) controller.submitBid(Bid.BidPass(controller.getCurrentBidder()));
        assertEquals(GamePhase.BIDDING, controller.getPhase());
        controller.submitBid(Bid.BidPass(controller.getCurrentBidder()));
        assertEquals(GamePhase.PASSED_OUT, controller.getPhase());
        assertNull(controller.getContract());
        assertNull(controller.getGameplay());
        assertFalse(controller.playCard(new Seat(position), new Card(Suit.CLUBS, Rank.ACE)));
    }
}
