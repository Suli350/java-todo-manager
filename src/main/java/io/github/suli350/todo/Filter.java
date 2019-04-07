package io.github.suli350.todo;

import java.time.LocalDate;

public enum Filter {
    ALL("All tasks"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    DUE_TODAY("Due today"),
    OVERDUE("Overdue");

    private final String label;

    Filter(String label) {
        this.label = label;
    }

    public boolean accepts(Task task, LocalDate today) {
        switch (this) {
            case ACTIVE: return !task.isDone();
            case COMPLETED: return task.isDone();
            case DUE_TODAY: return task.isDueOn(today);
            case OVERDUE: return task.isOverdue(today);
            default: return true;
        }
    }

    @Override
    public String toString() {
        return label;
    }
}
