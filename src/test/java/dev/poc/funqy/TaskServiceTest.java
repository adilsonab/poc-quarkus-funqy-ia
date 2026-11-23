package dev.poc.funqy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TaskServiceTest {
    @Inject
    TaskService taskService;

    @Inject
    DataSource dataSource;

    @BeforeEach
    void clearTasks() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                var statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM tasks");
        }
    }

    @Test
    void createsAndListsTasksFromPostgreSQL() {
        Task created = taskService.createTask("  Learn Funqy  ");

        assertTrue(created.getId() > 0);
        assertEquals("Learn Funqy", created.getTitle());
        assertEquals(1, taskService.listTasks().size());
        assertEquals(created.getId(), taskService.listTasks().getFirst().getId());
    }

    @Test
    void rejectsBlankTitles() {
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask("   "));
    }

    @Test
    void rejectsTitlesLongerThanTheDatabaseColumn() {
        assertThrows(IllegalArgumentException.class, () -> taskService.createTask("x".repeat(256)));
    }

    @Test
    void exposesTaskFunctionsOverFunqyHttpWithAngularCors() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest createRequest = HttpRequest.newBuilder(URI.create("http://localhost:8081/createTask"))
                .header("Content-Type", "application/json")
                .header("Origin", "http://localhost:4200")
                .POST(HttpRequest.BodyPublishers.ofString("{\"title\":\"From Funqy\"}"))
                .build();

        HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, createResponse.statusCode());
        assertTrue(createResponse.body().contains("\"title\":\"From Funqy\""));
        assertEquals("http://localhost:4200",
                createResponse.headers().firstValue("Access-Control-Allow-Origin").orElseThrow());

        HttpRequest listRequest = HttpRequest.newBuilder(URI.create("http://localhost:8081/listTasks"))
                .header("Origin", "http://localhost:4200")
                .GET()
                .build();
        HttpResponse<String> listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, listResponse.statusCode());
        assertTrue(listResponse.body().contains("\"title\":\"From Funqy\""));
    }
}
