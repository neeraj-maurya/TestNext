package com.testnext.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@org.springframework.boot.persistence.autoconfigure.EntityScan(basePackages = "com.testnext")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackages = "com.testnext")
@ComponentScan(basePackages = "com.testnext", excludeFilters = {
    // exclude the execution package (both the package itself and any subpackages)
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.testnext\\.execution(\\..*)?"),
    @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.testnext\\.api\\.controller\\.ExecutionController")
})
public class TestAppConfig {
    // Test-specific application configuration that excludes execution package to avoid bean name conflicts
}
