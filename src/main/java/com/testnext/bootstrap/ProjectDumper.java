package com.testnext.bootstrap;

import com.testnext.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProjectDumper implements CommandLineRunner {

    private final ProjectRepository repo;

    public ProjectDumper(ProjectRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== PROJECT DUMP START ===");
        repo.findAll().forEach(p -> {
            System.out.println("Project: " + p.getName() +
                    " | ID: " + p.getId() +
                    " | TenantID: " + p.getTenantId() +
                    " | AssignedUsers: " + p.getAssignedUserIds().size());
        });
        System.out.println("=== PROJECT DUMP END ===");
    }
}
