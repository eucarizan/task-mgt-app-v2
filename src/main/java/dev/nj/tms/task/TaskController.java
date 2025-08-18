package dev.nj.tms.task;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @GetMapping
    public String getTasks() {
        return "tasks";
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest taskRequest,
                                                   Principal principal) {
        String author = principal.getName().toLowerCase(Locale.ROOT);
        TaskResponse response = new TaskResponse(
                UUID.randomUUID().toString(),
                taskRequest.title(),
                taskRequest.description(),
                TaskStatus.CREATED.name(),
                author
        );
        return ResponseEntity.ok(response);
    }
}
