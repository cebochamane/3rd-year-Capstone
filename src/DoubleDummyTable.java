import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/** Immutable full-deal trick table. Rows use Suit order; columns use Position order. */
public final class DoubleDummyTable {
    public static final String SOLVER_VERSION = "DDS 2.9.0";
    private final String handId;
    private final int[][] tricks;

    /**
     * Constructs an immutable double-dummy trick table from a deal and a matrix of trick results for all suits and declarers.
     * 
     * @param deal The deal representing the card distribution.
     * @param values A 5x4 matrix of trick counts for each suit and declarer position.
     */
    public DoubleDummyTable(Deal deal, int[][] values) {
        this.handId = handId(deal);
        if (values == null || values.length != Suit.values().length) {
            throw new IllegalArgumentException("A double-dummy table needs all five strains.");
        }
        tricks = new int[5][4];
        for (Suit strain : Suit.values()) {
            int[] row = values[strain.ordinal()];
            if (row == null || row.length != 4) {
                throw new IllegalArgumentException("Every strain needs four declarer results.");
            }
            for (Position declarer : Position.values()) {
                int value = row[declarer.ordinal()];
                if (value < 0 || value > 13) throw new IllegalArgumentException("Tricks must be between 0 and 13.");
                tricks[strain.ordinal()][declarer.ordinal()] = value;
            }
        }
    }

    /**
     * Returns the unique SHA-256 hash identifier representing the canonical holdings of the deal.
     * 
     * @return A string containing the hexadecimal hand identifier.
     */
    public String getHandId() { return handId; }

    /**
     * Returns the number of tricks for the specified strain and declarer position.
     * 
     * @param strain The suit strain to query.
     * @param declarer The declarer position to query.
     * @return The number of tricks won (0 to 13).
     */
    public int getTricks(Suit strain, Position declarer) {
        if (strain == null || declarer == null) throw new IllegalArgumentException("Strain and declarer are required.");
        return tricks[strain.ordinal()][declarer.ordinal()];
    }

    /**
     * Returns the best trick count for the specified strain for the given partnership.
     * 
     * @param strain The suit strain to query.
     * @param northSouth True to query North-South partnership, false for East-West.
     * @return The maximum number of tricks won by either partner declaring in the partnership.
     */
    public int getPartnershipTricks(Suit strain, boolean northSouth) {
        return northSouth ? Math.max(getTricks(strain, Position.NORTH), getTricks(strain, Position.SOUTH))
                : Math.max(getTricks(strain, Position.EAST), getTricks(strain, Position.WEST));
    }

    /**
     * Creates a deep copy of the given deal including all hands and cards.
     * 
     * @param deal The deal to copy.
     * @return A new Deal object identical to the input.
     */
    public static Deal copyDeal(Deal deal) {
        if (deal == null) {
            throw new IllegalArgumentException("A complete deal is required.");
        }

        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);

        for (Position position : Position.values()) {
            Hand hand = deal.getHand(new Seat(position));
            if (hand == null) throw new IllegalArgumentException("Missing hand for " + position + ".");
            hands.put(position, new ArrayList<>(hand.getCards()));
        }

        return Deal.fromHands(deal.getDealer(), deal.getVulnerability(), hands);
    }

    /**
     * Generates a canonical string representing the card holdings of the given deal across all positions and suits.
     * 
     * @param deal The deal to generate holdings string for.
     * @return A space-separated string encoding bit masks for each suit and position.
     */
    public static String holdings(Deal deal) {
        Deal snapshot = copyDeal(deal);
        StringBuilder text = new StringBuilder();
        for (Position position : Position.values()) {
            int[] masks = new int[4];
            for (Card card : snapshot.getHand(new Seat(position)).getCards()) {
                masks[DdsSolver.suitCode(card.getSuit())] |= 1 << (card.getRank().ordinal() + 2);
            }
            for (int mask : masks) {
                if (text.length() > 0) text.append(' ');
                text.append(mask);
            }
        }
        return text.toString();
    }

    /**
     * Computes the SHA-256 hash identifier string for the canonical representation of the given deal's holdings.
     * 
     * @param deal The deal to compute the hand identifier for.
     * @return A hexadecimal string representing the SHA-256 hash of the deal's canonical holdings.
     */
    public static String handId(Deal deal) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(holdings(deal).getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }

    /**
     * Reconstructs a Deal object from a canonical holdings string representation.
     * 
     * @param text The string encoding of stored holdings.
     * @return A Deal object reconstructed from the holdings data.
     */
    static Deal fromHoldings(String text) {
        if (text == null) throw new IllegalArgumentException("Missing stored holdings.");
        String[] fields = text.split(" ");
        if (fields.length != 16) throw new IllegalArgumentException("Invalid stored holdings.");
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);
        Suit[] suits = {Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS};
        for (Position seat : Position.values()) {
            List<Card> cards = new ArrayList<>();
            for (int suit = 0; suit < 4; suit++) {
                int mask = Integer.parseInt(fields[seat.ordinal() * 4 + suit]);
                if ((mask & ~0x7ffc) != 0) throw new IllegalArgumentException("Invalid stored card ranks.");
                for (Rank rank : Rank.values()) {
                    if ((mask & (1 << (rank.ordinal() + 2))) != 0) cards.add(new Card(suits[suit], rank));
                }
            }
            hands.put(seat, cards);
        }
        return Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, hands);
    }
    
    /**
     * Generates a multi-line textual summary of the double-dummy trick table results for all suits and declarers.
     * 
     * @return A string suitable for display summarizing trick counts by strain and partnership.
     */
    public String toDisplayText() {
        StringBuilder text = new StringBuilder("Original-deal double-dummy tricks (" + SOLVER_VERSION + ")\n");
        text.append("Strain: North, South, NS best | East, West, EW best\n");
        for (Suit strain : Suit.values()) {
            text.append(strain).append(": ").append(getTricks(strain, Position.NORTH)).append(", ")
                    .append(getTricks(strain, Position.SOUTH)).append(", ")
                    .append(getPartnershipTricks(strain, true)).append(" | ")
                    .append(getTricks(strain, Position.EAST)).append(", ")
                    .append(getTricks(strain, Position.WEST)).append(", ")
                    .append(getPartnershipTricks(strain, false)).append("\n");
        }
        return text.append("Best means the better result with either partner declaring.\n")
                .append("Each result is a separate possible contract; NS/EW best need not total 13.\n")
                .append("Assumes optimal play with all cards known; not a par contract or score.").toString();
    }

}
