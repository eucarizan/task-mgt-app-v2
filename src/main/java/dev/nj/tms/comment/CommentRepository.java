package dev.nj.tms.comment;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends ListCrudRepository<Comment, Long> {
    List<Comment> findAllByTaskId(Long taskId, Sort sort);

    long countByTaskId(Long taskId);
}
