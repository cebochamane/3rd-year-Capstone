import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

/**
 * Loads the two plain-text tutorial formats supplied for BridgeTut.
 *
 * The parser deliberately fails fast when a lesson has an invalid card code,
 * a duplicate card, an invalid auction, or a scripted play that cannot happen.
 */
public final class TutorialLessonParser {

    private static final String HEADER = "Cards held: spades;hearts;diamonds;clubs";

    /**
     * Constructs a TutorialLessonParser instance, preventing external instantiation.
     */
    private TutorialLessonParser() {
    }

    /**
     * Reads tutorial lessons from the file at the given path and parses them into a list of TutorialLesson objects.
     * 
     * @param path the file system path to the tutorial text file
     * @return a list of parsed TutorialLesson instances
     * @throws IOException if an I/O error occurs reading the file
     */
    public static List<TutorialLesson> parse(Path path) throws IOException {
        return parse(readText(path));
    }

    /**
     * Reads all bytes from the given file path and decodes them into a String, trying UTF-8 first and falling back to Windows-1252 on decoding errors.
     * @param path the file system path to read text from
     * @return the decoded text contents of the file
     * @throws IOException if an I/O error occurs reading the file
     */
    public static String readText(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);

        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            return new String(bytes, Charset.forName("windows-1252"));
        }
    }

    /**
     * Parses tutorial lessons from the given text string into a list of TutorialLesson objects.
     * 
     * @param text the tutorial text to parse
     * @return a list of parsed TutorialLesson instances
     */
    public static List<TutorialLesson> parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("A tutorial file cannot be empty.");
        }

        String[] lines = text.replace("\r", "").split("\n", -1);
        int line = 0;
        List<TutorialLesson> lessons = new ArrayList<>();

        while (true) {
            line = skipBlankLines(lines, line);
            if (line == lines.length) {
                return lessons;
            }
            if (!lines[line].trim().equalsIgnoreCase(HEADER)) {
                throw error(line + 1, "Expected '" + HEADER + "'.");
            }
            line++;

            ParsedHands parsedHands = parseHands(lines, line);
            line = parsedHands.nextLine;

            if (line >= lines.length) {
                throw error(line + 1, "Missing lesson instructions after the four hands.");
            }

            if (startsWith(lines[line], "PLAY ")) {
                ParseResult result = parsePlayOnly(lines, line, parsedHands.hands);
                lessons.add(result.lesson);
                line = result.nextLine;
            } else {
                ParseResult result = parseBidAndPlay(lines, line, parsedHands.hands);
                lessons.add(result.lesson);
                line = result.nextLine;
            }
        }
    }

    /**
     * Parses the four hands from the tutorial lines starting at the given line index.
     * 
     * @param lines array of tutorial text lines
     * @param line the current line index to start parsing from
     * @return a ParsedHands object containing the hands map and next line index
     */
    private static ParsedHands parseHands(String[] lines, int line) {
        Map<Position, List<Card>> hands = new EnumMap<>(Position.class);
        for (int count = 0; count < Position.values().length; count++) {
            if (line >= lines.length) {
                throw error(line + 1, "Expected four hand lines.");
            }

            String handLine = lines[line].trim();
            if (handLine.length() < 3 || handLine.charAt(1) != ':') {
                throw error(line + 1, "Expected a hand line such as 'S: AKQ;-;T98;432'.");
            }

            Position position = parsePosition(handLine.substring(0, 1), line + 1);
            if (hands.containsKey(position)) {
                throw error(line + 1, "The " + position + " hand appears more than once.");
            }
            hands.put(position, parseHand(handLine.substring(2), line + 1));
            line++;
        }

        if (hands.size() != Position.values().length) {
            throw error(line, "The lesson must provide North, East, South and West hands.");
        }
        return new ParsedHands(hands, line);
    }

    /**
     * Parses a bid-and-play lesson section including dealer, vulnerability, auction, contract, and play from tutorial lines starting at the given line.
     * 
     * @param lines array of tutorial text lines
     * @param line the current line index to start parsing from
     * @param hands map of hands by position
     * @return a ParseResult containing the parsed TutorialLesson and the next line index
     */
    private static ParseResult parseBidAndPlay(String[] lines, int line,
            Map<Position, List<Card>> hands) {
        Seat dealer = new Seat(parseDealer(lines[line++], line));
        Vulnerability vulnerability = parseVulnerability(lines[line++], line);

        if (line >= lines.length || !lines[line].trim().endsWith(".")) {
            throw error(line + 1, "Expected a semicolon-separated auction ending with '.'.");
        }
        List<Bid> bids = parseAuction(lines[line++], dealer, line);
        Contract contract = createContract(bids, line);
        Deal deal = createDeal(dealer, vulnerability, hands, line);
        return parsePlay(lines, line, TutorialLessonType.BID_AND_PLAY, deal, contract, bids);
    }

    /**
     * Parses a play-only lesson section including contract and play from tutorial lines starting at the given line.
     * 
     * @param lines array of tutorial text lines
     * @param line the current line index to start parsing from
     * @param hands map of hands by position
     * @return a ParseResult containing the parsed TutorialLesson and the next line index
     */
    private static ParseResult parsePlayOnly(String[] lines, int line,
            Map<Position, List<Card>> hands) {
        Contract contract = parsePlayContract(lines[line], line + 1);
        line++;

        /* In play-only lessons, South is always the learner and declarer. */
        Seat south = new Seat(Position.SOUTH);
        contract = new Contract(contract.getLevel(), contract.getDenomination(), south, false, false);
        Deal deal = createDeal(south, Vulnerability.NONE, hands, line);
        return parsePlay(lines, line, TutorialLessonType.PLAY_ONLY, deal, contract, List.of());
    }

    /**
     * Parses the scripted play section of a tutorial lesson based on provided type, deal, contract, and auction bids, starting from the given line.
     * 
     * @param lines array of tutorial text lines
     * @param line the current line index to start parsing from
     * @param type the type of tutorial lesson (bid and play or play only)
     * @param deal the deal associated with the lesson
     * @param contract the contract of the lesson
     * @param bids the list of auction bids
     * @return a ParseResult containing the parsed TutorialLesson and the next line index
     */
    private static ParseResult parsePlay(String[] lines, int line, TutorialLessonType type,
            Deal deal, Contract contract, List<Bid> bids) {
        List<TutorialAction> actions = new ArrayList<>();
        Map<Position, Hand> remainingHands = copyHands(deal);
        for (Bid bid : bids) {
            actions.add(TutorialAction.bid(bid));
        }

        Seat leader = contract.getDeclarer().next();
        while (line < lines.length) {
            String value = lines[line].trim();
            if (value.equalsIgnoreCase("Claim") || value.equalsIgnoreCase("Concede")) {
                actions.add(value.equalsIgnoreCase("Claim")
                        ? TutorialAction.claim() : TutorialAction.concede());
                line++;
                break;
            }

            List<Card> cards = parseTrick(value, line + 1);
            Trick trick = new Trick(actions.size() + 1, leader);
            Seat player = leader;
            for (Card card : cards) {
                Hand playerHand = remainingHands.get(player.getPosition());
                if (!playerHand.contains(card)) {
                    throw error(line + 1, player + " is scripted to play " + card
                            + " but does not hold that remaining card.");
                }
                if (!isLegalPlay(playerHand, card, leadSuit(trick))) {
                    throw error(line + 1, player + " must follow suit and cannot play " + card + ".");
                }
                trick.addPlay(player, card);
                actions.add(TutorialAction.card(player, card));
                playerHand.removeCard(card);
                player = player.next();
            }
            leader = trick.getWinner(contract.getDenomination());
            line++;
        }

        if (actions.isEmpty() || (actions.get(actions.size() - 1).getType() != TutorialAction.Type.CLAIM
                && actions.get(actions.size() - 1).getType() != TutorialAction.Type.CONCEDE)) {
            throw error(line + 1, "Every lesson must end with Claim or Concede.");
        }

        if (line >= lines.length || !startsWith(lines[line], "NOTE:")) {
            throw error(line + 1, "Claim or Concede must be followed by NOTE:.");
        }
        String note = lines[line].trim().substring("NOTE:".length()).trim();
        if (note.isEmpty()) {
            throw error(line + 1, "The lesson note cannot be empty.");
        }
        return new ParseResult(new TutorialLesson(type, deal, contract, actions, note), line + 1);
    }

    /**
     * Creates a Deal object from the given dealer seat, vulnerability, hands map, and line index for error reporting.
     * 
     * @param dealer the dealer seat
     * @param vulnerability the vulnerability state
     * @param hands map of hands by position
     * @param line the line number for error messages
     * @return a Deal representing the bridge hands and state
     */
    private static Deal createDeal(Seat dealer, Vulnerability vulnerability,
            Map<Position, List<Card>> hands, int line) {
        try {
            return Deal.fromHands(dealer, vulnerability, hands);
        } catch (IllegalArgumentException exception) {
            throw error(line, exception.getMessage());
        }
    }

    /**
     * Creates a deep copy of the hands contained in the given Deal, mapping positions to Hand objects.
     * 
     * @param deal the Deal whose hands to copy
     * @return a map from positions to independent Hand objects copied from the deal
     */
    private static Map<Position, Hand> copyHands(Deal deal) {
        Map<Position, Hand> copies = new EnumMap<>(Position.class);
        for (Position position : Position.values()) {
            copies.put(position, new Hand(new ArrayList<>(deal.getHand(new Seat(position)).getCards())));
        }
        return copies;
    }

    /**
     * Determines the lead suit of the given trick by inspecting the first played card.
     * 
     * @param trick the Trick object to examine
     * @return the Suit led in the trick, or null if no card led
     */
    private static Suit leadSuit(Trick trick) {
        Card leadCard = trick.getPlays().get(trick.getLeader());
        return leadCard == null ? null : leadCard.getSuit();
    }

    /**
     * Parses a single hand's suit holdings from a semicolon-separated string representing spades, hearts, diamonds, and clubs.
     * 
     * @param text the hand text to parse
     * @param line the line number for error reporting
     * @return a list of Card objects representing the parsed hand
     */
    private static List<Card> parseHand(String text, int line) {
        String[] suits = text.trim().split(";", -1);
        if (suits.length != 4) {
            throw error(line, "A hand needs four semicolon-separated suit holdings.");
        }

        Suit[] order = { Suit.SPADES, Suit.HEARTS, Suit.DIAMONDS, Suit.CLUBS };
        List<Card> cards = new ArrayList<>();
        for (int index = 0; index < order.length; index++) {
            String ranks = suits[index].trim().toUpperCase(Locale.ROOT);
            if (ranks.equals("-")) {
                continue;
            }
            for (char rankCode : ranks.toCharArray()) {
                cards.add(new Card(order[index], parseRank(rankCode, line)));
            }
        }
        return cards;
    }

    /**
     * Parses the four comma-separated card codes for a trick into a list of Card objects.
     * 
     * @param text the trick line text to parse
     * @param line the line number for error reporting
     * @return a list of four Card objects representing the trick's plays
     */
    private static List<Card> parseTrick(String text, int line) {
        String[] codes = text.split(",", -1);
        if (codes.length != Position.values().length) {
            throw error(line, "A trick needs exactly four comma-separated cards.");
        }
        List<Card> cards = new ArrayList<>();
        for (String code : codes) {
            cards.add(parseCard(code.trim(), line));
        }
        return cards;
    }

    /**
     * Checks follow-suit legality by card value rather than object identity.
     * Tutorial cards are parsed separately from the cards stored in the deal, so List.contains(card) cannot be used here.
     */
    private static boolean isLegalPlay(Hand hand, Card card, Suit leadSuit) {
        return leadSuit == null
                || !hand.contains(leadSuit)
                || card.getSuit() == leadSuit;
    }

    /**
     * Parses a two-character card code into a Card object.
     * 
     * @param code the two-character card code (rank and suit)
     * @param line the line number for error reporting
     * @return a Card corresponding to the code
     */
    private static Card parseCard(String code, int line) {
        if (code.length() != 2) {
            throw error(line, "Invalid card code '" + code + "'. Use forms such as AS, TD or 7C.");
        }
        return new Card(parseSuitCode(code.charAt(1), line), parseRank(code.charAt(0), line));
    }

    /**
     * Parses a play contract line starting with 'PLAY' into a Contract for the South declarer position.
     * 
     * @param text the play contract line text
     * @param line the line number for error reporting
     * @return a Contract object representing the parsed contract for South
     */
    private static Contract parsePlayContract(String text, int line) {
        String value = text.trim().toUpperCase(Locale.ROOT);
        if (!value.startsWith("PLAY ")) {
            throw error(line, "Expected a play contract such as 'PLAY 3NT'.");
        }
        String contractText = value.substring(5).trim();
        ParsedContract parsed = parseContractText(contractText, line);
        return new Contract(parsed.level, parsed.denomination, new Seat(Position.SOUTH), false, false);
    }

    /**
     * Parses a semicolon-separated auction string ending with a '.' into a list of Bid objects starting from the given dealer seat.
     * 
     * @param text the auction string to parse
     * @param dealer the seat of the dealer starting the auction
     * @param line the line number for error reporting
     * @return a list of Bid objects representing the auction
     */
    private static List<Bid> parseAuction(String text, Seat dealer, int line) {
        String[] tokens = text.trim().substring(0, text.trim().length() - 1).split(";", -1);
        List<Bid> bids = new ArrayList<>();
        Seat bidder = dealer;
        for (String token : tokens) {
            String value = token.trim().toUpperCase(Locale.ROOT);
            if (value.equals("P")) {
                bids.add(Bid.BidPass(bidder));
            } else if (value.equals("DBL")) {
                bids.add(Bid.BidDouble(bidder));
            } else if (value.equals("RDBL")) {
                bids.add(Bid.BidRedouble(bidder));
            } else {
                ParsedContract contract = parseContractText(value, line);
                bids.add(new Bid(bidder, contract.level, contract.denomination));
            }
            bidder = bidder.next();
        }
        return bids;
    }

    /**
     * Constructs the final Contract from a list of auction bids, identifying the final bid and declarer, and any doubling.
     * 
     * @param bids the list of auction bids
     * @param line the line number for error reporting
     * @return the final Contract derived from the auction bids
     */
    private static Contract createContract(List<Bid> bids, int line) {
        Bid finalBid = null;
        int finalIndex = -1;
        for (int index = 0; index < bids.size(); index++) {
            Bid bid = bids.get(index);
            if (!bid.isPass() && !bid.isDouble() && !bid.isRedouble()) {
                finalBid = bid;
                finalIndex = index;
            }
        }
        if (finalBid == null) {
            throw error(line, "A bid-and-play lesson needs a final contract bid.");
        }

        Seat declarer = finalBid.getBidder();
        for (Bid bid : bids) {
            if (!bid.isPass() && !bid.isDouble() && !bid.isRedouble()
                    && bid.getDenomination() == finalBid.getDenomination()
                    && bid.getBidder().isPartner(finalBid.getBidder())) {
                declarer = bid.getBidder();
                break;
            }
        }

        boolean doubled = false;
        boolean redoubled = false;
        for (int index = finalIndex + 1; index < bids.size(); index++) {
            if (bids.get(index).isDouble()) {
                doubled = true;
            } else if (bids.get(index).isRedouble()) {
                redoubled = true;
            }
        }
        return new Contract(finalBid.getLevel(), finalBid.getDenomination(), declarer, doubled, redoubled);
    }

    /**
     * Parses a contract string such as '3NT' or '2H' into its level and suit denomination components.
     * 
     * @param text the contract text to parse
     * @param line the line number for error reporting
     * @return a ParsedContract holding the level and suit denomination
     */
    private static ParsedContract parseContractText(String text, int line) {
        String value = text.trim().toUpperCase(Locale.ROOT);
        if (value.length() < 2 || value.length() > 3 || value.charAt(0) < '1' || value.charAt(0) > '7') {
            throw error(line, "Invalid contract '" + text + "'.");
        }
        String denomination = value.substring(1);
        Suit suit;
        if (denomination.equals("NT")) {
            suit = Suit.NT;
        } else if (denomination.length() == 1) {
            suit = parseSuitCode(denomination.charAt(0), line);
        } else {
            throw error(line, "Invalid contract '" + text + "'.");
        }
        return new ParsedContract(value.charAt(0) - '0', suit);
    }

    /**
     * Parses a dealer line formatted like 'D: S' into a Position representing the dealer.
     * 
     * @param text the dealer line text
     * @param line the line number for error reporting
     * @return the Position of the dealer
     */
    private static Position parseDealer(String text, int line) {
        String value = text.trim().toUpperCase(Locale.ROOT);
        if (!value.startsWith("D:")) {
            throw error(line, "Expected dealer line such as 'D: S'.");
        }
        return parsePosition(value.substring(2).trim(), line);
    }

    /**
     * Parses a vulnerability line into the corresponding Vulnerability enum value.
     * 
     * @param text the vulnerability line text
     * @param line the line number for error reporting
     * @return the Vulnerability value parsed
     */
    private static Vulnerability parseVulnerability(String text, int line) {
        String value = text.trim().toUpperCase(Locale.ROOT);
        return switch (value) {
            case "NONE VUL" -> Vulnerability.NONE;
            case "NS VUL" -> Vulnerability.NS;
            case "EW VUL" -> Vulnerability.EW;
            case "BOTH VUL" -> Vulnerability.ALL;
            default -> throw error(line, "Invalid vulnerability '" + text + "'.");
        };
    }

    /**
     * Parses a single-character string into its corresponding Position enum value.
     * 
     * @param value the position string to parse (e.g., 'N')
     * @param line the line number for error reporting
     * @return the Position corresponding to the input
     */
    private static Position parsePosition(String value, int line) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "N" -> Position.NORTH;
            case "E" -> Position.EAST;
            case "S" -> Position.SOUTH;
            case "W" -> Position.WEST;
            default -> throw error(line, "Invalid position '" + value + "'.");
        };
    }

    /**
     * Parses a character code into the corresponding Suit enum value.
     * 
     * @param code a single character suit code
     * @param line the line number for error reporting
     * @return the Suit represented by the code
     */
    private static Suit parseSuitCode(char code, int line) {
        return switch (Character.toUpperCase(code)) {
            case 'C' -> Suit.CLUBS;
            case 'D' -> Suit.DIAMONDS;
            case 'H' -> Suit.HEARTS;
            case 'S' -> Suit.SPADES;
            default -> throw error(line, "Invalid suit code '" + code + "'.");
        };
    }

    /**
     * Parses a character code into the corresponding Rank enum value, using 'T' for ten.
     * 
     * @param code a single character rank code
     * @param line the line number for error reporting
     * @return the Rank represented by the code
     */
    private static Rank parseRank(char code, int line) {
        return switch (Character.toUpperCase(code)) {
            case '2' -> Rank.TWO;
            case '3' -> Rank.THREE;
            case '4' -> Rank.FOUR;
            case '5' -> Rank.FIVE;
            case '6' -> Rank.SIX;
            case '7' -> Rank.SEVEN;
            case '8' -> Rank.EIGHT;
            case '9' -> Rank.NINE;
            case 'T' -> Rank.TEN;
            case 'J' -> Rank.JACK;
            case 'Q' -> Rank.QUEEN;
            case 'K' -> Rank.KING;
            case 'A' -> Rank.ACE;
            default -> throw error(line, "Invalid rank code '" + code + "'. Use T for ten.");
        };
    }

    /**
     * Advances the line index past any blank lines in the given array starting at the specified line.
     * 
     * @param lines array of tutorial text lines
     * @param line the current line index
     * @return the index of the first non-blank line at or after the input line
     */
    private static int skipBlankLines(String[] lines, int line) {
        while (line < lines.length && lines[line].isBlank()) {
            line++;
        }
        return line;
    }

    /**
     * Checks if the input text starts with the given prefix, ignoring case and leading/trailing whitespace.
     * 
     * @param text the text to check
     * @param prefix the prefix to look for
     * @return true if the text starts with the prefix; false otherwise
     */
    private static boolean startsWith(String text, String prefix) {
        return text.trim().toUpperCase(Locale.ROOT).startsWith(prefix.toUpperCase(Locale.ROOT));
    }

    /**
     * Constructs an IllegalArgumentException with a formatted message including the line number.
     * 
     * @param line the line number where the error occurred
     * @param message the error message
     * @return an IllegalArgumentException containing the error details
     */
    private static IllegalArgumentException error(int line, String message) {
        return new IllegalArgumentException("Tutorial lesson line " + line + ": " + message);
    }

    private static final class ParsedHands {
        private final Map<Position, List<Card>> hands;
        private final int nextLine;

        /**
         * Constructs a ParsedHands object containing the map of hands and the next line index after parsing hands.
         * 
         * @param hands a map from Position to list of Cards representing parsed hands
         * @param nextLine the line index after parsing the hands
         */
        private ParsedHands(Map<Position, List<Card>> hands, int nextLine) {
            this.hands = hands;
            this.nextLine = nextLine;
        }
    }

    private static final class ParsedContract {
        private final int level;
        private final Suit denomination;

        /**
         * Constructs a ParsedContract consisting of the contract level and suit denomination.
         * 
         * @param level the contract level (1-7)
         * @param denomination the suit denomination (including NT)
         */
        private ParsedContract(int level, Suit denomination) {
            this.level = level;
            this.denomination = denomination;
        }
    }

    private static final class ParseResult {
        private final TutorialLesson lesson;
        private final int nextLine;

        /**
         * Constructs a ParseResult pairing a parsed TutorialLesson and the next line index to continue parsing.
         * 
         * @param lesson the TutorialLesson parsed from the input
         * @param nextLine the next line index to continue parsing after this lesson
         */
        private ParseResult(TutorialLesson lesson, int nextLine) {
            this.lesson = lesson;
            this.nextLine = nextLine;
        }
    }
}