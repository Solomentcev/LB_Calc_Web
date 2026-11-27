package com.lb_calc_web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** DTO комбинированного модуля LBC, включая функциональные назначения и выбранное оборудование. */
@Schema(description = "Конфигурация комбинированного модуля LBC, объединяющего модуль хранения и управляющий модуль.")
public class LBCDTO {

    private Long id;

    private String name;
    private String description;

    private String type;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private int shelfThick;
    private int deltaWidth;
    private int serviceZoneWidth;
    private int doorThickness;

    private int countCells;

    private double heightCell;
    private int widthCell;
    private int depthCell;

    private String directionDoorOpening;

    private String colorDoor;
    private String colorBody;

    @Valid
    private FunctionalConfigurationDTO functionalConfiguration;
    /** Выбранные модели с названиями и характеристиками для отображения в форме. */
    @Valid
    private List<EquipmentDTO> selectedEquipment = new ArrayList<>();
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean equipmentProvided;

    @JsonIgnore
    private String stringLBCImage;

    public LBCDTO() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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

    public int getShelfThick() {
        return shelfThick;
    }

    public void setShelfThick(int shelfThick) {
        this.shelfThick = shelfThick;
    }

    public int getDeltaWidth() {
        return deltaWidth;
    }

    public void setDeltaWidth(int deltaWidth) {
        this.deltaWidth = deltaWidth;
    }

    public int getServiceZoneWidth() {
        return serviceZoneWidth;
    }

    public void setServiceZoneWidth(int serviceZoneWidth) {
        this.serviceZoneWidth = serviceZoneWidth;
    }

    public int getDoorThickness() {
        return doorThickness;
    }

    public void setDoorThickness(int doorThickness) {
        this.doorThickness = doorThickness;
    }

    public int getCountCells() {
        return countCells;
    }

    public void setCountCells(int countCells) {
        this.countCells = countCells;
    }

    public double getHeightCell() {
        return heightCell;
    }

    public void setHeightCell(double heightCell) {
        this.heightCell = heightCell;
    }

    public int getWidthCell() {
        return widthCell;
    }

    public void setWidthCell(int widthCell) {
        this.widthCell = widthCell;
    }

    public int getDepthCell() {
        return depthCell;
    }

    public void setDepthCell(int depthCell) {
        this.depthCell = depthCell;
    }

    public String getDirectionDoorOpening() {
        return directionDoorOpening;
    }

    public void setDirectionDoorOpening(String directionDoorOpening) {
        this.directionDoorOpening = directionDoorOpening;
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


    public FunctionalConfigurationDTO getFunctionalConfiguration() {
        return functionalConfiguration;
    }

    public void setFunctionalConfiguration(FunctionalConfigurationDTO configuration) {
        this.functionalConfiguration = configuration;
    }

    public List<EquipmentDTO> getSelectedEquipment() {
        return selectedEquipment;
    }

    public void setSelectedEquipment(List<EquipmentDTO> selectedEquipment) {
        this.selectedEquipment = selectedEquipment == null ? new ArrayList<>() : new ArrayList<>(selectedEquipment);
    }

    public boolean isEquipmentProvided() {
        return equipmentProvided;
    }

    public void setEquipmentProvided(boolean provided) {
        this.equipmentProvided = provided;
    }

    public String getStringLBCImage() {
        return stringLBCImage;
    }

    public void setStringLBCImage(String stringLBCImage) {
        this.stringLBCImage = stringLBCImage;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LBCDTO other)) return false;
        return height == other.height && width == other.width && depth == other.depth
                && upperFrame == other.upperFrame && bottomFrame == other.bottomFrame
                && shelfThick == other.shelfThick && deltaWidth == other.deltaWidth
                && serviceZoneWidth == other.serviceZoneWidth && doorThickness == other.doorThickness
                && countCells == other.countCells && Double.compare(heightCell, other.heightCell) == 0
                && widthCell == other.widthCell && depthCell == other.depthCell
                && Objects.equals(type, other.type)
                && Objects.equals(directionDoorOpening, other.directionDoorOpening)
                && Objects.equals(colorDoor, other.colorDoor) && Objects.equals(colorBody, other.colorBody)
                && Objects.equals(functionalConfiguration, other.functionalConfiguration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, height, width, depth, upperFrame, bottomFrame, shelfThick,
                deltaWidth, serviceZoneWidth, doorThickness, countCells, heightCell,
                widthCell, depthCell, directionDoorOpening, colorDoor, colorBody,
                functionalConfiguration);
    }

    @Override
    public String toString() {
        return "LBCDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", functionalConfiguration=" + functionalConfiguration +
                '}';
    }
}
