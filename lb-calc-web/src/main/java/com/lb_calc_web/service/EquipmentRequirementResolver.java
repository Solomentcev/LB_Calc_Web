package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.PrintOption;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentItem;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Подбирает активные модели оборудования под функции модуля управления.
 *
 * <p>PIN и способы оплаты не связываются с оборудованием: текущая модель
 * каталога не содержит соответствующих аппаратных характеристик.</p>
 */
@Service
public class EquipmentRequirementResolver {
    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentRequirementResolver(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    public ControlConfiguration resolve(ControlConfiguration configuration) {
        List<EquipmentItem> selected = new ArrayList<>(
                configuration.getEquipmentConfiguration().getAllItems());

        if (configuration.getAccessMethods().contains(AccessMethod.BARCODE)) {
            ensureEquipment(selected, "BAR_READER",
                    equipment -> equipment.supportsBarcode1d()
                            || equipment.supportsBarcode2d(),
                    "Для доступа по штрихкоду нужен совместимый сканер");
        }

        if (configuration.getAccessMethods().contains(AccessMethod.QR_CODE)) {
            ensureEquipment(selected, "BAR_READER", Equipment::supportsBarcode2d,
                    "Для доступа по QR-коду нужен 2D-сканер");
        }

        if (configuration.getAccessMethods().contains(AccessMethod.RFID)) {
            ensureEquipment(selected, "RFID_READER", Equipment::supportsRfid,
                    "Для доступа по RFID нужен совместимый RFID-считыватель");
        }

        if (!configuration.getPrintOptions().isEmpty()) {
            ensureEquipment(selected, "PRINTER", Equipment::supportsPrint,
                    "Для выбранных вариантов печати нужен совместимый принтер");
        }

        return new ControlConfiguration(
                configuration.getPayment(),
                configuration.getAccessMethods(),
                configuration.getPrintOptions(),
                EquipmentConfiguration.fromItems(selected, List.of())
        );
    }

    private void ensureEquipment(List<EquipmentItem> selected, String category,
                                 Predicate<Equipment> compatibility, String errorMessage) {
        List<Equipment> categoryItems = selected.stream()
                .map(EquipmentItem::equipment)
                .filter(item -> category.equals(item.getCategory()))
                .toList();

        if (!categoryItems.isEmpty()) {
            if (categoryItems.stream().noneMatch(compatibility)) {
                throw new IllegalArgumentException(errorMessage
                        + ". Выбранная модель не поддерживает требуемую функцию.");
            }
            return;
        }

        Equipment candidate = equipmentCatalogService.findActive().stream()
                .map(equipmentCatalogService::toDomain)
                .filter(item -> category.equals(item.getCategory()))
                .filter(compatibility)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        errorMessage + ". В активном каталоге нет подходящей модели."));
        selected.add(new EquipmentItem(candidate, 1));
    }
}
