package com.lb_calc_web.repository.specification;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogSpecificationsTest {

    @Test
    void equal_shouldIgnoreBlankString() {
        assertNull(CatalogSpecifications.equal("type", ""));
        assertNull(CatalogSpecifications.equal("type", "   "));
    }

    @Test
    void equal_shouldCreateSpecificationForNonBlankString() {
        assertNotNull(CatalogSpecifications.equal("type", "TYPE1"));
        assertNotNull(CatalogSpecifications.equal("type", " TYPE1 "));
    }

    @Test
    void equal_shouldIgnoreNullValue() {
        assertNull(CatalogSpecifications.equal("type", null));
    }
}
