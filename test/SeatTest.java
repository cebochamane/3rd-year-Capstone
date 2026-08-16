import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class SeatTest {

    @Test
    void nextFollowsTheClockwiseBridgeTableOrder() {
        assertEquals(Position.EAST, new Seat(Position.NORTH).next().getPosition());
        assertEquals(Position.SOUTH, new Seat(Position.EAST).next().getPosition());
        assertEquals(Position.WEST, new Seat(Position.SOUTH).next().getPosition());
        assertEquals(Position.NORTH, new Seat(Position.WEST).next().getPosition());
    }

    @Test
    void previousFollowsTheCounterClockwiseBridgeTableOrder() {
        assertEquals(Position.WEST, new Seat(Position.NORTH).previous().getPosition());
        assertEquals(Position.NORTH, new Seat(Position.EAST).previous().getPosition());
        assertEquals(Position.EAST, new Seat(Position.SOUTH).previous().getPosition());
        assertEquals(Position.SOUTH, new Seat(Position.WEST).previous().getPosition());
    }

    @Test
    void oppositeReturnsTheOtherMemberOfThePartnership() {
        assertEquals(Position.SOUTH, new Seat(Position.NORTH).opposite().getPosition());
        assertEquals(Position.WEST, new Seat(Position.EAST).opposite().getPosition());
        assertEquals(Position.NORTH, new Seat(Position.SOUTH).opposite().getPosition());
        assertEquals(Position.EAST, new Seat(Position.WEST).opposite().getPosition());
    }

    @Test
    void isPartnerRecognisesOppositeSeatsAndHandlesNull() {
        Seat north = new Seat(Position.NORTH);

        assertTrue(north.isPartner(new Seat(Position.SOUTH)));
        assertFalse(north.isPartner(new Seat(Position.EAST)));
        assertFalse(north.isPartner(null));
    }

    @Test
    void toStringUsesThePositionName() {
        assertEquals("WEST", new Seat(Position.WEST).toString());
    }
}
