package io.github.suli350.todo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** In-memory list of tasks with change notifications. The UI observes it; the store saves it. */
public class TaskList {

    /** Active first, then overdue/soonest due date, then highest priority, then oldest. */
    public static final Comparator<Task> SMART_ORDER = Comparator
            .comparing(Task::isDone)
            .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(Task::getPriority, Comparator.reverseOrder())
            .thenComparing(Task::getId);

    private final List<Task> tasks = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private final Supplier<LocalDate> clock;
    private long nextId = 1;

    public TaskList(Supplier<LocalDate> clock) {
        this.clock = clock;
    }

    public TaskList() {
        this(LocalDate::now);
    }

    public LocalDate today() {
        return clock.get();
    }

    public void setAll(List<Task> loaded) {
        tasks.clear();
        tasks.addAll(loaded);
        nextId = loaded.stream().mapToLong(Task::getId).max().orElse(0) + 1;
        fireChanged();
    }

    public Task add(String title, String notes, Priority priority, LocalDate due) {
        Task t = new Task(nextId++, title, notes, priority, due, false, today());
        tasks.add(t);
        fireChanged();
        return t;
    }

    public void update(Task task, String title, String notes, Priority priority, LocalDate due) {
        task.setTitle(title);
        task.setNotes(notes);
        task.setPriority(priority);
        task.setDueDate(due);
        fireChanged();
    }

    public void setDone(Task task, boolean done) {
        task.setDone(done);
        fireChanged();
    }

    public boolean remove(Task task) {
        boolean removed = tasks.remove(task);
        if (removed) {
            fireChanged();
        }
        return removed;
    }

    public int clearCompleted() {
        int before = tasks.size();
        tasks.removeIf(Task::isDone);
        int removed = before - tasks.size();
        if (removed > 0) {
            fireChanged();
        }
        return removed;
    }

    public List<Task> all() {
        return Collections.unmodifiableList(tasks);
    }

    public List<Task> view(Filter filter, String query) {
        LocalDate today = today();
        return tasks.stream()
                .filter(t -> filter.accepts(t, today))
                .filter(t -> t.matches(query))
                .sorted(SMART_ORDER)
                .collect(Collectors.toList());
    }

    public long count(Filter filter) {
        LocalDate today = today();
        return tasks.stream().filter(t -> filter.accepts(t, today)).count();
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    private void fireChanged() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}
