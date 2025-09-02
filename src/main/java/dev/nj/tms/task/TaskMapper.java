package dev.nj.tms.task;

import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getAuthor()
        );
    }
}
