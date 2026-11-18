package com.lb_calc_web.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

/** Каталожная позиция оборудования с устойчивым идентификатором. */
@Entity
@Table(name = "equipment_catalog")
public class EquipmentCatalogEntity {
    @Id
    @Column(name = "catalog_position_id", nullable = false, length = 100)
    private String catalogPositionId;

    @Column(name = "category", nullable = false, length = 40)
    private String category;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "width", nullable = false)
    private int width;

    @Column(name = "height", nullable = false)
    private int height;

    @Column(name = "depth", nullable = false)
    private int depth;

    @Column(name = "display_width", nullable = false)
    private int displayWidth;

    @Column(name = "display_height", nullable = false)
    private int displayHeight;

    @Column(name = "minimum_module_width", nullable = false)
    private int minimumModuleWidth;

    @Column(name = "supports_barcode_1d", nullable = false)
    private boolean supportsBarcode1d;

    @Column(name = "supports_barcode_2d", nullable = false)
    private boolean supportsBarcode2d;

    @Column(name = "supports_rfid", nullable = false)
    private boolean supportsRfid;

    @Column(name = "supports_print", nullable = false)
    private boolean supportsPrint;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected EquipmentCatalogEntity() { }

    public EquipmentCatalogEntity(String id, String category, String name, String description,
                                  int width, int height, int depth, int displayWidth,
                                  int displayHeight, int minimumModuleWidth,
                                  boolean supportsBarcode1d, boolean supportsBarcode2d,
                                  boolean supportsRfid, boolean supportsPrint, boolean active) {
        if (id == null || id.isBlank() || category == null || category.isBlank()
                || name == null || name.isBlank()) {
            throw new IllegalArgumentException("ID, категория и название оборудования обязательны");
        }
        if (width < 0 || height < 0 || depth < 0 || displayWidth < 0
                || displayHeight < 0 || minimumModuleWidth < 0) {
            throw new IllegalArgumentException("Габариты оборудования не могут быть отрицательными");
        }
        this.catalogPositionId = id;
        this.category = category;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.minimumModuleWidth = minimumModuleWidth;
        this.supportsBarcode1d = supportsBarcode1d;
        this.supportsBarcode2d = supportsBarcode2d;
        this.supportsRfid = supportsRfid;
        this.supportsPrint = supportsPrint;
        this.active = active;
    }

    public String getCatalogPositionId() { return catalogPositionId; }
    public String getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }
    public int getDisplayWidth() { return displayWidth; }
    public int getDisplayHeight() { return displayHeight; }
    public int getMinimumModuleWidth() { return minimumModuleWidth; }
    public boolean isSupportsBarcode1d() { return supportsBarcode1d; }
    public boolean isSupportsBarcode2d() { return supportsBarcode2d; }
    public boolean isSupportsRfid() { return supportsRfid; }
    public boolean isSupportsPrint() { return supportsPrint; }
    public boolean isActive() { return active; }

    public void update(String category, String name, String description, int width, int height,
                       int depth, int displayWidth, int displayHeight, int minimumModuleWidth,
                       boolean barcode1d, boolean barcode2d, boolean rfid, boolean print) {
        if (category == null || category.isBlank() || name == null || name.isBlank()
                || width < 0 || height < 0 || depth < 0 || displayWidth < 0
                || displayHeight < 0 || minimumModuleWidth < 0) {
            throw new IllegalArgumentException("Некорректные данные каталожной позиции");
        }
        this.category = category;
        this.name = name;
        this.description = Objects.requireNonNullElse(description, "");
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.minimumModuleWidth = minimumModuleWidth;
        this.supportsBarcode1d = barcode1d;
        this.supportsBarcode2d = barcode2d;
        this.supportsRfid = rfid;
        this.supportsPrint = print;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }
}
