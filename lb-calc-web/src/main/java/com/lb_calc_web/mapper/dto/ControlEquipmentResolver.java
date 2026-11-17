package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.domain.equipment.EquipmentItem;
import com.lb_calc_web.domain.equipment.Printer;
import com.lb_calc_web.domain.equipment.RfidReader;
import com.lb_calc_web.domain.model.EquipmentConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Преобразует поля оборудования DTO в единую доменную конфигурацию.
 *
 * <p>Используется мапперами LC и LBC, чтобы одинаково обрабатывать
 * каталожные позиции и совместимость со старыми DTO без количеств.</p>
 */
final class ControlEquipmentResolver {

    private ControlEquipmentResolver() {
    }

    static EquipmentConfiguration resolve(
            String displayName,
            Integer displayQuantity,
            String barReaderName,
            Integer barReaderQuantity,
            boolean printer,
            Integer printerQuantity,
            boolean rfidReader,
            Integer rfidReaderQuantity,
            String moduleType
    ) {
        int resolvedDisplayQuantity = resolveQuantity(
                displayQuantity,
                isAbsent(displayName, Display.NONE),
                1
        );
        int resolvedBarReaderQuantity = resolveQuantity(
                barReaderQuantity,
                isAbsent(barReaderName, BarReader.NONE),
                1
        );
        int resolvedPrinterQuantity = resolveQuantity(
                printerQuantity, !printer, 1
        );
        int resolvedRfidReaderQuantity = resolveQuantity(
                rfidReaderQuantity, !rfidReader, 1
        );

        List<EquipmentItem> items = new ArrayList<>();
        addItem(items, displayName, resolvedDisplayQuantity, Display.NONE, moduleType);
        addItem(items, barReaderName, resolvedBarReaderQuantity, BarReader.NONE, moduleType);
        addCatalogItem(items, Printer.CATALOG_POSITION_ID, resolvedPrinterQuantity);
        addCatalogItem(items, RfidReader.CATALOG_POSITION_ID, resolvedRfidReaderQuantity);

        return EquipmentConfiguration.fromItems(items, List.of());
    }

    private static int resolveQuantity(
            Integer quantity,
            boolean legacyAbsent,
            int legacyPresentQuantity
    ) {
        int result = quantity == null
                ? (legacyAbsent ? 0 : legacyPresentQuantity)
                : quantity;

        if (result < 0) {
            throw new IllegalArgumentException(
                    "Количество оборудования не может быть отрицательным"
            );
        }
        return result;
    }

    private static boolean isAbsent(String value, Equipment none) {
        return value == null
                || value.isBlank()
                || value.equals(none.getName())
                || value.equals(none.getCatalogPositionId());
    }

    private static void addItem(
            List<EquipmentItem> items,
            String nameOrId,
            int quantity,
            Equipment none,
            String moduleType
    ) {
        if (quantity == 0) {
            return;
        }

        Equipment equipment = EquipmentCatalog.resolve(nameOrId);
        if (equipment == null) {
            throw new IllegalArgumentException(
                    "Не указана позиция оборудования для количества " + quantity
            );
        }
        if (equipment.equals(none)) {
            throw new IllegalArgumentException(
                    "Количество оборудования типа «" + none.getName()
                            + "» в модуле " + moduleType + " должно быть равно 0"
            );
        }

        items.add(new EquipmentItem(equipment, quantity));
    }

    private static void addCatalogItem(
            List<EquipmentItem> items,
            String catalogPositionId,
            int quantity
    ) {
        if (quantity == 0) {
            return;
        }
        items.add(new EquipmentItem(
                EquipmentCatalog.findByCatalogPositionId(catalogPositionId),
                quantity
        ));
    }
}
