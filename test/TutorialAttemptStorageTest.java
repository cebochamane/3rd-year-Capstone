import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** Integration tests use a separate temporary SQLite database for every test. */
class TutorialAttemptStorageTest {
    @TempDir Path directory;
    Path database;
    Storage storage;
    User user;
    TutorialEntry entry;

    @BeforeEach
    void setup() {
        database = directory.resolve("attempts.db");
        storage = new Storage(database.toString());
        user = storage.createUser("Learner");
        entry = TutorialTestSupport.entry(TutorialTestSupport.early("Claim", 9));
    }

    @Test
    void reportSurvivesReopeningAndRemovalOfSource() throws Exception {
        Path source = directory.resolve("lesson.txt");
        Files.writeString(source, "Original lesson");
        TutorialEntry actualEntry = TutorialEntry.scripted(source, 2, entry.getLesson());
        String report = "Original hands\nN: A♠\nTeaching note: café\nLearner's note\nScore: 400";
        String id = storage.saveTutorialAttempt(user, actualEntry, report);
        Files.writeString(source, "Changed lesson");
        Files.delete(source);

        Storage reopened = new Storage(database.toString());
        var attempts = reopened.getTutorialAttempts(user);
        assertEquals(1, attempts.size());
        var saved = attempts.get(0);
        assertEquals(id, saved.attemptId());
        assertEquals("lesson.txt", saved.sourceFile());
        assertEquals(2, saved.lessonIndex());
        assertFalse(saved.savedAt().isBlank());
        assertEquals(report, saved.reportText());
        assertEquals(attempts, reopened.getTutorialAttempts(user), "Reading must not change a report");
        assertEquals(saved.savedAt() + " | lesson.txt | Lesson 2", saved.toString());
    }

    @Test
    void savesIndependentSnapshotsAndOrdersSameTimestampNewestFirst() throws Exception {
        String first = storage.saveTutorialAttempt(user, entry, "First notes");
        String second = storage.saveTutorialAttempt(user, entry, "Notes after reflection");
        assertNotEquals(first, second);
        // Force a timestamp tie instead of relying on wall-clock timing.
        try (var connection = DriverManager.getConnection("jdbc:sqlite:" + database);
                var statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE tutorial_attempts SET saved_at = '2026-01-01 00:00:00'");
        }
        var attempts = storage.getTutorialAttempts(user);
        assertEquals(2, attempts.size());
        assertEquals(second, attempts.get(0).attemptId());
        assertEquals(first, attempts.get(1).attemptId());
        assertEquals("First notes", attempts.get(1).reportText());
    }

    @Test
    void isolatesProfilesAndTreatsSqlLikeInputAsData() {
        String payload = "Learner's note: '); DROP TABLE users; --\n♠ finesse";
        User other = storage.createUser("Other ' OR '1'='1");
        storage.saveTutorialAttempt(user, entry, payload);
        assertTrue(storage.getTutorialAttempts(other).isEmpty());
        assertEquals(payload, storage.getTutorialAttempts(user).get(0).reportText());
        assertTrue(storage.getTutorialAttempts(new User("' OR 1=1 --", "Forged")).isEmpty());
        assertEquals(2, storage.getUsers().size());
        storage.saveTutorialAttempt(other, entry, "Other report");
        assertEquals(1, storage.getTutorialAttempts(user).size());
        assertEquals("Other report", storage.getTutorialAttempts(other).get(0).reportText());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n\t"})
    void rejectsMissingReportWithoutCreatingARow(String report) {
        assertThrows(IllegalArgumentException.class,
                () -> storage.saveTutorialAttempt(user, entry, report));
        assertTrue(storage.getTutorialAttempts(user).isEmpty());
    }

    @Test
    void rejectsInvalidOwnersAndEntriesWithoutCreatingARow() {
        assertThrows(IllegalArgumentException.class,
                () -> storage.saveTutorialAttempt(null, entry, "Report"));
        assertThrows(IllegalArgumentException.class, () -> storage.getTutorialAttempts(null));
        assertThrows(IllegalArgumentException.class,
                () -> storage.saveTutorialAttempt(user, null, "Report"));
        assertThrows(IllegalArgumentException.class, () -> storage.saveTutorialAttempt(user,
                TutorialEntry.practice(Path.of("practice.html")), "Report"));
        assertThrows(IllegalArgumentException.class, () -> storage.saveTutorialAttempt(user,
                TutorialEntry.scripted(Path.of("lesson.txt"), 0, entry.getLesson()), "Report"));
        assertThrows(IllegalStateException.class, () -> storage.saveTutorialAttempt(
                new User("absent", "Absent"), entry, "Report"));
        assertTrue(storage.getTutorialAttempts(user).isEmpty());
    }

    @Test
    void controllerRequiresStorageAndProfileThenUsesSelectedProfile() {
        GameController controller = new GameController();
        assertThrows(IllegalStateException.class, controller::getTutorialAttempts);
        assertThrows(IllegalStateException.class,
                () -> controller.saveTutorialAttempt(entry, "Report"));
        controller.setStorage(storage);
        assertThrows(IllegalStateException.class, controller::getTutorialAttempts);
        assertThrows(IllegalStateException.class,
                () -> controller.saveTutorialAttempt(entry, "Report"));
        controller.setCurrentUser(user);
        String id = controller.saveTutorialAttempt(entry, "Report");
        assertEquals(id, controller.getTutorialAttempts().get(0).attemptId());
        controller.setCurrentUser(storage.createUser("Another"));
        assertTrue(controller.getTutorialAttempts().isEmpty());
    }
}
