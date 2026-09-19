import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/** Adapter to the bundled, unmodified Bo Haglund/Soren Hein DDS 2.9.0 solver. */
//Let's go

public final class DdsSolver {
    /**
     * Creates a new instance of the DdsSolver class, which is not instantiable externally.
     */
    private DdsSolver() {}

    private static final Map<String, Integer> CACHE = new LinkedHashMap<>(32, .75f, true) {
        /**
         * Determines whether the eldest entry should be removed to maintain the cache size limit.
         * 
         * @param entry The eldest entry in the cache map being considered for removal
         * @return true if the cache size exceeds 128 entries and the eldest should be removed, false otherwise
         */
        @Override protected boolean removeEldestEntry(Map.Entry<String, Integer> entry) {
            return size() > 128;
        }
    };

    /** Immutable verified result for one exact position, including a partial trick. */
    public static final class Analysis {
        private final String position;
        private final int remaining;
        private final int declarerTricks;
        private final Position declarer;

        /**
         * Constructs a new Analysis instance representing the verified result for one exact position and partial trick state.
         * 
         * @param position The encoded string representation of the position and trick state
         * @param remaining The number of remaining tricks in the hand
         * @param declarerTricks The number of tricks won by the declarer
         * @param declarer The position of the declarer player
         */
        private Analysis(String position, int remaining, int declarerTricks, Position declarer) {
            this.declarer = declarer;
            this.position = position;
            this.remaining = remaining;
            this.declarerTricks = declarerTricks;
        }

        /**
         * Returns the number of remaining tricks left to be played in the hand.
         * 
         * @return The count of remaining tricks
         */
        public int remainingTricks() { 
            return remaining; 
        }

        /**
         * Returns the number of tricks won by the declarer so far in the analysis.
         * 
         * @return The count of declarer's tricks
         */
        public int declarerTricks() { 
            return declarerTricks; 
        }

        /**
         * Calculates the number of tricks taken by a given seat according to the contract and analysis result.
         * 
         * @param seat The seat (player) for whom to determine trick count
         * @param contract The contract describing declarer and denomination
         * @return The number of tricks won by the specified seat
         */
        public int tricksFor(Seat seat, Contract contract) {
            if (seat == null || seat.getPosition() == null) {
                throw new IllegalArgumentException("A valid player is required.");
            }
            Seat declarer = contract.getDeclarer();
            return seat.equals(declarer) || seat.isPartner(declarer) ? declarerTricks : remaining - declarerTricks;
        }

        /**
         * Checks if the analysis matches the current unfinished gameplay position and state exactly.
         * 
         * @param gameplay The Gameplay instance to compare against this analysis
         * @return true if the analysis corresponds exactly to the gameplay state, false otherwise
         */
        boolean matches(Gameplay gameplay) {
            return !gameplay.isFinished() && remaining == gameplay.getRemainingTricks()
                    && declarer == gameplay.getContract().getDeclarer().getPosition() && position.equals(encode(gameplay));
        }
    }

    /**
     * Performs double-dummy analysis on an unfinished gameplay hand by invoking the external DDS solver.
     * 
     * @param gameplay The current gameplay state which must not be finished
     * @return An Analysis object capturing the verified number of declarer tricks and position encoding
     */
    public static Analysis analyze(Gameplay gameplay) {
        if (gameplay == null || gameplay.isFinished()) {
            throw new IllegalStateException("Only an unfinished hand can be analysed.");
        }
        // Capture all input before starting the external process. The GUI locks
        // card play while analysing; Analysis.matches rejects stale completions.
        String position = encode(gameplay);
        int remaining = gameplay.getRemainingTricks();
        Seat player = gameplay.getCurrentPlayer();
        Seat declarer = gameplay.getContract().getDeclarer();
        boolean declarerToPlay = player.equals(declarer) || player.isPartner(declarer);

        String configured = System.getProperty("bridgetut.dds");
        if (configured == null || configured.isBlank()) configured = System.getenv("BRIDGETUT_DDS_EXECUTABLE");
        Path executable = Path.of(configured == null || configured.isBlank()
                ? "native/bin/bridgetut-dds" : configured).toAbsolutePath();
        if (!Files.isExecutable(executable)) {
            throw new IllegalStateException("Double-dummy solver is unavailable. "
                    + "Run python3 native/build_dds.py before starting BridgeTut.");
        }
        String key = executable + "\n" + position;
        Integer value;
        synchronized (CACHE) { value = CACHE.get(key); }
        if (value == null) {
            value = solve(executable, position);
            if (value < 0 || value > remaining) {
                throw new IllegalStateException("DDS returned a trick count outside the remaining hand.");
            }
            synchronized (CACHE) { CACHE.put(key, value); }
        }
        return new Analysis(position, remaining, declarerToPlay ? value : remaining - value, declarer.getPosition());
    }

    /**
     * Runs the external DDS solver process with specified input and returns the calculated declarer tricks.
     * 
     * @param executable The file path to the DDS solver executable
     * @param input The encoded input string representing current hand and trick state
     * @return The number of tricks determined by the DDS solver
     */
    private static int solve(Path executable, String input) {
        Process process = null;
        try {
            process = new ProcessBuilder(executable.toString()).redirectErrorStream(true).start();
            try (var stream = process.getOutputStream()) {
                stream.write(input.getBytes(StandardCharsets.US_ASCII));
            }
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Double-dummy analysis timed out. The hand has not changed.");
            }
            String output = new String(process.getInputStream().readNBytes(4096), StandardCharsets.UTF_8).trim();
            if (process.exitValue() != 0 || !output.matches("DDS20900 (?:[0-9]|1[0-3])")) {
                throw new IllegalStateException("Double-dummy analysis failed: " + output);
            }
            return Integer.parseInt(output.substring("DDS20900 ".length()));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not run the double-dummy solver. The hand has not changed.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Double-dummy analysis was cancelled. The hand has not changed.", exception);
        } finally {
            if (process != null && process.isAlive()) process.destroyForcibly();
        }
    }

    /**
     * Encodes the current incomplete trick and remaining hand state into a string suitable for DDS input.
     * 
     * @param gameplay The Gameplay instance with an active, incomplete trick to encode
     * @return A string encoding the trick, leader, contract, played cards, and remaining hands
     */
    static String encode(Gameplay gameplay) {
        Trick trick = gameplay.getCurrentTrick();
        if (trick == null || trick.isComplete()) throw new IllegalStateException("No active trick to analyse.");
        int count = trick.getPlays().size();
        StringBuilder text = new StringBuilder();
        text.append(suitCode(gameplay.getContract().getDenomination())).append(' ')
                .append(seatCode(trick.getLeader().getPosition())).append(' ').append(count).append(' ');
        Set<Card> seen = new HashSet<>();
        Seat next = trick.getLeader();
        for (int i = 0; i < count; i++) {
            Card card = trick.getPlays().get(next);
            validateCard(card, seen);
            text.append(suitCode(card.getSuit())).append(' ').append(card.getRank().ordinal() + 2).append(' ');
            next = next.next();
        }
        for (Position position : new Position[]{Position.NORTH, Position.EAST, Position.SOUTH, Position.WEST}) {
            int[] masks = new int[4];
            Hand hand = gameplay.getRemainingHand(new Seat(position));
            if (hand == null) throw new IllegalStateException("Missing hand for " + position);
            for (Card card : hand.getCards()) {
                validateCard(card, seen);
                masks[suitCode(card.getSuit())] |= 1 << (card.getRank().ordinal() + 2);
            }
            for (int mask : masks) text.append(mask).append(' ');
        }
        if (seen.size() != 4 * gameplay.getRemainingTricks()) {
            throw new IllegalStateException("Remaining cards do not match the recorded trick totals.");
        }
        return text.append('\n').toString();
    }

    /**
     * Validates that a card is valid for double-dummy input and not duplicated in the current set.
     * 
     * @param card The card to validate
     * @param seen Set of cards already seen to detect duplicates
     */
    private static void validateCard(Card card, Set<Card> seen) {
        if (card == null || card.getSuit() == null || card.getSuit() == Suit.NT || card.getRank() == null) {
            throw new IllegalStateException("Invalid card in double-dummy input.");
        }
        if (!seen.add(card)) throw new IllegalStateException("Duplicate card in double-dummy input.");
    }

    /**
     * Converts a Suit enum into the integer code expected by the DDS solver.
     * 
     * @param suit The suit to encode
     * @return Integer code corresponding to the suit (Spades=0, Hearts=1, Diamonds=2, Clubs=3, NT=4)
     */
    static int suitCode(Suit suit) {
        return switch (suit) {
            case SPADES -> 0;
            case HEARTS -> 1;
            case DIAMONDS -> 2;
            case CLUBS -> 3;
            case NT -> 4;
        };
    }

    /**
     * Converts a Position enum into the integer code used in DDS input encoding.
     * 
     * @param position The player position to encode
     * @return Integer code for the position (North=0, East=1, South=2, West=3)
     */
    private static int seatCode(Position position) {
        return switch (position) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };
    }
}
