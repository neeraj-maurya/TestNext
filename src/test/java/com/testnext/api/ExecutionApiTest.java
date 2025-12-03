package com.testnext.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testnext.api.dto.ProjectDto;
import com.testnext.api.dto.TenantDto;
import com.testnext.api.dto.TestDto;
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
public class ExecutionApiTest {

        private MockMvc mockMvc;

        @Autowired
        private WebApplicationContext webApplicationContext;

        private ObjectMapper objectMapper = new ObjectMapper();
        private Long projectId;
        private Long suiteId;
        private Long testId;
        private String testManagerUsername;

        @BeforeEach
        void setup() throws Exception {
                this.mockMvc = webAppContextSetup(this.webApplicationContext)
                                .apply(springSecurity())
                                .build();

                // Create test manager
                SystemUser testManager = new SystemUser();
                testManager.setUsername("tm_exec_" + UUID.randomUUID());
                testManager.setEmail("tm_exec@example.com");
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
                tenant.name = "Exec Test Tenant";
                tenant.schemaName = "exec_schema_" + UUID.randomUUID().toString().replace("-", "");
                tenant.testManagerId = createdManager.getId();

                MvcResult tenantResult = mockMvc.perform(post("/api/tenants")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tenant))
                                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TenantDto createdTenant = objectMapper.readValue(tenantResult.getResponse().getContentAsString(),
                                TenantDto.class);

                // Create project
                ProjectDto project = new ProjectDto();
                project.name = "Exec Test Project";

                MvcResult projectResult = mockMvc.perform(post("/api/tenants/" + createdTenant.id + "/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                ProjectDto createdProject = objectMapper.readValue(projectResult.getResponse().getContentAsString(),
                                ProjectDto.class);
                projectId = createdProject.id;

                // Create suite
                TestSuiteDto suite = new TestSuiteDto();
                suite.name = "Exec Test Suite";

                MvcResult suiteResult = mockMvc.perform(post("/api/projects/" + projectId + "/suites")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(suite))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestSuiteDto createdSuite = objectMapper.readValue(suiteResult.getResponse().getContentAsString(),
                                TestSuiteDto.class);
                suiteId = createdSuite.id;

                // Create test case
                TestDto testCase = new TestDto();
                testCase.name = "Exec Test Case";

                MvcResult testResult = mockMvc.perform(post("/api/test-suites/" + suiteId + "/tests")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testCase))
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                TestDto createdTest = objectMapper.readValue(testResult.getResponse().getContentAsString(),
                                TestDto.class);
                testId = createdTest.id;
        }

        @Test
        void testStartTestExecution() throws Exception {
                mockMvc.perform(post("/api/tests/" + testId + "/executions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.status").exists());
        }

        @Test
        void testStartSuiteExecution() throws Exception {
                mockMvc.perform(post("/api/executions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"suiteId\": " + suiteId + "}")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testListExecutions() throws Exception {
                mockMvc.perform(get("/api/executions")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testListExecutionsByProject() throws Exception {
                mockMvc.perform(get("/api/projects/" + projectId + "/executions")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray());
        }

        @Test
        void testGetExecution() throws Exception {
                // Start an execution first
                MvcResult execResult = mockMvc.perform(post("/api/tests/" + testId + "/executions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                String executionId = objectMapper.readTree(execResult.getResponse().getContentAsString()).get("id")
                                .asText();

                // Get execution
                mockMvc.perform(get("/api/executions/" + executionId)
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(executionId));
        }

        @Test
        void testDeleteExecution() throws Exception {
                // Start an execution first
                MvcResult execResult = mockMvc.perform(post("/api/tests/" + testId + "/executions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk())
                                .andReturn();

                String executionId = objectMapper.readTree(execResult.getResponse().getContentAsString()).get("id")
                                .asText();

                // Delete execution
                mockMvc.perform(delete("/api/executions/" + executionId)
                                .with(jwt().jwt(builder -> builder.subject(testManagerUsername).claim("username",
                                                testManagerUsername))
                                                .authorities(new SimpleGrantedAuthority("ROLE_TEST_MANAGER"))))
                                .andExpect(status().isOk());
        }
}
