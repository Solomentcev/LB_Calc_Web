package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.dto.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class SizeValidator {

    private static final Logger logger =
            LoggerFactory.getLogger(SizeValidator.class);

    private static int UPPER_FRAME_MIN;
    private static int UPPER_FRAME_MAX;

    private static int BOTTOM_FRAME_MIN;
    private static int BOTTOM_FRAME_MAX;

    private static int HEIGHT_CELL_MIN;
    private static int HEIGHT_MAX;
    private static int HEIGHT_MIN;
    private static int HEIGHT_LC_MIN;
    private static int HEIGHT_LC_PANEL_MIN;

    private static int COUNT_CELLS_MIN;
    private static int COUNT_CELLS_MAX;

    private static int WIDTH_CELL_MIN;
    private static int WIDTH_MAX;

    private static int DEPTH_CELL_MIN;
    private static int DEPTH_CELL_MAX;
    private static int DEPTH_MIN;
    private static int DEPTH_MAX;

    private static int DOOR_THICKNESS_MIN;
    private static int DOOR_THICKNESS_MAX;

    private static int TYPE_DELTA_WIDTH_MIN;
    private static int TYPE_DELTA_WIDTH_MAX;

    private static int TYPE_SHELF_THICK_MIN;
    private static int TYPE_SHELF_THICK_MAX;

    private static int TYPE_SERVICE_ZONE_WIDTH_MIN;
    private static int TYPE_SERVICE_ZONE_WIDTH_MAX;

    /*
     * Значения берутся только из size-bounds.properties.
     */

    @Value("${size.frame.upper.min}")
    private int upperFrameMin;

    @Value("${size.frame.upper.max}")
    private int upperFrameMax;

    @Value("${size.frame.bottom.min}")
    private int bottomFrameMin;

    @Value("${size.frame.bottom.max}")
    private int bottomFrameMax;

    @Value("${size.height.cell.min}")
    private int heightCellMin;

    @Value("${size.height.max}")
    private int heightMax;

    @Value("${size.height.min}")
    private int heightMin;

    @Value("${size.height.lc.min}")
    private int heightLcMin;

    @Value("${size.height.lc.panel.min}")
    private int heightLcPanelMin;

    @Value("${size.count.cells.min}")
    private int countCellsMin;

    @Value("${size.count.cells.max}")
    private int countCellsMax;

    @Value("${size.width.cell.min}")
    private int widthCellMin;

    @Value("${size.width.max}")
    private int widthMax;

    @Value("${size.depth.cell.min}")
    private int depthCellMin;

    @Value("${size.depth.cell.max}")
    private int depthCellMax;

    @Value("${size.depth.min}")
    private int depthMin;

    @Value("${size.depth.max}")
    private int depthMax;

    @Value("${size.door.thickness.min}")
    private int doorThicknessMin;

    @Value("${size.door.thickness.max}")
    private int doorThicknessMax;

    @Value("${size.type.delta-width.min}")
    private int typeDeltaWidthMin;

    @Value("${size.type.delta-width.max}")
    private int typeDeltaWidthMax;

    @Value("${size.type.shelf-thick.min}")
    private int typeShelfThickMin;

    @Value("${size.type.shelf-thick.max}")
    private int typeShelfThickMax;

    @Value("${size.type.service-zone-width.min}")
    private int typeServiceZoneWidthMin;

    @Value("${size.type.service-zone-width.max}")
    private int typeServiceZoneWidthMax;

    @PostConstruct
    private void init() {

        UPPER_FRAME_MIN = upperFrameMin;
        UPPER_FRAME_MAX = upperFrameMax;

        BOTTOM_FRAME_MIN = bottomFrameMin;
        BOTTOM_FRAME_MAX = bottomFrameMax;

        HEIGHT_CELL_MIN = heightCellMin;
        HEIGHT_MAX = heightMax;
        HEIGHT_MIN = heightMin;
        HEIGHT_LC_MIN = heightLcMin;
        HEIGHT_LC_PANEL_MIN = heightLcPanelMin;

        COUNT_CELLS_MIN = countCellsMin;
        COUNT_CELLS_MAX = countCellsMax;

        WIDTH_CELL_MIN = widthCellMin;
        WIDTH_MAX = widthMax;

        DEPTH_CELL_MIN = depthCellMin;
        DEPTH_CELL_MAX = depthCellMax;
        DEPTH_MIN = depthMin;
        DEPTH_MAX = depthMax;

        DOOR_THICKNESS_MIN = doorThicknessMin;
        DOOR_THICKNESS_MAX = doorThicknessMax;

        TYPE_DELTA_WIDTH_MIN = typeDeltaWidthMin;
        TYPE_DELTA_WIDTH_MAX = typeDeltaWidthMax;

        TYPE_SHELF_THICK_MIN = typeShelfThickMin;
        TYPE_SHELF_THICK_MAX = typeShelfThickMax;

        TYPE_SERVICE_ZONE_WIDTH_MIN = typeServiceZoneWidthMin;
        TYPE_SERVICE_ZONE_WIDTH_MAX = typeServiceZoneWidthMax;

        logger.info("SizeValidator configuration loaded");
    }

    public static int getUpperFrameMin() {
        return UPPER_FRAME_MIN;
    }

    public static int getUpperFrameMax() {
        return UPPER_FRAME_MAX;
    }

    public static int getBottomFrameMin() {
        return BOTTOM_FRAME_MIN;
    }

    public static int getBottomFrameMax() {
        return BOTTOM_FRAME_MAX;
    }

    public static int getHeightCellMin() {
        return HEIGHT_CELL_MIN;
    }

    public static int getHeightMax() {
        return HEIGHT_MAX;
    }

    public static int getHeightMin() {
        return HEIGHT_MIN;
    }

    public static int getHeightLcMin() {
        return HEIGHT_LC_MIN;
    }

    public static int getHeightLcPanelMin() {
        return HEIGHT_LC_PANEL_MIN;
    }

    public static int getCountCellsMin() {
        return COUNT_CELLS_MIN;
    }

    public static int getCountCellsMax() {
        return COUNT_CELLS_MAX;
    }

    public static int getWidthCellMin() {
        return WIDTH_CELL_MIN;
    }

    public static int getWidthMax() {
        return WIDTH_MAX;
    }

    public static int getDepthCellMin() {
        return DEPTH_CELL_MIN;
    }

    public static int getDepthCellMax() {
        return DEPTH_CELL_MAX;
    }

    public static int getDepthMin() {
        return DEPTH_MIN;
    }

    public static int getDepthMax() {
        return DEPTH_MAX;
    }

    public static int getDoorThicknessMin() {
        return DOOR_THICKNESS_MIN;
    }

    public static int getDoorThicknessMax() {
        return DOOR_THICKNESS_MAX;
    }

    /*
     * Эти setters оставляем для существующих unit-тестов.
     */

    public static void setUpperFrameMin(int value) {
        UPPER_FRAME_MIN = value;
    }

    public static void setUpperFrameMax(int value) {
        UPPER_FRAME_MAX = value;
    }

    public static void setBottomFrameMin(int value) {
        BOTTOM_FRAME_MIN = value;
    }

    public static void setBottomFrameMax(int value) {
        BOTTOM_FRAME_MAX = value;
    }

    public static void setHeightCellMin(int value) {
        HEIGHT_CELL_MIN = value;
    }

    public static void setHeightMax(int value) {
        HEIGHT_MAX = value;
    }

    public static void setHeightMin(int value) {
        HEIGHT_MIN = value;
    }

    public static void setHeightLcMin(int value) {
        HEIGHT_LC_MIN = value;
    }

    public static void setHeightLcPanelMin(int value) {
        HEIGHT_LC_PANEL_MIN = value;
    }

    public static void setCountCellsMin(int value) {
        COUNT_CELLS_MIN = value;
    }

    public static void setWidthCellMin(int value) {
        WIDTH_CELL_MIN = value;
    }

    public static void setWidthMax(int value) {
        WIDTH_MAX = value;
    }

    public static void setDepthCellMin(int value) {
        DEPTH_CELL_MIN = value;
    }

    public static void setDepthMax(int value) {
        DEPTH_MAX = value;
    }

    public static void setDepthMin(int value) {
        DEPTH_MIN = value;
    }

    public static void setDepthCellMax(int value) {
        DEPTH_CELL_MAX = value;
    }

    public static void setDoorThicknessMin(int value) {
        DOOR_THICKNESS_MIN = value;
    }

    public static void setDoorThicknessMax(int value) {
        DOOR_THICKNESS_MAX = value;
    }

    /**
     * Проверка диапазона.
     */
    static void validateRange(
            ValidationResult result,
            String fieldName,
            int value,
            int min,
            int max,
            String fieldLabel
    ) {
        if (value < min) {
            result.addError(
                    result,
                    fieldName,
                    fieldLabel + " меньше допустимой",
                    value,
                    min,
                    max
            );
        } else if (value > max) {
            result.addError(
                    result,
                    fieldName,
                    fieldLabel + " больше допустимой",
                    value,
                    min,
                    max
            );
        }
    }

    /**
     * Проверка консистентности LC.
     */
    static void validateLCConsistency(
            ValidationResult result,
            LCDTO lc
    ) {
        int usableHeight =
                lc.getHeight()
                        - lc.getUpperFrame()
                        - lc.getBottomFrame();

        if (usableHeight < HEIGHT_LC_PANEL_MIN) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight,
                    HEIGHT_LC_PANEL_MIN,
                    HEIGHT_MAX
            );
        }
    }

    /**
     * Проверка консистентности ALS.
     */
    static void validateALSConsistency(
            ValidationResult result,
            ALSDTO als
    ) {
        boolean hasControlModule =
                als.getLC() != null || als.getLBC() != null;

        boolean hasStorageModule =
                (als.getLbList() != null
                        && !als.getLbList().isEmpty())
                        || als.getLBC() != null;

        if (!hasControlModule) {
            result.addError(
                    result,
                    "controlModule",
                    "Модуль управления отсутствует",
                    null,
                    null,
                    null
            );
        }

        if (!hasStorageModule) {
            result.addError(
                    result,
                    "storageModules",
                    "Список модулей хранения пустой",
                    null,
                    null,
                    null
            );
        }

        int usableHeight =
                als.getHeight()
                        - als.getUpperFrame()
                        - als.getBottomFrame();

        if (usableHeight < HEIGHT_CELL_MIN) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для ячейки",
                    usableHeight,
                    HEIGHT_CELL_MIN,
                    HEIGHT_MAX
            );
        }

        if (usableHeight < HEIGHT_LC_PANEL_MIN) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight,
                    HEIGHT_LC_PANEL_MIN,
                    HEIGHT_MAX
            );
        }
    }

    /**
     * Проверка консистентности LBC.
     * LBC должен одновременно обеспечивать хранение и панель управления.
     */
    static void validateLBCConsistency(
            ValidationResult result,
            LBCDTO lbc
    ) {
        int usableHeight =
                lbc.getHeight()
                        - lbc.getUpperFrame()
                        - lbc.getBottomFrame();

        if (usableHeight < HEIGHT_LC_PANEL_MIN) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления LBC",
                    usableHeight,
                    HEIGHT_LC_PANEL_MIN,
                    HEIGHT_MAX
            );
        }
    }

    /**
     * Валидация количества ячеек LB.
     */
    static void validateLBCellCount(
            ValidationResult result,
            LBDTO lb,
            TypeLb typeLb
    ) {
        int usableHeight =
                lb.getHeight()
                        - lb.getUpperFrame()
                        - lb.getBottomFrame();

        if (usableHeight <= 0) {
            return;
        }

        int calculatedCountCellsMax =
                (usableHeight + typeLb.getShelfThick())
                        / (HEIGHT_CELL_MIN + typeLb.getShelfThick());

        int countCellsMax =
                Math.min(
                        calculatedCountCellsMax,
                        COUNT_CELLS_MAX
                );

        validateRange(
                result,
                "countCells",
                lb.getCountCells(),
                COUNT_CELLS_MIN,
                countCellsMax,
                "Количество ячеек"
        );
    }

    /**
     * Валидация размеров ячеек LB.
     */
    static void validateLBCellDimensions(
            ValidationResult result,
            LBDTO lb,
            TypeLb typeLb
    ) {
        int countCells = lb.getCountCells();

        if (countCells <= 0) {
            return;
        }

        double heightCell =
                (
                        lb.getHeight()
                                - lb.getUpperFrame()
                                - lb.getBottomFrame()
                                - (countCells - 1)
                                * typeLb.getShelfThick()
                ) / (double) countCells;

        if (heightCell < HEIGHT_CELL_MIN) {
            result.addError(
                    result,
                    "heightCell",
                    "Высота ячейки меньше допустимой",
                    String.format("%.2f", heightCell),
                    HEIGHT_CELL_MIN,
                    lb.getHeight()
            );
        }

        /*
         * Раньше здесь было:
         *
         * int depthCell = lb.getDepth() - 20;
         *
         * Теперь используется реальная толщина дверцы LB.
         */
        int depthCell =
                lb.getDepth() - lb.getDoorThickness();

        if (depthCell < DEPTH_CELL_MIN) {
            result.addError(
                    result,
                    "depthCell",
                    "Глубина ячейки меньше допустимой",
                    depthCell,
                    DEPTH_CELL_MIN,
                    DEPTH_CELL_MAX
            );
        } else if (depthCell > DEPTH_CELL_MAX) {
            result.addError(
                    result,
                    "depthCell",
                    "Глубина ячейки больше допустимой",
                    depthCell,
                    DEPTH_CELL_MIN,
                    DEPTH_CELL_MAX
            );
        }

        int widthCell =
                lb.getWidth() - typeLb.getDeltaWidth();

        int widthCellMax =
                WIDTH_MAX - typeLb.getDeltaWidth();

        if (widthCell < WIDTH_CELL_MIN) {
            result.addError(
                    result,
                    "widthCell",
                    "Ширина ячейки меньше допустимой",
                    widthCell,
                    WIDTH_CELL_MIN,
                    widthCellMax
            );
        } else if (widthCell > widthCellMax) {
            result.addError(
                    result,
                    "widthCell",
                    "Ширина ячейки больше допустимой",
                    widthCell,
                    WIDTH_CELL_MIN,
                    widthCellMax
            );
        }
    }

    /**
     * Валидация типа LB.
     *
     * <p>TypeLb больше не enum. Поэтому создаём его
     * из параметров DTO.</p>
     */
    static boolean validateLBType(
            ValidationResult result,
            LBDTO lb
    ) {
        if (lb.getType() == null || lb.getType().isBlank()) {
            result.addError(
                    result,
                    "type",
                    "Тип модуля не указан",
                    null,
                    null,
                    null
            );

            return false;
        }

        validateRange(
                result,
                "deltaWidth",
                lb.getDeltaWidth(),
                TYPE_DELTA_WIDTH_MIN,
                TYPE_DELTA_WIDTH_MAX,
                "Добавочная ширина"
        );

        validateRange(
                result,
                "shelfThick",
                lb.getShelfThick(),
                TYPE_SHELF_THICK_MIN,
                TYPE_SHELF_THICK_MAX,
                "Толщина полки"
        );

        validateRange(
                result,
                "serviceZoneWidth",
                lb.getServiceZoneWidth(),
                TYPE_SERVICE_ZONE_WIDTH_MIN,
                TYPE_SERVICE_ZONE_WIDTH_MAX,
                "Ширина сервисной зоны"
        );

        return true;
    }

    /**
     * Валидация LB.
     */
    public static ValidationResult validateLB(LBDTO lb) {

        logger.info(
                "Валидация размеров LB (id:{} type:{})",
                lb.getId(),
                lb.getType()
        );

        ValidationResult result =
                new ValidationResult(
                        "LB",
                        lb.getId()
                );

        validateRange(
                result,
                "upperFrame",
                lb.getUpperFrame(),
                UPPER_FRAME_MIN,
                UPPER_FRAME_MAX,
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lb.getBottomFrame(),
                BOTTOM_FRAME_MIN,
                BOTTOM_FRAME_MAX,
                "Нижняя рама"
        );

        if (!validateLBType(result, lb)) {
            logValidationResult(result);
            return result;
        }

        /*
         * TypeLb теперь обычный объект.
         */
        TypeLb typeLb;

        try {
            typeLb = new TypeLb(
                    lb.getType(),
                    lb.getDeltaWidth(),
                    lb.getShelfThick(),
                    lb.getServiceZoneWidth()
            );
        } catch (IllegalArgumentException e) {

            result.addError(
                    result,
                    "type",
                    "Некорректные параметры типа LB: "
                            + e.getMessage(),
                    null,
                    null,
                    null
            );

            logValidationResult(result);
            return result;
        }

        /*
         * Высота модуля должна обеспечивать
         * минимальную высоту ячейки.
         */
        int heightMin =
                Math.max(
                        HEIGHT_MIN,
                        HEIGHT_CELL_MIN
                                + lb.getUpperFrame()
                                + lb.getBottomFrame()
                );

        validateRange(
                result,
                "height",
                lb.getHeight(),
                heightMin,
                HEIGHT_MAX,
                "Высота модуля"
        );

        /*
         * Глубина модуля.
         */
        validateRange(
                result,
                "depth",
                lb.getDepth(),
                DEPTH_MIN,
                DEPTH_MAX,
                "Глубина модуля"
        );

        /*
         * Ширина модуля зависит от выбранного типа LB.
         */
        int widthMin =
                WIDTH_CELL_MIN
                        + typeLb.getDeltaWidth();

        validateRange(
                result,
                "width",
                lb.getWidth(),
                widthMin,
                WIDTH_MAX,
                "Ширина модуля"
        );

        /*
         * Толщина дверцы.
         */
        validateRange(
                result,
                "doorThickness",
                lb.getDoorThickness(),
                DOOR_THICKNESS_MIN,
                DOOR_THICKNESS_MAX,
                "Толщина дверцы"
        );

        /*
         * Количество ячеек.
         */
        validateLBCellCount(
                result,
                lb,
                typeLb
        );

        /*
         * Размеры ячеек.
         */
        validateLBCellDimensions(
                result,
                lb,
                typeLb
        );

        logValidationResult(result);

        return result;
    }

    /**
     * Валидация LBC.
     *
     * <p>LBC проверяется как storage-модуль и как модуль управления.
     * Он остаётся одним физическим модулем.</p>
     */
    public static ValidationResult validateLBC(LBCDTO lbc) {

        logger.info(
                "Валидация размеров LBC (id:{} type:{})",
                lbc.getId(),
                lbc.getType()
        );

        ValidationResult result =
                new ValidationResult(
                        "LBC",
                        lbc.getId()
                );

        validateRange(
                result,
                "upperFrame",
                lbc.getUpperFrame(),
                UPPER_FRAME_MIN,
                UPPER_FRAME_MAX,
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lbc.getBottomFrame(),
                BOTTOM_FRAME_MIN,
                BOTTOM_FRAME_MAX,
                "Нижняя рама"
        );

        if (lbc.getType() == null || lbc.getType().isBlank()) {
            result.addError(
                    result,
                    "type",
                    "Тип модуля не указан",
                    null,
                    null,
                    null
            );
            logValidationResult(result);
            return result;
        }

        validateRange(
                result,
                "deltaWidth",
                lbc.getDeltaWidth(),
                TYPE_DELTA_WIDTH_MIN,
                TYPE_DELTA_WIDTH_MAX,
                "Добавочная ширина"
        );

        validateRange(
                result,
                "shelfThick",
                lbc.getShelfThick(),
                TYPE_SHELF_THICK_MIN,
                TYPE_SHELF_THICK_MAX,
                "Толщина полки"
        );

        validateRange(
                result,
                "serviceZoneWidth",
                lbc.getServiceZoneWidth(),
                TYPE_SERVICE_ZONE_WIDTH_MIN,
                TYPE_SERVICE_ZONE_WIDTH_MAX,
                "Ширина сервисной зоны"
        );

        TypeLb typeLb;

        try {
            typeLb = new TypeLb(
                    lbc.getType(),
                    lbc.getDeltaWidth(),
                    lbc.getShelfThick(),
                    lbc.getServiceZoneWidth()
            );
        } catch (IllegalArgumentException e) {
            result.addError(
                    result,
                    "type",
                    "Некорректные параметры типа LBC: "
                            + e.getMessage(),
                    null,
                    null,
                    null
            );
            logValidationResult(result);
            return result;
        }

        int heightMin =
                Math.max(
                        HEIGHT_MIN,
                        HEIGHT_CELL_MIN
                                + lbc.getUpperFrame()
                                + lbc.getBottomFrame()
                );

        validateRange(
                result,
                "height",
                lbc.getHeight(),
                heightMin,
                HEIGHT_MAX,
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                lbc.getDepth(),
                DEPTH_MIN,
                DEPTH_MAX,
                "Глубина модуля"
        );

        int storageWidthMin =
                WIDTH_CELL_MIN
                        + typeLb.getDeltaWidth();

        int controlWidthMin =
                requiredLBCControlWidth(
                        lbc.getDisplay()
                );

        int widthMin =
                Math.max(
                        storageWidthMin,
                        controlWidthMin
                );

        validateRange(
                result,
                "width",
                lbc.getWidth(),
                widthMin,
                WIDTH_MAX,
                "Ширина модуля"
        );

        validateRange(
                result,
                "doorThickness",
                lbc.getDoorThickness(),
                DOOR_THICKNESS_MIN,
                DOOR_THICKNESS_MAX,
                "Толщина дверцы"
        );

        int usableHeight =
                lbc.getHeight()
                        - lbc.getUpperFrame()
                        - lbc.getBottomFrame();

        if (usableHeight > 0) {
            int calculatedCountCellsMax =
                    (usableHeight + typeLb.getShelfThick())
                            / (HEIGHT_CELL_MIN + typeLb.getShelfThick());

            int countCellsMax =
                    Math.min(
                            calculatedCountCellsMax,
                            COUNT_CELLS_MAX
                    );

            validateRange(
                    result,
                    "countCells",
                    lbc.getCountCells(),
                    COUNT_CELLS_MIN,
                    countCellsMax,
                    "Количество ячеек"
            );
        }

        if (lbc.getCountCells() > 0) {
            double heightCell =
                    (
                            lbc.getHeight()
                                    - lbc.getUpperFrame()
                                    - lbc.getBottomFrame()
                                    - (lbc.getCountCells() - 1)
                                    * typeLb.getShelfThick()
                    ) / (double) lbc.getCountCells();

            if (heightCell < HEIGHT_CELL_MIN) {
                result.addError(
                        result,
                        "heightCell",
                        "Высота ячейки меньше допустимой",
                        String.format("%.2f", heightCell),
                        HEIGHT_CELL_MIN,
                        lbc.getHeight()
                );
            }

            int depthCell =
                    lbc.getDepth() - lbc.getDoorThickness();

            if (depthCell < DEPTH_CELL_MIN) {
                result.addError(
                        result,
                        "depthCell",
                        "Глубина ячейки меньше допустимой",
                        depthCell,
                        DEPTH_CELL_MIN,
                        DEPTH_CELL_MAX
                );
            } else if (depthCell > DEPTH_CELL_MAX) {
                result.addError(
                        result,
                        "depthCell",
                        "Глубина ячейки больше допустимой",
                        depthCell,
                        DEPTH_CELL_MIN,
                        DEPTH_CELL_MAX
                );
            }

            int widthCell =
                    lbc.getWidth() - typeLb.getDeltaWidth();

            int widthCellMax =
                    WIDTH_MAX - typeLb.getDeltaWidth();

            if (widthCell < WIDTH_CELL_MIN) {
                result.addError(
                        result,
                        "widthCell",
                        "Ширина ячейки меньше допустимой",
                        widthCell,
                        WIDTH_CELL_MIN,
                        widthCellMax
                );
            } else if (widthCell > widthCellMax) {
                result.addError(
                        result,
                        "widthCell",
                        "Ширина ячейки больше допустимой",
                        widthCell,
                        WIDTH_CELL_MIN,
                        widthCellMax
                );
            }
        }

        validateLBCConsistency(
                result,
                lbc
        );

        logValidationResult(result);
        return result;
    }

    private static int requiredLBCControlWidth(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return 0;
        }

        if (displayName.equals(Display.NONE.getName())) {
            return 0;
        }

        return List.of(
                        Display.LC10,
                        Display.LC17,
                        Display.LC19
                )
                .stream()
                .filter(display ->
                        display.getName().equals(displayName))
                .mapToInt(Display::getWidth)
                .findFirst()
                .orElse(0);
    }

    /**
     * Валидация LC.
     */
    public static ValidationResult validateLC(LCDTO lc) {

        logger.info(
                "Валидация размеров LC (id:{})",
                lc.getId()
        );

        ValidationResult result =
                new ValidationResult(
                        "LC",
                        lc.getId()
                );

        validateRange(
                result,
                "upperFrame",
                lc.getUpperFrame(),
                UPPER_FRAME_MIN,
                UPPER_FRAME_MAX,
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lc.getBottomFrame(),
                BOTTOM_FRAME_MIN,
                BOTTOM_FRAME_MAX,
                "Нижняя рама"
        );

        validateRange(
                result,
                "height",
                lc.getHeight(),
                HEIGHT_LC_MIN,
                HEIGHT_MAX,
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                lc.getDepth(),
                DEPTH_MIN,
                DEPTH_MAX,
                "Глубина модуля"
        );

        validateLCConsistency(
                result,
                lc
        );

        logValidationResult(result);

        return result;
    }

    /**
     * Валидация ALS.
     */
    public static ValidationResult validateALS(ALSDTO als) {

        logger.info(
                "Валидация размеров ALS (id:{})",
                als.getId()
        );

        ValidationResult result =
                new ValidationResult(
                        "ALS",
                        als.getId()
                );

        validateRange(
                result,
                "upperFrame",
                als.getUpperFrame(),
                UPPER_FRAME_MIN,
                UPPER_FRAME_MAX,
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                als.getBottomFrame(),
                BOTTOM_FRAME_MIN,
                BOTTOM_FRAME_MAX,
                "Нижняя рама"
        );

        validateRange(
                result,
                "height",
                als.getHeight(),
                HEIGHT_MIN,
                HEIGHT_MAX,
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                als.getDepth(),
                DEPTH_MIN,
                DEPTH_MAX,
                "Глубина модуля"
        );

        validateALSConsistency(
                result,
                als
        );

        logValidationResult(result);

        return result;
    }

    /**
     * Глубокая валидация ALS.
     */
    public static List<ValidationResult> deepValidateALS(
            ALSDTO als
    ) {
        logger.info(
                "Глубокая валидация ALS (id:{})",
                als.getId()
        );

        List<ValidationResult> results =
                new ArrayList<>();

        results.add(
                validateALS(als)
        );

        if (als.getLC() != null) {
            results.add(
                    validateLC(als.getLC())
            );
        }

        if (als.getLBC() != null) {
            results.add(
                    validateLBC(als.getLBC())
            );
        }

        if (als.getLbList() != null) {
            for (LBDTO lb : als.getLbList()) {
                results.add(
                        validateLB(lb)
                );
            }
        }

        logger.info(
                "Глубокая валидация ALS завершена. "
                        + "Всего результатов: {}. Ошибок: {}",
                results.size(),
                results.stream()
                        .filter(r -> !r.isValid())
                        .count()
        );

        return results;
    }

    /**
     * Валидация всего проекта.
     */
    public static List<ValidationResult> validateProject(
            ProjectDTO projectDTO
    ) {
        logger.info(
                "Валидация проекта (id:{} ALS count:{})",
                projectDTO.getId(),
                projectDTO.getAlsList().size()
        );

        List<ValidationResult> allResults =
                new ArrayList<>();

        for (ALSDTO als : projectDTO.getAlsList()) {
            allResults.addAll(
                    deepValidateALS(als)
            );
        }

        logger.info(
                "Валидация проекта завершена. "
                        + "Всего результатов: {}. Ошибок: {}",
                allResults.size(),
                allResults.stream()
                        .filter(r -> !r.isValid())
                        .count()
        );

        return allResults;
    }

    private static void logValidationResult(
            ValidationResult result
    ) {
        if (result.isValid()) {
            logger.info(
                    "{} (id:{}) - ошибок не найдено",
                    result.getObjectType(),
                    result.getObjectId()
            );
        } else {
            logger.warn(
                    "{} (id:{}) - найдено {} ошибок",
                    result.getObjectType(),
                    result.getObjectId(),
                    result.getErrorCount()
            );

            result.getErrors()
                    .forEach(
                            error -> logger.warn(
                                    "  - {}",
                                    error
                            )
                    );
        }
    }

    private static List<String> convertValidationResultToStrings(
            ValidationResult result
    ) {
        List<String> strings =
                new ArrayList<>();

        result.getErrors()
                .forEach(
                        error -> strings.add(
                                error.toString()
                        )
                );

        return strings;
    }

    public static List<String> getErrorValidateALSSizesList(
            ALSDTO als
    ) {
        return convertValidationResultToStrings(
                validateALS(als)
        );
    }

    public static List<String> getErrorValidateLCSizesList(
            LCDTO lc
    ) {
        return convertValidationResultToStrings(
                validateLC(lc)
        );
    }

    public static List<String> getErrorValidateLBCSizesList(
            LBCDTO lbc
    ) {
        return convertValidationResultToStrings(
                validateLBC(lbc)
        );
    }


    public static List<List<String>> getErrorValidateLBSizesLists(
            ALSDTO als
    ) {
        List<List<String>> result =
                new ArrayList<>();

        for (LBDTO lb : als.getLbList()) {

            List<String> errors =
                    getErrorValidateLBSizesList(lb);

            if (!errors.isEmpty()) {
                result.add(errors);
            }
        }

        return result;
    }

    public static List<String> getErrorValidateLBSizesList(
            LBDTO lb
    ) {
        return convertValidationResultToStrings(
                validateLB(lb)
        );
    }

    public static List<List<List<List<String>>>>
    getErrorValidateProjectSizeList(
            ProjectDTO projectDTO
    ) {
        List<List<List<List<String>>>> result =
                new ArrayList<>();

        int validCount = 0;

        for (ALSDTO als : projectDTO.getAlsList()) {

            List<String> alsErrors =
                    getErrorValidateALSSizesList(als);

            List<String> controlErrors =
                    als.getLBC() != null
                            ? getErrorValidateLBCSizesList(als.getLBC())
                            : als.getLC() != null
                            ? getErrorValidateLCSizesList(als.getLC())
                            : List.of();

            List<List<String>> lbErrors =
                    getErrorValidateLBSizesLists(als);

            if (als.getLBC() != null) {
                lbErrors.add(
                        getErrorValidateLBCSizesList(
                                als.getLBC()
                        )
                );
            }

            List<List<String>> alsErrorsNested =
                    new ArrayList<>();

            List<List<String>> lcErrorsNested =
                    new ArrayList<>();

            alsErrorsNested.add(alsErrors);
            lcErrorsNested.add(controlErrors);

            List<List<List<String>>> alsResult =
                    new ArrayList<>();

            alsResult.add(alsErrorsNested);
            alsResult.add(lcErrorsNested);
            alsResult.add(lbErrors);

            if (
                    alsErrors.isEmpty()
                            && controlErrors.isEmpty()
                            && lbErrors.isEmpty()
            ) {
                validCount++;
            }

            result.add(alsResult);
        }

        if (validCount == result.size()) {
            result.clear();
        }

        return result;
    }
}