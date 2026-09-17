package com.lb_calc_web.domain.equipment;

public class Printer extends Equipment {

    public Printer() {
        super("Принтер");
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public String getDescription() {
        return getName();
    }
}