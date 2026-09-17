import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Unit tests for the Learning Note system.
 */
public class LearningNoteTest {

    private LearningNote note;
    private LearningNote biddingNote;
    private LearningNote prePlayNote;
    private LearningNote postPlayNote;

    @BeforeEach
    void setUp() {
        note = new LearningNote();
        biddingNote = new LearningNote(NotePhase.BIDDING);
        prePlayNote = new LearningNote(NotePhase.PRE_PLAY);
        postPlayNote = new LearningNote(NotePhase.POST_PLAY);
    }

    // ===== Constructor Tests =====
    @Test
    @DisplayName("Default constructor creates note with current timestamp")
    void testDefaultConstructor() {
        assertNotNull(note.getTimestamp());
        assertEquals(NotePhase.GENERAL, note.getPhase());
        assertEquals(0, note.getTrickNumber());
        assertNull(note.getNoteText());
        assertNull(note.getContract());
        assertNull(note.getHandAnalysis());
    }

    @Test
    @DisplayName("Constructor with phase parameter works correctly")
    void testConstructorWithPhase() {
        assertEquals(NotePhase.BIDDING, biddingNote.getPhase());
        assertEquals(NotePhase.PRE_PLAY, prePlayNote.getPhase());
        assertEquals(NotePhase.POST_PLAY, postPlayNote.getPhase());
        assertNotNull(biddingNote.getTimestamp());
    }

    // ===== Setter/Getter Tests =====
    @Test
    @DisplayName("Setting and getting note text works")
    void testSetAndGetNoteText() {
        String text = "I decided to bid 1S because I have a strong spade suit.";
        biddingNote.setNoteText(text);
        assertEquals(text, biddingNote.getNoteText());
    }

    @Test
    @DisplayName("Setting and getting hand analysis works")
    void testSetAndGetHandAnalysis() {
        String analysis = "#Winners 4 | #Losers 3 | #Opportunities finesse hearts";
        prePlayNote.setHandAnalysis(analysis);
        assertEquals(analysis, prePlayNote.getHandAnalysis());
    }

    @Test
    @DisplayName("Setting and getting contract works")
    void testSetAndGetContract() {
        String contract = "4S";
        postPlayNote.setContract(contract);
        assertEquals(contract, postPlayNote.getContract());
    }

    @Test
    @DisplayName("Setting and getting trick number works")
    void testSetAndGetTrickNumber() {
        int trickNumber = 7;
        note.setTrickNumber(trickNumber);
        assertEquals(trickNumber, note.getTrickNumber());
    }

    @Test
    @DisplayName("Setting and getting bidding sequence works")
    void testSetAndGetBiddingSequence() {
        String sequence = "South Pass, West 1C, North 1S, East Pass";
        biddingNote.setBiddingSequence(sequence);
        assertEquals(sequence, biddingNote.getBiddingSequence());
    }

    @Test
    @DisplayName("Setting and getting lead card works")
    void testSetAndGetLeadCard() {
        String leadCard = "AH";
        note.setLeadCard(leadCard);
        assertEquals(leadCard, note.getLeadCard());
    }

    // ===== Keyword Detection Tests =====
    @Test
    @DisplayName("Detects finesse keyword")
    void testKeywordFinesse() {
        String text = "I need to finesse the king of hearts";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(1, keywords.length);
        assertEquals("finesse", keywords[0]);
    }

    @Test
    @DisplayName("Detects duck and entry keywords")
    void testKeywordDuck() {
        String text = "I should duck this trick to preserve my entry";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(2, keywords.length);
        assertTrue(containsKeyword(keywords, "duck"));
        assertTrue(containsKeyword(keywords, "entry"));
    }

    @Test
    @DisplayName("Detects multiple keywords in one note")
    void testMultipleKeywords() {
        String text = "I will ruff this trick with a small trump and then cross-ruff";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(3, keywords.length);
        assertTrue(containsKeyword(keywords, "ruff"));
        assertTrue(containsKeyword(keywords, "trump"));
        assertTrue(containsKeyword(keywords, "cross-ruff"));
    }

    @Test
    @DisplayName("Detects all keywords correctly")
    void testAllKeywords() {
        String text = "establish finesse duck ruff cross-ruff trump promote discard entry dummy declarer defender lead signal count";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(LearningNote.KEYWORDS.length, keywords.length);
    }

    @Test
    @DisplayName("Keyword detection is case insensitive")
    void testKeywordCaseInsensitivity() {
        String text = "I need to FINESSE the KING and DUCK this trick";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(2, keywords.length);
        assertTrue(containsKeyword(keywords, "finesse"));
        assertTrue(containsKeyword(keywords, "duck"));
    }

    @Test
    @DisplayName("No keywords found in text without keywords")
    void testNoKeywords() {
        String text = "I made a simple bid without any fancy words";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(0, keywords.length);
    }

    @Test
    @DisplayName("Null note text returns empty array")
    void testNullNoteText() {
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(0, keywords.length);
    }

    @Test
    @DisplayName("Keywords with punctuation are detected")
    void testKeywordsWithPunctuation() {
        String text = "I need to finesse, duck, and ruff the trick!";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(3, keywords.length);
        assertTrue(containsKeyword(keywords, "finesse"));
        assertTrue(containsKeyword(keywords, "duck"));
        assertTrue(containsKeyword(keywords, "ruff"));
    }

    @Test
    @DisplayName("Partial word matches are not detected")
    void testPartialWordMatch() {
        String text = "establishment finessed duckling";
        postPlayNote.setNoteText(text);
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(0, keywords.length);
    }

    @Test
    @DisplayName("Very long note text is handled")
    void testLongNoteText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("finesse ");
        }
        postPlayNote.setNoteText(longText.toString());
        String[] keywords = postPlayNote.getKeywordsUsed();
        assertEquals(1, keywords.length);
        assertEquals("finesse", keywords[0]);
    }

    // ===== toString Tests =====
    @Test
    @DisplayName("toString with all fields set")
    void testToStringFull() {
        postPlayNote.setNoteText("I learned to finesse the king");
        postPlayNote.setContract("4S");
        postPlayNote.setTrickNumber(13);
        String result = postPlayNote.toString();
        assertTrue(result.contains("POST_PLAY"));
        assertTrue(result.contains("Trick 13"));
        assertTrue(result.contains("Contract: 4S"));
        assertTrue(result.contains("I learned to finesse the king"));
    }

    @Test
    @DisplayName("toString with minimal fields")
    void testToStringMinimal() {
        note.setNoteText("Quick note");
        String result = note.toString();
        assertTrue(result.contains("GENERAL"));
        assertTrue(result.contains("Quick note"));
        assertFalse(result.contains("Trick"));
        assertFalse(result.contains("Contract:"));
    }

    @Test
    @DisplayName("toString includes timestamp")
    void testToStringTimestamp() {
        note.setNoteText("Test note");
        String result = note.toString();
        boolean hasTimestamp = result.contains("[") && result.contains("]");
        assertTrue(hasTimestamp, "Should contain timestamp but was: " + result);
    }

    // ===== Collection Tests =====
    @Test
    @DisplayName("Adding notes to collection works")
    void testAddingNotesToCollection() {
        ArrayList<LearningNote> notes = new ArrayList<>();
        notes.add(biddingNote);
        notes.add(prePlayNote);
        notes.add(postPlayNote);
        assertEquals(3, notes.size());
    }

    @Test
    @DisplayName("Retrieving notes from collection works")
    void testRetrievingNotesFromCollection() {
        ArrayList<LearningNote> notes = new ArrayList<>();
        notes.add(biddingNote);

        LearningNote retrieved = notes.get(0);
        assertEquals(NotePhase.BIDDING, retrieved.getPhase());
    }

    @Test
    @DisplayName("Multiple notes with different data work")
    void testMultipleNotesWithDifferentData() {
        biddingNote.setNoteText("Bid 1S with 15 points");
        biddingNote.setBiddingSequence("South Pass, West 1C, North 1S");

        prePlayNote.setNoteText("Plan to finesse hearts");
        prePlayNote.setHandAnalysis("#Winners 5 | #Losers 2");
        prePlayNote.setContract("1S");

        postPlayNote.setNoteText("Made the contract! Ruff was key");
        postPlayNote.setContract("1S");
        postPlayNote.setTrickNumber(13);

        assertEquals("Bid 1S with 15 points", biddingNote.getNoteText());
        assertEquals("Plan to finesse hearts", prePlayNote.getNoteText());
        assertTrue(postPlayNote.getKeywordsUsed().length > 0);
    }

    // ===== Timestamp Tests =====
    @Test
    @DisplayName("Timestamp is created automatically")
    void testTimestampAutoCreated() {
        assertNotNull(note.getTimestamp());
        assertNotNull(biddingNote.getTimestamp());
        assertNotNull(prePlayNote.getTimestamp());
        assertNotNull(postPlayNote.getTimestamp());
    }

    @Test
    @DisplayName("Timestamp can be manually set")
    void testManualTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        note.setTimestamp(now);
        assertEquals(now, note.getTimestamp());
    }

    // ===== Helper Methods =====
    private boolean containsKeyword(String[] keywords, String keyword) {
        for (String kw : keywords) {
            if (kw.equals(keyword)) {
                return true;
            }
        }
        return false;
    }
}