package dev.nj.tms.task;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
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
    public ResponseEntity<List<TaskListResponse>> getTasks(@RequestParam(name = "author", required = false) String author,
                                                       @RequestParam(name = "assignee", required = false) String assignee) {
        logger.info("Received request to get tasks, author filter: [{}], assignee filter: [{}]", author, assignee);
        List<TaskListResponse> tasks;

        if (author != null && assignee != null) {
            tasks = taskService.getTasksByAuthorAndAssignee(author, assignee);
        } else if (author != null) {
            tasks = taskService.getTasksByAuthor(author);
        } else if (assignee != null) {
            tasks = taskService.getTasksByAssignee(assignee);
        } else {
            tasks = taskService.getTasks();
        }

        logger.info("Returning {} tasks", tasks.size());
        return ResponseEntity.ok(tasks);
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

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long taskId,
                                                   @Valid @RequestBody AssignTaskRequest request,
                                                   Principal principal) {
        String authorEmail = principal.getName().toLowerCase(Locale.ROOT);
        logger.info("Received request to assign task {} to {} by {}", taskId, request.assignee(), authorEmail);
        TaskResponse response = taskService.assignTask(taskId, request.assignee(), authorEmail);
        logger.info("Successfully assigned task {} to {}", taskId, request.assignee());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long taskId,
                                                         @Valid @RequestBody UpdateTaskStatusRequest request,
                                                         Principal principal) {
        String authorEmail = principal.getName().toLowerCase(Locale.ROOT);
        logger.info("Received request to update task {} stats to {} by {}", taskId, request.status(), authorEmail);

        TaskStatus status;
        try {
            status = TaskStatus.valueOf(request.status());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value. Must be one of: CREATED, IN_PROGRESS, COMPLETED");
        }

        TaskResponse response = taskService.updateTaskStatus(taskId, status, authorEmail);
        logger.info("Successfully updated task {} status to {}", taskId, status);
        return ResponseEntity.ok(response);
    }
}
