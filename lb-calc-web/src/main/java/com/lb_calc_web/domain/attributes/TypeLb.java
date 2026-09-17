package com.lb_calc_web.domain.attributes;

import java.util.Objects;

/**
 * Конструктивный тип модуля хранения LB.
 *
 * <p>Тип LB определяет параметры конструкции, которые используются
 * при расчёте размеров ячеек и сервисной зоны.</p>
 *
 * <p>Значения типа не являются жёстко заданными enum-константами.
 * Они могут быть загружены из конфигурации приложения.</p>
 */
public final class TypeLb {

    private final String type;
    private final int deltaWidth;
    private final int shelfThick;
    private final int serviceZoneWidth;

    /**
     * Создаёт тип LB.
     *
     * @param type наименование типа, например {@code TYPE1}
     * @param deltaWidth добавочная ширина, используемая при расчёте
     *                   ширины ячейки, мм
     * @param shelfThick толщина полки между ячейками, мм
     * @param serviceZoneWidth ширина сервисной зоны, мм
     * @throws IllegalArgumentException если тип пустой
     *                                  или один из размеров отрицательный
     */
    public TypeLb(
            String type,
            int deltaWidth,
            int shelfThick,
            int serviceZoneWidth
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Тип LB не может быть пустым"
            );
        }

        if (deltaWidth < 0) {
            throw new IllegalArgumentException(
                    "Добавочная ширина не может быть отрицательной"
            );
        }

        if (shelfThick < 0) {
            throw new IllegalArgumentException(
                    "Толщина полки не может быть отрицательной"
            );
        }

        if (serviceZoneWidth < 0) {
            throw new IllegalArgumentException(
                    "Ширина сервисной зоны не может быть отрицательной"
            );
        }

        this.type = type;
        this.deltaWidth = deltaWidth;
        this.shelfThick = shelfThick;
        this.serviceZoneWidth = serviceZoneWidth;
    }

    /**
     * Возвращает тип LB.
     *
     * @return наименование типа
     */
    public String getType() {
        return type;
    }

    /**
     * Возвращает добавочную ширину.
     *
     * @return добавочная ширина, мм
     */
    public int getDeltaWidth() {
        return deltaWidth;
    }

    /**
     * Возвращает толщину полки.
     *
     * @return толщина полки, мм
     */
    public int getShelfThick() {
        return shelfThick;
    }

    /**
     * Возвращает ширину сервисной зоны.
     *
     * @return ширина сервисной зоны, мм
     */
    public int getServiceZoneWidth() {
        return serviceZoneWidth;
    }

    /**
     * Сравнивает типы LB по всем конструктивным параметрам.
     *
     * @param o объект для сравнения
     * @return {@code true}, если тип и его параметры совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TypeLb other)) {
            return false;
        }

        return deltaWidth == other.deltaWidth
                && shelfThick == other.shelfThick
                && serviceZoneWidth == other.serviceZoneWidth
                && type.equals(other.type);
    }

    /**
     * Возвращает хэш-код типа LB.
     *
     * @return хэш-код объекта
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                deltaWidth,
                shelfThick,
                serviceZoneWidth
        );
    }

    /**
     * Возвращает подробное строковое представление типа LB.
     *
     * @return тип и его конструктивные размеры в миллиметрах
     */
    @Override
    public String toString() {
        return "TypeLb{" +
                "type='" + type + '\'' +
                ", deltaWidth=" + deltaWidth + " мм" +
                ", shelfThick=" + shelfThick + " мм" +
                ", serviceZoneWidth=" + serviceZoneWidth + " мм" +
                '}';
    }
}

