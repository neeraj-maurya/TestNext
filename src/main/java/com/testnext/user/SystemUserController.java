package com.testnext.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/system/users")
public class SystemUserController {

    private final SystemUserService svc;

    public SystemUserController(SystemUserService svc) {
        this.svc = svc;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public List<SystemUser> listSystemUsers() {
        return svc.list();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public SystemUser create(@RequestBody SystemUser in) {
        return svc.create(in);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public SystemUser update(@PathVariable UUID id, @RequestBody SystemUser in) {
        return svc.update(id, in);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SYSTEM_ADMIN')")
    public void delete(@PathVariable UUID id) {
        svc.delete(id);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public SystemUser me() {
        // placeholder - map principal to SystemUser
        return new SystemUser();
    }
}
