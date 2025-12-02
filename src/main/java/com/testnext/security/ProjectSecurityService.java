package com.testnext.security;

import com.testnext.project.entity.ProjectEntity;
import com.testnext.repository.ProjectRepository;
import com.testnext.user.SystemUser;
import com.testnext.user.SystemUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("projectSecurity")
public class ProjectSecurityService {

    private final ProjectRepository projectRepo;
    private final TenantSecurityService tenantSecurity;

    public ProjectSecurityService(ProjectRepository projectRepo, TenantSecurityService tenantSecurity) {
        this.projectRepo = projectRepo;
        this.tenantSecurity = tenantSecurity;
    }

    public boolean hasAccess(Authentication auth, Long projectId) {
        if (auth == null || !auth.isAuthenticated())
            return false;

        Optional<ProjectEntity> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty())
            return false;

        ProjectEntity project = projectOpt.get();
        // Delegate to TenantSecurityService using the project's tenantId
        return tenantSecurity.hasAccess(auth, project.getTenantId());
    }
}
