package io.github.suli350.todo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves tasks as RFC 4180 style CSV. Fields containing commas, quotes or
 * newlines are quoted, and quotes are doubled. Writes go to a temp file that is
 * then moved over the real one, so a crash never leaves a half-written file.
 */
public class CsvTaskStore {

    static final String HEADER = "id,title,notes,priority,due,done,created";
    private final Path file;

    public CsvTaskStore(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public List<Task> load() throws IOException {
        List<Task> tasks = new ArrayList<>();
        if (!Files.exists(file)) {
            return tasks;
        }
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        List<List<String>> rows = parse(content);
        for (int i = 1; i < rows.size(); i++) {  // skip header
            List<String> r = rows.get(i);
            if (r.size() == 1 && r.get(0).isEmpty()) {
                continue;
            }
            if (r.size() != 7) {
                throw new IOException("Line " + (i + 1) + ": expected 7 fields but found " + r.size());
            }
            try {
                tasks.add(new Task(
                        Long.parseLong(r.get(0)), r.get(1), r.get(2), Priority.valueOf(r.get(3)),
                        r.get(4).isEmpty() ? null : LocalDate.parse(r.get(4)),
                        Boolean.parseBoolean(r.get(5)), LocalDate.parse(r.get(6))));
            } catch (RuntimeException e) {
                throw new IOException("Line " + (i + 1) + ": " + e.getMessage(), e);
            }
        }
        return tasks;
    }

    public void save(List<Task> tasks) throws IOException {
        StringBuilder sb = new StringBuilder(HEADER).append('\n');
        for (Task t : tasks) {
            sb.append(t.getId()).append(',')
              .append(escape(t.getTitle())).append(',')
              .append(escape(t.getNotes())).append(',')
              .append(t.getPriority().name()).append(',')
              .append(t.getDueDate() == null ? "" : t.getDueDate()).append(',')
              .append(t.isDone()).append(',')
              .append(t.getCreated()).append('\n');
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path tmp = Files.createTempFile(parent, "tasks", ".tmp");
        Files.write(tmp, sb.toString().getBytes(StandardCharsets.UTF_8));
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    static String escape(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return '"' + s.replace("\"", "\"\"") + '"';
        }
        return s;
    }

    /** Minimal CSV parser supporting quoted fields with commas, quotes and newlines. */
    static List<List<String>> parse(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < content.length() && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (c == '\n') {
                row.add(field.toString());
                field.setLength(0);
                rows.add(row);
                row = new ArrayList<>();
            } else if (c != '\r') {
                field.append(c);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }
}
