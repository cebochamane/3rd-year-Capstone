import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;

class TutorialLessonParserTest {

    @Test
    void parsesABidAndPlayLessonAndFindsTheCorrectDeclarer() {
        TutorialLesson lesson = TutorialLessonParser.parse(bidAndPlayLesson()).get(0);

        assertEquals(TutorialLessonType.BID_AND_PLAY, lesson.getType());
        assertEquals(Position.SOUTH, lesson.getDeal().getDealer().getPosition());
        assertEquals(Vulnerability.ALL, lesson.getDeal().getVulnerability());
        assertEquals(1, lesson.getContract().getLevel());
        assertEquals(Suit.NT, lesson.getContract().getDenomination());
        assertEquals(Position.SOUTH, lesson.getContract().getDeclarer().getPosition());
        assertEquals("South lets the computer demonstrate the opening lead.", lesson.getNote());
    }

    @Test
    void acceptsABidAndPlayLessonThatOmitsClosingPasses() {
        String abbreviatedAuction = bidAndPlayLesson().replace("1NT;P;P;P.", "1NT.");

        TutorialLesson lesson = TutorialLessonParser.parse(abbreviatedAuction).get(0);

        assertEquals(Position.SOUTH, lesson.getContract().getDeclarer().getPosition());
        assertEquals(1, lesson.getContract().getLevel());
        assertEquals(Suit.NT, lesson.getContract().getDenomination());
    }

    @Test
    void parsesAPlayOnlyLessonWithSouthAsDeclarer() {
        TutorialLesson lesson = TutorialLessonParser.parse(playOnlyLesson()).get(0);

        assertEquals(TutorialLessonType.PLAY_ONLY, lesson.getType());
        assertEquals(Position.SOUTH, lesson.getContract().getDeclarer().getPosition());
        assertEquals(5, lesson.getActions().size());
        assertEquals(TutorialAction.Type.CLAIM, lesson.getActions().get(4).getType());
    }

    @Test
    void rejectsAnInvalidCardCodeWithTheSourceLineNumber() {
        String invalid = playOnlyLesson().replace("AC,AS,AD,AH", "FC,AS,AD,AH");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TutorialLessonParser.parse(invalid));

        assertTrue(exception.getMessage().contains("line 7"));
        assertTrue(exception.getMessage().contains("Invalid rank code 'F'"));
    }

    @Test
    void rejectsAScriptedCardThatIsNotHeldByItsPlayer() {
        String invalid = playOnlyLesson().replace("AC,AS,AD,AH", "AC,KH,AD,AH");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> TutorialLessonParser.parse(invalid));

        assertTrue(exception.getMessage().contains("NORTH"));
        assertTrue(exception.getMessage().contains("does not hold"));
    }

    static String playOnlyLesson() {
        return """
                Cards held: spades;hearts;diamonds;clubs
                N: AKQJT98765432;-;-;-
                S: -;AKQJT98765432;-;-
                E: -;-;AKQJT98765432;-
                W: -;-;-;AKQJT98765432
                PLAY 1NT
                AC,AS,AD,AH
                Claim
                NOTE: South lets the computer demonstrate the opening lead.
                """;
    }

    static String bidAndPlayLesson() {
        return """
                Cards held: spades;hearts;diamonds;clubs
                N: AKQJT98765432;-;-;-
                S: -;AKQJT98765432;-;-
                E: -;-;AKQJT98765432;-
                W: -;-;-;AKQJT98765432
                D: S
                BOTH VUL
                1NT;P;P;P.
                AC,AS,AD,AH
                Claim
                NOTE: South lets the computer demonstrate the opening lead.
                """;
    }
}
