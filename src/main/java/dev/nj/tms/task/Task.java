package dev.nj.tms.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private String author;

    private String assignee;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonIgnore
    private LocalDateTime created;

    public Task() {}

    public Task(String title, String description, String author) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.CREATED;
        this.author = author;
        this.assignee = null;
        this.created = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public LocalDateTime getCreated() {
        return created;
    }
}
