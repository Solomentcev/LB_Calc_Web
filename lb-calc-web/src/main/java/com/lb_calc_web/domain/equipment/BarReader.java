package com.lb_calc_web.domain.equipment;

public class BarReader extends Equipment {

    public static final BarReader NONE =
            new BarReader("Без сканера");

    public static final BarReader READER_1D =
            new BarReader("Сканер штрихкода 1D");

    public static final BarReader READER_2D =
            new BarReader("Сканер штрихкода 2D");

    private BarReader(String name) {
        super(name);
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