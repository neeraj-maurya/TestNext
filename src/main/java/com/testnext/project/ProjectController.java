package com.testnext.project;

import com.testnext.permissions.ProjectPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Duplicate controller removed from component-scan to avoid bean name conflicts.
// This class used to expose project update endpoints but the canonical API controller
// is located under `com.testnext.api.controller.ProjectController`.
// Keeping this placeholder file (non-controller) to preserve any shared helper logic if needed.
public class ProjectController {
    // Intentionally left non-annotated. If you need to restore behavior, migrate logic
    // into `com.testnext.api.controller.ProjectController` and delete this placeholder.
}
