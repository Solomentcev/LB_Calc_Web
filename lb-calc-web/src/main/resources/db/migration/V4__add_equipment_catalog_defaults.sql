-- Для каждой категории задаётся активная модель по умолчанию.
ALTER TABLE equipment_catalog
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE equipment_catalog
SET is_default = TRUE
WHERE catalog_position_id IN (
    'DISPLAY_LC10',
    'BAR_READER_2D',
    'PRINTER_STANDARD',
    'RFID_READER_STANDARD'
);

-- Уникальность действует только для активных моделей по умолчанию.
ALTER TABLE equipment_catalog
    ADD COLUMN active_default_category VARCHAR(40)
        GENERATED ALWAYS AS (
            CASE WHEN active = TRUE AND is_default = TRUE THEN category ELSE NULL END
        ) STORED;

CREATE UNIQUE INDEX uk_equipment_catalog_active_default_category
    ON equipment_catalog (active_default_category);
