package com.lb_calc_web.domain.equipment;

import java.util.Objects;

/** Конкретная позиция оборудования из каталога. */
public class Equipment {
    private final String catalogPositionId;
    private final String category;
    private final String name;
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

    public Equipment(String catalogPositionId, String name) {
        this(catalogPositionId, "OTHER", name, name,
                0, 0, 0, 0, 0, 0, false, false, false, false);
    }

    public Equipment(String catalogPositionId, String category, String name, String description,
                     int width, int height, int depth, int minimumModuleWidth,
                     int displayWidth, int displayHeight, boolean barcode1d,
                     boolean barcode2d, boolean rfid, boolean print) {
        if (catalogPositionId == null || catalogPositionId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор позиции каталога не должен быть пустым");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Категория оборудования не должна быть пустой");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название оборудования не должно быть пустым");
        }
        if (width < 0 || height < 0 || depth < 0 || minimumModuleWidth < 0
                || displayWidth < 0 || displayHeight < 0) {
            throw new IllegalArgumentException("Габариты оборудования не могут быть отрицательными");
        }
        this.catalogPositionId = catalogPositionId;
        this.category = category;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
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

    public String getCatalogPositionId() { return catalogPositionId; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWidth() { return isDisplay() ? minimumModuleWidth : width; }
    public int getHeight() { return isDisplay() ? displayHeight : height; }
    public int getDepth() { return depth; }
    public int getMinimumModuleWidth() { return minimumModuleWidth; }
    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public boolean isDisplay() { return "DISPLAY".equals(category); }
    public boolean isBarReader() { return "BAR_READER".equals(category); }
    public boolean isPrinter() { return "PRINTER".equals(category); }
    public boolean isRfidReader() { return "RFID_READER".equals(category); }
    public boolean supportsBarcode1d() { return barcode1d; }
    public boolean supportsBarcode2d() { return barcode2d; }
    public boolean supportsRfid() { return rfid; }
    public boolean supportsPrint() { return print; }

    @Override
    public final boolean equals(Object object) {
        return this == object || object instanceof Equipment other
                && catalogPositionId.equals(other.catalogPositionId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(catalogPositionId);
    }

    @Override
    public String toString() {
        return "Equipment{catalogPositionId='" + catalogPositionId
                + "', category='" + category + "', name='" + name + "'}";
    }
}
