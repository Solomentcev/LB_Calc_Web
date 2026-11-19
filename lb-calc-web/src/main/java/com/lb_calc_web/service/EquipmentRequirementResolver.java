package com.lb_calc_web.service;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentItem;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Подбирает оборудование управляющего модуля по выбранным функциям.
 *
 * <p>Оборудование, не относящееся к функциональным категориям,
 * сохраняется без изменений.</p>
 */
@Service
public class EquipmentRequirementResolver {
    private static final String PAYMENT_TERMINAL = "PAYMENT_TERMINAL";
    private static final String CASH_ACCEPTOR = "CASH_ACCEPTOR";

    private static final Set<String> MANAGED_CATEGORIES = Set.of(
            "DISPLAY", "BAR_READER", "PRINTER", "RFID_READER",
            PAYMENT_TERMINAL, CASH_ACCEPTOR
    );

    private final EquipmentCatalogService equipmentCatalogService;

    public EquipmentRequirementResolver(EquipmentCatalogService equipmentCatalogService) {
        this.equipmentCatalogService = equipmentCatalogService;
    }

    public ControlConfiguration resolve(ControlConfiguration configuration) {
        boolean requiresBarcode = configuration.getAccessMethods().contains(AccessMethod.BARCODE);
        boolean requiresQrCode = configuration.getAccessMethods().contains(AccessMethod.QR_CODE);
        boolean requiresPin = configuration.getAccessMethods().contains(AccessMethod.PIN);
        boolean requiresRfid = configuration.getAccessMethods().contains(AccessMethod.RFID);

        if (requiresPin && !requiresRfid && !requiresBarcode && !requiresQrCode) {
            throw new IllegalArgumentException(
                    "PIN не может быть единственным способом доступа: выберите RFID, штрихкод или QR-код");
        }

        Set<String> requiredCategories = new HashSet<>();
        boolean requiresDisplay = configuration.getEquipmentConfiguration().getAllItems()
                .stream()
                .anyMatch(item -> "DISPLAY".equals(item.equipment().getCategory()));

        if (configuration.getPayment() == Payment.CASHLESS
                || configuration.getPayment() == Payment.CASH_AND_CARDS) {
            requiredCategories.add(PAYMENT_TERMINAL);
        }
        if (configuration.getPayment() == Payment.CASH
                || configuration.getPayment() == Payment.CASH_AND_CARDS) {
            requiredCategories.add(CASH_ACCEPTOR);
        }

        if (requiresBarcode || requiresQrCode) requiredCategories.add("BAR_READER");
        if (requiresRfid) requiredCategories.add("RFID_READER");
        // Дисплей — самостоятельный функциональный выбор; PIN также требует его.
        if (requiresDisplay || requiresPin) requiredCategories.add("DISPLAY");

        boolean requiresPrinter = configuration.getPayment() != Payment.NONE
                || requiresBarcode || requiresQrCode || requiresPin;
        if (requiresPrinter) requiredCategories.add("PRINTER");

        List<EquipmentItem> selected = new ArrayList<>();
        List<EquipmentItem> additional = new ArrayList<>();

        for (EquipmentItem item : configuration.getEquipmentConfiguration().getAllItems()) {
            String category = item.equipment().getCategory();
            if (!MANAGED_CATEGORIES.contains(category)) {
                additional.add(item);
            } else if (requiredCategories.contains(category)) {
                selected.add(item);
            }
        }

        if (requiresDisplay || requiresPin) {
            ensureSingleEquipment(selected, "DISPLAY", "Для модуля выбран дисплей");
        }

        if (requiredCategories.contains(PAYMENT_TERMINAL)) {
            ensureSingleEquipment(selected, PAYMENT_TERMINAL,
                    "Для безналичной оплаты нужен банковский терминал");
        }
        if (requiredCategories.contains(CASH_ACCEPTOR)) {
            ensureSingleEquipment(selected, CASH_ACCEPTOR,
                    "Для оплаты наличными нужен купюроприёмник");
        }

        if (requiresBarcode || requiresQrCode) {
            ensureSingleEquipment(selected, "BAR_READER",
                    "Для выбранных способов доступа нужен один сканер штрихкодов");
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
            ensureSingleEquipment(selected, "RFID_READER",
                    "Для доступа по RFID нужен один RFID-считыватель");
            ensureEquipment(selected, "RFID_READER", Equipment::supportsRfid,
                    "Для доступа по RFID нужен совместимый RFID-считыватель");
        }

        if (requiresPin) {
            ensureEquipment(selected, "DISPLAY", Equipment::isDisplay,
                    "Для доступа по PIN нужен дисплей");
        }

        if (requiresPrinter) {
            ensureSingleEquipment(selected, "PRINTER",
                    "Для выбранных функций нужен один принтер");
            ensureEquipment(selected, "PRINTER", Equipment::supportsPrint,
                    "Для выбранных функций нужен совместимый принтер");
        }

        return new ControlConfiguration(
                configuration.getPayment(),
                configuration.getAccessMethods(),
                configuration.getPrintOptions(),
                EquipmentConfiguration.fromItems(selected, additional)
        );
    }

    /**
     * Проверяет, что для категории выбрана одна каталожная позиция
     * в количестве одной единицы, либо добавляет модель по умолчанию.
     */
    private void ensureSingleEquipment(List<EquipmentItem> selected, String category,
                                       String errorMessage) {
        List<EquipmentItem> categoryItems = selected.stream()
                .filter(item -> category.equals(item.equipment().getCategory()))
                .toList();

        if (categoryItems.size() > 1
                || (!categoryItems.isEmpty() && categoryItems.get(0).quantity() != 1)) {
            throw new IllegalArgumentException(
                    "Для категории " + category + " допускается ровно одна позиция в количестве 1");
        }
        ensureEquipment(selected, category, equipment -> true, errorMessage);
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
            throw new IllegalStateException(
                    errorMessage + ". Активная модель по умолчанию не соответствует требованию каталога.");
        }
        selected.add(new EquipmentItem(candidate, 1));
    }
}
