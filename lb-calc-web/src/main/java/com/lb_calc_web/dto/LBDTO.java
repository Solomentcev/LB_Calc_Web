package com.lb_calc_web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Positive;

import java.util.Objects;

/** DTO модуля хранения LB для передачи параметров между веб-слоем и сервисами. */
@Schema(description = "Конфигурация модуля хранения LB, включая габариты, ячейки, конструктивные параметры и цвета.")
public class LBDTO {

    private Long id;
    private String name;
    private String description;

    private String type;

    @Schema(description = "Высота модуля LB, мм.")
    @Positive(message = "height должен быть больше нуля")
    private int height;
    @Schema(description = "Ширина модуля LB, мм.")
    @Positive(message = "width должен быть больше нуля")
    private int width;
    @Schema(description = "Глубина модуля LB, мм.")
    @Positive(message = "depth должен быть больше нуля")
    private int depth;

    @Schema(description = "Высота верхней рамы, мм.")
    @Positive(message = "upperFrame должен быть больше нуля")
    private int upperFrame;
    @Schema(description = "Высота нижней рамы, мм.")
    @Positive(message = "bottomFrame должен быть больше нуля")
    private int bottomFrame;

    @Schema(description = "Толщина двери, мм.")
    @Positive(message = "doorThickness должен быть больше нуля")
    private int doorThickness;

    @Positive(message = "countCells должен быть больше нуля")
    private int countCells;

    @Schema(description = "Высота ячейки хранения, мм.")
    private double heightCell;
    @Schema(description = "Ширина ячейки хранения, мм.")
    private int widthCell;
    @Schema(description = "Глубина ячейки хранения, мм.")
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