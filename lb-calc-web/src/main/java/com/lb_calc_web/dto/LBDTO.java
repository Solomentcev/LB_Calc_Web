package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class LBDTO {

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

    @JsonIgnore
    private String stringLBImage;

    public LBDTO() {
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

    public String getStringLBImage() {
        return stringLBImage;
    }

    public void setStringLBImage(String stringLBImage) {
        this.stringLBImage = stringLBImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LBDTO lbdto = (LBDTO) o;

        return height == lbdto.height
                && width == lbdto.width
                && depth == lbdto.depth
                && upperFrame == lbdto.upperFrame
                && bottomFrame == lbdto.bottomFrame
                && shelfThick == lbdto.shelfThick
                && deltaWidth == lbdto.deltaWidth
                && serviceZoneWidth == lbdto.serviceZoneWidth
                && doorThickness == lbdto.doorThickness
                && countCells == lbdto.countCells
                && Double.compare(heightCell, lbdto.heightCell) == 0
                && widthCell == lbdto.widthCell
                && depthCell == lbdto.depthCell
                && Objects.equals(type, lbdto.type)
                && Objects.equals(directionDoorOpening, lbdto.directionDoorOpening)
                && Objects.equals(colorDoor, lbdto.colorDoor)
                && Objects.equals(colorBody, lbdto.colorBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                height,
                width,
                depth,
                upperFrame,
                bottomFrame,
                shelfThick,
                deltaWidth,
                serviceZoneWidth,
                doorThickness,
                countCells,
                heightCell,
                widthCell,
                depthCell,
                directionDoorOpening,
                colorDoor,
                colorBody
        );
    }

    @Override
    public String toString() {
        return "LBDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", shelfThick=" + shelfThick +
                ", deltaWidth=" + deltaWidth +
                ", serviceZoneWidth=" + serviceZoneWidth +
                ", doorThickness=" + doorThickness +
                ", countCells=" + countCells +
                ", heightCell=" + heightCell +
                ", widthCell=" + widthCell +
                ", depthCell=" + depthCell +
                ", directionDoorOpening='" + directionDoorOpening + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", colorBody='" + colorBody + '\'' +
                '}';
    }
}