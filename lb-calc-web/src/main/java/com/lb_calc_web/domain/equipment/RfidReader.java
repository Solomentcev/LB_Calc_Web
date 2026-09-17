package com.lb_calc_web.domain.equipment;

public class RfidReader extends Equipment {

    public RfidReader() {
        super("RFID-считыватель");
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