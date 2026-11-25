package com.lb_calc_web.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Общие правила серверной пагинации и сортировки каталогов. */
public final class CatalogPageSupport {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private CatalogPageSupport() {
    }

    /**
     * Создаёт Pageable, ограничивая размер страницы и разрешая сортировку
     * только по явно заданному набору скалярных полей.
     */
    public static Pageable pageable(
            int page,
            int size,
            String sort,
            String direction,
            String defaultSort,
            Set<String> allowedSorts
    ) {
        int safePage = Math.max(0, page);
        int requestedSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        int safeSize = Math.min(requestedSize, MAX_PAGE_SIZE);
        String safeSort = allowedSorts.contains(sort) ? sort : defaultSort;
        Sort.Direction safeDirection = Sort.Direction.fromOptionalString(direction)
                .orElse(Sort.Direction.ASC);
        return PageRequest.of(safePage, safeSize, Sort.by(safeDirection, safeSort));
    }

    /** Добавляет в модель параметры страницы, необходимые шаблону списка. */
    public static void addModelAttributes(
            Model model,
            Page<?> page,
            String query,
            Pageable pageable,
            String baseUrl,
            Map<String, String> sortOptions
    ) {
        model.addAttribute("page", page);
        model.addAttribute("q", query == null ? "" : query);
        model.addAttribute("sort", pageable.getSort().stream()
                .findFirst().map(Sort.Order::getProperty).orElse("id"));
        model.addAttribute("direction", pageable.getSort().stream()
                .findFirst().map(order -> order.getDirection().name().toLowerCase()).orElse("asc"));
        model.addAttribute("size", pageable.getPageSize());
        model.addAttribute("baseUrl", baseUrl);
        model.addAttribute("sortOptions", sortOptions);
    }

    /** Формирует упорядоченный набор значений для списка сортировки. */
    public static Map<String, String> sortOptions(String... values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            result.put(values[i], values[i + 1]);
        }
        return result;
    }
}
