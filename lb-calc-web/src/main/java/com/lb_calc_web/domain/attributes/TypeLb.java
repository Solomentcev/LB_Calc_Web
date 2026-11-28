package com.lb_calc_web.domain.attributes;

/**
 * Конструктивный тип модуля хранения LB/LBC.
 *
 * <p>Геометрические параметры типа задаются централизованно
 * в конфигурации SizeBounds.</p>
 */
public enum TypeLb {
    TYPE1,
    TYPE2,
    TYPE3,
    TYPE4;

    /** Возвращает строковый идентификатор типа для DTO и текстовых представлений. */
    public String getType() {
        return name();
    }
}
