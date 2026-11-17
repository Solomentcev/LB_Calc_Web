package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.TypeLb;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Создаёт неизменяемую конфигурацию конструктивного типа модуля хранения
 * на основании параметров приложения.
 *
 * <p>Значения типа разрешаются на application-слое и передаются в доменную
 * модель целиком. Домен не зависит от Spring Environment и не изменяет
 * уже созданный конструктивный тип.</p>
 */
public final class TypeLbFactory {

    private TypeLbFactory() {
    }

    /**
     * Создаёт конструктивный тип LB из настроек приложения.
     *
     * @param environment источник конфигурационных параметров
     * @param typeName имя выбранного типа
     * @return конфигурация конструктивного типа
     */
    public static TypeLb forLB(Environment environment, String typeName) {
        Objects.requireNonNull(environment, "Environment не должен быть null");
        String prefix = typePrefix("lb", typeName);
        return create(environment, typeName, prefix, null);
    }

    /**
     * Создаёт конструктивный тип LBC. Для параметров, не заданных
     * отдельно для LBC, используются настройки соответствующего типа LB.
     *
     * @param environment источник конфигурационных параметров
     * @param typeName имя выбранного типа
     * @return конфигурация конструктивного типа
     */
    public static TypeLb forLBC(Environment environment, String typeName) {
        Objects.requireNonNull(environment, "Environment не должен быть null");
        String prefix = typePrefix("lbc", typeName);
        String fallbackPrefix = typePrefix("lb", typeName);
        return create(environment, typeName, prefix, fallbackPrefix);
    }

    private static TypeLb create(
            Environment environment,
            String typeName,
            String prefix,
            String fallbackPrefix
    ) {
        return new TypeLb(
                typeName,
                requiredInt(environment, prefix, fallbackPrefix, "delta-width"),
                requiredInt(environment, prefix, fallbackPrefix, "shelf-thick"),
                requiredInt(environment, prefix, fallbackPrefix, "service-zone-width")
        );
    }

    private static String typePrefix(String module, String typeName) {
        if (typeName == null || typeName.isBlank()) {
            throw new IllegalArgumentException("Тип модуля не указан");
        }
        return module + ".type." + typeName;
    }

    private static int requiredInt(
            Environment environment,
            String prefix,
            String fallbackPrefix,
            String property
    ) {
        String key = prefix + "." + property;
        if (environment.containsProperty(key)) {
            return environment.getRequiredProperty(key, Integer.class);
        }

        if (fallbackPrefix != null) {
            String fallbackKey = fallbackPrefix + "." + property;
            return environment.getRequiredProperty(fallbackKey, Integer.class);
        }

        return environment.getRequiredProperty(key, Integer.class);
    }
}
