package dev.nj.tms.task;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(String title, String description, String author);
    List<TaskResponse> getTasks();
}
