package com.lb_calc_web.domain.equipment;

/** Сканеры штрихкода, доступные для установки в модуль управления. */
public class BarReader extends Equipment {
    public static final String NONE_ID = "BAR_READER_NONE";
    public static final String READER_1D_ID = "BAR_READER_1D";
    public static final String READER_2D_ID = "BAR_READER_2D";

    public static final BarReader NONE = new BarReader(NONE_ID, "Без сканера");
    public static final BarReader READER_1D =
            new BarReader(READER_1D_ID, "Сканер штрихкода 1D");
    public static final BarReader READER_2D =
            new BarReader(READER_2D_ID, "Сканер штрихкода 2D");

    private BarReader(String id, String name) { super(id, name); }
    @Override public boolean isBarReader() { return true; }
    @Override public String getCategory() { return "BAR_READER"; }
    @Override public int getWidth() { return 0; }
    @Override public int getHeight() { return 0; }
    @Override public int getDepth() { return 0; }
    @Override public String getDescription() { return getName(); }
}