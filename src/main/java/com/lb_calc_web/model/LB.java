package com.lb_calc_web.model;

import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = {"type","height","width","depth",
"upper_frame","bottom_frame","shelf_thick","count_cells","height_cell","width_cell","depth_cell",
"direction_door_opening","color_body","color_door"}) })
public class LB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private TypeLb type;
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
    @Enumerated(EnumType.STRING)
    private DirectionDoorOpening directionDoorOpening;
    @Enumerated(EnumType.STRING)
    private Colors colorDoor;
    @Enumerated(EnumType.STRING)
    private Colors colorBody;
    @OneToMany(mappedBy = "lb")
    private Set<ALSLB> quantityLB=new HashSet<>();
    public LB() {    }

    public LB( String name, String description, TypeLb type, int height, int width, int depth, int upperFrame, int bottomFrame, int shelfThick, int countCells, double heightCell, int widthCell, int depthCell, DirectionDoorOpening directionDoorOpening, Colors colorDoor, Colors colorBody) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.upperFrame = upperFrame;
        this.bottomFrame = bottomFrame;
        this.shelfThick = shelfThick;
        this.countCells = countCells;
        this.heightCell = heightCell;
        this.widthCell = widthCell;
        this.depthCell = depthCell;
        this.directionDoorOpening = directionDoorOpening;
        this.colorDoor = colorDoor;
        this.colorBody = colorBody;
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

    public TypeLb getType() {
        return type;
    }

    public void setType(TypeLb type) {
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

    public DirectionDoorOpening getDirectionDoorOpening() {
        return directionDoorOpening;
    }

    public void setDirectionDoorOpening(DirectionDoorOpening directionDoorOpening) {
        this.directionDoorOpening = directionDoorOpening;
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(Colors colorDoor) {
        this.colorDoor = colorDoor;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    public void setColorBody(Colors colorBody) {
        this.colorBody = colorBody;
    }


    public Set<ALSLB> getQuantityLB() {
        return quantityLB;
    }

    public void setQuantityLB(Set<ALSLB> quantityLB) {
        this.quantityLB = quantityLB;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LB lb = (LB) o;
        return  height == lb.height && width == lb.width && depth == lb.depth && upperFrame == lb.upperFrame && bottomFrame == lb.bottomFrame && shelfThick == lb.shelfThick && countCells == lb.countCells && Double.compare(heightCell, lb.heightCell) == 0 && widthCell == lb.widthCell && depthCell == lb.depthCell && type == lb.type && directionDoorOpening == lb.directionDoorOpening && colorDoor == lb.colorDoor && colorBody == lb.colorBody;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, height, width, depth, upperFrame, bottomFrame, shelfThick, countCells, heightCell, widthCell, depthCell, directionDoorOpening, colorDoor, colorBody);
    }

    @Override
    public String toString() {
        return "LB{" +
                "id=" + id +
                ", name='" + name +
                ", countCells=" + countCells +
                ", type=" + type +
                ", depth=" + depth +
                ", width=" + width +
                ", height=" + height +
                ", depthCell=" + depthCell +
                ", widthCell=" + widthCell +
                ", heightCell=" + heightCell +
                ", shelfThick=" + shelfThick +
                ", bottomFrame=" + bottomFrame +
                ", upperFrame=" + upperFrame +
                ", colorBody=" + colorBody +
                ", colorDoor=" + colorDoor +
                ", directionDoorOpening=" + directionDoorOpening +
                '}';
    }
}
