package io.github.suli350.todo;

public enum Priority {
    LOW, MEDIUM, HIGH;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
