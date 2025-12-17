package com.testnext.bootstrap;

import com.testnext.user.SystemUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserDumper implements CommandLineRunner {

    private final SystemUserRepository repo;

    public UserDumper(SystemUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== USER DUMP START ===");
        repo.findAll().forEach(u -> {
            System.out.println("User: " + u.getUsername() +
                    " | Role: " + u.getRole() +
                    " | Active: " + u.isActive() +
                    " | TenantID: " + u.getTenantId() +
                    " | Pass: " + u.getHashedPassword());
        });
        System.out.println("=== USER DUMP END ===");
    }
}
