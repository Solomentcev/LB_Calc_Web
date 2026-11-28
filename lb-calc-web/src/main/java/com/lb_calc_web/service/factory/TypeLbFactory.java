package com.lb_calc_web.service.factory;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.service.validation.size.SizeBounds;

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
     * @param sizeBounds централизованные настройки приложения
     * @param typeName имя выбранного типа
     * @return конфигурация конструктивного типа
     */
    public static TypeLb forLB(SizeBounds sizeBounds, String typeName) {
        Objects.requireNonNull(sizeBounds, "SizeBounds не должен быть null");
        return sizeBounds.getLbType(typeName);
    }

    /**
     * Создаёт конструктивный тип LBC. Для параметров, не заданных
     * отдельно для LBC, используются настройки соответствующего типа LB.
     *
     * @param sizeBounds централизованные настройки приложения
     * @param typeName имя выбранного типа
     * @return конфигурация конструктивного типа
     */
    public static TypeLb forLBC(SizeBounds sizeBounds, String typeName) {
        Objects.requireNonNull(sizeBounds, "SizeBounds не должен быть null");
        return sizeBounds.getLbcType(typeName);
    }


}
