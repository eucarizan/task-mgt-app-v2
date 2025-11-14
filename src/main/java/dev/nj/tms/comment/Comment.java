package dev.nj.tms.comment;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private String author;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime created;

    public Comment() {};

    public Comment(Long taskId, String text, String author) {
        this.taskId = taskId;
        this.text = text;
        this.author = author;
        this.created = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDateTime getCreated() {
        return created;
    }
}
