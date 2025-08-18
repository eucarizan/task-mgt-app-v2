package dev.nj.tms.task;

public interface TaskService {
    TaskResponse createTask(String title, String description, String author);
}
