import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.UUID;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public class Storage {

    private final String filePath;

    /**
     * Creates a storage object using the specified file path.
     *
     * @param filePath the location used for persistent game data
     * @throws IllegalArgumentexception if the path is null or blank
     */
    public Storage(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("The storage file cannot be null or blank.");
        }
        this.filePath = filePath.trim();
        initialiseDatabase();
    }

    /**
     * Opens a connection to the SQLite database.
     * 
     * @return an open database connection
     * @throws SQLExcepton if the connection cannot be created
     */
    private Connection openConnection() throws SQLException {
        String databaseUrl = "jdbc:sqlite:" + filePath;
        Connection connection = DriverManager.getConnection(databaseUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /**
     * Create the database tables when they do not already exist.
     */
    private void initialiseDatabase() {

        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "user_id TEXT PRIMARY KEY, " + "username TEXT NOT NULL COLLATE NOCASE UNIQUE, "
                + "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP" + ")";

        String createGamesTable = "CREATE TABLE IF NOT EXISTS games (" + "game_id TEXT PRIMARY KEY, "
                + "user_id TEXT NOT NULL," + "dealer TEXT NOT NULL, "
                + "vulnerability TEXT NOT NULL, " + "phase TEXT NOT NULL, " + "current_bidder TEXT, "
                + "saved_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (user_id) REFERENCES users(user_id) "
                + "ON DELETE CASCADE" + ")";

        String createDealCardsTable = "CREATE TABLE IF NOT EXISTS deal_cards (" + "game_id TEXT NOT NULL, "
                + "seat TEXT NOT NULL, "
                + "suit TEXT NOT NULL, " + "rank TEXT NOT NULL, " + "PRIMARY KEY (game_id, suit, rank), "
                + "FOREIGN KEY (game_id) REFERENCES games(game_id) " + "ON DELETE CASCADE" + ")";

        String createBidsTable = "CREATE TABLE IF NOT EXISTS bids (" + "game_id TEXT NOT NULL, "
                + "bid_number INTEGER NOT NULL, " + "bidder TEXT NOT NULL, " + "bid_type TEXT NOT NULL, "
                + "level INTEGER, " + "denomination TEXT, " + "PRIMARY KEY (game_id, bid_number), "
                + "FOREIGN KEY (game_id) REFERENCES games(game_id) " + "ON DELETE CASCADE" + ")";

        String createContractsTable = "CREATE TABLE IF NOT EXISTS contracts ("
                + "game_id TEXT PRIMARY KEY, " + "level INTEGER NOT NULL, " + "denomination TEXT NOT NULL, "
                + "declarer TEXT NOT NULL, " + "doubled INTEGER NOT NULL, " + "redoubled INTEGER NOT NULL, "
                + "FOREIGN KEY (game_id) REFERENCES games(game_id) " + "ON DELETE CASCADE" + ")";

        String createCardPlaysTable = "CREATE TABLE IF NOT EXISTS card_plays ("
                + "game_id TEXT NOT NULL, " + "play_number INTEGER NOT NULL, " + "trick_number INTEGER NOT NULL, "
                + "seat TEXT NOT NULL, " + "suit TEXT NOT NULL, " + "rank TEXT NOT NULL, "
                + "PRIMARY KEY (game_id, play_number), "
                + "UNIQUE (game_id, suit, rank), " + "FOREIGN KEY (game_id) REFERENCES games(game_id) "
                + "ON DELETE CASCADE" + ")";

        String createScoresTable = "CREATE TABLE IF NOT EXISTS scores ("
                + "game_id TEXT PRIMARY KEY, " + "score_ns INTEGER NOT NULL, " + "score_ew INTEGER NOT NULL, "
                + "FOREIGN KEY (game_id) REFERENCES games(game_id) " + "ON DELETE CASCADE" + ")";

        String createGameResultsTable = "CREATE TABLE IF NOT EXISTS game_results (\n"
                + "    game_id TEXT PRIMARY KEY,\n" 
                + "    ending_type TEXT NOT NULL\n" 
                + "        CHECK (ending_type IN ('PLAYED_OUT', 'CLAIM')),\n" 
                + "    claimant TEXT\n"
                + "        CHECK (claimant IN ('NORTH', 'EAST', 'SOUTH', 'WEST')),\n"
                + "    played_cards INTEGER NOT NULL\n"
                + "        CHECK (played_cards BETWEEN 0 AND 52),\n"
                + "    declarer_tricks INTEGER NOT NULL\n"
                + "        CHECK (declarer_tricks BETWEEN 0 AND 13),\n"
                + "    defender_tricks INTEGER NOT NULL\n"
                + "        CHECK (defender_tricks BETWEEN 0 AND 13),\n" + "\n"
                + "    CHECK (declarer_tricks + defender_tricks = 13),\n" + "\n"
                + "    CHECK (\n"
                + "        (ending_type = 'PLAYED_OUT'\n"
                + "            AND claimant IS NULL\n"
                + "            AND played_cards = 52)\n"
                + "        OR\n"
                + "        (ending_type = 'CLAIM'\n"
                + "            AND claimant IS NOT NULL\n"
                + "            AND played_cards < 52)\n" 
                + "    ),\n" + "\n"
                + "    FOREIGN KEY (game_id)\n" 
                + "        REFERENCES games(game_id)\n"
                + "        ON DELETE CASCADE\n" + ");";

        String createTutorialSessionsTable = "CREATE TABLE IF NOT EXISTS tutorial_sessions ("
            + "tutorial_id TEXT PRIMARY KEY, "
            + "user_id TEXT NOT NULL, "
            + "source_file TEXT NOT NULL, "
            + "lesson_index INTEGER NOT NULL CHECK (lesson_index > 0), "
            + "saved_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE"
            + ")";

        try (Connection connection = openConnection();
                Statement statement = connection.createStatement()) {
            statement.execute(createUsersTable);
            statement.execute(createGamesTable);
            statement.execute(createDealCardsTable);
            statement.execute(createBidsTable);
            statement.execute(createContractsTable);
            statement.execute(createCardPlaysTable);
            statement.execute(createScoresTable);
            statement.execute(createGameResultsTable);
            statement.execute(createTutorialSessionsTable);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialise the BridgeTut database.", exception);
        }
    }

    /**
     * Saves the complete game state and returns its game ID.
     *
     * @param controller the game controller containing the current state
     * @return the ID assigned to the saved game
     */
    public String saveGame(User user, GameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("The game controller cannot be null.");
        }
        if (user == null) {
            throw new IllegalStateException("A user is required to save a game");
        }

        Deal deal = controller.getCurrentDeal();

        if (deal == null) {
            throw new IllegalStateException("There is no current deal to save.");
        }
        String gameId = UUID.randomUUID().toString();

        String insertGame = "INSERT INTO games (" + "game_id, user_id, dealer, vulnerability, phase, current_bidder"
                + ") VALUES (?, ?, ?, ?,?, ?)";
        try (Connection connection = openConnection()) { // Manage connection
            connection.setAutoCommit(false);
            try {// Manage the Transaction

                try (PreparedStatement statement = connection.prepareStatement(insertGame)) { // Manage the prepared
                                                                                              // statement
                    statement.setString(1, gameId);
                    statement.setString(2, user.getUserId());
                    statement.setString(3, deal.getDealer().getPosition().name());
                    statement.setString(4, deal.getVulnerability().name());
                    statement.setString(5, controller.getPhase().name());

                    Seat currentBidder = controller.getCurrentBidder();
                    if (currentBidder == null) {
                        statement.setNull(6, Types.VARCHAR);
                    } else {
                        statement.setString(6, currentBidder.getPosition().name());
                    }
                    statement.executeUpdate();

                }

                this.saveDeal(connection, gameId, deal);

                if (controller.getAuction() != null) {
                    this.saveAuction(connection, gameId, controller.getAuction());
                }
                this.saveContract(connection, gameId, controller.getContract());
                this.saveGameplay(connection, gameId, controller.getGameplay());
                this.saveCompletedResult(connection, gameId, controller);
                connection.commit();
                return gameId;

            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();// all or nothing.
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw new IllegalStateException("Could not save the current game.", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not access the BridgeTut database.", exception);
        }
    }

    private boolean restoreSavedResult(Connection connection, String gameId, GameController controller,
            GamePhase savedPhase) throws SQLException {

        String sql = """
                SELECT ending_type, claimant, played_cards,
                    declarer_tricks, defender_tricks
                FROM game_results
                WHERE game_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameId);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return false;
                }

                if (savedPhase != GamePhase.FINISHED) {
                    throw new IllegalStateException("An unfinished saved game has a completed-result record.");
                }

                Gameplay gameplay = controller.getGameplay();

                if (gameplay == null) {
                    throw new IllegalStateException("The saved result has no corresponding gameplay.");
                }

                String ending = results.getString("ending_type");
                String claimantText = results.getString("claimant");
                int playedCards = results.getInt("played_cards");
                int declarerTricks = results.getInt("declarer_tricks");
                int defenderTricks = results.getInt("defender_tricks");

                if (playedCards != countPlayedCards(gameplay)) {
                    throw new IllegalStateException("The saved result does not match the recorded card count.");
                }

                if (declarerTricks < 0 || declarerTricks > 13 || defenderTricks < 0 
                        || defenderTricks > 13 || declarerTricks + defenderTricks != 13) {
                    throw new IllegalStateException("The saved result has invalid final trick totals.");
                }

                if ("CLAIM".equals(ending)) {
                    if (claimantText == null || playedCards < 0 || playedCards >= 52) {
                        throw new IllegalStateException("The saved claim has invalid metadata.");
                    }

                    Position claimantPosition;

                    try {
                        claimantPosition = Position.valueOf(claimantText);
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalStateException("The saved claimant is invalid.", exception);
                    }

                    controller.restoreAcceptedClaim(new Seat(claimantPosition), declarerTricks, defenderTricks);

                } else if ("PLAYED_OUT".equals(ending)) {
                    if (claimantText != null || playedCards != 52
                            || !gameplay.isFinished() || gameplay.getDeclarerTricks() != declarerTricks
                            || gameplay.getDefenderTricks() != defenderTricks) {
                        throw new IllegalStateException("The saved played-out result disagrees with replay.");
                    }

                } else {
                    throw new IllegalStateException("Unknown saved game ending: " + ending);
                }

                return true;
            }
        }
    }

    private void verifySavedScore(Connection connection, String gameId, GameController controller,
            boolean required) throws SQLException {

        String sql = """
                SELECT score_ns, score_ew
                FROM scores
                WHERE game_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameId);

            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    if (required) {
                        throw new IllegalStateException("The completed result is missing its saved score.");
                    }

                    // Older saves did not persist scores.
                    return;
                }

                if (controller.getPhase() != GamePhase.FINISHED) {
                    throw new IllegalStateException("An unfinished game has a saved final score.");
                }

                Score calculated = controller.getScore();

                if (results.getInt("score_ns") != calculated.getScoreNS()
                        || results.getInt("score_ew") != calculated.getScoreEW()) {

                    throw new IllegalStateException(
                            "The saved score disagrees with the contract, "
                            + "vulnerability and final trick totals.");
                }
            }
        }
    }

    /**
     * Loads and reconstructs the auction belonging to a saved game.
     *
     * @param connection active database connection
     * @param gameId     saved game ID
     * @return reconstructed auction
     * @throws SQLException if the bids cannot be loaded
     */
    private Auction loadAuction(Connection connection, String gameId) throws SQLException {

        if (connection == null || gameId == null) {
            throw new IllegalArgumentException("Connection and game ID are required.");
        }

        String selectBids = "SELECT bidder, bid_type, level, denomination "
                + "FROM bids WHERE game_id = ? ORDER BY bid_number ASC";
        Auction auction = new Auction();

        try (PreparedStatement statement = connection.prepareStatement(selectBids)) {

            statement.setString(1, gameId);
            try (ResultSet results = statement.executeQuery()) {

                while (results.next()) {
                    Position bidderPosition = Position.valueOf(results.getString("bidder"));

                    Seat bidder = new Seat(bidderPosition);
                    String bidType = results.getString("bid_type");
                    Bid bid;

                    if (bidType.equals("PASS")) {
                        bid = Bid.BidPass(bidder);

                    } else if (bidType.equals("DOUBLE")) {
                        bid = Bid.BidDouble(bidder);

                    } else if (bidType.equals("REDOUBLE")) {
                        bid = Bid.BidRedouble(bidder);

                    } else if (bidType.equals("CONTRACT")) {
                        int level = results.getInt("level");

                        Suit denomination = Suit.valueOf(results.getString("denomination"));

                        bid = new Bid(bidder, level, denomination);

                    } else {
                        throw new IllegalStateException("Unknown saved bid type: " + bidType);
                    }
                    boolean added = auction.addBid(bid);

                    if (!added) {
                        throw new IllegalStateException("The saved auction contains an invalid bid.");
                    }
                }
            }
        }

        return auction;
    }

    /**
     * Replays all saved card plays through the controller.
     *
     * @param connection active database connection
     * @param gameId     saved game ID
     * @param controller controller being reconstructed
     * @throws SQLException if the plays cannot be loaded
     */
    private void replayCardPlays(Connection connection, String gameId,
            GameController controller) throws SQLException {

        if (connection == null || gameId == null || controller == null) {
            throw new IllegalArgumentException("Connection, game ID and controller are required.");
        }

        String selectPlays = "SELECT play_number, seat, suit, rank "
                + "FROM card_plays WHERE game_id = ? ORDER BY play_number ASC";

        try (PreparedStatement statement = connection.prepareStatement(selectPlays)) {

            statement.setString(1, gameId);
            try (ResultSet results = statement.executeQuery()) {

                int expectedPlayNumber = 1;
                while (results.next()) {
                    int savedPlayNumber = results.getInt("play_number");

                    if (savedPlayNumber != expectedPlayNumber) {
                        throw new IllegalStateException("The saved card-play sequence is incomplete.");
                    }

                    Position position = Position.valueOf(results.getString("seat"));
                    Suit suit = Suit.valueOf(results.getString("suit"));
                    Rank rank = Rank.valueOf(results.getString("rank"));

                    Seat seat = new Seat(position);
                    Card card = new Card(suit, rank);

                    boolean played = controller.playCard(seat, card);

                    if (!played) {
                        throw new IllegalStateException("The saved game contains an illegal card play.");
                    }

                    expectedPlayNumber++;
                }
            }
        }
    }

    /**
     * Loads a saved game belonging to a particular user.
     *
     * @param user   owner of the saved game
     * @param gameId ID of the game to load
     * @return reconstructed game controller
     */
    public GameController loadGame(User user, String gameId) {

        if (user == null) {
            throw new IllegalArgumentException("A user is required to load a game.");
        }

        if (gameId == null || gameId.trim().isEmpty()) {
            throw new IllegalArgumentException("The game ID cannot be null or blank.");
        }

        String selectGame = "SELECT dealer, vulnerability, phase, current_bidder "
                + "FROM games WHERE game_id = ? AND user_id = ?";

        try (Connection connection = openConnection()) {

            Seat dealer;
            Vulnerability vulnerability;
            GamePhase savedPhase;
            Position savedBidderPosition = null;

            try (PreparedStatement statement = connection.prepareStatement(selectGame)) {

                statement.setString(1, gameId.trim());
                statement.setString(2, user.getUserId());

                try (ResultSet results = statement.executeQuery()) {

                    if (!results.next()) {
                        throw new IllegalArgumentException("No saved game with that ID belongs to this user.");
                    }

                    Position dealerPosition = Position.valueOf(results.getString("dealer"));

                    dealer = new Seat(dealerPosition);
                    vulnerability = Vulnerability.valueOf(results.getString("vulnerability"));
                    savedPhase = GamePhase.valueOf(results.getString("phase"));
                    String currentBidderText = results.getString("current_bidder");

                    if (currentBidderText != null) {
                        savedBidderPosition = Position.valueOf(currentBidderText);
                    }
                }
            }

            /*
             * Load the saved model objects.
             */
            Deal deal = this.loadDeal(connection, gameId.trim(), dealer, vulnerability);
            Auction savedAuction = this.loadAuction(connection, gameId.trim());
            Contract savedContract = this.loadContract(connection, gameId.trim());

            /*
             * Reconstruct the controller
             */
            GameController loadedController = new GameController();

            loadedController.startLoadedDeal(deal);

            for (Bid bid : savedAuction.getBids()) {
                loadedController.submitBid(bid);
            }

            /*
             * The auction should reproduce the stored contract.
             */
            if (!contractsMatch(savedContract, loadedController.getContract())) {

                throw new IllegalStateException("The saved contract does not agree with the saved auction.");
            }

            /*
             * Reconstruct gameplay by replaying cards.
             */
            this.replayCardPlays(connection, gameId.trim(), loadedController);

            boolean hasSavedResult = restoreSavedResult(connection, gameId.trim(), loadedController, savedPhase);

            if (!hasSavedResult
                    && savedPhase == GamePhase.FINISHED
                    && loadedController.getPhase() != GamePhase.FINISHED) {

                throw new IllegalStateException(
                        "This saved game is marked finished, but its recorded cards "
                        + "do not complete the hand and it has no accepted-claim record. "
                        + "Its final result cannot be restored reliably.");
            }

            /*
             * Check the final reconstructed phase.
             */
            if (loadedController.getPhase() != savedPhase) {
                throw new IllegalStateException("The reconstructed game phase does not match the saved phase.");
            }

            Position loadedBidderPosition = null;

            if (loadedController.getCurrentBidder() != null) {
                loadedBidderPosition = loadedController.getCurrentBidder().getPosition();
            }

            if (loadedBidderPosition != savedBidderPosition) {
                throw new IllegalStateException("The reconstructed current bidder does not match the saved bidder.");
            }

            verifySavedScore(connection, gameId.trim(), loadedController, hasSavedResult);

            return loadedController;

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the saved game.", exception);
        }
    }

    /**
     * Saves all four hands belonging to one saved game.
     *
     * @param connection active transaction connection
     * @param gameId     ID of the saved game
     * @param deal       deal containing the four hands
     * @throws SQLException if the cards cannot be inserted
     */
    private void saveDeal(Connection connection, String gameId, Deal deal) throws SQLException {

        if (connection == null || gameId == null || deal == null) {
            throw new IllegalArgumentException("Connection, game ID and deal are required.");
        }

        String insertCard = "INSERT INTO deal_cards (game_id, seat, suit, rank) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertCard)) {
            int cardCount = 0;
            for (Position position : Position.values()) {
                Seat seat = new Seat(position);
                Hand hand = deal.getHand(seat);

                if (hand == null) {
                    throw new IllegalStateException("The deal has no hand for " + position + ".");
                }

                for (Card card : hand.getCards()) {
                    statement.setString(1, gameId);
                    statement.setString(2, position.name());
                    statement.setString(3, card.getSuit().name());
                    statement.setString(4, card.getRank().name());

                    statement.addBatch();
                    cardCount++;
                }
            }

            if (cardCount != 52) {
                throw new IllegalStateException("A saved deal must contain exactly 52 cards.");
            }

            statement.executeBatch();
        }
    }

    /**
     * Saves all calls made during one auction.
     *
     * @param connection active transaction connection
     * @param gameId     ID of the saved game
     * @param auction    auction containing the calls
     * @throws SQLException if the calls cannot be inserted
     */
    private void saveAuction(Connection connection, String gameId, Auction auction) throws SQLException {

        if (connection == null || gameId == null || auction == null) {
            throw new IllegalArgumentException("Connection, game ID and auction are required.");
        }

        String insertBid = "INSERT INTO bids (" + "game_id, bid_number, bidder, " + "bid_type, level, denomination"
                + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertBid)) {

            for (int index = 0; index < auction.getBids().size(); index++) {
                Bid bid = auction.getBids().get(index);
                String bidType;

                if (bid.isPass()) {
                    bidType = "PASS";
                } else if (bid.isDouble()) {
                    bidType = "DOUBLE";
                } else if (bid.isRedouble()) {
                    bidType = "REDOUBLE";
                } else {
                    bidType = "CONTRACT";
                }

                statement.setString(1, gameId);
                statement.setInt(2, index + 1);
                statement.setString(3, bid.getBidder().getPosition().name());
                statement.setString(4, bidType);

                if (bidType.equals("CONTRACT")) {
                    statement.setInt(5, bid.getLevel());
                    statement.setString(6, bid.getDenomination().name());
                } else {
                    statement.setNull(5, Types.INTEGER);
                    statement.setNull(6, Types.VARCHAR);
                }
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    /**
     * Saves the final contract for one game.
     *
     * A passed-out game has no contract, so a null contract
     * results in no database row being inserted.
     *
     * @param connection active transaction connection
     * @param gameId     ID of the saved game
     * @param contract   final contract, or null when passed out
     * @throws SQLException if the contract cannot be inserted
     */
    private void saveContract(Connection connection, String gameId, Contract contract) throws SQLException {

        if (connection == null || gameId == null) {
            throw new IllegalArgumentException("Connection and game ID are required.");
        }

        if (contract == null) {
            return;
        }

        String insertContract = "INSERT INTO contracts (" + "game_id, level, denomination, declarer, "
                + "doubled, redoubled" + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertContract)) {

            statement.setString(1, gameId);
            statement.setInt(2, contract.getLevel());
            statement.setString(3, contract.getDenomination().name());
            statement.setString(4, contract.getDeclarer().getPosition().name());
            statement.setBoolean(5, contract.isDoubled());
            statement.setBoolean(6, contract.isRedoubled());

            statement.executeUpdate();
        }
    }

    /**
     * Saves every card that has been played in the game.
     *
     * @param connection active transaction connection
     * @param gameId     ID of the saved game
     * @param gameplay   current gameplay state, or null
     * @throws SQLException if the card plays cannot be inserted
     */
    private void saveGameplay(Connection connection, String gameId, Gameplay gameplay) throws SQLException {

        if (connection == null || gameId == null) {
            throw new IllegalArgumentException("Connection and game ID are required.");
        }

        /*
         * A game saved during bidding has no Gameplay object yet.
         */
        if (gameplay == null) {
            return;
        }

        String insertPlay = "INSERT INTO card_plays (" + "game_id, play_number, trick_number, "
                + "seat, suit, rank" + ") VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertPlay)) {

            int playNumber = 1;
            for (Trick trick : gameplay.getTricks()) {
                Seat currentSeat = trick.getLeader();

                for (int i = 0; i < Position.values().length; i++) {
                    Card card = trick.getPlays().get(currentSeat);

                    /*
                     * Stop when the current trick is incomplete.
                     */
                    if (card == null) {
                        break;
                    }

                    statement.setString(1, gameId);
                    statement.setInt(2, playNumber);
                    statement.setInt(3, trick.getTrickNo());
                    statement.setString(4, currentSeat.getPosition().name());
                    statement.setString(5, card.getSuit().name());
                    statement.setString(6, card.getRank().name());

                    statement.addBatch();

                    playNumber++;
                    currentSeat = currentSeat.next();
                }
            }

            statement.executeBatch();
        }
    }

    private void saveScore(Connection connection, String gameId, Score score) throws SQLException {

        String sql = """
                INSERT INTO scores (game_id, score_ns, score_ew)
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement =
                connection.prepareStatement(sql)) {

            statement.setString(1, gameId);
            statement.setInt(2, score.getScoreNS());
            statement.setInt(3, score.getScoreEW());
            statement.executeUpdate();
        }
    }

    private int countPlayedCards(Gameplay gameplay) {
        int count = 0;

        if (gameplay != null) {
            for (Trick trick : gameplay.getTricks()) {
                count += trick.getPlays().size();
            }
        }

        return count;
    }

    private void saveCompletedResult( Connection connection, String gameId, GameController controller) 
            throws SQLException {

        if (controller.getPhase() != GamePhase.FINISHED) {
            return;
        }

        Gameplay gameplay = controller.getGameplay();

        // Also validates that gameplay finished with 13 allocated tricks.
        Score score = controller.getScore();
        Seat claimant = gameplay.getAcceptedClaimant();
        int playedCards = countPlayedCards(gameplay);

        if (claimant == null && playedCards != 52) {
            throw new IllegalStateException("A game without an accepted claim must finish all 52 cards.");
        }

        if (claimant != null && playedCards >= 52) {
            throw new IllegalStateException("An accepted claim must occur before all cards are played.");
        }

        String sql = """
                INSERT INTO game_results (
                    game_id, ending_type, claimant, played_cards,
                    declarer_tricks, defender_tricks
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gameId);
            statement.setString(2, claimant == null ? "PLAYED_OUT" : "CLAIM");

            if (claimant == null) {
                statement.setNull(3, Types.VARCHAR);
            } else {
                statement.setString(3, claimant.getPosition().name());
            }

            statement.setInt(4, playedCards);
            statement.setInt(5, gameplay.getDeclarerTricks());
            statement.setInt(6, gameplay.getDefenderTricks());
            statement.executeUpdate();
        }

        saveScore(connection, gameId, score);
    }
    /**
     * Creates a new user with the specified username and no tutorials.
     * 
     * @param username the username for the new user
     * @return the created user
     */
    public User createUser(String username) {
        return createUser(username, List.of());
    }

    /**
     * Creates a new user with the specified username and assigns the given tutorials.
     * 
     * @param username the username for the new user
     * @param tutorials list of tutorial entries to assign
     * @return the created user
     */
    public User createUser(String username, List<TutorialEntry> tutorials) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The username cannot be null or blank.");
        }

        String cleanUsername = username.trim();
        String userId = UUID.randomUUID().toString();

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);

            try {
                String insertUser = "INSERT INTO users (user_id, username) VALUES (?, ?)";

                try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                    statement.setString(1, userId);
                    statement.setString(2, cleanUsername);
                    statement.executeUpdate();
                }

                User user = new User(userId, cleanUsername);
                assignTutorials(connection, user, tutorials);

                connection.commit();
                return user;
            } catch (SQLException exception) {
                connection.rollback();
                throw new IllegalStateException(
                        "Could not create the user profile and tutorials.", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(
                    "Could not create the user profile. The username may already exist.", exception);
        }
    }

    /**
     * Assigns the specified tutorials to the given user in the database using the provided connection.
     * 
     * @param connection database connection for transaction
     * @param user user to assign tutorials to
     * @param tutorials list of tutorial entries to assign
     * @throws SQLException if tutorials cannot be inserted
     */
    private void assignTutorials(Connection connection, User user, List<TutorialEntry> tutorials) throws SQLException {

        if (tutorials == null) {
            return;
        }

        String insertTutorial = "INSERT INTO tutorial_sessions "
                + "(tutorial_id, user_id, source_file, lesson_index) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insertTutorial)) {
            for (TutorialEntry tutorial : tutorials) {
                if (tutorial == null || !tutorial.isScriptedTutorial()) {
                    continue;
                }

                statement.setString(1, UUID.randomUUID().toString());
                statement.setString(2, user.getUserId());
                statement.setString(3, tutorial.getSourceFileName());
                statement.setInt(4, tutorial.getLessonIndex());
                statement.addBatch();
            }

            statement.executeBatch();
        }
}

    /**
     * Loads all local user profiles from the database.
     *
     * @return users ordered alphabetically by username
     */
    public ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        String selectUsers = "SELECT user_id, username FROM users ORDER BY username COLLATE NOCASE";

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(selectUsers);
                ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                String userId = results.getString("user_id");
                String username = results.getString("username");

                User user = new User(userId, username);
                users.add(user);
            }
            return users;

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load the user profiles.", exception);
        }
    }

    /**
     * Returns summaries of all games belonging to one user.
     *
     * The most recently saved games appear first.
     *
     * @param user owner of the saved games
     * @return the user's saved games
     */
    public ArrayList<SavedGame> getSavedGames(User user) {
        if (user == null) {
            throw new IllegalArgumentException("A user is required.");
        }

        ArrayList<SavedGame> savedGames = new ArrayList<>();

        String selectGames = "SELECT game_id, phase, dealer, saved_at "
                + "FROM games WHERE user_id = ? ORDER BY saved_at DESC";

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(selectGames)) {

            statement.setString(1, user.getUserId());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String gameId = results.getString("game_id");

                    GamePhase phase = GamePhase.valueOf(results.getString("phase"));
                    Position dealerPosition = Position.valueOf(results.getString("dealer"));
                    Seat dealer = new Seat(dealerPosition);

                    String savedAt = results.getString("saved_at");
                    SavedGame savedGame = new SavedGame(gameId, phase, dealer, savedAt);

                    savedGames.add(savedGame);
                }
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not retrieve the user's saved games.", exception);
        }

        return savedGames;
    }

    /**
     * Loads the final contract belonging to a saved game.
     *
     * @param connection active database connection
     * @param gameId     saved game ID
     * @return reconstructed contract, or null if none exists
     * @throws SQLException if the contract cannot be loaded
     */
    private Contract loadContract(Connection connection, String gameId) throws SQLException {

        if (connection == null || gameId == null) {
            throw new IllegalArgumentException("Connection and game ID are required.");
        }

        String selectContract = "SELECT level, denomination, declarer, "
                + "doubled, redoubled FROM contracts WHERE game_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(selectContract)) {

            statement.setString(1, gameId);
            try (ResultSet results = statement.executeQuery()) {

                /*
                 * No row means the auction has not produced
                 * a contract or the deal was passed out.
                 */
                if (!results.next()) {
                    return null;
                }

                int level = results.getInt("level");

                Suit denomination = Suit.valueOf(results.getString("denomination"));
                Position declarerPosition = Position.valueOf(results.getString("declarer"));
                Seat declarer = new Seat(declarerPosition);
                boolean doubled = results.getBoolean("doubled");
                boolean redoubled = results.getBoolean("redoubled");

                return new Contract(level, denomination, declarer, doubled, redoubled);
            }
        }
    }

    /**
     * Checks whether two contracts contain the same values.
     *
     * @param first  first contract
     * @param second second contract
     * @return true when both contracts represent the same contract
     */
    private boolean contractsMatch(Contract first, Contract second) {

        if (first == null || second == null) {
            return first == second;
        }

        return first.getLevel() == second.getLevel()
                && first.getDenomination() == second.getDenomination()
                && first.getDeclarer().getPosition() == second.getDeclarer().getPosition()
                && first.isDoubled() == second.isDoubled() && first.isRedoubled() == second.isRedoubled();
    }

    /**
     * Loads and reconstructs the deal belonging to a saved game.
     *
     * @param connection    active database connection
     * @param gameId        saved game ID
     * @param dealer        dealer for the saved deal
     * @param vulnerability vulnerability for the saved deal
     * @return reconstructed deal
     * @throws SQLException if the cards cannot be loaded
     */
    private Deal loadDeal(Connection connection, String gameId, Seat dealer, Vulnerability vulnerability)
            throws SQLException {

        String selectCards = "SELECT seat, suit, rank FROM deal_cards WHERE game_id = ?";

        Map<Position, List<Card>> suppliedHands = new EnumMap<>(Position.class);

        /*
         * Create an empty list for every player.
         */
        for (Position position : Position.values()) {
            suppliedHands.put(position, new ArrayList<>());
        }

        int cardCount = 0;

        try (PreparedStatement statement = connection.prepareStatement(selectCards)) {

            statement.setString(1, gameId);

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {

                    Position position = Position.valueOf(results.getString("seat"));
                    Suit suit = Suit.valueOf(results.getString("suit"));
                    Rank rank = Rank.valueOf(results.getString("rank"));

                    Card card = new Card(suit, rank);
                    suppliedHands.get(position).add(card);
                    cardCount++;
                }
            }
        }

        if (cardCount != 52) {
            throw new IllegalStateException("The saved deal does not contain exactly 52 cards.");
        }

        return Deal.fromHands(dealer, vulnerability, suppliedHands);
    }

    /**
     * Saves a scripted tutorial entry progress for the specified user and returns the tutorial ID.
     * 
     * @param user user saving the tutorial
     * @param tutorial scripted tutorial entry to save
     * @return the saved tutorial ID
     */
    public String saveTutorial(User user, TutorialEntry tutorial) {
        if (user == null) {
            throw new IllegalArgumentException("A user is required to save a tutorial.");
        }
        if (tutorial == null || !tutorial.isScriptedTutorial()) {
            throw new IllegalArgumentException("Only scripted tutorials can be saved as tutorials.");
        }

        String tutorialId = UUID.randomUUID().toString();

        String insert = "INSERT INTO tutorial_sessions "
                + "(tutorial_id, user_id, source_file, lesson_index) VALUES (?, ?, ?, ?)";

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(insert)) {

            statement.setString(1, tutorialId);
            statement.setString(2, user.getUserId());
            statement.setString(3, tutorial.getSourceFileName());
            statement.setInt(4, tutorial.getLessonIndex());
            statement.executeUpdate();

            return tutorialId;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save the tutorial.", exception);
        }
    }

    /**
     * Returns a list of all saved tutorials for the specified user ordered by save date descending.
     * 
     * @param user user whose tutorials to retrieve
     * @return list of saved tutorials
     */
    public ArrayList<SavedTutorial> getSavedTutorials(User user) {
        if (user == null) {
            throw new IllegalArgumentException("A user is required.");
        }

        String select = "SELECT tutorial_id, source_file, lesson_index, saved_at "
                + "FROM tutorial_sessions WHERE user_id = ? ORDER BY saved_at DESC";

        ArrayList<SavedTutorial> tutorials = new ArrayList<>();

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(select)) {

            statement.setString(1, user.getUserId());

            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    tutorials.add(new SavedTutorial(
                            results.getString("tutorial_id"),
                            results.getString("source_file"),
                            results.getInt("lesson_index"),
                            results.getString("saved_at")));
                }
            }

            return tutorials;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not retrieve saved tutorials.", exception);
        }
    }

    public int syncTutorials(User user, List<TutorialEntry> tutorials) {
        if (user == null) {
            throw new IllegalArgumentException("A user is required.");
        }

        if (tutorials == null) {
            return 0;
        }

        String selectExisting = "SELECT source_file, lesson_index "
                + "FROM tutorial_sessions WHERE user_id = ?";

        String insertTutorial = "INSERT INTO tutorial_sessions "
                + "(tutorial_id, user_id, source_file, lesson_index) VALUES (?, ?, ?, ?)";

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);

            try {
                Set<String> existingTutorials = new HashSet<>();

                try (PreparedStatement select = connection.prepareStatement(selectExisting)) {
                    select.setString(1, user.getUserId());

                    try (ResultSet results = select.executeQuery()) {
                        while (results.next()) {
                            existingTutorials.add(
                                    tutorialKey(
                                            results.getString("source_file"),
                                            results.getInt("lesson_index")));
                        }
                    }
                }

                int addedCount = 0;

                try (PreparedStatement insert = connection.prepareStatement(insertTutorial)) {
                    for (TutorialEntry tutorial : tutorials) {
                        if (tutorial == null || !tutorial.isScriptedTutorial()) {
                            continue;
                        }

                        String sourceFile = tutorial.getSourceFileName();
                        int lessonIndex = tutorial.getLessonIndex();
                        String key = tutorialKey(sourceFile, lessonIndex);

                        if (existingTutorials.contains(key)) {
                            continue;
                        }

                        insert.setString(1, UUID.randomUUID().toString());
                        insert.setString(2, user.getUserId());
                        insert.setString(3, sourceFile);
                        insert.setInt(4, lessonIndex);
                        insert.addBatch();

                        existingTutorials.add(key);
                        addedCount++;
                    }

                    if (addedCount > 0) {
                        insert.executeBatch();
                    }
                }

                connection.commit();
                return addedCount;

            } catch (SQLException exception) {
                connection.rollback();
                throw new IllegalStateException("Could not synchronise the user's tutorials.", exception);
            }

        } catch (SQLException exception) {
            throw new IllegalStateException("Could not access the BridgeTut database.", exception);
        }
    }

    private String tutorialKey(String sourceFile, int lessonIndex) {
        return sourceFile + "\u0000" + lessonIndex;
    }
}