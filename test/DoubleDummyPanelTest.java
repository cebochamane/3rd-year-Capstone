import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class DoubleDummyPanelTest {
    @Test void displaysSeparateDeclarersAndPartnershipBestsWithoutEditableCells() throws Exception {
        TutorialTestSupport.onEdt(()->{
            Deal deal=DoubleDummyTableTest.referenceDeal();
            Gameplay game=new Gameplay(deal,new Contract(1,Suit.CLUBS,new Seat(Position.NORTH),false,false));
            DoubleDummyPanel panel=new DoubleDummyPanel(DoubleDummyTableTest.expected(),deal,game);
            var table=panel.getResultsTable();assertEquals(5,table.getRowCount());assertEquals(7,table.getColumnCount());
            assertEquals(7,table.getValueAt(Suit.CLUBS.ordinal(),1));
            assertEquals(7,table.getValueAt(Suit.CLUBS.ordinal(),3));
            assertEquals(5,table.getValueAt(Suit.CLUBS.ordinal(),6));
            assertFalse(table.isCellEditable(0,1));return null;
        });
    }
    @Test void comparesActualDeclarerInsteadOfTheBetterPartnerAndIncludesSettlement() {
        Deal deal=DoubleDummyTableTest.referenceDeal();
        Gameplay game=new Gameplay(deal,new Contract(1,Suit.CLUBS,new Seat(Position.NORTH),false,false));
        int[][] values=new int[5][4];values[0][0]=7;values[0][2]=9;
        DoubleDummyTable results=new DoubleDummyTable(deal,values);
        assertTrue(DoubleDummyPanel.comparison(results,game).contains("benchmark: 7 tricks"));
        assertTrue(DoubleDummyPanel.comparison(results,game).contains("Play in progress"));
        assertTrue(game.concede(game.getCurrentPlayer())); // East concedes to NS.
        String finalText=DoubleDummyPanel.comparison(results,game);
        assertTrue(finalText.contains("13 tricks (+6 versus benchmark"));
        assertTrue(finalText.contains("claim/concession"));
    }
    @Test void requiresMatchingOriginalHandAndAnExistingSession() {
        Deal deal=DoubleDummyTableTest.referenceDeal();
        assertThrows(IllegalArgumentException.class,()->new DoubleDummyPanel(DoubleDummyTableTest.expected(),deal,null));
        Gameplay game=new Gameplay(deal,new Contract(1,Suit.CLUBS,new Seat(Position.NORTH),false,false));
        Deal other=TutorialTestSupport.early("Claim",null).getDeal();
        assertThrows(IllegalArgumentException.class,()->new DoubleDummyPanel(DoubleDummyTableTest.expected(),other,game));
    }
}
