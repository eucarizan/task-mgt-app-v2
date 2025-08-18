package dev.nj.tms.task;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Locale;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public String getTasks() {
        return "tasks";
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest taskRequest,
                                                   Principal principal) {
        String author = principal.getName().toLowerCase(Locale.ROOT);
        logger.info("Received request to create a task by: {}", author);
        TaskResponse response = taskService.createTask(taskRequest.title(), taskRequest.description(), author);
        logger.info("Successfully created task with id {} by: {}", response.id(), author);
        return ResponseEntity.ok(response);
    }
}
