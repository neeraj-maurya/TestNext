package com.testnext.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/users")
public class SystemUserController {

    // In a real app inject a SystemUserService that talks to public.system_users

    @GetMapping
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public List<SystemUser> listSystemUsers() {
        // placeholder - return empty list
        return List.of();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public SystemUser me() {
        // placeholder - map principal to SystemUser
        return new SystemUser();
    }
}
