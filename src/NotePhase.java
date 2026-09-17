public enum NotePhase {
    BIDDING,
    PRE_PLAY,
    PLAYING,
    POST_PLAY,
    GENERAL;

    /**
     * Maps a GamePhase to the corresponding NotePhase.
     */
    public static NotePhase fromGamePhase(GamePhase gp) {
        if (gp == null) {
            return GENERAL;
        }

        switch (gp) {
            case BIDDING:
                return BIDDING;
            case AUCTION_COMPLETE:
                return PRE_PLAY;
            case PLAYING:
                return PLAYING;
            case FINISHED:
                return POST_PLAY;
            case MENU:
            case PASSED_OUT:
                return GENERAL;
        }

        return GENERAL;
    }
}