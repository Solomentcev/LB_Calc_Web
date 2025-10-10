package com.lb_calc_web.dto;

import com.lb_calc_web.model.ALSLB;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.PositionLC;
import jakarta.persistence.*;

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
    private LCDTO lcDTO;
    private String positionLC;
    private String colorDoor;
    private String colorBody;
    private List<LBDTO> lbDtoList=new ArrayList<>();
    private Map<ALSDTO, Integer> quantityLB=new HashMap<>();

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

    public LCDTO getLcDTO() {
        return lcDTO;
    }

    public void setLcDTO(LCDTO lcDTO) {
        this.lcDTO = lcDTO;
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

    public List<LBDTO> getLbDtoList() {
        return lbDtoList;
    }

    public void setLbDtoList(List<LBDTO> lbDtoList) {
        this.lbDtoList = lbDtoList;
    }

    public Map<ALSDTO, Integer> getQuantityLB() {
        return quantityLB;
    }

    public void setQuantityLB(Map<ALSDTO, Integer> quantityLB) {
        this.quantityLB = quantityLB;
    }
}
