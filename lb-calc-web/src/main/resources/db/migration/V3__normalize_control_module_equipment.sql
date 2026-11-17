-- Перенос состава оборудования из устаревших скалярных колонок
-- в отдельные таблицы коллекций LC и LBC.
--
-- V2 уже нормализовала значения display/bar_reader до стабильных
-- catalog_position_id и рассчитала количества оборудования.

CREATE TABLE lc_equipment (
    lc_id BIGINT NOT NULL,
    catalog_position_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT uk_lc_equipment
        UNIQUE (lc_id, catalog_position_id),

    CONSTRAINT chk_lc_equipment_quantity
        CHECK (quantity >= 1),

    CONSTRAINT fk_lc_equipment_lc
        FOREIGN KEY (lc_id)
            REFERENCES lc (id)
            ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE lbc_equipment (
    lbc_id BIGINT NOT NULL,
    catalog_position_id VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT uk_lbc_equipment
        UNIQUE (lbc_id, catalog_position_id),

    CONSTRAINT chk_lbc_equipment_quantity
        CHECK (quantity >= 1),

    CONSTRAINT fk_lbc_equipment_lbc
        FOREIGN KEY (lbc_id)
            REFERENCES lbc (id)
            ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO lc_equipment (lc_id, catalog_position_id, quantity)
SELECT id, display, display_quantity
FROM lc
WHERE display IS NOT NULL
  AND display_quantity > 0;

INSERT INTO lc_equipment (lc_id, catalog_position_id, quantity)
SELECT id, bar_reader, bar_reader_quantity
FROM lc
WHERE bar_reader IS NOT NULL
  AND bar_reader_quantity > 0;

INSERT INTO lc_equipment (lc_id, catalog_position_id, quantity)
SELECT id, 'PRINTER_STANDARD', printer_quantity
FROM lc
WHERE printer_quantity > 0;

INSERT INTO lc_equipment (lc_id, catalog_position_id, quantity)
SELECT id, 'RFID_READER_STANDARD', rfid_reader_quantity
FROM lc
WHERE rfid_reader_quantity > 0;

INSERT INTO lbc_equipment (lbc_id, catalog_position_id, quantity)
SELECT id, display, display_quantity
FROM lbc
WHERE display IS NOT NULL
  AND display_quantity > 0;

INSERT INTO lbc_equipment (lbc_id, catalog_position_id, quantity)
SELECT id, bar_reader, bar_reader_quantity
FROM lbc
WHERE bar_reader IS NOT NULL
  AND bar_reader_quantity > 0;

INSERT INTO lbc_equipment (lbc_id, catalog_position_id, quantity)
SELECT id, 'PRINTER_STANDARD', printer_quantity
FROM lbc
WHERE printer_quantity > 0;

INSERT INTO lbc_equipment (lbc_id, catalog_position_id, quantity)
SELECT id, 'RFID_READER_STANDARD', rfid_reader_quantity
FROM lbc
WHERE rfid_reader_quantity > 0;

-- Способ оплаты является обязательным атрибутом доменной модели.
-- Для старых записей без выбранного способа оплаты используется NONE.
UPDATE lc
SET payment = 'NONE'
WHERE payment IS NULL;

UPDATE lbc
SET payment = 'NONE'
WHERE payment IS NULL;

-- Удаляем старое представление оборудования только после переноса данных.
ALTER TABLE lc
    DROP COLUMN display,
    DROP COLUMN bar_reader,
    DROP COLUMN printer,
    DROP COLUMN rfid_reader,
    DROP COLUMN display_quantity,
    DROP COLUMN bar_reader_quantity,
    DROP COLUMN printer_quantity,
    DROP COLUMN rfid_reader_quantity;

ALTER TABLE lbc
    DROP COLUMN display,
    DROP COLUMN bar_reader,
    DROP COLUMN printer,
    DROP COLUMN rfid_reader,
    DROP COLUMN display_quantity,
    DROP COLUMN bar_reader_quantity,
    DROP COLUMN printer_quantity,
    DROP COLUMN rfid_reader_quantity;

ALTER TABLE lc
    MODIFY COLUMN payment VARCHAR(50) NOT NULL;

ALTER TABLE lbc
    MODIFY COLUMN payment VARCHAR(50) NOT NULL;
