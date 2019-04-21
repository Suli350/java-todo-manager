# To-Do Manager (Java / Swing)

A desktop task manager with priorities, due dates, filters, live search and
automatic saving to a CSV file.

## Features

- Add, edit (double-click) and delete (Delete key) tasks, tick them off in the table
- Priorities (Low / Medium / High) and optional due dates
- Due date shortcuts: `today`, `tomorrow`, `+3` (in 3 days) or `2019-12-24`
- Filters: All, Active, Completed, Due today, Overdue
- Live search over titles and notes
- Smart ordering: active first, then soonest due, then highest priority
- Overdue tasks in red, high priority in bold, done tasks greyed out
- Click a column header to sort by it
- Autosave after every change to `~/.todo-manager/tasks.csv`
  (atomic write, proper CSV quoting for commas, quotes and newlines)
- `Ctrl/Cmd + N` for a new task

## Structure

```
src/main/java/io/github/suli350/todo/
├── Main.java            loads the store and opens the window
├── TodoFrame.java       main window: toolbar, table, status bar
├── TaskDialog.java      add/edit form with date parsing
├── TaskTableModel.java  JTable model
├── TaskList.java        in-memory model with change listeners
├── CsvTaskStore.java    CSV persistence
└── Task.java, Priority.java, Filter.java
```

The model (`TaskList`, `Task`, `CsvTaskStore`) has no Swing code, so it is
fully unit tested.

## Build and run

```bash
mvn test
mvn package
java -jar target/todo-manager-1.0.0.jar                 # default file
java -jar target/todo-manager-1.0.0.jar my-tasks.csv    # custom file
```

## Ideas for extending it

- Tags / projects and a sidebar to filter by them
- Recurring tasks
- Desktop notifications for tasks due today
