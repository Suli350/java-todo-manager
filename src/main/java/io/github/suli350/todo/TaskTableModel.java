package io.github.suli350.todo;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

class TaskTableModel extends AbstractTableModel {

    static final int DONE = 0;
    static final int TITLE = 1;
    static final int PRIORITY = 2;
    static final int DUE = 3;
    static final int CREATED = 4;
    private static final String[] COLUMNS = {"Done", "Title", "Priority", "Due", "Created"};

    private final TaskList taskList;
    private List<Task> rows = new ArrayList<>();

    TaskTableModel(TaskList taskList) {
        this.taskList = taskList;
    }

    void setRows(List<Task> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    Task getTask(int row) {
        return rows.get(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case DONE: return Boolean.class;
            case PRIORITY: return Priority.class;
            case DUE:
            case CREATED: return java.time.LocalDate.class;
            default: return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == DONE;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Task t = rows.get(row);
        switch (column) {
            case DONE: return t.isDone();
            case TITLE: return t.getTitle();
            case PRIORITY: return t.getPriority();
            case DUE: return t.getDueDate();
            case CREATED: return t.getCreated();
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == DONE) {
            taskList.setDone(rows.get(row), (Boolean) value);
        }
    }
}
