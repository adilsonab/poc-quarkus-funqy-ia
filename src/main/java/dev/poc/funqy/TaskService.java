package dev.poc.funqy;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TaskService {
    private static final String INSERT_TASK_SQL = "INSERT INTO tasks (title) VALUES (?) RETURNING id";
    private static final String LIST_TASKS_SQL = "SELECT id, title FROM tasks ORDER BY id";
    private static final int MAX_TITLE_LENGTH = 255;

    @Inject
    DataSource dataSource;

    public List<Task> listTasks() {
        List<Task> tasks = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(LIST_TASKS_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tasks.add(new Task(resultSet.getLong("id"), resultSet.getString("title")));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not load tasks from PostgreSQL", exception);
        }

        return tasks;
    }

    public Task createTask(String title) {
        String normalizedTitle = title == null ? "" : title.strip();
        if (normalizedTitle.isEmpty()) {
            throw new IllegalArgumentException("Task title must not be blank");
        }
        if (normalizedTitle.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("Task title must not exceed 255 characters");
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_TASK_SQL)) {
            statement.setString(1, normalizedTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("PostgreSQL did not return the new task id");
                }
                return new Task(resultSet.getLong("id"), normalizedTitle);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not save task to PostgreSQL", exception);
        }
    }
}
