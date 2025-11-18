package dev.nj.tms.task;

import dev.nj.tms.comment.CommentRepository;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final CommentRepository commentRepository;

    public TaskMapper(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getAuthor(),
                task.getAssignee() != null ? task.getAssignee() : "none"
        );
    }

    public TaskListResponse toListResponse(Task task) {
        int totalComments = (int) commentRepository.countByTaskId(task.getId());
        return new TaskListResponse(
                task.getId().toString(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getAuthor(),
                task.getAssignee() != null ? task.getAssignee() : "none",
                totalComments
        );
    }
}
