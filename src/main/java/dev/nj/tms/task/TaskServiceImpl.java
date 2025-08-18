package dev.nj.tms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public String getAllTasks() {
        return "tasks";
    }

    @Override
    public TaskResponse createTask(String title, String description, String author) {
        logger.debug("Attempting to create a task by: {}", author);
        Task task = taskRepository.save(new Task(title, description, author));
        TaskResponse response = new TaskResponse(task.getId().toString(), task.getTitle(), task.getDescription(), task.getStatus().toString(), task.getAuthor());
        logger.debug("Successfully create a task with id {} by: {}", response.id(), author);
        return response;
    }
}
