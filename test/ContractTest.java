import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Unit tests for the Contract class.
 * Tests contract creation, getters, and business logic methods.
 */
public class ContractTest {

    private Contract contract1S;
    private Contract contract1NT;
    private Contract contract4S;
    private Contract contract4SDoubled;
    private Contract contract4SRedoubled;
    private Seat declarer;

    @BeforeEach
    void setUp() {
        declarer = new Seat(Position.SOUTH);
        contract1S = new Contract(1, Suit.SPADES, declarer, false, false);
        contract1NT = new Contract(1, Suit.NT, declarer, false, false);
        contract4S = new Contract(4, Suit.SPADES, declarer, false, false);
        contract4SDoubled = new Contract(4, Suit.SPADES, declarer, true, false);
        contract4SRedoubled = new Contract(4, Suit.SPADES, declarer, false, true);
    }

    // ===== Constructor Tests =====
    @Test
    @DisplayName("Contract constructor sets all fields correctly")
    void testConstructor() {
        Seat northDeclarer = new Seat(Position.NORTH);
        Contract contract = new Contract(2, Suit.HEARTS, northDeclarer, true, false);

        assertEquals(2, contract.getLevel());
        assertEquals(Suit.HEARTS, contract.getDenomination());
        assertEquals(northDeclarer, contract.getDeclarer());
        assertTrue(contract.isDoubled());
        assertFalse(contract.isRedoubled());
    }

    @Test
    @DisplayName("Contract with different declarer positions works")
    void testConstructorWithDifferentDeclarers() {
        Seat north = new Seat(Position.NORTH);
        Seat east = new Seat(Position.EAST);
        Seat south = new Seat(Position.SOUTH);
        Seat west = new Seat(Position.WEST);

        Contract contractN = new Contract(1, Suit.CLUBS, north, false, false);
        Contract contractE = new Contract(1, Suit.DIAMONDS, east, false, false);
        Contract contractS = new Contract(1, Suit.HEARTS, south, false, false);
        Contract contractW = new Contract(1, Suit.SPADES, west, false, false);

        assertEquals(north, contractN.getDeclarer());
        assertEquals(east, contractE.getDeclarer());
        assertEquals(south, contractS.getDeclarer());
        assertEquals(west, contractW.getDeclarer());
    }

    // ===== Getter Tests =====
    @Test
    @DisplayName("getLevel returns correct level")
    void testGetLevel() {
        assertEquals(1, contract1S.getLevel());
        assertEquals(1, contract1NT.getLevel());
        assertEquals(4, contract4S.getLevel());
    }

    @Test
    @DisplayName("getDenomination returns correct suit")
    void testGetDenomination() {
        assertEquals(Suit.SPADES, contract1S.getDenomination());
        assertEquals(Suit.NT, contract1NT.getDenomination());
        assertEquals(Suit.SPADES, contract4S.getDenomination());
    }

    @Test
    @DisplayName("getDeclarer returns correct seat")
    void testGetDeclarer() {
        assertEquals(declarer, contract1S.getDeclarer());
        assertEquals(declarer, contract1NT.getDeclarer());
        assertEquals(declarer, contract4S.getDeclarer());
    }

    @Test
    @DisplayName("isDoubled returns correct status")
    void testIsDoubled() {
        assertFalse(contract1S.isDoubled());
        assertFalse(contract1NT.isDoubled());
        assertFalse(contract4S.isDoubled());
        assertTrue(contract4SDoubled.isDoubled());
        assertFalse(contract4SRedoubled.isDoubled());
    }

    @Test
    @DisplayName("isRedoubled returns correct status")
    void testIsRedoubled() {
        assertFalse(contract1S.isRedoubled());
        assertFalse(contract1NT.isRedoubled());
        assertFalse(contract4S.isRedoubled());
        assertFalse(contract4SDoubled.isRedoubled());
        assertTrue(contract4SRedoubled.isRedoubled());
    }

    // ===== isNoTrump Tests =====
    @Test
    @DisplayName("isNoTrump returns true for NT contracts")
    void testIsNoTrumpTrue() {
        assertTrue(contract1NT.isNoTrump());
    }

    @Test
    @DisplayName("isNoTrump returns false for suit contracts")
    void testIsNoTrumpFalse() {
        assertFalse(contract1S.isNoTrump());
        assertFalse(contract4S.isNoTrump());
    }

    // ===== isTrumpContract Tests =====
    @Test
    @DisplayName("isTrumpContract returns true for suit contracts")
    void testIsTrumpContractTrue() {
        assertTrue(contract1S.isTrumpContract());
        assertTrue(contract4S.isTrumpContract());
    }

    @Test
    @DisplayName("isTrumpContract returns false for NT contracts")
    void testIsTrumpContractFalse() {
        assertFalse(contract1NT.isTrumpContract());
    }

    // ===== getRequiredTricks Tests =====
    @Test
    @DisplayName("getRequiredTricks returns correct number (level + 6)")
    void testGetRequiredTricks() {
        assertEquals(7, contract1S.getRequiredTricks()); // 1 + 6 = 7
        assertEquals(7, contract1NT.getRequiredTricks()); // 1 + 6 = 7
        assertEquals(10, contract4S.getRequiredTricks()); // 4 + 6 = 10
    }

    @Test
    @DisplayName("getRequiredTricks works for all levels 1-7")
    void testGetRequiredTricksAllLevels() {
        Seat northDeclarer = new Seat(Position.NORTH);

        for (int level = 1; level <= 7; level++) {
            Contract contract = new Contract(level, Suit.SPADES, northDeclarer, false, false);
            assertEquals(level + 6, contract.getRequiredTricks(),
                    "Level " + level + " should require " + (level + 6) + " tricks");
        }
    }

    // ===== Edge Cases =====
    @Test
    @DisplayName("Contract with level 7 returns correct required tricks (13)")
    void testLevel7Contract() {
        Seat eastDeclarer = new Seat(Position.EAST);
        Contract contract = new Contract(7, Suit.NT, eastDeclarer, false, false);
        assertEquals(13, contract.getRequiredTricks()); // 7 + 6 = 13
    }

    @Test
    @DisplayName("Doubled and Redoubled status are independent")
    void testDoubledAndRedoubledIndependence() {
        Seat northDeclarer = new Seat(Position.NORTH);

        // Neither
        Contract neither = new Contract(1, Suit.CLUBS, northDeclarer, false, false);
        assertFalse(neither.isDoubled());
        assertFalse(neither.isRedoubled());

        // Doubled only
        Contract doubled = new Contract(1, Suit.DIAMONDS, northDeclarer, true, false);
        assertTrue(doubled.isDoubled());
        assertFalse(doubled.isRedoubled());

        // Redoubled only
        Contract redoubled = new Contract(1, Suit.HEARTS, northDeclarer, false, true);
        assertFalse(redoubled.isDoubled());
        assertTrue(redoubled.isRedoubled());

        // Both (should not happen in practice, but test anyway)
        Contract both = new Contract(1, Suit.SPADES, northDeclarer, true, true);
        assertTrue(both.isDoubled());
        assertTrue(both.isRedoubled());
    }

    @Test
    @DisplayName("isTrumpContract and isNoTrump are mutually exclusive")
    void testTrumpAndNoTrumpMutuallyExclusive() {
        // For each contract, exactly one should be true
        assertTrue(contract1S.isTrumpContract() != contract1S.isNoTrump());
        assertTrue(contract1NT.isTrumpContract() != contract1NT.isNoTrump());
        assertTrue(contract4S.isTrumpContract() != contract4S.isNoTrump());
    }

    @Test
    @DisplayName("toString contains level and denomination information")
    void testToString() {
        String result = contract4S.toString();
        // Check that level and suit appear in the string
        assertTrue(result.contains("4") || result.contains("SPADES") || result.contains("S"));
    }

    @Test
    @DisplayName("Multiple contracts can exist with different properties")
    void testMultipleContracts() {
        Seat north = new Seat(Position.NORTH);
        Seat south = new Seat(Position.SOUTH);

        Contract[] contracts = {
                new Contract(1, Suit.CLUBS, north, false, false),
                new Contract(2, Suit.DIAMONDS, south, true, false),
                new Contract(3, Suit.HEARTS, north, false, true),
                new Contract(4, Suit.SPADES, south, true, true),
                new Contract(5, Suit.NT, north, false, false)
        };

        assertEquals(5, contracts.length);
        assertEquals(Suit.CLUBS, contracts[0].getDenomination());
        assertTrue(contracts[1].isDoubled());
        assertTrue(contracts[2].isRedoubled());
        assertTrue(contracts[3].isDoubled() && contracts[3].isRedoubled());
        assertEquals(11, contracts[4].getRequiredTricks()); // 5 + 6 = 11
    }
}