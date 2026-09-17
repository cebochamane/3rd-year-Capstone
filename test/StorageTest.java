import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsDatabaseAndUser() {
        Path databasePath = temporaryDirectory.resolve("bridgetut-test.db");

        Storage storage = new Storage(databasePath.toString());

        User user = storage.createUser("Sanele");

        assertNotNull(user);
        assertNotNull(user.getUserId());
        assertFalse(user.getUserId().isBlank());
        assertEquals("Sanele", user.getUsername());
        assertTrue(Files.exists(databasePath));
    }

    @Test
    void savesGameForUser() {

        Path databasePath = temporaryDirectory.resolve("save-game-test.db");
        Storage storage = new Storage(databasePath.toString());
        User user = storage.createUser("Sanele");

        GameController controller = new GameController();
        controller.startRandomDeal(new Seat(Position.NORTH), Vulnerability.NONE);
        String gameId = storage.saveGame(user, controller);

        assertNotNull(gameId);
        assertFalse(gameId.isBlank());
    }

    @Test
    void savesAndLoadsBiddingGame() {
        Path databasePath = temporaryDirectory.resolve("load-game-test.db");

        Storage firstStorage = new Storage(databasePath.toString());

        User user = firstStorage.createUser("Sanele");
        GameController originalController = new GameController();
        originalController.startRandomDeal(new Seat(Position.NORTH), Vulnerability.NONE);

        /*
         * Add two auction calls.
         */
        originalController.submitBid(Bid.BidPass(new Seat(Position.NORTH)));
        originalController.submitBid(new Bid(new Seat(Position.EAST), 1, Suit.HEARTS));

        String gameId = firstStorage.saveGame(user, originalController);

        /*
         * A new Storage object simulates reopening the application later.
         */
        Storage reopenedStorage = new Storage(databasePath.toString()); // simulates a closing and reopening the
                                                                        // program.

        GameController loadedController = reopenedStorage.loadGame(user, gameId);

        assertEquals(GamePhase.BIDDING, loadedController.getPhase());
        assertEquals(Position.SOUTH, loadedController.getCurrentBidder().getPosition());
        assertEquals(2, loadedController.getAuction().getBids().size());
        assertNull(loadedController.getContract());
        assertNull(loadedController.getGameplay());

        /*
         * Compare every original hand with its reconstructed hand.
         */
        for (Position position : Position.values()) {
            Seat seat = new Seat(position);

            Hand originalHand = originalController.getCurrentDeal().getHand(seat);
            Hand loadedHand = loadedController.getCurrentDeal().getHand(seat);
            assertEquals(originalHand.getCards().size(), loadedHand.getCards().size());

            for (Card card : originalHand.getCards()) {
                assertTrue(loadedHand.contains(card));
            }
        }
    }

    @Test
    void savesAndLoadsPlayingGame() {
        Path databasePath = temporaryDirectory.resolve("playing-game.db");
        Storage storage = new Storage(databasePath.toString());
        User user = storage.createUser("Sanele");

        Deal deal = new Deal(new Seat(Position.NORTH), Vulnerability.NONE);
        deal.dealCards();

        GameController originalController = new GameController();
        originalController.startLoadedDeal(deal);

        /*
         * Create a 1NT contract:
         * North bids 1NT, followed by three passes.
         */
        originalController.submitBid(new Bid(new Seat(Position.NORTH), 1, Suit.NT));
        originalController.submitBid(Bid.BidPass(new Seat(Position.EAST)));
        originalController.submitBid(Bid.BidPass(new Seat(Position.SOUTH)));
        originalController.submitBid(Bid.BidPass(new Seat(Position.WEST)));

        /*
         * Play one legal card.
         */
        Gameplay originalGameplay = originalController.getGameplay();
        Seat openingLeader = originalGameplay.getCurrentPlayer();
        Hand openingHand = originalGameplay.getRemainingHand(openingLeader);

        Card playedCard = originalGameplay.getLegalCards(openingLeader, openingHand).get(0);
        originalController.playCard(openingLeader, playedCard);

        /*
         * Save and reload the game.
         */
        String gameId = storage.saveGame(user, originalController);
        GameController loadedController = storage.loadGame(user, gameId);

        Gameplay loadedGameplay = loadedController.getGameplay();

        /*
         * Verify that the playing state was restored.
         */
        assertEquals(GamePhase.PLAYING, loadedController.getPhase());
        assertEquals(1, loadedGameplay.getCurrentTrick().getPlays().size());
        assertEquals(12, loadedGameplay.getRemainingHand(openingLeader).getCards().size());
        assertFalse(loadedGameplay.getRemainingHand(openingLeader).contains(playedCard));
        assertEquals(openingLeader.next(), loadedGameplay.getCurrentPlayer());
    }

    @Test
    void returnsOnlyGamesBelongingToSelectedUser() {
        Path databasePath = temporaryDirectory.resolve("multiple-users.db");

        Storage storage = new Storage(databasePath.toString());

        User user1 = storage.createUser("user1");
        User user2 = storage.createUser("User2");

        /*
         * Create and save user1's game.
         */
        Deal firstDeal = new Deal(new Seat(Position.NORTH), Vulnerability.NONE);
        firstDeal.dealCards();

        GameController firstController = new GameController();
        firstController.startLoadedDeal(firstDeal);

        String user1GameId = storage.saveGame(user1, firstController);

        /*
         * Create and save the other user's game.
         */
        Deal secondDeal = new Deal(new Seat(Position.SOUTH), Vulnerability.ALL);
        secondDeal.dealCards();

        GameController secondController = new GameController();
        secondController.startLoadedDeal(secondDeal);

        String user2GameId = storage.saveGame(user2, secondController);

        /*
         * Retrieve each user's saved-game list.
         */
        ArrayList<SavedGame> user1Games = storage.getSavedGames(user1);

        ArrayList<SavedGame> user2Games = storage.getSavedGames(user2);

        /*
         * Each user should see exactly one game:
         * their own game.
         */
        assertEquals(1, user1Games.size());
        assertEquals(user1GameId, user1Games.get(0).getGameId());

        assertEquals(1, user2Games.size());
        assertEquals(user2GameId, user2Games.get(0).getGameId());
    }

    @Test
    void createsUserWithAssignedTutorials() {
        Path databasePath = temporaryDirectory.resolve("tutorial-user.db");
        Storage storage = new Storage(databasePath.toString());

        TutorialEntry first = TutorialEntry.scripted(
                Path.of("first.txt"), 1, null);

        TutorialEntry second = TutorialEntry.scripted(
                Path.of("second.txt"), 2, null);

        User user = storage.createUser(
                "Cyril",
                java.util.List.of(first, second));

        ArrayList<SavedTutorial> tutorials = storage.getSavedTutorials(user);

        assertEquals(2, tutorials.size());
    }
}