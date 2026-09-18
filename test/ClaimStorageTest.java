import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ClaimStorageTest {

    @TempDir
    Path temporaryDirectory;

    /*
     * North: all clubs
     * East: all diamonds
     * South: all hearts
     * West: all spades
     *
     * North declares.
     *
     * In clubs, North's partnership wins every trick.
     * In NT, East's diamonds win every trick.
     */
    private GameController game(
            GameController controller, Suit strain) {

        Map<Position, List<Card>> hands =
                new EnumMap<>(Position.class);

        Suit[] suits = {
            Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES
        };

        Position[] positions = Position.values();

        for (int index = 0; index < positions.length; index++) {
            List<Card> cards = new ArrayList<>();

            for (Rank rank : Rank.values()) {
                cards.add(new Card(suits[index], rank));
            }

            hands.put(positions[index], cards);
        }

        Deal deal = Deal.fromHands(
                new Seat(Position.NORTH),
                Vulnerability.NONE,
                hands);

        controller.startLoadedDeal(deal);
        controller.submitBid(
                new Bid(new Seat(Position.NORTH), 1, strain));
        controller.submitBid(Bid.BidPass(new Seat(Position.EAST)));
        controller.submitBid(Bid.BidPass(new Seat(Position.SOUTH)));
        controller.submitBid(Bid.BidPass(new Seat(Position.WEST)));

        return controller;
    }

    private GameController game(Suit strain) {
        return game(new GameController(), strain);
    }

    private void playCards(GameController controller, int number) {
        for (int index = 0; index < number; index++) {
            Gameplay gameplay = controller.getGameplay();
            Seat player = gameplay.getCurrentPlayer();

            Card card = gameplay.getLegalCards(
                    player, gameplay.getRemainingHand(player)).get(0);

            assertTrue(controller.playCard(player, card));
        }
    }

    @ParameterizedTest
    @CsvSource({
        "CLUBS, 4, NORTH, 13, 0, 190, 0",
        "CLUBS, 1, SOUTH, 13, 0, 190, 0",
        "NT,    0, EAST,   0, 13, 0, 350",
        "NT,    2, WEST,   0, 13, 0, 350"
    })
    void acceptedClaimsSurviveClosingAndReopeningStorage(
            Suit strain,
            int cardsBeforeClaim,
            Position claimant,
            int declarerTricks,
            int defenderTricks,
            int scoreNS,
            int scoreEW) throws Exception {

        Path database = temporaryDirectory.resolve("claims.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController original = game(strain);
        playCards(original, cardsBeforeClaim);

        assertEquals(
                claimant,
                original.getGameplay().getCurrentPlayer().getPosition());

        assertTrue(original.claim());

        String gameId = storage.saveGame(user, original);

        assertEquals(1, count(database, "game_results"));
        assertEquals(1, count(database, "scores"));

        Storage reopened = new Storage(database.toString());
        GameController loaded = reopened.loadGame(user, gameId);

        assertEquals(GamePhase.FINISHED, loaded.getPhase());
        assertTrue(loaded.getGameplay().isFinished());
        assertEquals(
                new Seat(claimant),
                loaded.getGameplay().getAcceptedClaimant());

        assertEquals(
                declarerTricks,
                loaded.getGameplay().getDeclarerTricks());

        assertEquals(
                defenderTricks,
                loaded.getGameplay().getDefenderTricks());

        assertEquals(scoreNS, loaded.getScore().getScoreNS());
        assertEquals(scoreEW, loaded.getScore().getScoreEW());

        // Loading preserves actual plays, including a partial trick.
        assertEquals(cardsBeforeClaim, playedCards(loaded));

        for (Position position : Position.values()) {
            Seat seat = new Seat(position);

            assertEquals(
                    original.getGameplay().getRemainingHand(seat).getCards(),
                    loaded.getGameplay().getRemainingHand(seat).getCards());
        }

        // Ensure a restored claim can itself be saved and loaded again.
        String secondId = reopened.saveGame(user, loaded);
        GameController loadedAgain = reopened.loadGame(user, secondId);

        assertEquals(scoreNS, loadedAgain.getScore().getScoreNS());
        assertEquals(scoreEW, loadedAgain.getScore().getScoreEW());
        assertEquals(cardsBeforeClaim, playedCards(loadedAgain));
    }

    @Test
    void rejectedClaimDoesNotCreateAFinalResult() throws Exception {
        Path database = temporaryDirectory.resolve("rejected.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController controller = game(Suit.NT);

        // East leads a diamond. South cannot claim East's winners.
        playCards(controller, 1);
        assertFalse(controller.claim());
        assertNull(controller.getGameplay().getAcceptedClaimant());

        String id = storage.saveGame(user, controller);

        assertEquals(0, count(database, "game_results"));
        assertEquals(0, count(database, "scores"));

        GameController loaded = storage.loadGame(user, id);

        assertEquals(GamePhase.PLAYING, loaded.getPhase());
        assertEquals(1, playedCards(loaded));
    }

    @Test
    void playedOutGameLoadsWithNewRecordsAndAsALegacySave()
            throws Exception {

        Path database = temporaryDirectory.resolve("played-out.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController controller = game(Suit.CLUBS);
        playCards(controller, 52);

        String id = storage.saveGame(user, controller);

        assertEquals(1, count(database, "game_results"));
        assertEquals(1, count(database, "scores"));

        GameController loaded = storage.loadGame(user, id);
        assertEquals(GamePhase.FINISHED, loaded.getPhase());
        assertEquals(190, loaded.getScore().getScoreNS());
        assertNull(loaded.getGameplay().getAcceptedClaimant());

        // Simulate an older save created before these tables were populated.
        change(database,
                "DELETE FROM game_results WHERE game_id = ?", id);
        change(database,
                "DELETE FROM scores WHERE game_id = ?", id);

        GameController legacy = storage.loadGame(user, id);

        assertEquals(GamePhase.FINISHED, legacy.getPhase());
        assertEquals(52, playedCards(legacy));
        assertEquals(190, legacy.getScore().getScoreNS());
    }

    @Test
    void legacyClaimWithoutItsResultProducesAClearError()
            throws Exception {

        Path database = temporaryDirectory.resolve("legacy-claim.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController controller = game(Suit.CLUBS);
        playCards(controller, 4);
        assertTrue(controller.claim());

        String id = storage.saveGame(user, controller);

        change(database,
                "DELETE FROM game_results WHERE game_id = ?", id);
        change(database,
                "DELETE FROM scores WHERE game_id = ?", id);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> storage.loadGame(user, id));

        assertTrue(exception.getMessage().contains(
                "no accepted-claim record"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        """
        UPDATE game_results
        SET declarer_tricks = 12, defender_tricks = 1
        WHERE game_id = ?
        """,
        """
        UPDATE game_results
        SET claimant = 'EAST'
        WHERE game_id = ?
        """,
        """
        UPDATE game_results
        SET played_cards = 3
        WHERE game_id = ?
        """,
        """
        UPDATE scores
        SET score_ns = score_ns + 1
        WHERE game_id = ?
        """,
        "DELETE FROM scores WHERE game_id = ?"
    })
    void rejectsInconsistentOrMissingResultData(String sql)
            throws Exception {

        Path database = temporaryDirectory.resolve("corrupt.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController controller = game(Suit.CLUBS);
        playCards(controller, 4);
        assertTrue(controller.claim());

        String id = storage.saveGame(user, controller);
        change(database, sql, id);

        assertThrows(
                IllegalStateException.class,
                () -> storage.loadGame(user, id));
    }

    @Test
    void scoreInsertFailureRollsBackTheEntireGame() throws Exception {
        Path database = temporaryDirectory.resolve("sql-rollback.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        // Force failure after the game and result have been inserted.
        try (Connection connection = connect(database);
                Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TRIGGER reject_score_insert
                    BEFORE INSERT ON scores
                    BEGIN
                        SELECT RAISE(ABORT, 'Deliberate test failure');
                    END
                    """);
        }

        GameController controller = game(Suit.CLUBS);
        playCards(controller, 4);
        assertTrue(controller.claim());

        assertThrows(
                IllegalStateException.class,
                () -> storage.saveGame(user, controller));

        assertNoGameRows(database);
        assertEquals(1, count(database, "users"));
    }

    @Test
    void runtimeScoringFailureRollsBackTheEntireGame() throws Exception {
        Path database = temporaryDirectory.resolve("runtime-rollback.db");
        Storage storage = new Storage(database.toString());
        User user = storage.createUser("Learner");

        GameController controller = game(new GameController() {
            @Override
            public Score getScore() {
                throw new IllegalStateException(
                        "Deliberate scoring failure");
            }
        }, Suit.CLUBS);

        playCards(controller, 4);
        assertTrue(controller.claim());

        assertThrows(
                IllegalStateException.class,
                () -> storage.saveGame(user, controller));

        assertNoGameRows(database);
        assertEquals(1, count(database, "users"));
    }

    private int playedCards(GameController controller) {
        return controller.getGameplay().getTricks().stream()
                .mapToInt(trick -> trick.getPlays().size())
                .sum();
    }

    private Connection connect(Path database) throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + database);
    }

    private void change(Path database, String sql, String gameId)
            throws Exception {

        try (Connection connection = connect(database);
                PreparedStatement statement =
                        connection.prepareStatement(sql)) {

            statement.setString(1, gameId);
            statement.executeUpdate();
        }
    }

    private int count(Path database, String table) throws Exception {
        // The table names are fixed test constants, not user input.
        try (Connection connection = connect(database);
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery(
                        "SELECT COUNT(*) FROM " + table)) {

            assertTrue(results.next());
            return results.getInt(1);
        }
    }

    private void assertNoGameRows(Path database) throws Exception {
        for (String table : List.of(
                "games", "deal_cards", "bids", "contracts",
                "card_plays", "game_results", "scores")) {

            assertEquals(0, count(database, table), table);
        }
    }
}