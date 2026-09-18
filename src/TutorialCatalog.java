import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class TutorialCatalog {
    private static final String HEADER = "Cards held: spades;hearts;diamonds;clubs";

    private final List<TutorialEntry> entries;
    private final List<String> warnings;

    /**
     * Constructs a TutorialCatalog with the specified tutorial entries and warnings.
     * 
     * @param entries the list of tutorial entries to include in this catalog
     * @param warnings the list of warning messages associated with loading tutorials
     */
    private TutorialCatalog(List<TutorialEntry> entries, List<String> warnings) {
        this.entries = List.copyOf(entries);
        this.warnings = List.copyOf(warnings);
    }

    /**
     * Loads tutorials from the specified directory, returning a catalog of tutorial entries and warnings.
     * 
     * @param tutorialDirectory the directory path containing tutorial files
     * @return a TutorialCatalog containing loaded tutorial entries and any warnings encountered
     * @throws IOException TODO
     */
    public static TutorialCatalog load(Path tutorialDirectory) throws IOException {
        if (tutorialDirectory == null || !Files.isDirectory(tutorialDirectory)) {
            throw new IllegalArgumentException("The tutorials directory could not be found.");
        }

        List<TutorialEntry> entries = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try (Stream<Path> paths = Files.list(tutorialDirectory)) {
            List<Path> files = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().startsWith("."))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();

            for (Path file : files) {
                String filename = file.getFileName().toString().toLowerCase(Locale.ROOT);

                if (filename.startsWith("tutorial-template-")) {
                    continue;
                }

                try {
                    String contents = TutorialLessonParser.readText(file);

                    if (isHtml(contents)) {
                        HtmlDealParser.parse(contents); // Validate before showing it.
                        entries.add(TutorialEntry.practice(file));
                    } else {
                        loadScriptedLessons(file, contents, entries, warnings);
                    }
                } catch (IOException | IllegalArgumentException exception) {
                    warnings.add(file.getFileName() + ": " + exception.getMessage());
                }
            }
        }

        return new TutorialCatalog(entries, warnings);
    }

    /**
     * Determines whether the given content string is an HTML document.
     * 
     * @param contents the string content of a file to check
     * @return true if the content starts with an HTML doctype or html tag, false otherwise
     */
    private static boolean isHtml(String contents) {
        String trimmed = contents.stripLeading().toLowerCase(Locale.ROOT);
        return trimmed.startsWith("<!doctype html") || trimmed.startsWith("<html");
    }

    /**
     * Parses scripted lessons from the given content and adds them to entries, recording warnings if parsing fails.
     * 
     * @param file the path to the source tutorial file
     * @param contents the textual content of the tutorial file
     * @param entries the list to which parsed tutorial entries will be added
     * @param warnings the list to which warning messages will be added
     */
    private static void loadScriptedLessons(
            Path file,
            String contents,
            List<TutorialEntry> entries,
            List<String> warnings) {

        String[] blocks = contents.replace("\r", "")
                .split("(?m)(?=^Cards held: spades;hearts;diamonds;clubs\\s*$)");

        int lessonIndex = 0;

        for (String block : blocks) {
            if (!block.stripLeading().startsWith(HEADER)) {
                continue;
            }

            lessonIndex++;

            try {
                List<TutorialLesson> lessons = TutorialLessonParser.parse(block);

                if (lessons.size() != 1) {
                    throw new IllegalArgumentException("Expected one tutorial lesson.");
                }

                entries.add(TutorialEntry.scripted(file, lessonIndex, lessons.get(0)));
            } catch (IllegalArgumentException exception) {
                warnings.add(file.getFileName() + ", lesson " + lessonIndex + ": "
                        + exception.getMessage());
            }
        }
    }

    /**
     * Returns the list of tutorial entries contained in this catalog.
     * 
     * @return an unmodifiable list of TutorialEntry objects
     */
    public List<TutorialEntry> getEntries() {
        return entries;
    }

    /**
     * Returns the list of warnings encountered during tutorial loading.
     * 
     * @return an unmodifiable list of warning message strings
     */
    public List<String> getWarnings() {
        return warnings;
    }

    /**
     * Finds a scripted tutorial entry matching the given source file name and lesson index.
     * 
     * @param sourceFile the name of the source file
     * @param lessonIndex the index of the lesson within the source file
     * @return the matching TutorialEntry if found, or null if no match exists
     */
    public TutorialEntry findScripted(String sourceFile, int lessonIndex) {
        for (TutorialEntry entry : entries) {
            if (entry.isScriptedTutorial()
                    && entry.getSourceFileName().equals(sourceFile)
                    && entry.getLessonIndex() == lessonIndex) {
                return entry;
            }
        }
        return null;
    }
}