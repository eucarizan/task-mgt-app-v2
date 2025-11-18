package dev.nj.tms.comment;

import dev.nj.tms.task.TaskNotFoundException;
import dev.nj.tms.task.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.commentMapper = commentMapper;
    }

    @Override
    public CommentResponse createComment(Long taskId, String text, String author) {
        logger.debug("Attempting to create comment on task {} by {}", taskId, author);

        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        Comment comment = new Comment(taskId, text, author);
        Comment savedComment = commentRepository.save(comment);

        logger.debug("Successfully created comment {} on task {}", savedComment.getId(), taskId);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        logger.debug("Attempting to get comments for task {}", taskId);

        taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + taskId));

        List<CommentResponse> comments = commentRepository
                .findAllByTaskId(taskId, Sort.by(Sort.Direction.DESC, "created"))
                .stream()
                .map(commentMapper::toResponse)
                .toList();

        logger.debug("Successfully retrieved {} comments for task {}", comments.size(), taskId);
        return comments;
    }
}
