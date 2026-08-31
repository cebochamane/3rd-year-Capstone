import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class GameControllerTest {

    @Test
    void newControllerStartsAtTheMenu() {
        GameController controller = new GameController();

        assertEquals(GamePhase.MENU, controller.getPhase());
        assertEquals("MENU", controller.getCurrentState()); // checks that the String version of phase is "MENU"
        assertNull(controller.getCurrentDeal());
        assertNull(controller.getAuction());
        assertNull(controller.getContract());
        assertNull(controller.getGameplay());
    }

    @Test
    void createsARandomDealWithTheSelectedMetadata() {
        GameController controller = new GameController();
        controller.startRandomDeal(new Seat(Position.NORTH), Vulnerability.NS);

        Deal deal = controller.getCurrentDeal();
        assertNotNull(deal);
        assertEquals(Position.NORTH, deal.getDealer().getPosition());
        assertEquals(Vulnerability.NS, deal.getVulnerability());

        for (Position position : Position.values()) {
            assertEquals(13, deal.getHand(new Seat(position)).getCards().size()); // each hand should have 13 cards
        }
    }

    @Test
    void anAttemptToStartPlayBeforeAuctionIsComplete() {
        GameController controller = new GameController();

        assertFalse(controller.startPlay(null));
        assertNull(controller.getContract());
        assertNull(controller.getGameplay());
        assertEquals(GamePhase.MENU, controller.getPhase());
    }
}