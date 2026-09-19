import static org.junit.jupiter.api.Assertions.*;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AuctionPanelTest {
    @ParameterizedTest
    @EnumSource(Position.class)
    void callsRemainUnderTheirSeatsAcrossRowsAndRefreshes(Position dealer) throws Exception {
        TutorialTestSupport.onEdt(() -> {
            AuctionPanel panel = new AuctionPanel();
            Auction auction = new Auction();
            Seat seat = new Seat(dealer);
            panel.updateAuction(auction, seat, seat);
            assertColumns(panel, dealer, new String[0]);

            Bid[] calls = {
                new Bid(seat, 1, Suit.CLUBS),
                Bid.BidDouble(seat.next()),
                Bid.BidRedouble(seat.next().next()),
                Bid.BidPass(seat.previous()),
                new Bid(seat, 1, Suit.NT),
                Bid.BidPass(seat.next())
            };
            String[] labels = {"1♣", "Double", "Redouble", "Pass", "1NT", "Pass"};
            for (int count = 0; count < calls.length; count++) {
                assertTrue(auction.addBid(calls[count]));
                panel.updateAuction(auction, seat, calls[count].getBidder().next());
                assertColumns(panel, dealer, java.util.Arrays.copyOf(labels, count + 1));
            }
            panel.updateAuction(new Auction(), new Seat(Position.NORTH), new Seat(Position.NORTH));
            assertColumns(panel, Position.NORTH, new String[0]);
            return null;
        });
    }

    @Test
    void rejectsMissingDealerMetadata() throws Exception {
        TutorialTestSupport.onEdt(() -> {
            AuctionPanel panel = new AuctionPanel();
            assertThrows(IllegalArgumentException.class,
                    () -> panel.updateAuction(new Auction(), null, null));
            assertThrows(IllegalArgumentException.class,
                    () -> panel.updateAuction(new Auction(), new Seat(null), null));
            return null;
        });
    }

    private void assertColumns(AuctionPanel panel, Position dealer, String[] bids) {
        JPanel history = null;
        for (Component child : panel.getComponents()) {
            if (child instanceof JPanel candidate && candidate.getComponentCount() >= 4
                    && candidate.getComponent(0) instanceof JLabel first
                    && first.getText().equals("NORTH")) {
                history = candidate;
                break;
            }
        }
        assertNotNull(history, "Auction history should be present in the visible component tree");
        String[] headings = {"NORTH", "EAST", "SOUTH", "WEST"};
        for (int column = 0; column < 4; column++) {
            assertEquals(headings[column], ((JLabel) history.getComponent(column)).getText());
        }
        int offset = java.util.List.of(Position.NORTH, Position.EAST,
                Position.SOUTH, Position.WEST).indexOf(dealer);
        int cells = ((offset + bids.length + 3) / 4) * 4;
        assertEquals(4 + cells, history.getComponentCount());
        for (int cell = 0; cell < cells; cell++) {
            int bid = cell - offset;
            assertEquals(bid >= 0 && bid < bids.length ? bids[bid] : "",
                    ((JLabel) history.getComponent(4 + cell)).getText(), "History cell " + cell);
        }
    }
}
