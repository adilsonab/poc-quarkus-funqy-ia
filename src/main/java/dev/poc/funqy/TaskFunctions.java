package dev.poc.funqy;

import io.quarkus.funqy.Funq;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class TaskFunctions {
    @Inject
    TaskService taskService;

    @Funq("listTasks")
    public List<Task> listTasks() {
        return taskService.listTasks();
    }

    @Funq("createTask")
    public Task createTask(CreateTaskInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Task input is required");
        }
        return taskService.createTask(input.getTitle());
    }
}
