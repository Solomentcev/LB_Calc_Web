package com.lb_calc_web.domain.equipment;

/** Принтер, устанавливаемый в модуль управления. */
public class Printer extends Equipment {
    public static final String CATALOG_POSITION_ID = "PRINTER_STANDARD";
    public Printer() { super(CATALOG_POSITION_ID, "Принтер"); }
    @Override public boolean isPrinter() { return true; }
    @Override public String getCategory() { return "PRINTER"; }
    @Override public int getWidth() { return 0; }
    @Override public int getHeight() { return 0; }
    @Override public int getDepth() { return 0; }
    @Override public String getDescription() { return getName(); }
}