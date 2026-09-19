import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SettlementStorageTest {
    @TempDir Path directory;
    private GameController game() {
        Map<Position,List<Card>> hands = new EnumMap<>(Position.class);
        Suit[] suits = {Suit.CLUBS, Suit.DIAMONDS, Suit.HEARTS, Suit.SPADES};
        for (Position seat : Position.values()) {
            List<Card> cards = new ArrayList<>();
            for (Rank rank : Rank.values()) cards.add(new Card(suits[seat.ordinal()], rank));
            hands.put(seat, cards);
        }
        GameController game = new GameController();
        game.startLoadedDeal(Deal.fromHands(new Seat(Position.NORTH), Vulnerability.NONE, hands));
        game.submitBid(new Bid(new Seat(Position.NORTH), 1, Suit.CLUBS));
        for (int i=0;i<3;i++) game.submitBid(Bid.BidPass(game.getCurrentBidder()));
        DdsSolverTest.play(game,4);
        return game;
    }
    @Test void partialClaimSurvivesReopeningAndRejectsTamperedAllocation() throws Exception {
        Path file=directory.resolve("partial.db"); Storage storage=new Storage(file.toString());
        User user=storage.createUser("Claim test"); GameController original=game();
        assertTrue(original.claim(5));
        String id=storage.saveGame(user, original);
        GameController restored=new Storage(file.toString()).loadGame(user,id);
        assertEquals(6,restored.getGameplay().getDeclarerTricks());
        assertEquals(7,restored.getGameplay().getDefenderTricks());
        assertEquals(5,restored.getGameplay().getAcceptedTricks());
        assertEquals(original.getScore().getScoreEW(),restored.getScore().getScoreEW());
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file); Statement s=c.createStatement()) {
            s.executeUpdate("UPDATE game_results SET awarded_tricks=6");
        }
        assertThrows(IllegalStateException.class,()->storage.loadGame(user,id));
    }
    @Test void concessionSurvivesReopening() {
        Path file=directory.resolve("concede.db"); Storage storage=new Storage(file.toString());
        User user=storage.createUser("Concede test"); GameController original=game();
        assertTrue(original.concede());
        String id=storage.saveGame(user,original);
        GameController restored=new Storage(file.toString()).loadGame(user,id);
        assertTrue(restored.getGameplay().isConceded());
        assertNull(restored.getGameplay().getAcceptedClaimant());
        assertEquals(new Seat(Position.NORTH),restored.getGameplay().getSettlementActor());
        assertEquals(1,restored.getGameplay().getDeclarerTricks());
        assertEquals(12,restored.getGameplay().getDefenderTricks());
        assertEquals(original.getScore().getScoreEW(),restored.getScore().getScoreEW());
    }
    @Test void migratesPopulatedLegacyResultsAndAcceptsNewPartialClaims() throws Exception {
        Path file=directory.resolve("legacy.db"); Storage storage=new Storage(file.toString());
        User user=storage.createUser("Legacy test"); GameController original=game();
        assertTrue(original.claim()); String id=storage.saveGame(user,original);
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file); Statement s=c.createStatement()) {
            s.execute("ALTER TABLE game_results RENAME TO saved_results");
            s.execute("CREATE TABLE game_results (game_id TEXT PRIMARY KEY, ending_type TEXT NOT NULL CHECK(ending_type IN ('PLAYED_OUT','CLAIM')), claimant TEXT, played_cards INTEGER NOT NULL, declarer_tricks INTEGER NOT NULL, defender_tricks INTEGER NOT NULL)");
            s.execute("INSERT INTO game_results SELECT game_id,ending_type,claimant,played_cards,declarer_tricks,defender_tricks FROM saved_results");
            s.execute("DROP TABLE saved_results");
        }
        Storage migrated=new Storage(file.toString());
        assertEquals(13,migrated.loadGame(user,id).getGameplay().getDeclarerTricks());
        GameController partial=game(); assertTrue(partial.claim(5));
        String partialId=migrated.saveGame(user,partial);
        assertEquals(6,new Storage(file.toString()).loadGame(user,partialId).getGameplay().getDeclarerTricks());
        assertEquals(13,new Storage(file.toString()).loadGame(user,id).getGameplay().getDeclarerTricks());
    }
}
