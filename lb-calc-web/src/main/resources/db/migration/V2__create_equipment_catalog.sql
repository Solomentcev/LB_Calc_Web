-- Каталог оборудования. Идентификаторы совпадают с ранее сохранявшимися
-- catalog_position_id, поэтому существующие конфигурации продолжают разрешаться.
CREATE TABLE equipment_catalog (
    catalog_position_id VARCHAR(255) NOT NULL,
    category VARCHAR(40) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    width INT NOT NULL DEFAULT 0,
    height INT NOT NULL DEFAULT 0,
    depth INT NOT NULL DEFAULT 0,
    display_width INT NOT NULL DEFAULT 0,
    display_height INT NOT NULL DEFAULT 0,
    minimum_module_width INT NOT NULL DEFAULT 0,
    supports_barcode_1d BOOLEAN NOT NULL DEFAULT FALSE,
    supports_barcode_2d BOOLEAN NOT NULL DEFAULT FALSE,
    supports_rfid BOOLEAN NOT NULL DEFAULT FALSE,
    supports_print BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_equipment_catalog PRIMARY KEY (catalog_position_id),
    CONSTRAINT chk_equipment_catalog_dimensions CHECK (
        width >= 0 AND height >= 0 AND depth >= 0
        AND display_width >= 0 AND display_height >= 0
        AND minimum_module_width >= 0
    )
) ENGINE=InnoDB;

INSERT INTO equipment_catalog
(catalog_position_id, category, name, description, width, height, depth,
 display_width, display_height, minimum_module_width, supports_barcode_1d,
 supports_barcode_2d, supports_rfid, supports_print, active)
VALUES
('DISPLAY_LC10', 'DISPLAY', 'LC10', 'LC10', 255, 174, 0, 255, 174, 300, FALSE, FALSE, FALSE, FALSE, TRUE),
('DISPLAY_LC17', 'DISPLAY', 'LC17', 'LC17', 338, 270, 0, 338, 270, 450, FALSE, FALSE, FALSE, FALSE, TRUE),
('DISPLAY_LC19', 'DISPLAY', 'LC19', 'LC19', 376, 301, 0, 376, 301, 500, FALSE, FALSE, FALSE, FALSE, TRUE),
('BAR_READER_1D', 'BAR_READER', 'Сканер штрихкода 1D', 'Сканер штрихкода 1D', 0, 0, 0, 0, 0, 0, TRUE, FALSE, FALSE, FALSE, TRUE),
('BAR_READER_2D', 'BAR_READER', 'Сканер штрихкода 2D', 'Сканер штрихкода 2D', 0, 0, 0, 0, 0, 0, TRUE, TRUE, FALSE, FALSE, TRUE),
('PRINTER_STANDARD', 'PRINTER', 'Принтер', 'Принтер', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, TRUE, TRUE),
('RFID_READER_STANDARD', 'RFID_READER', 'RFID-считыватель', 'RFID-считыватель', 0, 0, 0, 0, 0, 0, FALSE, FALSE, TRUE, FALSE, TRUE);
