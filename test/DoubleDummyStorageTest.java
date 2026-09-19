import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DoubleDummyStorageTest {
    @TempDir Path directory;
    @Test void persistsEveryValueAndReusesWithoutAnExecutable() throws Exception {
        Path file=directory.resolve("analysis.db");Storage store=new Storage(file.toString());
        Deal deal=DoubleDummyTableTest.referenceDeal();
        assertNull(store.getDoubleDummyTable(deal));
        store.saveDoubleDummyTable(deal,DoubleDummyTableTest.expected());
        Storage reopened=new Storage(file.toString());
        DoubleDummyTableTest.sameTable(DoubleDummyTableTest.expected(),reopened.getDoubleDummyTable(deal));
        GameController controller=new GameController();controller.setStorage(reopened);
        String previous=System.getProperty("bridgetut.dds");
        try {
            System.setProperty("bridgetut.dds","missing-executable");
            DoubleDummyTableTest.sameTable(DoubleDummyTableTest.expected(),controller.getOrCalculateDoubleDummyTable(deal));
        } finally { if(previous==null)System.clearProperty("bridgetut.dds");else System.setProperty("bridgetut.dds",previous); }
        assertEquals(20,count(file,"double_dummy_results"));
    }
    @Test void originalHandIsLinkedToSavedGameAndSurvivesPlayedCards() throws Exception {
        Path file=directory.resolve("game.db");Storage store=new Storage(file.toString());
        Deal deal=DoubleDummyTableTest.referenceDeal();store.saveDoubleDummyTable(deal,DoubleDummyTableTest.expected());
        User user=store.createUser("Player");GameController game=new GameController();game.startLoadedDeal(deal);
        game.submitBid(new Bid(new Seat(Position.NORTH),1,Suit.CLUBS));
        for(int i=0;i<3;i++)game.submitBid(Bid.BidPass(game.getCurrentBidder()));
        DdsSolverTest.play(game,9);
        String id=store.saveGame(user,game);
        GameController loaded=new Storage(file.toString()).loadGame(user,id);
        DoubleDummyTableTest.sameTable(DoubleDummyTableTest.expected(),store.getDoubleDummyTable(loaded.getCurrentDeal()));
        assertEquals(1,count(file,"game_analysis_hands"));
        assertEquals(1,count(file,"analysis_hands"));
    }
    @Test void tutorialAttemptLinksToOriginalHandEvenBeforeAnalysisFinishes() throws Exception {
        Path file=directory.resolve("tutorial.db");Storage store=new Storage(file.toString());
        User user=store.createUser("Learner");TutorialLesson lesson=TutorialTestSupport.early("Claim",null);
        store.saveTutorialAttempt(user,TutorialTestSupport.entry(lesson),"Finished report");
        assertEquals(1,count(file,"tutorial_attempt_analysis_hands"));
        assertEquals(1,count(file,"analysis_hands"));assertEquals(0,count(file,"double_dummy_results"));
        DoubleDummyTable table=DdsSolver.analyzeDeal(lesson.getDeal());store.saveDoubleDummyTable(lesson.getDeal(),table);
        assertEquals(1,count(file,"analysis_hands"));
        DoubleDummyTableTest.sameTable(table,new Storage(file.toString()).getDoubleDummyTable(lesson.getDeal()));
    }
    @Test void rejectsTablesForAnotherHandAndDoesNotShareResultsAfterSwap() {
        Storage store=new Storage(directory.resolve("swap.db").toString());Deal original=DoubleDummyTableTest.referenceDeal();
        store.saveDoubleDummyTable(original,DoubleDummyTableTest.expected());
        Deal changed=DoubleDummyTable.copyDeal(original);Seat n=new Seat(Position.NORTH),e=new Seat(Position.EAST);
        changed.swapCards(n,changed.getHand(n).getCards().get(0),e,changed.getHand(e).getCards().get(0));
        assertNull(store.getDoubleDummyTable(changed));
        assertThrows(IllegalArgumentException.class,()->store.saveDoubleDummyTable(changed,DoubleDummyTableTest.expected()));
    }
    @Test void failedReplacementRollsBackEveryResult() throws Exception {
        Path file=directory.resolve("atomic.db");Storage store=new Storage(file.toString());Deal deal=DoubleDummyTableTest.referenceDeal();
        store.saveDoubleDummyTable(deal,DoubleDummyTableTest.expected());
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file);Statement s=c.createStatement()) {
            s.execute("CREATE TRIGGER reject_table BEFORE INSERT ON double_dummy_results WHEN NEW.strain='HEARTS' BEGIN SELECT RAISE(ABORT,'test failure'); END");
        }
        assertThrows(IllegalStateException.class,()->store.saveDoubleDummyTable(deal,new DoubleDummyTable(deal,new int[5][4])));
        DoubleDummyTableTest.sameTable(DoubleDummyTableTest.expected(),store.getDoubleDummyTable(deal));
        assertEquals(20,count(file,"double_dummy_results"));
    }
    @Test void rejectsIncompleteStoredTablesAndDatabaseRejectsImpossibleTricks() throws Exception {
        Path file=directory.resolve("invalid.db");Storage store=new Storage(file.toString());Deal deal=DoubleDummyTableTest.referenceDeal();
        store.saveDoubleDummyTable(deal,DoubleDummyTableTest.expected());
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file);Statement s=c.createStatement()) {
            assertThrows(SQLException.class,()->s.executeUpdate("UPDATE double_dummy_results SET tricks=14"));
            s.executeUpdate("DELETE FROM double_dummy_results WHERE strain='CLUBS'");
        }
        assertThrows(IllegalStateException.class,()->store.getDoubleDummyTable(deal));
    }
    @Test void savedTutorialAnalysisIsOwnedAndIndependentOfSourceFiles() throws Exception {
        Path file=directory.resolve("attempt.db");Storage store=new Storage(file.toString());
        User owner=store.createUser("Owner"),other=store.createUser("Other");
        TutorialLesson lesson=TutorialTestSupport.early("Claim",null);
        TutorialEntry entry=TutorialEntry.scripted(Path.of("missing-source.txt"),1,lesson);
        String id=store.saveTutorialAttempt(owner,entry,"Original report");
        assertNull(store.getTutorialAttemptDoubleDummyTable(owner,id));
        DoubleDummyTable expected=new DoubleDummyTable(lesson.getDeal(),new int[5][4]);
        store.saveDoubleDummyTable(lesson.getDeal(),expected);
        DoubleDummyTableTest.sameTable(expected,new Storage(file.toString()).getTutorialAttemptDoubleDummyTable(owner,id));
        assertNull(store.getTutorialAttemptDoubleDummyTable(other,id));
        assertNull(store.getTutorialAttemptDoubleDummyTable(owner,"' OR 1=1 --"));
        assertEquals("Original report",store.getTutorialAttempts(owner).get(0).reportText());
    }
    @Test void addingAnalysisTablesPreservesLegacyGamesAndAttempts() throws Exception {
        Path file=directory.resolve("legacy.db");Storage store=new Storage(file.toString());
        User user=store.createUser("Legacy");GameController game=new GameController();
        game.startLoadedDeal(DoubleDummyTableTest.referenceDeal());String gameId=store.saveGame(user,game);
        String attemptId=store.saveTutorialAttempt(user,TutorialTestSupport.entry(TutorialTestSupport.early("Claim",null)),"Old report");
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file);Statement statement=c.createStatement()) {
            statement.execute("DROP TABLE tutorial_attempt_analysis_hands");
            statement.execute("DROP TABLE game_analysis_hands");
            statement.execute("DROP TABLE double_dummy_results");
            statement.execute("DROP TABLE analysis_hands");
        }
        Storage upgraded=new Storage(file.toString());
        assertEquals(GamePhase.BIDDING,upgraded.loadGame(user,gameId).getPhase());
        assertEquals("Old report",upgraded.getTutorialAttempts(user).get(0).reportText());
        assertNull(upgraded.getTutorialAttemptDoubleDummyTable(user,attemptId));
        assertNull(upgraded.getDoubleDummyTable(game.getCurrentDeal()));
        upgraded.saveDoubleDummyTable(game.getCurrentDeal(),DoubleDummyTableTest.expected());
        DoubleDummyTableTest.sameTable(DoubleDummyTableTest.expected(),upgraded.getDoubleDummyTable(upgraded.loadGame(user,gameId).getCurrentDeal()));
    }

    private int count(Path file,String table) throws Exception {
        try(Connection c=DriverManager.getConnection("jdbc:sqlite:"+file);Statement s=c.createStatement();
            ResultSet r=s.executeQuery("SELECT count(*) FROM "+table)) { assertTrue(r.next()); return r.getInt(1); }
    }
}
