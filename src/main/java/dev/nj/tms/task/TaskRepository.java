package dev.nj.tms.task;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends ListCrudRepository<Task, Long>, ListPagingAndSortingRepository<Task, Long> {
    List<Task> findAllByAuthorIgnoreCase(String author, Sort sort);

    List<Task> findAllByAssigneeIgnoreCase(String assignee, Sort sort);

    List<Task> findAllByAuthorIgnoreCaseAndAssigneeIgnoreCase(String author, String assignee, Sort sort);
}
