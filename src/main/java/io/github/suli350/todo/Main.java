package io.github.suli350.todo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Path file = args.length > 0
                ? Paths.get(args[0])
                : Paths.get(System.getProperty("user.home"), ".todo-manager", "tasks.csv");
        CsvTaskStore store = new CsvTaskStore(file);
        TaskList tasks = new TaskList();
        SwingUtilities.invokeLater(() -> {
            try {
                tasks.setAll(store.load());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not read " + file + ":\n" + e.getMessage(),
                        "Load failed", JOptionPane.ERROR_MESSAGE);
            }
            new TodoFrame(tasks, store).setVisible(true);
        });
    }
}
