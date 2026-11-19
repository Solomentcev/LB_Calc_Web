package com.lb_calc_web.dto;

/** Данные каталожной позиции для REST API. */
public record EquipmentCatalogDTO(
        String catalogPositionId,
        String category,
        String name,
        String description,
        int width,
        int height,
        int depth,
        int displayWidth,
        int displayHeight,
        int minimumModuleWidth,
        boolean supportsBarcode1d,
        boolean supportsBarcode2d,
        boolean supportsRfid,
        boolean supportsPrint,
        boolean active,
        boolean defaultPosition
) { }
