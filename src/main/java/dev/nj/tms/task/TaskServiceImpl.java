package dev.nj.tms.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskResponse> getTasks() {
        logger.debug("Attempting to list tasks");
        List<TaskResponse> tasks = taskRepository.findAll().stream().map(taskMapper::toResponse).toList();
        logger.debug("Successfully list tasks: {}", tasks.size());
        return tasks;
    }

    @Override
    public TaskResponse createTask(String title, String description, String author) {
        logger.debug("Attempting to create a task by: {}", author);
        Task task = taskRepository.save(new Task(title, description, author.toLowerCase()));
        TaskResponse response = taskMapper.toResponse(task);
        logger.debug("Successfully create a task with id {} by: {}", response.id(), author);
        return response;
    }
}
