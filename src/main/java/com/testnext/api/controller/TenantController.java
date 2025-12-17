package com.testnext.api.controller;

import com.testnext.api.dto.TenantDto;
import com.testnext.service.TenantService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService svc;
    private final com.testnext.user.SystemUserRepository userRepo;

    public TenantController(TenantService svc, com.testnext.user.SystemUserRepository userRepo) {
        this.svc = svc;
        this.userRepo = userRepo;
    }

    @PostMapping
    public TenantDto create(@RequestBody TenantDto in) {
        return svc.create(in.name, in.schemaName, in.testManagerId, in.active);
    }

    @GetMapping
    public List<TenantDto> list() {
        return svc.list();
    }

    @PutMapping("/{id}")
    public TenantDto update(@PathVariable Long id, @RequestBody TenantDto in) {
        return svc.update(id, in.name, in.schemaName, in.testManagerId, in.active);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }

    @GetMapping("/{id}/users")
    @org.springframework.security.access.prepost.PreAuthorize("@tenantSecurity.hasAccess(authentication, #id) or hasRole('SYSTEM_ADMIN')")
    public java.util.List<com.testnext.user.SystemUser> listUsers(@PathVariable Long id) {
        return userRepo.findByTenantId(id);
    }
}
