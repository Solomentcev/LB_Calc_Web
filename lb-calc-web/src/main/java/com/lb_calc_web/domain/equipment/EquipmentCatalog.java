package com.lb_calc_web.domain.equipment;

import java.util.List;

/**
 * Каталог поддерживаемых в приложении позиций оборудования.
 * Идентификаторы являются стабильными ключами каталожных позиций.
 */
public final class EquipmentCatalog {
    private static final List<Equipment> POSITIONS = List.of(
            Display.LC10, Display.LC17, Display.LC19,
            BarReader.READER_1D, BarReader.READER_2D,
            new Printer(), new RfidReader()
    );

    private EquipmentCatalog() { }

    public static Equipment findByCatalogPositionId(String id) {
        if (id == null || id.isBlank()) return null;
        if (Display.NONE_ID.equals(id)) return Display.NONE;
        if (BarReader.NONE_ID.equals(id)) return BarReader.NONE;
        return POSITIONS.stream()
                .filter(item -> item.getCatalogPositionId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неизвестный идентификатор позиции оборудования: " + id));
    }

    /** Разрешает ID каталога или прежнее отображаемое имя. */
    public static Equipment resolve(String idOrLegacyName) {
        if (idOrLegacyName == null || idOrLegacyName.isBlank()) return null;
        if (Display.NONE_ID.equals(idOrLegacyName)
                || Display.NONE.getName().equals(idOrLegacyName)) {
            return Display.NONE;
        }
        if (BarReader.NONE_ID.equals(idOrLegacyName)
                || BarReader.NONE.getName().equals(idOrLegacyName)) {
            return BarReader.NONE;
        }
        return POSITIONS.stream()
                .filter(item -> item.getCatalogPositionId().equals(idOrLegacyName)
                        || item.getName().equals(idOrLegacyName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Неизвестная позиция оборудования: " + idOrLegacyName));
    }

    public static List<Equipment> getPositions() { return POSITIONS; }
}