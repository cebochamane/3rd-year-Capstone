import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

class DoubleDummyTableTest {
    // Upstream DDS 2.9.0 examples/hands.cpp and hands/list100.txt, Apache-2.0.
    // https://github.com/dds-bridge/dds/tree/v2.9.0
    record Fixture(String pbn, String results) {}
    static Stream<Fixture> references() {
        return Stream.of(
            new Fixture("N:QJ6.K652.J85.T98 873.J97.AT764.Q4 K5.T83.KQ9.A7652 AT942.AQ4.32.KJ3",
                "5 8 5 8 6 6 6 6 5 7 5 7 7 5 7 5 6 6 6 6"),
            new Fixture("E:QJT5432.T.6.QJ82 .J97543.K7532.94 87.A62.QJT4.AT75 AK96.KQ8.A98.K63",
                "4 9 4 9 10 2 10 2 8 3 8 3 6 7 6 7 9 3 9 3"),
            new Fixture("N:73.QJT.AQ54.T752 QT6.876.KJ9.AQ84 5.A95432.7632.K6 AKJ9842.K.T8.J93",
                "3 10 3 10 9 4 9 4 8 4 8 4 3 9 3 9 4 8 4 8"),
            new Fixture("W:AK8.A83.KT.KQJ98 QJT96543.4.AJ.A5 .KQJ976.742.7642 72.T52.Q98653.T3",
                "8 4 8 5 2 10 2 11 7 6 7 6 2 10 2 11 2 10 2 11"));
    }
    static Deal referenceDeal() { return deal(references().findFirst().orElseThrow().pbn()); }
    static Deal deal(String pbn) {
        int first="NESW".indexOf(pbn.charAt(0));
        String[] hands=pbn.substring(2).split(" ");
        Map<Position,List<Card>> cards=new EnumMap<>(Position.class);
        Suit[] suits={Suit.SPADES,Suit.HEARTS,Suit.DIAMONDS,Suit.CLUBS};
        for (int i=0;i<4;i++) {
            List<Card> hand=new ArrayList<>();String[] holdings=hands[i].split("\\.",-1);
            for(int s=0;s<4;s++) for(char rank:holdings[s].toCharArray()) {
                hand.add(new Card(suits[s],Rank.values()["23456789TJQKA".indexOf(rank)]));
            }
            cards.put(Position.values()[(first+i)%4],hand);
        }
        return Deal.fromHands(new Seat(Position.NORTH),Vulnerability.NONE,cards);
    }
    static DoubleDummyTable expected() {
        Fixture fixture=references().findFirst().orElseThrow();
        return DdsSolver.parseTable(deal(fixture.pbn()),"DDS20900_TABLE "+fixture.results());
    }
    static void sameTable(DoubleDummyTable expected, DoubleDummyTable actual) {
        assertEquals(expected.getHandId(),actual.getHandId());
        for(Suit strain:Suit.values()) for(Position seat:Position.values()) {
            assertEquals(expected.getTricks(strain,seat),actual.getTricks(strain,seat),strain+" "+seat);
        }
    }
    @ParameterizedTest @MethodSource("references")
    void matchesAllTwentyPublishedResults(Fixture fixture) {
        Deal deal=deal(fixture.pbn());String before=DoubleDummyTable.holdings(deal);
        DoubleDummyTable actual=DdsSolver.analyzeDeal(deal);
        // Compare independently in DDS's documented strain order.
        int[] known=Arrays.stream(fixture.results().split(" ")).mapToInt(Integer::parseInt).toArray();
        Suit[] suits={Suit.SPADES,Suit.HEARTS,Suit.DIAMONDS,Suit.CLUBS,Suit.NT};
        for(int row=0;row<5;row++) for(int col=0;col<4;col++) {
            assertEquals(known[row*4+col],actual.getTricks(suits[row],Position.values()[col]));
        }
        assertEquals(before,DoubleDummyTable.holdings(deal));
    }
    @Test void partnershipBestKeepsDeclarerDifferencesAndDifferentBestStrains() {
        Fixture fixture=references().skip(3).findFirst().orElseThrow();
        DoubleDummyTable table=DdsSolver.parseTable(deal(fixture.pbn()),"DDS20900_TABLE "+fixture.results());
        assertEquals(4,table.getTricks(Suit.SPADES,Position.EAST));
        assertEquals(5,table.getTricks(Suit.SPADES,Position.WEST));
        assertEquals(5,table.getPartnershipTricks(Suit.SPADES,false));
        assertEquals(8,table.getPartnershipTricks(Suit.SPADES,true));
        assertEquals(11,table.getPartnershipTricks(Suit.HEARTS,false));
        assertEquals(2,table.getPartnershipTricks(Suit.HEARTS,true));
    }
    @Test void identityIgnoresHandSortingDealerAndVulnerabilityButTracksSwaps() {
        Deal original=referenceDeal();String id=DoubleDummyTable.handId(original);
        Deal copy=DoubleDummyTable.copyDeal(original);
        for(Hand hand:copy.getAllHands().values())Collections.reverse(hand.getCards());
        assertEquals(id,DoubleDummyTable.handId(copy));
        Map<Position,List<Card>> cards=new EnumMap<>(Position.class);
        for(Position seat:Position.values())cards.put(seat,copy.getHand(new Seat(seat)).getCards());
        assertEquals(id,DoubleDummyTable.handId(Deal.fromHands(new Seat(Position.WEST),Vulnerability.ALL,cards)));
        Seat n=new Seat(Position.NORTH),e=new Seat(Position.EAST);
        assertTrue(copy.swapCards(n,copy.getHand(n).getCards().get(0),e,copy.getHand(e).getCards().get(0)));
        assertNotEquals(id,DoubleDummyTable.handId(copy));
        assertEquals(id,DoubleDummyTable.handId(original));
    }
    @Test void rejectsIncompleteAndDuplicateHandsBeforeStartingSolver() {
        assertThrows(IllegalArgumentException.class,()->DdsSolver.analyzeDeal(null));
        Deal deal=referenceDeal();deal.getHand(new Seat(Position.NORTH)).getCards().remove(0);
        assertThrows(IllegalArgumentException.class,()->DdsSolver.analyzeDeal(deal));
        Deal duplicate=referenceDeal();duplicate.getHand(new Seat(Position.NORTH)).getCards()
            .set(0,duplicate.getHand(new Seat(Position.EAST)).getCards().get(0));
        assertThrows(IllegalArgumentException.class,()->DdsSolver.analyzeDeal(duplicate));
    }
    @Test void rejectsMalformedOrOutOfRangeSolverTables() {
        Deal deal=referenceDeal();
        for(String text:List.of("DDS20900 5","DDS20900_TABLE 1 2", "DDS20900_TABLE "+"14 ".repeat(20),
                "DDS20900_TABLE "+"-1 ".repeat(20),"DDS20900_TABLE "+"x ".repeat(20),
                "DDS20900_TABLE "+"5 ".repeat(21))) {
            assertThrows(IllegalStateException.class,()->DdsSolver.parseTable(deal,text));
        }
    }
    @Test void tableOwnsItsArrayAndRejectsIncompleteRows() {
        int[][] values=new int[5][4];DoubleDummyTable table=new DoubleDummyTable(referenceDeal(),values);
        values[0][0]=13;assertEquals(0,table.getTricks(Suit.CLUBS,Position.NORTH));
        assertThrows(IllegalArgumentException.class,()->new DoubleDummyTable(referenceDeal(),new int[4][4]));
        values[2]=new int[3];assertThrows(IllegalArgumentException.class,()->new DoubleDummyTable(referenceDeal(),values));
    }
}
