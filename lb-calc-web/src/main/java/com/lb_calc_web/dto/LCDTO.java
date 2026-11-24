package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LCDTO {

    private Long id;
    private String name;
    private String description;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private FunctionalConfigurationDTO functionalConfiguration;
    private List<EquipmentSelectionDTO> equipmentSelections;

    @JsonIgnore
    private String stringLCImage;

    public LCDTO() {
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

    public FunctionalConfigurationDTO getFunctionalConfiguration() {
        return functionalConfiguration;
    }

    public void setFunctionalConfiguration(FunctionalConfigurationDTO configuration) {
        this.functionalConfiguration = configuration;
    }

    public List<EquipmentSelectionDTO> getEquipmentSelections() {
        return equipmentSelections;
    }

    public void setEquipmentSelections(List<EquipmentSelectionDTO> selections) {
        this.equipmentSelections = selections == null ? null : new ArrayList<>(selections);
    }

    public String getStringLCImage() {
        return stringLCImage;
    }

    public void setStringLCImage(String stringLCImage) {
        this.stringLCImage = stringLCImage;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof LCDTO other)) return false;
        return height == other.height && width == other.width && depth == other.depth
                && upperFrame == other.upperFrame && bottomFrame == other.bottomFrame
                && Objects.equals(functionalConfiguration, other.functionalConfiguration)
                && Objects.equals(equipmentSelections, other.equipmentSelections)
                && Objects.equals(colorBody, other.colorBody) && Objects.equals(colorDoor, other.colorDoor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width, depth, upperFrame, bottomFrame,
                colorBody, colorDoor, functionalConfiguration, equipmentSelections);
    }

    @Override
    public String toString() {
        return "LCDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", functionalConfiguration=" + functionalConfiguration +
                ", equipmentSelections=" + equipmentSelections +
                '}';
    }
}
