package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
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
 * <p>PIN требует дисплей и дополнительный способ доступа. Для вариантов оплаты
 * подбирается принтер, если он нужен по правилам конфигурации. Специализированное
 * оборудование приёма наличных и банковские терминалы пока не представлены
 * характеристиками каталога и автоматически не подбираются.</p>
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

        boolean requiresBarcode = configuration.getAccessMethods().contains(AccessMethod.BARCODE);
        boolean requiresQrCode = configuration.getAccessMethods().contains(AccessMethod.QR_CODE);
        boolean requiresPin = configuration.getAccessMethods().contains(AccessMethod.PIN);
        boolean requiresRfid = configuration.getAccessMethods().contains(AccessMethod.RFID);
        if (requiresBarcode || requiresQrCode) {
            Predicate<Equipment> barcodeCompatibility = equipment ->
                    (!requiresBarcode || equipment.supportsBarcode1d()
                            || equipment.supportsBarcode2d())
                            && (!requiresQrCode || equipment.supportsBarcode2d());
            String message = requiresQrCode
                    ? "Для выбранных способов доступа нужен совместимый 2D-сканер"
                    : "Для доступа по штрихкоду нужен совместимый сканер";
            ensureEquipment(selected, "BAR_READER", barcodeCompatibility, message);
        }

        if (requiresRfid) {
            ensureEquipment(selected, "RFID_READER", Equipment::supportsRfid,
                    "Для доступа по RFID нужен совместимый RFID-считыватель");
        }

        if (requiresPin) {
            if (!requiresRfid && !requiresBarcode && !requiresQrCode) {
                throw new IllegalArgumentException(
                        "PIN не может быть единственным способом доступа: выберите RFID, штрихкод или QR-код");
            }
            ensureEquipment(selected, "DISPLAY", Equipment::isDisplay,
                    "Для доступа по PIN нужен дисплей");
        }

        boolean requiresPrinter = configuration.getPayment() != Payment.NONE
                || !configuration.getPrintOptions().isEmpty()
                || requiresBarcode || requiresQrCode || requiresPin;
        if (requiresPrinter) {
            ensureEquipment(selected, "PRINTER", Equipment::supportsPrint,
                    "Для выбранных функций нужен совместимый принтер");
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

        Equipment candidate = equipmentCatalogService.findActiveDefault(category);
        if (!compatibility.test(candidate)) {
            throw new IllegalArgumentException(
                    errorMessage + ". Активная модель по умолчанию не поддерживает требуемую функцию.");
        }
        selected.add(new EquipmentItem(candidate, 1));
    }
}
