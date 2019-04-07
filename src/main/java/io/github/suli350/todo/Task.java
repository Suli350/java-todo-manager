package io.github.suli350.todo;

import java.time.LocalDate;
import java.util.Objects;

public class Task {

    private final long id;
    private String title;
    private String notes;
    private Priority priority;
    private LocalDate dueDate;   // may be null
    private boolean done;
    private final LocalDate created;

    public Task(long id, String title, String notes, Priority priority, LocalDate dueDate,
                boolean done, LocalDate created) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        this.id = id;
        this.title = title.trim();
        this.notes = notes == null ? "" : notes;
        this.priority = Objects.requireNonNull(priority);
        this.dueDate = dueDate;
        this.done = done;
        this.created = Objects.requireNonNull(created);
    }

    public boolean isOverdue(LocalDate today) {
        return !done && dueDate != null && dueDate.isBefore(today);
    }

    public boolean isDueOn(LocalDate day) {
        return !done && day.equals(dueDate);
    }

    public boolean matches(String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String q = query.toLowerCase().trim();
        return title.toLowerCase().contains(q) || notes.toLowerCase().contains(q);
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getNotes() { return notes; }
    public Priority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isDone() { return done; }
    public LocalDate getCreated() { return created; }

    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        this.title = title.trim();
    }

    public void setNotes(String notes) { this.notes = notes == null ? "" : notes; }
    public void setPriority(Priority priority) { this.priority = Objects.requireNonNull(priority); }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setDone(boolean done) { this.done = done; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Task && ((Task) o).id == id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "#" + id + " " + title + (done ? " [done]" : "");
    }
}
