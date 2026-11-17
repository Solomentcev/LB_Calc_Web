package com.lb_calc_web.domain.equipment;

/** RFID-считыватель, устанавливаемый в модуль управления. */
public class RfidReader extends Equipment {
    public static final String CATALOG_POSITION_ID = "RFID_READER_STANDARD";
    public RfidReader() { super(CATALOG_POSITION_ID, "RFID-считыватель"); }
    @Override public int getWidth() { return 0; }
    @Override public int getHeight() { return 0; }
    @Override public int getDepth() { return 0; }
    @Override public String getDescription() { return getName(); }
}