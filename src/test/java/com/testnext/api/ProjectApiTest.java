package com.testnext.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.api.dto.ProjectDto;
import com.testnext.api.dto.TenantDto;
import com.testnext.user.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import org.springframework.test.context.TestPropertySource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = com.testnext.config.TestAppConfig.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class ProjectApiTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper();
        private Long tenantId;
        private String testManagerUsername;

        @BeforeEach
        void setup() throws Exception {
                this.mockMvc = webAppContextSetup(this.webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Create test manager
                SystemUser testManager = new SystemUser();
                testManager.setUsername("tm_project_" + UUID.randomUUID());
                testManager.setEmail("tm_project@example.com");
                testManager.setHashedPassword("password123");
                testManager.setRole("ROLE_TEST_MANAGER");

                MvcResult userResult = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testManager))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdManager = objectMapper.readValue(userResult.getResponse().getContentAsString(),
                                SystemUser.class);
                testManagerUsername = createdManager.getUsername();

                // Create tenant
                TenantDto tenant = new TenantDto();
                tenant.name = "Project Test Tenant";
                tenant.schemaName = "project_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = createdManager.getId();

                MvcResult tenantResult = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto createdTenant = objectMapper.readValue(tenantResult.getResponse().getContentAsString(),
                                TenantDto.class);
                tenantId = createdTenant.id;
        }

        @Test
        void testCreateProject() throws Exception {
                ProjectDto project = new ProjectDto();
                project.name = "Test Project";
                project.description = "Test project description";

                mockMvc.perform(post("/api/tenants/" + tenantId + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.name").value("Test Project"))
                                .andExpect(jsonPath("$.description").value("Test project description"));
        }

        @Test
        void testListProjects() throws Exception {
                // Create a project first
                ProjectDto project = new ProjectDto();
                project.name = "List Test Project";
                project.description = "For listing test";

                mockMvc.perform(post("/api/tenants/" + tenantId + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk());

                // List projects
                mockMvc.perform(get("/api/tenants/" + tenantId + "/projects")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].name").value("List Test Project"));
        }

        @Test
        void testAccessControl() throws Exception {
                ProjectDto project = new ProjectDto();
                project.name = "Unauthorized Project";

                // Different user should not be able to create project
                mockMvc.perform(post("/api/tenants/" + tenantId + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project))
                                .with(jwt().jwt(builder -> builder.subject("other_user").claim("username",
                                                "other_user"))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isForbidden());
        }
}
