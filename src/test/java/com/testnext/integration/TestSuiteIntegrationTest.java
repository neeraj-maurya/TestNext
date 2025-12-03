package com.testnext.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.testnext.api.dto.ProjectDto;
import com.testnext.api.dto.TenantDto;
import com.testnext.api.dto.TestSuiteDto;
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

import com.testnext.config.TestAppConfig;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TestAppConfig.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("dev")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
public class TestSuiteIntegrationTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        private Long tenantId;
        private Long projectId;
        private UUID testManagerId;
        private String testManagerUsername;

        @BeforeEach
        void setup() throws Exception {
                this.mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                                .webAppContextSetup(this.webApplicationContext)
                                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                                                .springSecurity())
                                .build();

                // Debug: Print existing users
                try {
                        java.util.List<java.util.Map<String, Object>> users = new org.springframework.jdbc.core.JdbcTemplate(
                                        webApplicationContext.getBean(javax.sql.DataSource.class))
                                        .queryForList("SELECT * FROM system_users");
                        System.out.println("DEBUG: Existing users: " + users);
                } catch (Exception e) {
                        System.out.println("DEBUG: Failed to query users: " + e.getMessage());
                }

                // 1. Create Test Manager (via API as SYSTEM_ADMIN)
                SystemUser tm = new SystemUser();
                tm.setUsername("tm_suite_test_" + UUID.randomUUID());
                tm.setEmail("tm_" + UUID.randomUUID() + "@example.com");
                tm.setHashedPassword("{noop}pass");
                tm.setRole("ROLE_TEST_MANAGER");

                MvcResult uRes = mockMvc.perform(post("/api/system/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tm))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                                .andExpect(status().isOk())
                                .andReturn();

                SystemUser createdTm = objectMapper.readValue(uRes.getResponse().getContentAsString(),
                                SystemUser.class);
                testManagerId = createdTm.getId();
                testManagerUsername = createdTm.getUsername();

                // 2. Create Tenant (as Admin)
                TenantDto t = new TenantDto();
                t.name = "Suite Test Tenant " + UUID.randomUUID();
                t.schemaName = "suite_test_schema_" + UUID.randomUUID();
                t.testManagerId = testManagerId;

                MvcResult tRes = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(t))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto createdTenant = objectMapper.readValue(tRes.getResponse().getContentAsString(),
                                TenantDto.class);
                tenantId = createdTenant.id;

                // 3. Create Project (as Test Manager)
                ProjectDto p = new ProjectDto();
                p.name = "Suite Test Project";
                p.description = "Project for Suite Integration Test";

                MvcResult pRes = mockMvc.perform(post("/api/tenants/" + tenantId + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(p))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                ProjectDto createdProject = objectMapper.readValue(pRes.getResponse().getContentAsString(),
                                ProjectDto.class);
                projectId = createdProject.id;
        }

        @Test
        void testSuiteLifecycle() throws Exception {
                // 1. Create Suite
                TestSuiteDto s = new TestSuiteDto();
                s.name = "Integration Suite";
                s.description = "Created via Integration Test";

                MvcResult sRes = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(s))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto createdSuite = objectMapper.readValue(sRes.getResponse().getContentAsString(),
                                TestSuiteDto.class);

                // 2. List Suites
                mockMvc.perform(get("/api/projects/" + projectId + "/suites")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(createdSuite.id))
                                .andExpect(jsonPath("$[0].name").value("Integration Suite"));
        }

        @Test
        void testAccessControl() throws Exception {
                // Other user should not be able to create suite in this project
                TestSuiteDto s = new TestSuiteDto();
                s.name = "Unauthorized Suite";

                mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(s))
                                .with(jwt().jwt(builder -> builder.subject("other_user").claim("username",
                                                "other_user"))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isForbidden());
        }
}
