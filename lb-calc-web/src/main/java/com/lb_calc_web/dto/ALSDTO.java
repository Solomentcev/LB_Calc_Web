package com.lb_calc_web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DTO конфигурации ALS, включая управляющий модуль, модули хранения и геометрические параметры. */
@Schema(description = "Конфигурация автоматизированной камеры хранения: геометрия, состав модулей и их физический порядок.")
public class ALSDTO {

    private Long id;
    private String name;
    private String description;

    private int height;
    private int depth;
    private int width;

    private int upperFrame;
    private int bottomFrame;

    private int depthCell;
    private int countCells;

    private String colorDoor;
    private String colorBody;

    /**
     * Обычный модуль управления.
     */
    @Valid
    private LCDTO lc;

    /**
     * Комбинированный модуль.
     * LBC одновременно является storage- и control-модулем.
     */
    @Valid
    private LBCDTO lbc;

    /** Функциональные возможности ALS, синхронизированные с управляющим модулем. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private FunctionalConfigurationDTO functionalConfiguration = new FunctionalConfigurationDTO();

    /**
     * Положение единственного модуля управления.
     *
     * <p>Название обобщено, поскольку control-модулем может быть
     * как LC, так и LBC.</p>
     */
    @JsonProperty("positionControlModule")
    private String positionControlModule;

    /**
     * Список обычных LB.
     *
     * <p>LBC хранится отдельно в DTO, но на уровне Domain
     * он также входит в storage-модули ALS.</p>
     */
    @Valid
    private List<LBDTO> lbList = new ArrayList<>();

    /**
     * Физический порядок модулей слева направо.
     *
     * <p>Если поле отсутствует во входном DTO, порядок восстанавливается
     * по прежнему контракту из lbList и positionControlModule.</p>
     */
    @Valid
    private List<ALSModuleOrderDTO> moduleOrder;

    @JsonIgnore
    private String stringALSImage;

    public ALSDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getUpperFrame() {
        return upperFrame;
    }

    public void setUpperFrame(int upperFrame) {
        this.upperFrame = upperFrame;
    }

    public int getBottomFrame() {
        return bottomFrame;
    }

    public void setBottomFrame(int bottomFrame) {
        this.bottomFrame = bottomFrame;
    }

    public int getDepthCell() {
        return depthCell;
    }

    public void setDepthCell(int depthCell) {
        this.depthCell = depthCell;
    }

    public int getCountCells() {
        return countCells;
    }

    public void setCountCells(int countCells) {
        this.countCells = countCells;
    }

    public String getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(String colorDoor) {
        this.colorDoor = colorDoor;
    }

    public String getColorBody() {
        return colorBody;
    }

    public void setColorBody(String colorBody) {
        this.colorBody = colorBody;
    }

    public LCDTO getLC() {
        return lc;
    }

    public void setLC(LCDTO lc) {
        this.lc = lc;
    }

    public LBCDTO getLBC() {
        return lbc;
    }

    public void setLBC(LBCDTO lbc) {
        this.lbc = lbc;
    }

    /** Возвращает, передавал ли клиент выбор физического оборудования управляющего модуля. */
    @JsonIgnore
    public boolean isEquipmentSelectionProvided() {
        return lc != null ? lc.isEquipmentProvided()
                : lbc != null && lbc.isEquipmentProvided();
    }

    public FunctionalConfigurationDTO getFunctionalConfiguration() {
        return functionalConfiguration;
    }

    public void setFunctionalConfiguration(FunctionalConfigurationDTO configuration) {
        this.functionalConfiguration = configuration;
    }

    public String getPositionControlModule() {
        return positionControlModule;
    }

    public void setPositionControlModule(
            String positionControlModule
    ) {
        this.positionControlModule = positionControlModule;
    }

    public List<LBDTO> getLbList() {
        return lbList;
    }

    public void setLbList(List<LBDTO> lbList) {
        this.lbList =
                lbList == null
                        ? new ArrayList<>()
                        : lbList;
    }

    public List<ALSModuleOrderDTO> getModuleOrder() {
        return moduleOrder;
    }

    public void setModuleOrder(List<ALSModuleOrderDTO> moduleOrder) {
        this.moduleOrder = moduleOrder;
    }

    public String getStringALSImage() {
        return stringALSImage;
    }

    public void setStringALSImage(String stringALSImage) {
        this.stringALSImage = stringALSImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ALSDTO alsdto = (ALSDTO) o;

        return height == alsdto.height
                && depth == alsdto.depth
                && width == alsdto.width
                && upperFrame == alsdto.upperFrame
                && bottomFrame == alsdto.bottomFrame
                && depthCell == alsdto.depthCell
                && countCells == alsdto.countCells
                && Objects.equals(lc, alsdto.lc)
                && Objects.equals(lbc, alsdto.lbc)
                && Objects.equals(functionalConfiguration, alsdto.functionalConfiguration)
                && Objects.equals(
                positionControlModule,
                alsdto.positionControlModule
        )
                && Objects.equals(
                colorDoor,
                alsdto.colorDoor
        )
                && Objects.equals(
                colorBody,
                alsdto.colorBody
        )
                && Objects.equals(lbList, alsdto.lbList)
                && Objects.equals(moduleOrder, alsdto.moduleOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                height,
                depth,
                width,
                upperFrame,
                bottomFrame,
                depthCell,
                countCells,
                lc,
                lbc,
                functionalConfiguration,
                positionControlModule,
                colorDoor,
                colorBody,
                lbList,
                moduleOrder
        );
    }

    @Override
    public String toString() {
        return "ALSDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", height=" + height +
                ", depth=" + depth +
                ", width=" + width +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", depthCell=" + depthCell +
                ", countCells=" + countCells +
                ", lc=" + lc +
                ", lbc=" + lbc +
                ", functionalConfiguration=" + functionalConfiguration +
                ", positionControlModule='" +
                positionControlModule + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", colorBody='" + colorBody + '\'' +
                ", lbList=" + lbList +
                '}';
    }
}
