package com.lb_calc_web.domain.equipment;

public class Display extends Equipment {

    public static final Display NONE =
            new Display("Без дисплея", 0, 0, 300);

    public static final Display LC10 =
            new Display("LC10", 255, 174, 300);

    public static final Display LC17 =
            new Display("LC17", 338, 270, 450);

    public static final Display LC19 =
            new Display("LC19", 376, 301, 500);

    private final int displayWidth;
    private final int displayHeight;
    private final int minimumModuleWidth;

    private Display(
            String name,
            int displayWidth,
            int displayHeight,
            int minimumModuleWidth
    ) {
        super(name);
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.minimumModuleWidth = minimumModuleWidth;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public int getMinimumModuleWidth() {
        return minimumModuleWidth;
    }

    @Override
    public int getWidth() {
        return minimumModuleWidth;
    }

    @Override
    public int getHeight() {
        return displayHeight;
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