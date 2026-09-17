import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for the NotePhase enum and its GamePhase mapping.
 */
public class NotePhaseTest {

    @Test
    @DisplayName("Enum has exactly five values")
    void testEnumSize() {
        assertEquals(5, NotePhase.values().length);
    }

    @Test
    @DisplayName("All expected values exist")
    void testEnumValues() {
        assertNotNull(NotePhase.valueOf("BIDDING"));
        assertNotNull(NotePhase.valueOf("PRE_PLAY"));
        assertNotNull(NotePhase.valueOf("PLAYING"));
        assertNotNull(NotePhase.valueOf("POST_PLAY"));
        assertNotNull(NotePhase.valueOf("GENERAL"));
    }

    @Test
    @DisplayName("GamePhase.BIDDING maps to NotePhase.BIDDING")
    void testMappingBidding() {
        assertEquals(NotePhase.BIDDING, NotePhase.fromGamePhase(GamePhase.BIDDING));
    }

    @Test
    @DisplayName("GamePhase.AUCTION_COMPLETE maps to NotePhase.PRE_PLAY")
    void testMappingAuctionComplete() {
        assertEquals(NotePhase.PRE_PLAY, NotePhase.fromGamePhase(GamePhase.AUCTION_COMPLETE));
    }

    @Test
    @DisplayName("GamePhase.PLAYING maps to NotePhase.PLAYING")
    void testMappingPlaying() {
        assertEquals(NotePhase.PLAYING, NotePhase.fromGamePhase(GamePhase.PLAYING));
    }

    @Test
    @DisplayName("GamePhase.FINISHED maps to NotePhase.POST_PLAY")
    void testMappingFinished() {
        assertEquals(NotePhase.POST_PLAY, NotePhase.fromGamePhase(GamePhase.FINISHED));
    }

    @Test
    @DisplayName("GamePhase.MENU maps to NotePhase.GENERAL")
    void testMappingMenu() {
        assertEquals(NotePhase.GENERAL, NotePhase.fromGamePhase(GamePhase.MENU));
    }

    @Test
    @DisplayName("GamePhase.PASSED_OUT maps to NotePhase.GENERAL")
    void testMappingPassedOut() {
        assertEquals(NotePhase.GENERAL, NotePhase.fromGamePhase(GamePhase.PASSED_OUT));
    }

    @Test
    @DisplayName("Null GamePhase maps to NotePhase.GENERAL")
    void testMappingNull() {
        assertEquals(NotePhase.GENERAL, NotePhase.fromGamePhase(null));
    }

    @Test
    @DisplayName("fromGamePhase never returns null")
    void testNeverReturnsNull() {
        for (GamePhase gp : GamePhase.values()) {
            assertNotNull(NotePhase.fromGamePhase(gp));
        }
    }
}