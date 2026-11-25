package com.lb_calc_web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Проверяет ограничения серверной пагинации и whitelist сортировки. */
class CatalogPageSupportTest {

    @Test
    void pageable_shouldClampPageSizeAndRejectUnsupportedSort() {
        Pageable pageable = CatalogPageSupport.pageable(
                -4, 500, "functionalConfiguration.access", "desc",
                "id", Set.of("id", "name"));

        assertEquals(0, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC, "id"), pageable.getSort());
    }

    @Test
    void pageable_shouldAddStableIdOrderAfterSelectedSort() {
        Pageable pageable = CatalogPageSupport.pageable(
                2, 25, "name", "asc", "id", Set.of("id", "name"));

        assertEquals(2, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
        assertEquals(
                Sort.by(Sort.Direction.ASC, "name")
                        .and(Sort.by(Sort.Direction.ASC, "id")),
                pageable.getSort()
        );
    }
}
