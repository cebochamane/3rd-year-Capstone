import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlDealParser {
    private static final Pattern VULNERABILITY =
            Pattern.compile("vulnerable-(NONE|NS|EW|BOTH|ALL)", Pattern.CASE_INSENSITIVE);

    private static final Pattern DEALER =
            Pattern.compile("<abbr\\s+title\\s*=\\s*\"Dealer\"[^>]*>",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern SUIT_HOLDING =
            Pattern.compile(
                    "<i\\s+class\\s*=\\s*\"(spade|heart|diamond|club)\"[^>]*>.*?</i>"
                            + "\\s*<ul>(.*?)</ul>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern RANK =
            Pattern.compile("<li>\\s*([^<\\s]+)\\s*</li>",
                    Pattern.CASE_INSENSITIVE);

    /**
     * Constructs an instance of HtmlDealParser; private to prevent instantiation.
     */
    private HtmlDealParser() {
    }

    /**
     * Parses a Deal from the HTML content read from the given file path.
     * 
     * @param path the file path to the HTML deal file
     * @return the parsed Deal object extracted from the HTML file content
     * @throws IOException if an I/O error occurs reading from the file
     */
    public static Deal parse(Path path) throws IOException {
        return parse(TutorialLessonParser.readText(path));
    }

    /**
     * Parses a Deal from the given HTML string representation.
     * 
     * @param html the HTML string containing the deal information
     * @return the parsed Deal object representing the deal described in the HTML
     */
    public static Deal parse(String html) {
        if (html == null || html.isBlank()) {
            throw new IllegalArgumentException("The HTML deal file cannot be empty.");
        }

        Vulnerability vulnerability = parseVulnerability(html);
        Seat dealer = null;
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);

        for (Position position : Position.values()) {
            String block = findSeatBlock(html, position);

            if (DEALER.matcher(block).find()) {
                if (dealer != null) {
                    throw new IllegalArgumentException("The HTML deal has more than one dealer.");
                }
                dealer = new Seat(position);
            }

            hands.put(position, parseHand(block, position));
        }

        if (dealer == null) {
            throw new IllegalArgumentException("The HTML deal does not identify a dealer.");
        }

        return Deal.fromHands(dealer, vulnerability, hands);
    }

    /**
     * Finds and returns the HTML block corresponding to the specified seat position.
     * 
     * @param html the full HTML content to search within
     * @param position the seat position (e.g., NORTH, EAST) to find the block for
     * @return the HTML block string for the specified seat position
     */
    private static String findSeatBlock(String html, Position position) {
        String seat = position.name().toLowerCase(Locale.ROOT);

        Pattern blockPattern = Pattern.compile(
                "<div\\s+class\\s*=\\s*\"([^\"]*\\b" + seat + "\\b[^\"]*)\"[^>]*>"
                        + "(.*?)</div>\\s*<!--\\s*\\." + seat + "\\s*-->",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        Matcher matcher = blockPattern.matcher(html);

        if (!matcher.find()) {
            throw new IllegalArgumentException("The HTML deal is missing the " + position + " hand.");
        }

        return matcher.group(0);
    }

    /**
     * Parses and returns the vulnerability status from the given HTML content.
     * 
     * @param html the HTML content containing the vulnerability information
     * @return the Vulnerability enum value representing the parsed vulnerability
     */
    private static Vulnerability parseVulnerability(String html) {
        Matcher matcher = VULNERABILITY.matcher(html);

        if (!matcher.find()) {
            throw new IllegalArgumentException("The HTML deal does not identify vulnerability.");
        }

        return switch (matcher.group(1).toUpperCase(Locale.ROOT)) {
            case "NONE" -> Vulnerability.NONE;
            case "NS" -> Vulnerability.NS;
            case "EW" -> Vulnerability.EW;
            case "BOTH", "ALL" -> Vulnerability.ALL;
            default -> throw new IllegalArgumentException("Unknown vulnerability in HTML deal.");
        };
    }

    /**
     * Parses and returns the list of 13 cards for the given seat's HTML block and position.
     * 
     * @param block the HTML block containing the cards information for a seat
     * @param position the seat position associated with this hand
     * @return a list of Card objects representing the parsed hand
     */
    private static List<Card> parseHand(String block, Position position) {
        List<Card> cards = new ArrayList<>();
        Matcher suitMatcher = SUIT_HOLDING.matcher(block);

        while (suitMatcher.find()) {
            Suit suit = switch (suitMatcher.group(1).toLowerCase(Locale.ROOT)) {
                case "spade" -> Suit.SPADES;
                case "heart" -> Suit.HEARTS;
                case "diamond" -> Suit.DIAMONDS;
                case "club" -> Suit.CLUBS;
                default -> throw new IllegalArgumentException("Unknown suit in " + position + " hand.");
            };

            Matcher rankMatcher = RANK.matcher(suitMatcher.group(2));

            while (rankMatcher.find()) {
                cards.add(new Card(suit, parseRank(rankMatcher.group(1))));
            }
        }

        if (cards.size() != 13) {
            throw new IllegalArgumentException(
                    position + " must have 13 cards in the HTML deal; found " + cards.size() + ".");
        }

        return cards;
    }

    /**
     * Parses and returns the Rank enum value corresponding to the given rank string.
     * 
     * @param value the string representation of the card rank to parse
     * @return the Rank enum corresponding to the parsed rank string
     */
    private static Rank parseRank(String value) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "2" -> Rank.TWO;
            case "3" -> Rank.THREE;
            case "4" -> Rank.FOUR;
            case "5" -> Rank.FIVE;
            case "6" -> Rank.SIX;
            case "7" -> Rank.SEVEN;
            case "8" -> Rank.EIGHT;
            case "9" -> Rank.NINE;
            case "10", "T" -> Rank.TEN;
            case "J" -> Rank.JACK;
            case "Q" -> Rank.QUEEN;
            case "K" -> Rank.KING;
            case "A" -> Rank.ACE;
            default -> throw new IllegalArgumentException("Invalid HTML card rank: " + value);
        };
    }
}