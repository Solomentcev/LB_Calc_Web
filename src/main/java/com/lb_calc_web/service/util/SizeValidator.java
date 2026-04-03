package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.model.attributes.TypeLb;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Component
public class SizeValidator {
    private SizeValidator() {

    }

    private final static Logger logger = LoggerFactory.getLogger(SizeValidator.class);
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

    private static int WIDTH_CELL_MIN;
    private static int WIDTH_MAX;

    private static int DEPTH_CELL_MIN;
    private static int DEPTH_MIN;
    private static int DEPTH_MAX;
    private static int DEPTH_CELL_MAX;

    @Value("${size.frame.upper.min:20}")
    private int upperFrameMin;

    @Value("${size.frame.upper.max:300}")
    private int upperFrameMax;

    @Value("${size.frame.bottom.min:20}")
    private int bottomFrameMin;

    @Value("${size.frame.bottom.max:300}")
    private int bottomFrameMax;

    @Value("${size.height.cell.min:85}")
    private int heightCellMin;

    @Value("${size.height.max:2300}")
    private int heightMax;

    @Value("${size.height.min:125}")
    private int heightMin;

    @Value("${size.height.lc.min:600}")
    private int heightLcMin;

    @Value("${size.height.lc.panel.min:300}")
    private int heightLcPanelMin;

    @Value("${size.count.cells.min:1}")
    private int countCellsMin;

    @Value("${size.width.cell.min:100}")
    private int widthCellMin;

    @Value("${size.width.max:1200}")
    private int widthMax;

    @Value("${size.depth.cell.min:100}")
    private int depthCellMin;

    @Value("${size.depth.min:170}")
    private int depthMin;

    @Value("${size.depth.max:900}")
    private int depthMax;

    @Value("${size.depth.cell.max:880}")
    private int depthCellMax;

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

        WIDTH_CELL_MIN = widthCellMin;
        WIDTH_MAX = widthMax;

        DEPTH_CELL_MIN = depthCellMin;
        DEPTH_MIN = depthMin;
        DEPTH_MAX = depthMax;
        DEPTH_CELL_MAX = depthCellMax;
        logger.info("SizeValidator config loaded");
    }

    public static int getHeightLcPanelMin() {
        return HEIGHT_LC_PANEL_MIN;
    }

    public static void setHeightLcPanelMin(int heightLcPanelMin) {
        HEIGHT_LC_PANEL_MIN = heightLcPanelMin;
    }

    public static void setDepthCellMax(int depthCellMax) {
        DEPTH_CELL_MAX = depthCellMax;
    }

    public static void setDepthMax(int depthMax) {
        DEPTH_MAX = depthMax;
    }

    public static void setDepthMin(int depthMin) {
        DEPTH_MIN = depthMin;
    }

    public static void setDepthCellMin(int depthCellMin) {
        DEPTH_CELL_MIN = depthCellMin;
    }

    public static void setWidthMax(int widthMax) {
        WIDTH_MAX = widthMax;
    }

    public static void setWidthCellMin(int widthCellMin) {
        WIDTH_CELL_MIN = widthCellMin;
    }

    public static void setCountCellsMin(int countCellsMin) {
        COUNT_CELLS_MIN = countCellsMin;
    }

    public static void setHeightLcMin(int heightLcMin) {
        HEIGHT_LC_MIN = heightLcMin;
    }

    public static void setHeightMin(int heightMin) {
        HEIGHT_MIN = heightMin;
    }

    public static void setHeightMax(int heightMax) {
        HEIGHT_MAX = heightMax;
    }

    public static void setHeightCellMin(int heightCellMin) {
        HEIGHT_CELL_MIN = heightCellMin;
    }

    public static void setBottomFrameMax(int bottomFrameMax) {
        BOTTOM_FRAME_MAX = bottomFrameMax;
    }

    public static void setBottomFrameMin(int bottomFrameMin) {
        BOTTOM_FRAME_MIN = bottomFrameMin;
    }

    public static void setUpperFrameMax(int upperFrameMax) {
        UPPER_FRAME_MAX = upperFrameMax;
    }

    public static void setUpperFrameMin(int upperFrameMin) {
        UPPER_FRAME_MIN = upperFrameMin;
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

    public static int getCountCellsMin() {
        return COUNT_CELLS_MIN;
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

    public static int getDepthMin() {
        return DEPTH_MIN;
    }

    public static int getDepthMax() {
        return DEPTH_MAX;
    }

    public static int getDepthCellMax() {
        return DEPTH_CELL_MAX;
    }


    /**
     * Валидация значения в диапазоне
     */
    static void validateRange(ValidationResult result, String fieldName,
                                      int value, int min, int max, String fieldLabel) {
        if (value < min) {
            result.addError(fieldName,
                    fieldLabel + " меньше допустимой",
                    value, min, max);
        } else if (value > max) {
            result.addError(fieldName,
                    fieldLabel + " больше допустимой",
                    value, min, max);
        }
    }
    /**
     * Дополнительные проверки консистентности LC
     */
    static void validateLCConsistency(ValidationResult result, LCDTO lc) {
        int usableHeight = lc.getHeight() - lc.getUpperFrame() - lc.getBottomFrame();

        if (usableHeight < HEIGHT_LC_PANEL_MIN) {
            result.addError("heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight, HEIGHT_LC_PANEL_MIN, HEIGHT_MAX - UPPER_FRAME_MAX - BOTTOM_FRAME_MAX);
        }
    }
    /**
     * Логирование результата валидации
     */
    private static void logValidationResult(ValidationResult result) {
        if (result.isValid()) {
            logger.info("✓ {} (id:{}) - ошибок не найдено",
                    result.getObjectType(), result.getObjectId());
        } else {
            logger.warn("✗ {} (id:{}) - найдено {} ошибок",
                    result.getObjectType(), result.getObjectId(), result.getErrorCount());
            result.getErrors().forEach(e -> logger.warn("  - {}", e));
        }
    }
    private static List<String> convertValidationResultToStrings(ValidationResult result) {
        List<String> strings = new ArrayList<>();
        result.getErrors().forEach(e -> strings.add(e.toString()));
        return strings;
    }

    /**
     * Дополнительные проверки консистентности ALS
     */
    static void validateALSConsistency(ValidationResult result, ALSDTO als) {
        if (als.getLC() == null) {
            result.addError("lc", "МУ отсутствует", null, null, null);
        }
        if (als.getLbList() == null || als.getLbList().isEmpty()) {
            result.addError("lbList", "Список МХ пустой", null, null, null);
        }
        int usableHeight = als.getHeight() - als.getUpperFrame() - als.getBottomFrame();
        if (usableHeight < HEIGHT_CELL_MIN) {
            result.addError("heightConsistency",
                    "Полезная высота слишком мала для ячейки",
                    usableHeight, HEIGHT_CELL_MIN, HEIGHT_MAX - UPPER_FRAME_MAX - BOTTOM_FRAME_MAX);
        }

        if (usableHeight < HEIGHT_LC_PANEL_MIN) {
            result.addError("heightConsistency",
                    "Полезная высота слишком мала для панели управления",
                    usableHeight, HEIGHT_LC_PANEL_MIN, HEIGHT_MAX - UPPER_FRAME_MAX - BOTTOM_FRAME_MAX);
        }
    }


    /**
     * Валидация количества ячеек в LB
     */
    static void validateLBCellCount(ValidationResult result, LBDTO lb, TypeLb typeLb) {
        int heightCellMax = lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame();
        int countCellsMax = (heightCellMax + typeLb.getShelfThick()) /
                (HEIGHT_CELL_MIN+typeLb.getShelfThick());
        int countCells= lb.getCountCells();
        if (countCells < COUNT_CELLS_MIN) {
            result.addError("countCells",
                    "Количество ячеек меньше допустимого",
                    countCells, COUNT_CELLS_MIN, countCellsMax);
        } else if (countCells > countCellsMax) {
            result.addError("countCells",
                    "Количество ячеек больше допустимого",
                    countCells, COUNT_CELLS_MIN, countCellsMax);
        }
    }

    /**
     * Валидация размеров ячеек в LB
     */
    static void validateLBCellDimensions(ValidationResult result, LBDTO lb, TypeLb typeLb) {
        // Высота ячейки
        double heightCell = (double) (lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame() -
                (lb.getCountCells() - 1) * typeLb.getShelfThick()) / lb.getCountCells();

        if (heightCell < HEIGHT_CELL_MIN) {
            result.addError("heightCell",
                    "Высота ячейки меньше допустимой",
                    String.format("%.2f", heightCell),
                    HEIGHT_CELL_MIN,
                    lb.getHeight() - lb.getUpperFrame() - lb.getBottomFrame());
        }

        // Глубина ячейки
        int depthCell = lb.getDepth() - 20;
        if (depthCell < DEPTH_CELL_MIN) {
            result.addError("depthCell",
                    "Глубина ячейки меньше допустимой",
                    depthCell, DEPTH_CELL_MIN, DEPTH_CELL_MAX);
        } else if (depthCell > DEPTH_CELL_MAX) {
            result.addError("depthCell",
                    "Глубина ячейки больше допустимой",
                    depthCell, DEPTH_CELL_MIN, DEPTH_CELL_MAX);
        }

        // Ширина ячейки
        int widthCell = lb.getWidth() - typeLb.getDeltaWidth();
        int widthCellMax = WIDTH_MAX - typeLb.getDeltaWidth();

        if (widthCell < WIDTH_CELL_MIN) {
            result.addError("widthCell",
                    "Ширина ячейки меньше допустимой",
                    widthCell, WIDTH_CELL_MIN, widthCellMax);
        } else if (widthCell > widthCellMax) {
            result.addError("widthCell",
                    "Ширина ячейки больше допустимой",
                    widthCell, WIDTH_CELL_MIN, widthCellMax);
        }
    }
    /**
     * Валидация всего проекта
     */
    public static List<ValidationResult> validateProject(ProjectDTO projectDTO) {
        logger.info("Валидация проекта (id:{} ALS count:{})", projectDTO.getId(), projectDTO.getAlsList().size());

        List<ValidationResult> allResults = new ArrayList<>();

        for (ALSDTO als : projectDTO.getAlsList()) {
            List<ValidationResult> alsResult = deepValidateALS(als);
            allResults.addAll(alsResult);
        }

        logger.info("Валидация проекта завершена. Всего результатов: {}. Ошибок: {}",
                allResults.size(),
                allResults.stream().filter(r -> !r.isValid()).count());

        return allResults;
    }
    public static List<ValidationResult> deepValidateALS(ALSDTO als){
        logger.info("Глубокая валидация размеров АКХ (id:{})", als.getId());
        List<ValidationResult> allResults = new ArrayList<>();
        // Валидация ALS
        ValidationResult alsResult = validateALS(als);
        allResults.add(alsResult);

        // Валидация LC
        if (als.getLC() != null) {
            ValidationResult lcResult = validateLC(als.getLC());
            allResults.add(lcResult);
        }
        // Валидация LB
        if (als.getLbList()!=null) {
            for (LBDTO lb : als.getLbList()) {
                ValidationResult lbResult = validateLB(lb);
                allResults.add(lbResult);
            }
        }
        logger.info("Глубокая валидация АКХ завершена. Всего результатов: {}. Ошибок: {}",
                allResults.size(),
                allResults.stream().filter(r -> !r.isValid()).count());

        return allResults;
    }
    /**
     * Валидация размеров ALS (Автоматическая камера хранения)
     */
    public static ValidationResult validateALS(ALSDTO als) {
        logger.info("Валидация размеров АКХ (id:{})", als.getId());

        ValidationResult result = new ValidationResult("ALS", als.getId());

        // Проверка верхней рамы
        validateRange(result, "upperFrame", als.getUpperFrame(),
                UPPER_FRAME_MIN, UPPER_FRAME_MAX,
                "Верхняя рама");

        // Проверка нижней рамы
        validateRange(result, "bottomFrame", als.getBottomFrame(),
                BOTTOM_FRAME_MIN, BOTTOM_FRAME_MAX,
                "Нижняя рама");

        // Проверка высоты
        validateRange(result, "height", als.getHeight(),
                HEIGHT_MIN, HEIGHT_MAX,
                "Высота модуля");

        // Проверка глубины
        validateRange(result, "depth", als.getDepth(),
                DEPTH_MIN, DEPTH_MAX,
                "Глубина модуля");

        // Проверка консистентности размеров
        validateALSConsistency(result, als);

        logValidationResult(result);
        return result;
    }
    public static ValidationResult validateLC(LCDTO lc) {
        logger.info("Валидация размеров МУ (id:{}})", lc.getId());

        ValidationResult result = new ValidationResult("LC", lc.getId());

        // Проверка верхней рамы
        validateRange(result, "upperFrame", lc.getUpperFrame(),
                UPPER_FRAME_MIN, UPPER_FRAME_MAX,
                "Верхняя рама");

        // Проверка нижней рамы
        validateRange(result, "bottomFrame", lc.getBottomFrame(),
                BOTTOM_FRAME_MIN, BOTTOM_FRAME_MAX,
                "Нижняя рама");

        // Проверка высоты
        validateRange(result, "height", lc.getHeight(),
                HEIGHT_LC_MIN, HEIGHT_MAX,
                "Высота модуля");

        // Проверка глубины
        validateRange(result, "depth", lc.getDepth(),
                DEPTH_MIN, DEPTH_MAX,
                "Глубина модуля");

        // Проверка консистентности размеров
        validateLCConsistency(result, lc);

        logValidationResult(result);
        return result;
    }
    /**
     * Валидация размеров LB (Модуль хранения)
     */
    public static ValidationResult validateLB(LBDTO lb) {
        logger.info("Валидация размеров МХ (id:{} type:{})", lb.getId(),lb.getType());

        ValidationResult result = new ValidationResult("LB", lb.getId());

        // Проверка верхней рамы
        validateRange(result, "upperFrame", lb.getUpperFrame(),
                UPPER_FRAME_MIN, UPPER_FRAME_MAX,
                "Верхняя рама");

        // Проверка нижней рамы
        validateRange(result, "bottomFrame", lb.getBottomFrame(),
                BOTTOM_FRAME_MIN, BOTTOM_FRAME_MAX,
                "Нижняя рама");

        // Проверка типа
        if (lb.getType() == null) {
            result.addError("type", "Тип модуля не указан", null, null, null);
            logValidationResult(result);
            return result;
        }

        TypeLb typeLb = TypeLb.valueOf(lb.getType());

        // Проверка высоты
        int heightMin = HEIGHT_CELL_MIN + lb.getUpperFrame() + lb.getBottomFrame();
        validateRange(result, "height", lb.getHeight(),
                heightMin, HEIGHT_MAX,
                "Высота модуля");

        // Проверка глубины
        validateRange(result, "depth", lb.getDepth(),
                DEPTH_MIN, DEPTH_MAX,
                "Глубина модуля");

        // Проверка ширины
        int widthMin = WIDTH_CELL_MIN + typeLb.getDeltaWidth();
        validateRange(result, "width", lb.getWidth(),
                widthMin, WIDTH_MAX,
                "Ширина модуля");

        // Проверка количества ячеек
        validateLBCellCount(result, lb, typeLb);

        // Проверка размеров ячеек
        validateLBCellDimensions(result, lb, typeLb);

        logValidationResult(result);
        return result;
    }
    public static List<String> getErrorValidateALSSizesList(ALSDTO als)  {
        ValidationResult result = validateALS(als);
        return convertValidationResultToStrings(result);
    }
    public static List<String> getErrorValidateLCSizesList(LCDTO lc){
        ValidationResult result=validateLC(lc);
        return convertValidationResultToStrings(result);
    }
    public static List<List<String>> getErrorValidateLBSizesLists(ALSDTO als)  {
        List<List<String>> errorLBLists = new ArrayList<>();
        for (LBDTO lb : als.getLbList()) {
            List<String> errorLBList = getErrorValidateLBSizesList(lb);
            if (!errorLBList.isEmpty()) {
                errorLBLists.add(errorLBList);
            }
        }
        return errorLBLists;
    }
    public static List<String> getErrorValidateLBSizesList(LBDTO lb)  {
        ValidationResult result = validateLB(lb);
        return convertValidationResultToStrings(result);
    }
    public static List<List<List<List<String>>>> getErrorValidateProjectSizeList(ProjectDTO projectDTO){
        List<List<List<List<String>>>> errorProjectList =new ArrayList<>();
        int count=0;
        for(ALSDTO als: projectDTO.getAlsList()){

            List<String> errorALSList=getErrorValidateALSSizesList(als);
            List<String> errorLCList=getErrorValidateLCSizesList(als.getLC());
            List<List<String>> errorLBLists =getErrorValidateLBSizesLists(als);

            List<List<String>> errorALSLists=new ArrayList<>();
            List<List<String>> errorLCLists=new ArrayList<>();
            errorALSLists.add(errorALSList);
            errorLCLists.add(errorLCList);

            List<List<List<String>>> errorsALSLists=new ArrayList<>();
            errorsALSLists.add(errorALSLists);
            errorsALSLists.add(errorLCLists);
            errorsALSLists.add(errorLBLists);

            if(errorALSList.isEmpty() && errorLCList.isEmpty()&&errorLBLists.isEmpty()) count++;
            errorProjectList.add(errorsALSLists);
        }
        if (count==errorProjectList.size()){
            errorProjectList.clear();
        }
        return errorProjectList;
    }
}
