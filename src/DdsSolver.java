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
    private DdsSolver() {}

    private static final Map<String, Integer> CACHE = new LinkedHashMap<>(32, .75f, true) {
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

        private Analysis(String position, int remaining, int declarerTricks, Position declarer) {
            this.declarer = declarer;
            this.position = position;
            this.remaining = remaining;
            this.declarerTricks = declarerTricks;
        }

        public int remainingTricks() { return remaining; }
        public int declarerTricks() { return declarerTricks; }

        public int tricksFor(Seat seat, Contract contract) {
            if (seat == null || seat.getPosition() == null) {
                throw new IllegalArgumentException("A valid player is required.");
            }
            Seat declarer = contract.getDeclarer();
            return seat.equals(declarer) || seat.isPartner(declarer) ? declarerTricks : remaining - declarerTricks;
        }

        boolean matches(Gameplay gameplay) {
            return !gameplay.isFinished() && remaining == gameplay.getRemainingTricks()
                    && declarer == gameplay.getContract().getDeclarer().getPosition() && position.equals(encode(gameplay));
        }
    }

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

    private static void validateCard(Card card, Set<Card> seen) {
        if (card == null || card.getSuit() == null || card.getSuit() == Suit.NT || card.getRank() == null) {
            throw new IllegalStateException("Invalid card in double-dummy input.");
        }
        if (!seen.add(card)) throw new IllegalStateException("Duplicate card in double-dummy input.");
    }

    static int suitCode(Suit suit) {
        return switch (suit) {
            case SPADES -> 0;
            case HEARTS -> 1;
            case DIAMONDS -> 2;
            case CLUBS -> 3;
            case NT -> 4;
        };
    }

    private static int seatCode(Position position) {
        return switch (position) {
            case NORTH -> 0;
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
        };
    }
}
