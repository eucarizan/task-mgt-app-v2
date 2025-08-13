package dev.nj.tms.task;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    public String getAllTasks() {
        return "tasks";
    }
}
