package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Формирует пользовательские наименования и описания доменных модулей.
 *
 * <p>Класс не хранит состояние и не участвует в сравнении конфигураций.
 * Источником данных остаются характеристики соответствующих доменных объектов.</p>
 */
final class ModuleTextFormatter {

    private ModuleTextFormatter() {
    }

    static String storageName(StorageModule module, String prefix) {
        return prefix + ": " + module.getCountCells() + " яч."
                + ", " + module.getTypeLb().getType()
                + ", " + direction(module.getDirectionDoorOpening())
                + ", " + dimensions(module)
                + ", " + colorPair(module.getColorBody(), module.getColorDoor());
    }

    static String storageDescription(StorageModule module, String title) {
        return title + "\n"
                + "Количество ячеек: " + module.getCountCells() + "\n"
                + "Конструктивный тип: " + module.getTypeLb().getType() + "\n"
                + "Габариты модуля (В×Ш×Г): " + dimensions(module) + "\n"
                + "Размеры ячейки (В×Ш×Г): "
                + module.getCellHeight() + "×" + module.getCellWidth()
                + "×" + module.getCellDepth() + " мм\n"
                + "Верхняя рама: " + module.getUpperFrame() + " мм\n"
                + "Нижняя рама: " + module.getBottomFrame() + " мм\n"
                + "Направление открытия дверей: "
                + direction(module.getDirectionDoorOpening()) + "\n"
                + "Толщина дверцы: " + module.getDoorThickness() + " мм\n"
                + colors(module);
    }

    static String combinedName(StorageModule storageModule, ControlModule controlModule) {
        String details = controlNameDetails(controlModule);
        return "Модуль хранения и управления: " + storageModule.getCountCells()
                + " яч." + details
                + ", " + storageModule.getTypeLb().getType()
                + ", " + direction(storageModule.getDirectionDoorOpening())
                + ", " + dimensions(storageModule)
                + ", " + colorPair(storageModule.getColorBody(), storageModule.getColorDoor());
    }

    static String controlName(ControlModule controlModule, Module module) {
        return "Модуль управления:" + controlNameDetails(controlModule)
                + ", " + dimensions(module)
                + ", " + colorPair(module.getColorBody(), module.getColorDoor());
    }

    static String controlDescription(ControlModule controlModule, Module module) {
        return "Модуль управления\\n"
                + "Габариты модуля (В×Ш×Г): " + dimensions(module) + "\\n"
                + "Верхняя рама: " + module.getUpperFrame() + " мм\\n"
                + "Нижняя рама: " + module.getBottomFrame() + " мм\\n"
                + controlDetailsDescription(controlModule) + "\\n"
                + colors(module);
    }

    static String controlDetailsDescription(ControlModule controlModule) {
        return functionalDescription(controlModule) + "\\n"
                + equipmentDescription(controlModule);
    }

    static String controlNameDetails(ControlModule controlModule) {
        String details = functionalSummary(
                controlModule.getControlConfiguration().getFunctionalConfiguration());
        return details.isBlank() ? "" : ", " + details;
    }

    static String functionalSummary(FunctionalConfiguration configuration) {
        StringBuilder result = new StringBuilder();
        append(result, display(configuration.getDisplay()));
        String access = configuration.getAccess().stream()
                .sorted()
                .map(ModuleTextFormatter::access)
                .collect(Collectors.joining(", "));
        if (!access.isBlank()) {
            append(result, access);
        }
        if (configuration.getPayment() != PaymentPurpose.NONE) {
            append(result, payment(configuration.getPayment()));
        }
        if (configuration.getPrinter() != PrinterPurpose.ABSENT) {
            append(result, printer(configuration.getPrinter()));
        }
        return result.toString();
    }

    static String functionalDescription(ControlModule controlModule) {
        String summary = functionalSummary(
                controlModule.getControlConfiguration().getFunctionalConfiguration());
        return summary.isBlank() ? "Функциональные назначения: не заданы"
                : "Функциональные назначения: " + summary;
    }

    static String equipmentDescription(ControlModule controlModule) {
        List<Map.Entry<Equipment, Integer>> equipment =
                controlModule.getControlConfiguration().getEquipment().entrySet().stream()
                        .sorted(Comparator
                                .comparing((Map.Entry<Equipment, Integer> entry) ->
                                        entry.getKey().getFunctionalType().ordinal())
                                .thenComparing(entry -> entry.getKey().getCategory().ordinal())
                                .thenComparing(entry -> entry.getKey().getName()))
                        .toList();
        if (equipment.isEmpty()) {
            return "Установленное оборудование: отсутствует";
        }
        return "Установленное оборудование:\n" + equipment.stream()
                .map(entry -> "- " + entry.getKey().getName()
                        + " — " + purpose(entry.getKey())
                        + (entry.getValue() == 1 ? "" : " (" + entry.getValue() + " шт.)"))
                .collect(Collectors.joining("\n"));
    }

    static String alsName(int countCells, int storageModuleCount, Module control,
                          int height, int width, int depth, Colors body, Colors door) {
        ControlModule controlModule = (ControlModule) control;
        String details = controlNameDetails(controlModule);
        return "АКХ: " + countCells + " яч., " + storageModuleCount + " мод."
                + (details.isBlank() ? "" : details)
                + ", " + height + "×" + width + "×" + depth + " мм"
                + ", " + colorPair(body, door);
    }

    static String alsDescription(int countCells, int storageModuleCount,
                                 int height, int width, int depth,
                                 Colors body, Colors door, List<Module> modules) {
        StringBuilder result = new StringBuilder()
                .append("Автоматизированная камера хранения\n")
                .append("Количество ячеек: ").append(countCells).append("\n")
                .append("Количество модулей хранения: ").append(storageModuleCount).append("\n")
                .append("Габариты (В×Ш×Г): ").append(height).append("×")
                .append(width).append("×").append(depth).append(" мм\n")
                .append("Цвет корпуса: ").append(color(body)).append("\n")
                .append("Цвет дверей: ").append(color(door)).append("\n")
                .append("Состав модулей слева направо:");
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            result.append("\n\n").append(i + 1).append(". ").append(module.getName())
                    .append("\n").append(indent(module.getDescription()));
        }
        return result.toString();
    }

    static String dimensions(Module module) {
        return module.getHeight() + "×" + module.getWidth() + "×"
                + module.getDepth() + " мм";
    }

    static String colors(Module module) {
        return "Цвет корпуса: " + color(module.getColorBody())
                + ", цвет дверей: " + color(module.getColorDoor());
    }

    private static String colorPair(Colors body, Colors door) {
        return color(body) + "/" + color(door);
    }

    static String direction(DirectionDoorOpening direction) {
        return direction == DirectionDoorOpening.LEFT ? "влево" : "вправо";
    }

    static String color(Colors color) {
        return switch (color) {
            case Black -> "чёрный";
            case Blue -> "синий";
            case Cyan -> "голубой";
            case DarkGray -> "тёмно-серый";
            case Gray -> "серый";
            case Green -> "зелёный";
            case LightGray -> "светло-серый";
            case Magenta -> "пурпурный";
            case Orange -> "оранжевый";
            case Pink -> "розовый";
            case Red -> "красный";
            case White -> "белый";
            case Yellow -> "жёлтый";
        };
    }

    private static String display(DisplayPurpose purpose) {
        return switch (purpose) {
            case NONE -> "";
            case INCH_10 -> "дисплей 10″";
            case INCH_17 -> "дисплей 17″";
            case INCH_19 -> "дисплей 19″";
            case CUSTOM -> "дисплей индивидуального размера";
        };
    }

    private static String access(AccessPurpose purpose) {
        return switch (purpose) {
            case PIN -> "PIN-код";
            case RFID -> "RFID";
            case BAR_CODE -> "штрихкод";
            case QR_CODE -> "QR-код";
        };
    }

    private static String payment(PaymentPurpose purpose) {
        return switch (purpose) {
            case NONE -> "";
            case CARDS -> "банковские карты";
            case CASH -> "наличные";
            case CARDS_AND_CASH -> "банковские карты и наличные";
        };
    }

    private static String printer(PrinterPurpose purpose) {
        return switch (purpose) {
            case PRESENT -> "принтер";
            case ABSENT -> "";
            case OTHER -> "принтер (другое исполнение)";
        };
    }

    private static String purpose(Equipment equipment) {
        return switch (equipment.getFunctionalType()) {
            case DISPLAY -> display((DisplayPurpose) equipment.getPurpose());
            case ACCESS -> access((AccessPurpose) equipment.getPurpose());
            case PAYMENT -> payment((PaymentPurpose) equipment.getPurpose());
            case PRINTER -> printer((PrinterPurpose) equipment.getPurpose());
            case SYSTEM -> "системное оборудование";
        };
    }

    private static void append(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(", ");
        }
        builder.append(value);
    }

    private static String indent(String value) {
        return value.lines().map(line -> "   " + line)
                .collect(Collectors.joining("\n"));
    }
}
