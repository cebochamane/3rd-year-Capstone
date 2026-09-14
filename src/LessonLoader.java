import java.io.IOException;
import java.util.List;

/**
 * Reads a Lesson from a text file.
 */
public class LessonLoader {

    /**
     * Loads a lesson from a file on disk.
     */
    public static Lesson loadFromFile(String filePath) throws IOException {
        // TODO: read file, call parse()
        return null;
    }

    /**
     * Parses lesson text into a Lesson object.
     */
    public static Lesson parse(List<String> lines) {
        // TODO: parse sections [Dealer], [Vulnerability], [Contract],
        // [North], [East], [South], [West], [Play], [End], [Note]
        return null;
    }

    private static List<Card> parseHand(String line) {
        // TODO: parse "A K Q . J T 9 . 8 7 6 . 5 4 3 2"
        return null;
    }

    private static void parsePlayLine(String line, List<Card> seq, List<Seat> order) {
        // TODO: parse "N SA" -> North plays Ace of Spades
    }

    private static Card parseCard(String s) {
        // TODO: parse "SA", "H10", "D2"
        return null;
    }

    private static Rank parseRank(String s) {
        // TODO: parse "A", "K", "Q", "J", "T", "10", "9"..."2"
        return null;
    }
}