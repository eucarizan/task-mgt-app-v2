package dev.nj.tms.task;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    public Task() {}

    public Task(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
