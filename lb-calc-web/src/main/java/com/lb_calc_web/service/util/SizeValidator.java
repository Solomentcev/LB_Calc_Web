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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SizeValidator {

    private final Logger logger =
            LoggerFactory.getLogger(SizeValidator.class);

    private final SizeBounds bounds;

    public SizeValidator(SizeBounds bounds) {
        this.bounds = bounds;
    }

    public int getHeightLcPanelMin() {
        return bounds.getHeightLcPanelMin();
    }

    /**
     * Проверка диапазона.
     */
    void validateRange(
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
    void validateLCConsistency(
            ValidationResult result,
            LCDTO lc
    ) {
        int usableHeight =
                lc.getHeight()
                        - lc.getUpperFrame()
                        - lc.getBottomFrame();

        if (usableHeight < bounds.getHeightLcPanelMin()) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight,
                    bounds.getHeightLcPanelMin(),
                    bounds.getHeightMax()
            );
        }
    }

    /**
     * Проверка консистентности ALS.
     */
    void validateALSConsistency(
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

        if (usableHeight < bounds.getHeightCellMin()) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для ячейки",
                    usableHeight,
                    bounds.getHeightCellMin(),
                    bounds.getHeightMax()
            );
        }

        if (usableHeight < bounds.getHeightLcPanelMin()) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight,
                    bounds.getHeightLcPanelMin(),
                    bounds.getHeightMax()
            );
        }
    }

    /**
     * Проверка консистентности LBC.
     * LBC должен одновременно обеспечивать хранение и панель управления.
     */
    void validateLBCConsistency(
            ValidationResult result,
            LBCDTO lbc
    ) {
        int usableHeight =
                lbc.getHeight()
                        - lbc.getUpperFrame()
                        - lbc.getBottomFrame();

        if (usableHeight < bounds.getHeightLcPanelMin()) {
            result.addError(
                    result,
                    "heightConsistency",
                    "Полезная высота слишком мала для панели управления LBC",
                    usableHeight,
                    bounds.getHeightLcPanelMin(),
                    bounds.getHeightMax()
            );
        }
    }

    /**
     * Валидация количества ячеек LB.
     */
    void validateLBCellCount(
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
                        / (bounds.getHeightCellMin() + typeLb.getShelfThick());

        int countCellsMax =
                Math.min(
                        calculatedCountCellsMax,
                        bounds.getCountCellsMax()
                );

        validateRange(
                result,
                "countCells",
                lb.getCountCells(),
                bounds.getCountCellsMin(),
                countCellsMax,
                "Количество ячеек"
        );
    }

    /**
     * Валидация размеров ячеек LB.
     */
    void validateLBCellDimensions(
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

        if (heightCell < bounds.getHeightCellMin()) {
            result.addError(
                    result,
                    "heightCell",
                    "Высота ячейки меньше допустимой",
                    String.format("%.2f", heightCell),
                    bounds.getHeightCellMin(),
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

        if (depthCell < bounds.getDepthCellMin()) {
            result.addError(
                    result,
                    "depthCell",
                    "Глубина ячейки меньше допустимой",
                    depthCell,
                    bounds.getDepthCellMin(),
                    bounds.getDepthCellMax()
            );
        } else if (depthCell > bounds.getDepthCellMax()) {
            result.addError(
                    result,
                    "depthCell",
                    "Глубина ячейки больше допустимой",
                    depthCell,
                    bounds.getDepthCellMin(),
                    bounds.getDepthCellMax()
            );
        }

        int widthCell =
                lb.getWidth()
                        - typeLb.getDeltaWidth()
                        - typeLb.getServiceZoneWidth();

        int widthCellMax =
                bounds.getWidthMax()
                        - typeLb.getDeltaWidth()
                        - typeLb.getServiceZoneWidth();

        if (widthCell < bounds.getWidthCellMin()) {
            result.addError(
                    result,
                    "widthCell",
                    "Ширина ячейки меньше допустимой",
                    widthCell,
                    bounds.getWidthCellMin(),
                    widthCellMax
            );
        } else if (widthCell > widthCellMax) {
            result.addError(
                    result,
                    "widthCell",
                    "Ширина ячейки больше допустимой",
                    widthCell,
                    bounds.getWidthCellMin(),
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
    boolean validateLBType(
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
                bounds.getTypeDeltaWidthMin(),
                bounds.getTypeDeltaWidthMax(),
                "Добавочная ширина"
        );

        validateRange(
                result,
                "shelfThick",
                lb.getShelfThick(),
                bounds.getTypeShelfThickMin(),
                bounds.getTypeShelfThickMax(),
                "Толщина полки"
        );

        validateRange(
                result,
                "serviceZoneWidth",
                lb.getServiceZoneWidth(),
                bounds.getTypeServiceZoneWidthMin(),
                bounds.getTypeServiceZoneWidthMax(),
                "Ширина сервисной зоны"
        );

        return true;
    }

    /**
     * Валидация LB.
     */
    public ValidationResult validateLB(LBDTO lb) {

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
                bounds.getUpperFrameMin(),
                bounds.getUpperFrameMax(),
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lb.getBottomFrame(),
                bounds.getBottomFrameMin(),
                bounds.getBottomFrameMax(),
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
                        bounds.getHeightMin(),
                        bounds.getHeightCellMin()
                                + lb.getUpperFrame()
                                + lb.getBottomFrame()
                );

        validateRange(
                result,
                "height",
                lb.getHeight(),
                heightMin,
                bounds.getHeightMax(),
                "Высота модуля"
        );

        /*
         * Глубина модуля.
         */
        validateRange(
                result,
                "depth",
                lb.getDepth(),
                bounds.getDepthMin(),
                bounds.getDepthMax(),
                "Глубина модуля"
        );

        /*
         * Ширина модуля зависит от выбранного типа LB.
         */
        int widthMin =
                bounds.getWidthCellMin()
                        + typeLb.getDeltaWidth()
                        + typeLb.getServiceZoneWidth();

        validateRange(
                result,
                "width",
                lb.getWidth(),
                widthMin,
                bounds.getWidthMax(),
                "Ширина модуля"
        );

        /*
         * Толщина дверцы.
         */
        validateRange(
                result,
                "doorThickness",
                lb.getDoorThickness(),
                bounds.getDoorThicknessMin(),
                bounds.getDoorThicknessMax(),
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
    public ValidationResult validateLBC(LBCDTO lbc) {

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
                bounds.getUpperFrameMin(),
                bounds.getUpperFrameMax(),
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lbc.getBottomFrame(),
                bounds.getBottomFrameMin(),
                bounds.getBottomFrameMax(),
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
                bounds.getTypeDeltaWidthMin(),
                bounds.getTypeDeltaWidthMax(),
                "Добавочная ширина"
        );

        validateRange(
                result,
                "shelfThick",
                lbc.getShelfThick(),
                bounds.getTypeShelfThickMin(),
                bounds.getTypeShelfThickMax(),
                "Толщина полки"
        );

        validateRange(
                result,
                "serviceZoneWidth",
                lbc.getServiceZoneWidth(),
                bounds.getTypeServiceZoneWidthMin(),
                bounds.getTypeServiceZoneWidthMax(),
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
                        bounds.getHeightMin(),
                        bounds.getHeightCellMin()
                                + lbc.getUpperFrame()
                                + lbc.getBottomFrame()
                );

        validateRange(
                result,
                "height",
                lbc.getHeight(),
                heightMin,
                bounds.getHeightMax(),
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                lbc.getDepth(),
                bounds.getDepthMin(),
                bounds.getDepthMax(),
                "Глубина модуля"
        );

        int storageWidthMin =
                bounds.getWidthCellMin()
                        + typeLb.getDeltaWidth()
                        + typeLb.getServiceZoneWidth();

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
                bounds.getWidthMax(),
                "Ширина модуля"
        );

        validateRange(
                result,
                "doorThickness",
                lbc.getDoorThickness(),
                bounds.getDoorThicknessMin(),
                bounds.getDoorThicknessMax(),
                "Толщина дверцы"
        );

        int usableHeight =
                lbc.getHeight()
                        - lbc.getUpperFrame()
                        - lbc.getBottomFrame();

        if (usableHeight > 0) {
            int calculatedCountCellsMax =
                    (usableHeight + typeLb.getShelfThick())
                            / (bounds.getHeightCellMin() + typeLb.getShelfThick());

            int countCellsMax =
                    Math.min(
                            calculatedCountCellsMax,
                            bounds.getCountCellsMax()
                    );

            validateRange(
                    result,
                    "countCells",
                    lbc.getCountCells(),
                    bounds.getCountCellsMin(),
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

            if (heightCell < bounds.getHeightCellMin()) {
                result.addError(
                        result,
                        "heightCell",
                        "Высота ячейки меньше допустимой",
                        String.format("%.2f", heightCell),
                        bounds.getHeightCellMin(),
                        lbc.getHeight()
                );
            }

            int depthCell =
                    lbc.getDepth() - lbc.getDoorThickness();

            if (depthCell < bounds.getDepthCellMin()) {
                result.addError(
                        result,
                        "depthCell",
                        "Глубина ячейки меньше допустимой",
                        depthCell,
                        bounds.getDepthCellMin(),
                        bounds.getDepthCellMax()
                );
            } else if (depthCell > bounds.getDepthCellMax()) {
                result.addError(
                        result,
                        "depthCell",
                        "Глубина ячейки больше допустимой",
                        depthCell,
                        bounds.getDepthCellMin(),
                        bounds.getDepthCellMax()
                );
            }

            int widthCell =
                    lbc.getWidth()
                            - typeLb.getDeltaWidth()
                            - typeLb.getServiceZoneWidth();

            int widthCellMax =
                    bounds.getWidthMax()
                            - typeLb.getDeltaWidth()
                            - typeLb.getServiceZoneWidth();

            if (widthCell < bounds.getWidthCellMin()) {
                result.addError(
                        result,
                        "widthCell",
                        "Ширина ячейки меньше допустимой",
                        widthCell,
                        bounds.getWidthCellMin(),
                        widthCellMax
                );
            } else if (widthCell > widthCellMax) {
                result.addError(
                        result,
                        "widthCell",
                        "Ширина ячейки больше допустимой",
                        widthCell,
                        bounds.getWidthCellMin(),
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

    private int requiredLBCControlWidth(String displayName) {
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
    public ValidationResult validateLC(LCDTO lc) {

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
                bounds.getUpperFrameMin(),
                bounds.getUpperFrameMax(),
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                lc.getBottomFrame(),
                bounds.getBottomFrameMin(),
                bounds.getBottomFrameMax(),
                "Нижняя рама"
        );

        validateRange(
                result,
                "height",
                lc.getHeight(),
                bounds.getHeightLcMin(),
                bounds.getHeightMax(),
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                lc.getDepth(),
                bounds.getDepthMin(),
                bounds.getDepthMax(),
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
    public ValidationResult validateALS(ALSDTO als) {

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
                bounds.getUpperFrameMin(),
                bounds.getUpperFrameMax(),
                "Верхняя рама"
        );

        validateRange(
                result,
                "bottomFrame",
                als.getBottomFrame(),
                bounds.getBottomFrameMin(),
                bounds.getBottomFrameMax(),
                "Нижняя рама"
        );

        validateRange(
                result,
                "height",
                als.getHeight(),
                bounds.getHeightMin(),
                bounds.getHeightMax(),
                "Высота модуля"
        );

        validateRange(
                result,
                "depth",
                als.getDepth(),
                bounds.getDepthMin(),
                bounds.getDepthMax(),
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
    public List<ValidationResult> deepValidateALS(
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
    public List<ValidationResult> validateProject(
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

    private void logValidationResult(
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

    private List<String> convertValidationResultToStrings(
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

    public List<String> getErrorValidateALSSizesList(
            ALSDTO als
    ) {
        return convertValidationResultToStrings(
                validateALS(als)
        );
    }

    public List<String> getErrorValidateLCSizesList(
            LCDTO lc
    ) {
        return convertValidationResultToStrings(
                validateLC(lc)
        );
    }

    public List<String> getErrorValidateLBCSizesList(
            LBCDTO lbc
    ) {
        return convertValidationResultToStrings(
                validateLBC(lbc)
        );
    }


    public List<List<String>> getErrorValidateLBSizesLists(
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

    public List<String> getErrorValidateLBSizesList(
            LBDTO lb
    ) {
        return convertValidationResultToStrings(
                validateLB(lb)
        );
    }

    public List<List<List<List<String>>>>
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