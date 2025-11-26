package com.testnext.tenant;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaNameValidatorTest {
    @Test
    public void testSanitize() {
        SchemaNameValidator v = new SchemaNameValidator(null);
        String s = v.sanitize("Acme Corp!@#");
        assertTrue(s.contains("acme"));
    }

    @Test
    public void testPatternValid() {
        SchemaNameValidator v = new SchemaNameValidator(null);
        assertTrue(v.patternValid("acme_testnext"));
        assertFalse(v.patternValid("../bad"));
    }
}
