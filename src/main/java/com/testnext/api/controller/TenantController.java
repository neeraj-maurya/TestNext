package com.testnext.api.controller;

import com.testnext.api.dto.TenantDto;
import com.testnext.service.TenantService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {
    private final TenantService svc;

    public TenantController(TenantService svc) {
        this.svc = svc;
    }

    @PostMapping
    public TenantDto create(@RequestBody TenantDto in) {
        return svc.create(in.name, in.schemaName);
    }

    @GetMapping
    public List<TenantDto> list() { return svc.list(); }
}
