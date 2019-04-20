package io.github.suli350.todo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.time.LocalDate;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;

public class TodoFrame extends JFrame {

    private final TaskList taskList;
    private final CsvTaskStore store;
    private final TaskTableModel model;
    private final JTable table;
    private final JComboBox<Filter> filter = new JComboBox<>(Filter.values());
    private final JTextField search = new JTextField(16);
    private final JLabel status = new JLabel();

    public TodoFrame(TaskList taskList, CsvTaskStore store) {
        super("To-Do Manager");
        this.taskList = taskList;
        this.store = store;
        this.model = new TaskTableModel(taskList);
        this.table = new JTable(model);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        configureTable();
        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        taskList.addListener(this::onTasksChanged);
        refresh();

        setSize(new Dimension(820, 520));
        setLocationRelativeTo(null);
    }

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton clear = new JButton("Clear completed");
        add.addActionListener(e -> addTask());
        edit.addActionListener(e -> editSelected());
        delete.addActionListener(e -> deleteSelected());
        clear.addActionListener(e -> {
            int n = taskList.clearCompleted();
            status.setText("Removed " + n + " completed task(s)");
        });
        filter.addActionListener(e -> refresh());
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        bar.add(add);
        bar.add(edit);
        bar.add(delete);
        bar.add(clear);
        bar.add(new JLabel("   Show:"));
        bar.add(filter);
        bar.add(new JLabel("  Search:"));
        bar.add(search);
        return bar;
    }

    private void configureTable() {
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false);
        table.setRowSorter(new TableRowSorter<>(model));
        table.getColumnModel().getColumn(TaskTableModel.DONE).setMaxWidth(50);
        table.getColumnModel().getColumn(TaskTableModel.TITLE).setPreferredWidth(360);
        table.setDefaultRenderer(Object.class, new TaskRenderer());
        table.setDefaultRenderer(Priority.class, new TaskRenderer());
        table.setDefaultRenderer(LocalDate.class, new TaskRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.columnAtPoint(e.getPoint()) != TaskTableModel.DONE) {
                    editSelected();
                }
            }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        table.getInputMap(JComponent.WHEN_FOCUSED)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "deleteTask");
        table.getActionMap().put("deleteTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelected();
            }
        });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
             .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.Toolkit.getDefaultToolkit()
                     .getMenuShortcutKeyMaskEx()), "newTask");
        getRootPane().getActionMap().put("newTask", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
    }

    private Task selectedTask() {
        int viewRow = table.getSelectedRow();
        return viewRow < 0 ? null : model.getTask(table.convertRowIndexToModel(viewRow));
    }

    private void addTask() {
        TaskDialog d = new TaskDialog(this, null, taskList.today());
        if (d.showDialog()) {
            taskList.add(d.resultTitle, d.resultNotes, d.resultPriority, d.resultDue);
        }
    }

    private void editSelected() {
        Task t = selectedTask();
        if (t == null) {
            return;
        }
        TaskDialog d = new TaskDialog(this, t, taskList.today());
        if (d.showDialog()) {
            taskList.update(t, d.resultTitle, d.resultNotes, d.resultPriority, d.resultDue);
        }
    }

    private void deleteSelected() {
        Task t = selectedTask();
        if (t != null && JOptionPane.showConfirmDialog(this, "Delete \"" + t.getTitle() + "\"?",
                "Delete task", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            taskList.remove(t);
        }
    }

    private void onTasksChanged() {
        try {
            store.save(taskList.all());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not save tasks:\n" + e.getMessage(),
                    "Save failed", JOptionPane.ERROR_MESSAGE);
        }
        refresh();
    }

    private void refresh() {
        model.setRows(taskList.view((Filter) filter.getSelectedItem(), search.getText()));
        status.setText(String.format("%d active  ·  %d due today  ·  %d overdue  ·  %d done   —   %s",
                taskList.count(Filter.ACTIVE), taskList.count(Filter.DUE_TODAY),
                taskList.count(Filter.OVERDUE), taskList.count(Filter.COMPLETED), store.getFile()));
    }

    /** Colours: overdue in red, done in grey strikethrough, high priority in bold. */
    private class TaskRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value, boolean selected,
                                                       boolean focus, int row, int column) {
            Component c = super.getTableCellRendererComponent(tbl, value, selected, focus, row, column);
            Task t = model.getTask(tbl.convertRowIndexToModel(row));
            Font base = tbl.getFont();
            c.setFont(t.getPriority() == Priority.HIGH && !t.isDone() ? base.deriveFont(Font.BOLD) : base);
            if (!selected) {
                if (t.isDone()) {
                    c.setForeground(Color.GRAY);
                } else if (t.isOverdue(taskList.today())) {
                    c.setForeground(new Color(0xC62828));
                } else {
                    c.setForeground(tbl.getForeground());
                }
            }
            if (value == null) {
                setText("");
            }
            return c;
        }
    }
}
