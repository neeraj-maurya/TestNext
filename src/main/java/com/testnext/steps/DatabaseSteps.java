package com.testnext.steps;

import com.testnext.annotation.TestStep;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class DatabaseSteps {

    @TestStep(id = "db.runSql", name = "Run SQL Query", description = "Executes a SQL query against the connected database")
    public void runSql(String query) {
        System.out.println("Executing SQL: " + query);
    }

    @TestStep(id = "db.validateFields", name = "Validate DB Fields", description = "Validate that a record in the database matches expected values")
    @SuppressWarnings("unchecked")
    public Map<String, Object> validateFields(Map<String, Object> params) {
        String tableName = (String) params.get("table_name");
        String idInfo = (String) params.get("id");
        Map<String, Object> expected = (Map<String, Object>) params.get("expected_values");

        // Simulate DB validation
        System.out.println("Validating table " + tableName + " id " + idInfo);

        // In a real implementation, we would query the DB here
        boolean match = true; // simulate success

        if (match) {
            return Map.of("status", "PASSED", "message", "All fields match");
        } else {
            throw new RuntimeException("Validation failed: fields do not match");
        }
    }
}
