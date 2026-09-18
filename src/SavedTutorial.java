public final class SavedTutorial {
    private final String tutorialId;
    private final String sourceFile;
    private final int lessonIndex;
    private final String savedAt;

    /**
     * Constructs a SavedTutorial instance with the specified tutorial ID, source file, lesson index, and save timestamp.
     * 
     * @param tutorialId the unique identifier of the tutorial
     * @param sourceFile the name of the source file associated with the tutorial
     * @param lessonIndex the index of the lesson within the tutorial
     * @param savedAt the timestamp when the tutorial was saved
     */
    public SavedTutorial(String tutorialId, String sourceFile, int lessonIndex, String savedAt) {
        this.tutorialId = tutorialId;
        this.sourceFile = sourceFile;
        this.lessonIndex = lessonIndex;
        this.savedAt = savedAt;
    }

    /**
     * Returns the unique identifier of the tutorial.
     * 
     * @return the tutorial's unique identifier
     */
    public String getTutorialId() {
        return tutorialId;
    }

    /**
     * Returns the source file name associated with the tutorial.
     * 
     * @return the source file name
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * Returns the index of the lesson within the tutorial.
     * 
     * @return the lesson index
     */
    public int getLessonIndex() {
        return lessonIndex;
    }

    /**
     * Returns the timestamp indicating when the tutorial was saved.
     * 
     * @return the saved timestamp
     */
    public String getSavedAt() {
        return savedAt;
    }

    @Override
    /**
     * Returns a string representation of the saved tutorial including saved time, source file, and lesson index.
     * 
     * @return a formatted string summarizing the saved tutorial
     */
    public String toString() {
        return savedAt + " | " + sourceFile + " | Lesson " + lessonIndex;
    }
}