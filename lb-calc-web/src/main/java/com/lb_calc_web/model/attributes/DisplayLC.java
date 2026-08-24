package com.lb_calc_web.model.attributes;

public enum DisplayLC {
    NONE(0,0,300),
    LC10(255,174,300),
    LC17(338,270,450),
    LC19(376,301,500);
    private final int displayWidth;
    private final int displayHeight;
    private int width;
    DisplayLC(int displayWidth, int displayHeight,int width){
        this.displayWidth=displayWidth;
        this.displayHeight=displayHeight;
        this.width= width;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }


    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
