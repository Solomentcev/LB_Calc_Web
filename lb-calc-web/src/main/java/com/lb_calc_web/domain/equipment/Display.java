package com.lb_calc_web.domain.equipment;

/** Дисплеи, доступные для установки в модуль управления. */
public class Display extends Equipment {
    public static final String NONE_ID = "DISPLAY_NONE";
    public static final String LC10_ID = "DISPLAY_LC10";
    public static final String LC17_ID = "DISPLAY_LC17";
    public static final String LC19_ID = "DISPLAY_LC19";

    public static final Display NONE = new Display(
            NONE_ID, "Без дисплея", SystemPurpose.OTHER, EquipmentCategory.OTHER, 0, 0, 300);
    public static final Display LC10 = new Display(
            LC10_ID, "LC10", DisplayPurpose.INCH_10, EquipmentCategory.DISPLAY, 255, 174, 300);
    public static final Display LC17 = new Display(
            LC17_ID, "LC17", DisplayPurpose.INCH_17, EquipmentCategory.DISPLAY, 338, 270, 450);
    public static final Display LC19 = new Display(
            LC19_ID, "LC19", DisplayPurpose.INCH_19, EquipmentCategory.DISPLAY, 376, 301, 500);

    private final int displayWidth;
    private final int displayHeight;
    private final int minimumModuleWidth;

    private Display(String id, String name, EquipmentPurpose purpose, EquipmentCategory category,
                    int displayWidth, int displayHeight, int minimumModuleWidth) {
        super(id, category.getFunctionalType(), purpose, category, name, name,
                minimumModuleWidth, displayHeight, 0);
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.minimumModuleWidth = minimumModuleWidth;
    }

    @Override public boolean isDisplay() { return true; }
    @Override public String getCategory() { return "DISPLAY"; }

    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public int getMinimumModuleWidth() { return minimumModuleWidth; }
    @Override public int getWidth() { return minimumModuleWidth; }
    @Override public int getHeight() { return displayHeight; }
    @Override public int getDepth() { return 0; }
    @Override public String getDescription() { return getName(); }
}
