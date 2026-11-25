package com.lb_calc_web.repository.specification;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import java.util.List;

/** Общие предикаты для фильтрации строковых, числовых и коллекционных полей каталогов. */
public final class CatalogSpecifications {
    private CatalogSpecifications() {
    }

    /** Создаёт спецификацию точного совпадения идентификатора записи. */
    public static <T> Specification<T> id(Long value) {
        return value == null ? null : (root, query, builder) ->
                builder.equal(root.get("id"), value);
    }

    /** Создаёт спецификацию поиска подстроки без учёта регистра. */
    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) return null;
        String pattern = "%" + value.trim().toLowerCase(java.util.Locale.ROOT) + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get(field)), pattern);
    }

    /** Создаёт спецификацию точного совпадения значения поля; пустые строки игнорируются. */
    public static <T, V> Specification<T> equal(String field, V value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String string) {
            String normalized = string.trim();
            if (normalized.isEmpty()) {
                return null;
            }
            return (root, query, builder) ->
                    builder.equal(root.get(field), normalized);
        }
        return (root, query, builder) ->
                builder.equal(root.get(field), value);
    }

    /** Создаёт спецификацию проверки числового значения по одной или двум границам. */
    public static <T> Specification<T> integerRange(String field, Integer minimum, Integer maximum) {
        if (minimum == null && maximum == null) return null;
        return (root, query, builder) -> {
            var path = root.<Integer>get(field);
            if (minimum != null && maximum != null) return builder.between(path, minimum, maximum);
            return minimum != null
                    ? builder.greaterThanOrEqualTo(path, minimum)
                    : builder.lessThanOrEqualTo(path, maximum);
        };
    }

    /** Создаёт спецификацию проверки наличия значения в коллекционном поле. */
    public static <T, V> Specification<T> collectionContains(String field, V value) {
        if (value == null) return null;
        return (root, query, builder) -> {
            var subquery = query.subquery(Integer.class);
            Root<T> correlated = subquery.correlate(root);
            SetJoin<T, V> element = correlated.joinSet(field);
            subquery.select(builder.literal(1)).where(builder.equal(element, value));
            return builder.exists(subquery);
        };
    }

    /** Объединяет непустые спецификации логическим AND. */
    public static <T> Specification<T> all(List<Specification<T>> specifications) {
        return specifications.stream()
                .filter(java.util.Objects::nonNull)
                .reduce(Specification::and)
                .orElse((root, query, builder) -> builder.conjunction());
    }
}
