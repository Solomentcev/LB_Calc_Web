package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

public class ALSDTO {

    private int id;
    private String name;
    private String description;
    private int height;
    private int depth;
    private int width;
    private int upperFrame;
    private int bottomFrame;
    private int depthCell;
    private int countCells;
    private LCDTO lc;
    private String positionLC;
    private String colorDoor;
    private String colorBody;
    private List<LBDTO> lbList =new ArrayList<>();
    @JsonIgnore
    private Map<LBDTO, Integer> quantityLB=new HashMap<>();
    @JsonIgnore
    private String stringALSImage;


    public ALSDTO() {
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

    public LCDTO getLC() {
        return lc;
    }

    public void setLC(LCDTO lc) {
        this.lc = lc;
    }

    public String getPositionLC() {
        return positionLC;
    }

    public void setPositionLC(String positionLC) {
        this.positionLC = positionLC;
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

    public List<LBDTO> getLbList() {
        return lbList;
    }

    public void setLbList(List<LBDTO> lbList) {
        this.lbList = lbList;
    }

    public Map<LBDTO, Integer> getQuantityLB() {
        return quantityLB;
    }

    public void setQuantityLB(Map<LBDTO, Integer> quantityLB) {
        this.quantityLB = quantityLB;
    }

    public String getStringALSImage() {
        return stringALSImage;
    }

    public void setStringALSImage(String stringALSImage) {
        this.stringALSImage = stringALSImage;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALSDTO alsdto = (ALSDTO) o;
        return getHeight() == alsdto.getHeight() && getDepth() == alsdto.getDepth() && getWidth() == alsdto.getWidth()
                && getUpperFrame() == alsdto.getUpperFrame() && getBottomFrame() == alsdto.getBottomFrame()
                && getDepthCell() == alsdto.getDepthCell() && getCountCells() == alsdto.getCountCells()
                && Objects.equals(lc, alsdto.lc) && Objects.equals(getPositionLC(), alsdto.getPositionLC())
                && Objects.equals(getColorDoor(), alsdto.getColorDoor()) && Objects.equals(getColorBody(), alsdto.getColorBody())
                && Objects.deepEquals(getQuantityLB(), alsdto.getQuantityLB());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHeight(), getDepth(), getWidth(), getUpperFrame(), getBottomFrame(), getDepthCell(),
                getCountCells(), lc, getPositionLC(), getColorDoor(), getColorBody());
    }

    @Override
    public String toString() {
        return "ALSDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
//                ", description='" + description + '\'' +
                ", height=" + height +
                ", depth=" + depth +
                ", width=" + width +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", depthCell=" + depthCell +
                ", countCells=" + countCells +"\n"+
                ", lc=" + lc +
                ", positionLC='" + positionLC + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", colorBody='" + colorBody + '\'' +"\n"+
                ", lbList=" + lbList +"\n"+
                ", quantityLB=" + quantityLB +"\n"+
                '}';
    }
}
