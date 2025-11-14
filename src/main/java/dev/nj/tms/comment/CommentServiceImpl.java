package dev.nj.tms.comment;

import dev.nj.tms.task.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        // TODO: implement
        return null;
    }
}
