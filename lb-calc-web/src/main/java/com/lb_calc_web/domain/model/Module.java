package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;

public abstract class Module {

    private String name;
    private String description;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private Colors colorBody;
    private Colors colorDoor;

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    protected void setDepth(int depth) {
        this.depth = depth;
    }

    public int getUpperFrame() {
        return upperFrame;
    }

    protected void setUpperFrame(int upperFrame) {
        this.upperFrame = upperFrame;
    }

    public int getBottomFrame() {
        return bottomFrame;
    }

    protected void setBottomFrame(int bottomFrame) {
        this.bottomFrame = bottomFrame;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    protected void setColorBody(Colors colorBody) {
        this.colorBody = colorBody;
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    protected void setColorDoor(Colors colorDoor) {
        this.colorDoor = colorDoor;
    }

    public abstract void recalculate();
}