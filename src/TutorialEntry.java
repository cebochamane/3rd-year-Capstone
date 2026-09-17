import java.nio.file.Path;

public final class TutorialEntry {
    public enum Kind {
        SCRIPTED_TUTORIAL,
        PRACTICE_DEAL
    }

    private final Kind kind;
    private final Path source;
    private final int lessonIndex;
    private final TutorialLesson lesson;

    /**
     * Constructs a TutorialEntry with the specified kind, source path, lesson index, and lesson.
     * 
     * @param kind the type of tutorial entry
     * @param source the source file path of the tutorial
     * @param lessonIndex the index of the lesson
     * @param lesson the tutorial lesson instance
     */
    private TutorialEntry(Kind kind, Path source, int lessonIndex, TutorialLesson lesson) {
        this.kind = kind;
        this.source = source;
        this.lessonIndex = lessonIndex;
        this.lesson = lesson;
    }

    /**
     * Creates a scripted tutorial entry with the given source path, lesson index, and tutorial lesson.
     * 
     * @param source the source file path for the scripted tutorial
     * @param lessonIndex the index of the lesson within the tutorial
     * @param lesson the tutorial lesson object
     * @return a new TutorialEntry representing a scripted tutorial
     */
    public static TutorialEntry scripted(Path source, int lessonIndex, TutorialLesson lesson) {
        return new TutorialEntry(Kind.SCRIPTED_TUTORIAL, source, lessonIndex, lesson);
    }

    /**
     * Creates a practice deal tutorial entry with the specified source.
     * 
     * @param source the source file path for the practice deal
     * @return a new TutorialEntry representing a practice deal
     */
    public static TutorialEntry practice(Path source) {
        return new TutorialEntry(Kind.PRACTICE_DEAL, source, 0, null);
    }

    /**
     * Returns the kind of the tutorial entry.
     * 
     * @return the type of this tutorial entry
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Indicates whether this tutorial entry is a scripted tutorial.
     * 
     * @return true if the entry is a scripted tutorial, false otherwise
     */
    public boolean isScriptedTutorial() {
        return kind == Kind.SCRIPTED_TUTORIAL;
    }

    /**
     * Returns the source file path associated with this tutorial entry.
     * 
     * @return the Path of the tutorial source file
     */
    public Path getSource() {
        return source;
    }

    /**
     * Retrieves the file name of the tutorial source as a string.
     * 
     * @return the file name of the source path
     */
    public String getSourceFileName() {
        return source.getFileName().toString();
    }

    /**
     * Returns the index of the lesson in the tutorial.
     * 
     * @return the zero-based lesson index
     */
    public int getLessonIndex() {
        return lessonIndex;
    }

    /**
     * Returns the TutorialLesson object associated with this entry, if any.
     * 
     * @return the tutorial lesson instance or null if none
     */
    public TutorialLesson getLesson() {
        return lesson;
    }

    @Override
    /**
     * Returns a string representation of this tutorial entry, including its type and relevant details.
     * 
     * @return a descriptive string of this tutorial entry
     */
    public String toString() {
        if (isScriptedTutorial()) {
            return "Tutorial: " + getSourceFileName() + " — lesson " + lessonIndex;
        }
        return "Practice deal: " + getSourceFileName();
    }
}