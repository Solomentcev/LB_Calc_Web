package com.lb_calc_web.dto;

import java.util.Objects;

public class LBDTO {
    private int id;
    private String name;
    private String description;
    private String type;
    private int height;
    private int width;
    private int depth;
    private int upperFrame;
    private int bottomFrame;
    private int shelfThick;
    private int countCells;
    private double heightCell;
    private int widthCell;
    private int depthCell;
    private String directionDoorOpening;
    private String colorDoor;
    private String colorBody;
    private String stringLBImage;

    public LBDTO() {
    }

    public String getStringLBImage() {
        return stringLBImage;
    }

    public void setStringLBImage(String stringLBImage) {
        this.stringLBImage = stringLBImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LBDTO lbdto = (LBDTO) o;
        return getHeight() == lbdto.getHeight() && getWidth() == lbdto.getWidth() && getDepth() == lbdto.getDepth()
                && getUpperFrame() == lbdto.getUpperFrame() && getBottomFrame() == lbdto.getBottomFrame()
                && getShelfThick() == lbdto.getShelfThick() && getCountCells() == lbdto.getCountCells()
                && Double.compare(getHeightCell(), lbdto.getHeightCell()) == 0 && getWidthCell() == lbdto.getWidthCell()
                && getDepthCell() == lbdto.getDepthCell() && Objects.equals(getType(), lbdto.getType())
                && Objects.equals(getDirectionDoorOpening(), lbdto.getDirectionDoorOpening())
                && Objects.equals(getColorDoor(), lbdto.getColorDoor()) && Objects.equals(getColorBody(), lbdto.getColorBody());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getHeight(), getWidth(), getDepth(), getUpperFrame(), getBottomFrame(),
                getShelfThick(), getCountCells(), getHeightCell(), getWidthCell(), getDepthCell(),
                getDirectionDoorOpening(), getColorDoor(), getColorBody());
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
                ", countCells=" + countCells +
                ", heightCell=" + heightCell +
                ", widthCell=" + widthCell +
                ", depthCell=" + depthCell +
                ", directionDoorOpening='" + directionDoorOpening + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", colorBody='" + colorBody + '\'' +
                '}'+"\n";
    }
}
