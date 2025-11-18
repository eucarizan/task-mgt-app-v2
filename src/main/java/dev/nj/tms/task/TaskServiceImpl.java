package dev.nj.tms.task;

import dev.nj.tms.account.AccountNotFoundException;
import dev.nj.tms.account.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, AccountRepository accountRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.accountRepository = accountRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskListResponse> getTasks() {
        logger.debug("Attempting to list tasks");
        List<TaskListResponse> tasks = taskRepository
                .findAll(Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(taskMapper::toListResponse)
                .toList();
        logger.debug("Successfully list tasks: {}", tasks.size());
        return tasks;
    }

    @Override
    public List<TaskListResponse> getTasksByAuthor(String author) {
        logger.debug("Attempting to list tasks by author: {}", author);

        if (!isValidAuthorFormat(author)) {
            logger.warn("Invalid author format provided: {}", author);
            throw new IllegalArgumentException("Author must be in a valid format");
        }

        List<TaskListResponse> tasks = taskRepository
                .findAllByAuthorIgnoreCase(author, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(taskMapper::toListResponse)
                .toList();

        logger.debug("Successfully list tasks by author: {}", author);
        return tasks;
    }

    @Override
    public List<TaskListResponse> getTasksByAssignee(String assignee) {
        logger.debug("Attempting to list tasks by assignee: {}", assignee);

        if(!isValidAuthorFormat(assignee)) {
            logger.warn("Invalid assignee format provided: {}", assignee);
            throw new IllegalArgumentException("Assignee must be in a valid format");
        }

        List<TaskListResponse> tasks = taskRepository
                .findAllByAssigneeIgnoreCase(assignee, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(taskMapper::toListResponse)
                .toList();

        logger.debug("Successfully list tasks by assignee: {}", assignee);
        return tasks;
    }

    @Override
    public List<TaskListResponse> getTasksByAuthorAndAssignee(String author, String assignee) {
        logger.debug("Attempting to list tasks by author: {} and assignee: {}", author, assignee);

        if (!isValidAuthorFormat(author)) {
            logger.warn("Invalid author format: {}", author);
            throw new IllegalArgumentException("Author must be in valid format");
        }

        if (!isValidAuthorFormat(assignee)) {
            logger.warn("Invalid assignee format: {}", assignee);
            throw new IllegalArgumentException("Assignee must be in valid format");
        }

        List<TaskListResponse> tasks = taskRepository
                .findAllByAuthorIgnoreCaseAndAssigneeIgnoreCase(author, assignee, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(taskMapper::toListResponse)
                .toList();

        logger.debug("Successfully list tasks by author: {} and assigne: {}", author, assignee);
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

    @Override
    public TaskResponse assignTask(Long taskId, String assigneeEmail, String authorEmail) {
        logger.debug("Attempting to assign task {} to {}", taskId, assigneeEmail);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        if (!task.getAuthor().equals(authorEmail)) {
            throw new ForbiddenException("Only task author can assign tasks");
        }

        if ("none".equals(assigneeEmail)) {
            task.setAssignee(null);
        } else {
            if (!accountRepository.existsByEmailIgnoreCase(assigneeEmail)) {
                throw new AccountNotFoundException("Assignee not found with email: " + assigneeEmail);
            }
            task.setAssignee(assigneeEmail);
        }

        Task savedTask = taskRepository.save(task);

        logger.debug("Successfully assigned task {} to {}", taskId, assigneeEmail);
        return taskMapper.toResponse(savedTask);
    }

    @Override
    public TaskResponse updateTaskStatus(Long taskId, TaskStatus status, String authorEmail) {
        logger.debug("Attempting to update task {} status to {}", taskId, status);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        boolean isAuthor = task.getAuthor().equals(authorEmail);
        boolean isAssignee = task.getAssignee() != null && task.getAssignee().equals(authorEmail);

        if (!isAuthor && !isAssignee) {
            throw new ForbiddenException("Only task author or assignee can update task status");
        }

        task.setStatus(status);
        Task savedTask = taskRepository.save(task);

        logger.debug("Successfully update task {} status to {}", taskId, status);
        return taskMapper.toResponse(savedTask);
    }

    private boolean isValidAuthorFormat(String author) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9][A-Za-z0-9-]*(\\.[A-Za-z0-9]{2,})+$";
        return author != null && author.matches(emailRegex);
    }
}
