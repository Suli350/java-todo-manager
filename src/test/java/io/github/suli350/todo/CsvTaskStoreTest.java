package io.github.suli350.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CsvTaskStoreTest {

    private static final LocalDate DAY = LocalDate.of(2019, 4, 10);

    @Test
    void roundTripKeepsAwkwardText() throws IOException {
        Path file = Files.createTempDirectory("todo").resolve("sub/tasks.csv");
        CsvTaskStore store = new CsvTaskStore(file);
        List<Task> tasks = Arrays.asList(
                new Task(1, "Buy milk, eggs", "say \"hi\" to Sam\nsecond line", Priority.HIGH, DAY, false, DAY),
                new Task(7, "Plain", "", Priority.LOW, null, true, DAY.minusDays(3)));
        store.save(tasks);

        List<Task> loaded = store.load();
        assertEquals(2, loaded.size());
        Task a = loaded.get(0);
        assertEquals("Buy milk, eggs", a.getTitle());
        assertEquals("say \"hi\" to Sam\nsecond line", a.getNotes());
        assertEquals(Priority.HIGH, a.getPriority());
        assertEquals(DAY, a.getDueDate());
        Task b = loaded.get(1);
        assertEquals(7, b.getId());
        assertNull(b.getDueDate());
        assertTrue(b.isDone());
    }

    @Test
    void missingFileMeansNoTasks() throws IOException {
        Path file = Files.createTempDirectory("todo").resolve("none.csv");
        assertTrue(new CsvTaskStore(file).load().isEmpty());
    }

    @Test
    void corruptLineIsReported() throws IOException {
        Path file = Files.createTempFile("todo", ".csv");
        Files.write(file, (CsvTaskStore.HEADER + "\n1,only,three\n").getBytes(StandardCharsets.UTF_8));
        IOException e = assertThrows(IOException.class, () -> new CsvTaskStore(file).load());
        assertTrue(e.getMessage().startsWith("Line 2"));
    }

    @Test
    void parserHandlesQuotes() {
        List<List<String>> rows = CsvTaskStore.parse("a,\"b,c\",\"d\"\"e\"\n");
        assertEquals(Arrays.asList("a", "b,c", "d\"e"), rows.get(0));
    }
}
