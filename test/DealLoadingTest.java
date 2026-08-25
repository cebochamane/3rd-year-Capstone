import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DealLoadingTest {

    @Test
    void fromHandsLoadsEverySelectedCardAndPreservesMetadata() {
        Map<Position, List<Card>> source = completeDeal();
        Deal deal = Deal.fromHands(new Seat(Position.EAST), Vulnerability.NS, source);

        assertEquals(Position.EAST, deal.getDealer().getPosition());
        assertEquals(Vulnerability.NS, deal.getVulnerability());
        for (Position position : Position.values()) {
            assertEquals(13, deal.getHand(new Seat(position)).getCards().size());
            assertContainsSameCards(source.get(position),
                    deal.getHand(new Seat(position)).getCards());
        }
    }

    @Test
    void fromHandsRejectsAWrongSizedHand() {
        Map<Position, List<Card>> source = completeDeal();
        source.get(Position.NORTH).remove(0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, source));

        assertTrue(exception.getMessage().contains("NORTH"));
    }

    @Test
    void fromHandsRejectsDuplicateCardsAcrossPlayers() {
        Map<Position, List<Card>> source = completeDeal();
        source.get(Position.EAST).set(0, source.get(Position.NORTH).get(0));

        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, source));
    }

    @Test
    void draftShowsOnlyUnassignedCardsAndRejectsASecondAssignment() {
        DealDraft draft = new DealDraft(new Seat(Position.WEST), Vulnerability.EW);
        draft.assignCard(Position.NORTH, new Card(Suit.SPADES, Rank.ACE));

        assertEquals(51, draft.getAvailableCards().size());
        assertThrows(IllegalArgumentException.class,
                () -> draft.assignCard(Position.SOUTH, new Card(Suit.SPADES, Rank.ACE)));
    }

    @Test
    void draftCreatesAValidDealAfterAllCardsAreAssigned() {
        DealDraft draft = new DealDraft(new Seat(Position.SOUTH), Vulnerability.ALL);
        Map<Position, List<Card>> source = completeDeal();
        for (Position position : Position.values()) {
            for (Card card : source.get(position)) {
                draft.assignCard(position, card);
            }
        }

        Deal deal = draft.createDeal();

        assertTrue(draft.isComplete());
        assertEquals(Position.SOUTH, deal.getDealer().getPosition());
        assertEquals(13, deal.getHand(new Seat(Position.WEST)).getCards().size());
    }

    @Test
    void draftCannotCreateAnIncompleteDeal() {
        DealDraft draft = new DealDraft(new Seat(Position.NORTH), Vulnerability.NONE);
        draft.assignCard(Position.NORTH, new Card(Suit.CLUBS, Rank.TWO));

        assertThrows(IllegalStateException.class, draft::createDeal);
    }

    @Test
    void fromHandsRejectsMissingMetadataOrAPlayerHand() {
        Map<Position, List<Card>> source = completeDeal();

        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(null, Vulnerability.NONE, source));
        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), null, source));
        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, null));

        source.remove(Position.WEST);
        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, source));
    }

    @Test
    void fromHandsRejectsNullAndNoTrumpCards() {
        Map<Position, List<Card>> source = completeDeal();
        source.get(Position.NORTH).set(0, null);
        Map<Position, List<Card>> nullCardSource = source;

        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, nullCardSource));

        source = completeDeal();
        source.get(Position.NORTH).set(0, new Card(Suit.NT, Rank.ACE));
        Map<Position, List<Card>> noTrumpSource = source;
        assertThrows(IllegalArgumentException.class,
                () -> Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, noTrumpSource));
    }

    @Test
    void draftRemovesCardsAndReturnsReadOnlyCardLists() {
        DealDraft draft = new DealDraft(new Seat(Position.WEST), Vulnerability.EW);
        Card aceOfSpades = new Card(Suit.SPADES, Rank.ACE);
        draft.assignCard(Position.NORTH, aceOfSpades);

        assertTrue(draft.removeCard(Position.NORTH, new Card(Suit.SPADES, Rank.ACE)));
        assertFalse(draft.removeCard(Position.NORTH, aceOfSpades));
        assertEquals(52, draft.getAvailableCards().size());

        assertThrows(UnsupportedOperationException.class,
                () -> draft.getCards(Position.NORTH).add(aceOfSpades));
        assertThrows(UnsupportedOperationException.class,
                () -> draft.getAvailableCards().add(aceOfSpades));
    }

    @Test
    void draftRejectsInvalidAssignmentsAndAFourteenthCard() {
        DealDraft draft = new DealDraft(new Seat(Position.NORTH), Vulnerability.NONE);

        assertThrows(IllegalArgumentException.class,
                () -> draft.assignCard(null, new Card(Suit.CLUBS, Rank.TWO)));
        assertThrows(IllegalArgumentException.class,
                () -> draft.assignCard(Position.NORTH, null));
        assertThrows(IllegalArgumentException.class,
                () -> draft.assignCard(Position.NORTH, new Card(Suit.NT, Rank.ACE)));

        Map<Position, List<Card>> source = completeDeal();
        for (Card card : source.get(Position.NORTH)) {
            draft.assignCard(Position.NORTH, card);
        }
        assertThrows(IllegalArgumentException.class,
                () -> draft.assignCard(Position.NORTH, new Card(Suit.DIAMONDS, Rank.TWO)));
    }

    @Test
    void draftRequiresADealerPositionAndVulnerability() {
        assertThrows(IllegalArgumentException.class,
                () -> new DealDraft(null, Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
                () -> new DealDraft(new Seat(null), Vulnerability.NONE));
        assertThrows(IllegalArgumentException.class,
                () -> new DealDraft(new Seat(Position.NORTH), null));
    }

    private Map<Position, List<Card>> completeDeal() {
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            hands.put(position, new ArrayList<>());
        }
        int positionIndex = 0;
        for (Rank rank : Rank.values()) {
            for (Suit suit : Suit.values()) {
                if (suit != Suit.NT) {
                    Position position = Position.values()[positionIndex % Position.values().length];
                    hands.get(position).add(new Card(suit, rank));
                    positionIndex++;
                }
            }
        }
        return hands;
    }

    private void assertContainsSameCards(List<Card> expected, List<Card> actual) {
        assertEquals(expected.size(), actual.size());
        for (Card expectedCard : expected) {
            assertTrue(actual.stream().anyMatch(actualCard ->
                    actualCard.getSuit() == expectedCard.getSuit()
                            && actualCard.getRank() == expectedCard.getRank()),
                    "Missing " + expectedCard);
        }
    }
}
