public record SavedTutorialAttempt(
        String attemptId,
        String sourceFile,
        int lessonIndex,
        String savedAt,
        String reportText) {

    @Override
    /**
     * Returns a string representation of the saved tutorial attempt including the save time, source file, and lesson index.
     * 
     * @return a string summarizing the savedAt time, sourceFile name, and lessonIndex
     */
    public String toString() {
        return savedAt + " | " + sourceFile + " | Lesson " + lessonIndex;
    }
}