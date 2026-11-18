package com.lb_calc_web.domain.equipment;

/** Доменное представление произвольной позиции из сохраняемого каталога. */
public final class CatalogEquipment extends Equipment {
    private final String category;
    private final String description;
    private final int width;
    private final int height;
    private final int depth;
    private final int minimumModuleWidth;
    private final int displayWidth;
    private final int displayHeight;
    private final boolean barcode1d;
    private final boolean barcode2d;
    private final boolean rfid;
    private final boolean print;

    public CatalogEquipment(String id, String category, String name, String description,
                            int width, int height, int depth, int minimumModuleWidth,
                            int displayWidth, int displayHeight, boolean barcode1d,
                            boolean barcode2d, boolean rfid, boolean print) {
        super(id, name);
        this.category = category;
        this.description = description == null ? "" : description;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.minimumModuleWidth = minimumModuleWidth;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.barcode1d = barcode1d;
        this.barcode2d = barcode2d;
        this.rfid = rfid;
        this.print = print;
    }

    @Override public String getCategory() { return category; }
    @Override public boolean isDisplay() { return "DISPLAY".equals(category); }
    @Override public int getWidth() { return isDisplay() ? minimumModuleWidth : width; }
    @Override public int getHeight() { return isDisplay() ? displayHeight : height; }
    @Override public int getDepth() { return depth; }
    @Override public String getDescription() { return description; }
    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public int getMinimumModuleWidth() { return minimumModuleWidth; }
    public boolean supportsBarcode1d() { return barcode1d; }
    public boolean supportsBarcode2d() { return barcode2d; }
    public boolean supportsRfid() { return rfid; }
    public boolean supportsPrint() { return print; }
}
