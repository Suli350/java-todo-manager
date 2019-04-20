package io.github.suli350.todo;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Modal add/edit form. {@link #showDialog()} returns false if the user cancelled. */
class TaskDialog extends JDialog {

    private final JTextField title = new JTextField(28);
    private final JTextArea notes = new JTextArea(5, 28);
    private final JComboBox<Priority> priority = new JComboBox<>(Priority.values());
    private final JTextField due = new JTextField(10);
    private boolean confirmed;

    String resultTitle;
    String resultNotes;
    Priority resultPriority;
    LocalDate resultDue;

    TaskDialog(Window owner, Task existing, LocalDate today) {
        super(owner, existing == null ? "New task" : "Edit task", ModalityType.APPLICATION_MODAL);
        priority.setSelectedItem(Priority.MEDIUM);
        if (existing != null) {
            title.setText(existing.getTitle());
            notes.setText(existing.getNotes());
            priority.setSelectedItem(existing.getPriority());
            due.setText(existing.getDueDate() == null ? "" : existing.getDueDate().toString());
        }
        due.setToolTipText("yyyy-mm-dd, 'today', 'tomorrow', '+3' (days from now) or empty");
        notes.setLineWrap(true);
        notes.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        addRow(form, c, 0, "Title", title);
        addRow(form, c, 1, "Notes", new JScrollPane(notes));
        addRow(form, c, 2, "Priority", priority);
        addRow(form, c, 3, "Due date", due);

        JButton ok = new JButton(existing == null ? "Add" : "Save");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> onOk(today));
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel();
        buttons.add(ok);
        buttons.add(cancel);

        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(ok);
        pack();
        setLocationRelativeTo(owner);
    }

    private static void addRow(JPanel form, GridBagConstraints c, int row, String label,
                               java.awt.Component field) {
        c.gridx = 0;
        c.gridy = row;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
        c.weightx = 0;
    }

    private void onOk(LocalDate today) {
        if (title.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.", "Missing title",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            resultDue = parseDue(due.getText(), today);
        } catch (DateTimeParseException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Use yyyy-mm-dd, today, tomorrow or +N days.", "Invalid date",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        resultTitle = title.getText().trim();
        resultNotes = notes.getText();
        resultPriority = (Priority) priority.getSelectedItem();
        confirmed = true;
        dispose();
    }

    /** Accepts "", "today", "tomorrow", "+N" or an ISO date. */
    static LocalDate parseDue(String text, LocalDate today) {
        String s = text.trim().toLowerCase();
        if (s.isEmpty()) {
            return null;
        }
        if (s.equals("today")) {
            return today;
        }
        if (s.equals("tomorrow")) {
            return today.plusDays(1);
        }
        if (s.startsWith("+")) {
            return today.plusDays(Integer.parseInt(s.substring(1)));
        }
        return LocalDate.parse(s);
    }

    boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
}
