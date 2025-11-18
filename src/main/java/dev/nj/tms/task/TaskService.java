package dev.nj.tms.task;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(String title, String description, String author);

    List<TaskListResponse> getTasks();

    List<TaskListResponse> getTasksByAuthor(String author);

    List<TaskListResponse> getTasksByAssignee(String assignee);

    List<TaskListResponse> getTasksByAuthorAndAssignee(String author, String assignee);

    TaskResponse assignTask(Long taskId, String assigneeEmail, String authorEmail);

    TaskResponse updateTaskStatus(Long taskId, TaskStatus status, String authorEmail);
}
