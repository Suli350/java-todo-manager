package io.github.suli350.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskListTest {

    private static final LocalDate TODAY = LocalDate.of(2019, 4, 10);
    private TaskList list;
    private int changes;

    @BeforeEach
    void setUp() {
        list = new TaskList(() -> TODAY);
        list.addListener(() -> changes++);
        list.add("Write report", "quarterly numbers", Priority.MEDIUM, TODAY.minusDays(1)); // overdue
        list.add("Call dentist", "", Priority.HIGH, TODAY);                                // due today
        list.add("Read book", "sci-fi", Priority.LOW, null);
        list.add("Plan trip", "", Priority.HIGH, TODAY.plusDays(5));
    }

    private List<String> titles(List<Task> tasks) {
        return tasks.stream().map(Task::getTitle).collect(Collectors.toList());
    }

    @Test
    void idsAreSequentialAndListenersFire() {
        assertEquals(4, list.all().get(3).getId());
        assertEquals(4, changes);
    }

    @Test
    void filters() {
        assertEquals(1, list.count(Filter.OVERDUE));
        assertEquals(1, list.count(Filter.DUE_TODAY));
        assertEquals(4, list.count(Filter.ACTIVE));
        list.setDone(list.all().get(0), true);
        assertEquals(0, list.count(Filter.OVERDUE));
        assertEquals(1, list.count(Filter.COMPLETED));
    }

    @Test
    void searchMatchesTitleAndNotesCaseInsensitive() {
        assertEquals(List.of("Read book"), titles(list.view(Filter.ALL, "SCI")));
        assertEquals(List.of("Write report"), titles(list.view(Filter.ALL, "report")));
    }

    @Test
    void smartOrderPutsUrgentFirstAndDoneLast() {
        list.setDone(list.all().get(1), true); // Call dentist done
        assertEquals(List.of("Write report", "Plan trip", "Read book", "Call dentist"),
                titles(list.view(Filter.ALL, "")));
    }

    @Test
    void clearCompletedAndRemove() {
        list.setDone(list.all().get(2), true);
        assertEquals(1, list.clearCompleted());
        assertEquals(3, list.all().size());
        assertTrue(list.remove(list.all().get(0)));
        assertFalse(list.all().stream().anyMatch(t -> t.getTitle().equals("Write report")));
    }

    @Test
    void newIdsContinueAfterLoading() {
        TaskList other = new TaskList(() -> TODAY);
        other.setAll(list.all());
        assertEquals(5, other.add("New", "", Priority.LOW, null).getId());
    }

    @Test
    void emptyTitleIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> list.add("  ", "", Priority.LOW, null));
    }

    @Test
    void dueDateShortcuts() {
        assertEquals(TODAY, TaskDialog.parseDue("today", TODAY));
        assertEquals(TODAY.plusDays(1), TaskDialog.parseDue("Tomorrow", TODAY));
        assertEquals(TODAY.plusDays(3), TaskDialog.parseDue("+3", TODAY));
        assertEquals(LocalDate.of(2019, 12, 24), TaskDialog.parseDue("2019-12-24", TODAY));
        assertEquals(null, TaskDialog.parseDue("", TODAY));
    }
}
