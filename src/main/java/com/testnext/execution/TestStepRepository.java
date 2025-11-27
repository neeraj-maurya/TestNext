package com.testnext.execution;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TestStepRepository {
    private final JdbcTemplate jdbc;

    public TestStepRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<TestStep> findStepsByTestId(UUID testId) {
        // Simplified: select basic fields; expects tenant search_path to be set
        return jdbc.query("select id, step_definition_id, parameters_json from test_steps where test_id = ? order by step_order", (rs, rowNum) -> {
            TestStep ts = new TestStep();
            ts.setId(rs.getString("id"));
            ts.setStepDefinitionId(rs.getString("step_definition_id"));
            ts.setParameters(rs.getString("parameters_json") == null ? Map.of() : Map.of("params", rs.getString("parameters_json")));
            return ts;
        }, testId);
    }

    public TestStep findStepByExecutionStepId(UUID executionStepId) {
        // map execution_steps.test_step_id -> test_steps
        UUID testStepId = jdbc.queryForObject("select test_step_id from execution_steps where id = ?", UUID.class, executionStepId);
        return jdbc.queryForObject("select id, step_definition_id, parameters_json from test_steps where id = ?", (rs, rowNum) -> {
            TestStep ts = new TestStep();
            ts.setId(rs.getString("id"));
            ts.setStepDefinitionId(rs.getString("step_definition_id"));
            ts.setParameters(rs.getString("parameters_json") == null ? Map.of() : Map.of("params", rs.getString("parameters_json")));
            return ts;
        }, testStepId);
    }

    public List<TestStep> findDependentStepsRecursively(List<String> startStepIds) {
        if (startStepIds == null || startStepIds.isEmpty()) return List.of();
        List<TestStep> out = new ArrayList<>();
        Set<String> seen = new HashSet<>(startStepIds);
        Queue<String> q = new ArrayDeque<>(startStepIds);

        while (!q.isEmpty()) {
            List<String> batch = new ArrayList<>();
            // collect up to 50 ids for IN () clause
            while (!q.isEmpty() && batch.size() < 50) batch.add(q.poll());
            String inClause = String.join(",", java.util.Collections.nCopies(batch.size(), "?"));
            String sql = "select id, step_definition_id, parameters_json from test_steps where depends_on_step_id in (" + inClause + ")";
            List<Object> params = new ArrayList<>(batch);
            List<TestStep> found = jdbc.query(sql, (rs, rowNum) -> {
                TestStep ts = new TestStep();
                ts.setId(rs.getString("id"));
                ts.setStepDefinitionId(rs.getString("step_definition_id"));
                ts.setParameters(rs.getString("parameters_json") == null ? Map.of() : Map.of("params", rs.getString("parameters_json")));
                return ts;
            }, params.toArray());
            for (TestStep t : found) {
                if (seen.add(t.getId())) {
                    out.add(t);
                    q.add(t.getId());
                }
            }
        }
        return out;
    }
}
