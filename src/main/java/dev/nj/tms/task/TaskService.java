package dev.nj.tms.task;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(String title, String description, String author);

    List<TaskResponse> getTasks();

    List<TaskResponse> getTasksByAuthor(String author);

    List<TaskResponse> getTasksByAssignee(String assignee);

    List<TaskResponse> getTasksByAuthorAndAssignee(String author, String assignee);

    TaskResponse assignTask(Long taskId, String assigneeEmail, String authorEmail);

    TaskResponse updateTaskStatus(Long taskId, TaskStatus status, String authorEmail);
}
