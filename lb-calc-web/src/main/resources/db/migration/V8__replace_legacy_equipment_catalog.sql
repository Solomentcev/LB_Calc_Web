-- Переводим старый каталог позиций в каталог физических моделей оборудования.
-- Временный legacy-идентификатор нужен только для переноса связей LC/LBC.

CREATE TABLE equipment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    legacy_catalog_position_id VARCHAR(255) NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    functional_type VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    depth INT NOT NULL,
    CONSTRAINT pk_equipment PRIMARY KEY (id),
    CONSTRAINT uk_equipment_legacy_catalog_position UNIQUE (legacy_catalog_position_id),
    CONSTRAINT chk_equipment_dimensions
        CHECK (width >= 0 AND height >= 0 AND depth >= 0)
) ENGINE=InnoDB;

INSERT INTO equipment (
    legacy_catalog_position_id,
    name,
    description,
    functional_type,
    purpose,
    category,
    width,
    height,
    depth
)
SELECT
    catalog_position_id,
    name,
    description,
    CASE
        WHEN category = 'DISPLAY' THEN 'DISPLAY'
        WHEN category IN ('BAR_READER', 'RFID_READER') THEN 'ACCESS'
        WHEN category IN ('PAYMENT_TERMINAL', 'CASH_ACCEPTOR') THEN 'PAYMENT'
        WHEN category = 'PRINTER' THEN 'PRINTER'
        ELSE 'SYSTEM'
    END,
    CASE
        WHEN category = 'DISPLAY' THEN
            CASE catalog_position_id
                WHEN 'DISPLAY_LC10' THEN 'INCH_10'
                WHEN 'DISPLAY_LC17' THEN 'INCH_17'
                WHEN 'DISPLAY_LC19' THEN 'INCH_19'
                ELSE 'CUSTOM'
            END
        WHEN category = 'BAR_READER' THEN
            CASE WHEN supports_barcode_2d = TRUE THEN 'QR_CODE' ELSE 'BAR_CODE' END
        WHEN category = 'RFID_READER' THEN 'RFID'
        WHEN category = 'PAYMENT_TERMINAL' THEN 'CARDS'
        WHEN category = 'CASH_ACCEPTOR' THEN 'CASH'
        WHEN category = 'PRINTER' THEN 'PRESENT'
        WHEN category = 'COMPUTER' THEN 'COMPUTER'
        WHEN category = 'LOCK_CONTROLLER' THEN 'LOCK_CONTROL'
        WHEN category = 'POWER_SUPPLY' THEN 'POWER_SUPPLY'
        WHEN category = 'UPS' THEN 'BACKUP_POWER'
    END,
    CASE
        WHEN category = 'LOCK_CONTROLLER' THEN 'LOCK_CONTROL'
        WHEN category = 'UPS' THEN 'BACKUP_POWER'
        ELSE category
    END,
    width,
    height,
    depth
FROM equipment_catalog;

-- Сначала переносим ссылки в новые таблицы, пока старые связи ещё доступны.
CREATE TABLE lc_equipment_new (
    lc_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT uk_lc_equipment UNIQUE (lc_id, equipment_id),
    CONSTRAINT chk_lc_equipment_quantity CHECK (quantity >= 1),
    CONSTRAINT fk_lc_equipment_lc_v8
        FOREIGN KEY (lc_id) REFERENCES lc (id) ON DELETE CASCADE,
    CONSTRAINT fk_lc_equipment_equipment_v8
        FOREIGN KEY (equipment_id) REFERENCES equipment (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

INSERT INTO lc_equipment_new (lc_id, equipment_id, quantity)
SELECT old_position.lc_id, equipment.id, old_position.quantity
FROM lc_equipment old_position
JOIN equipment
    ON equipment.legacy_catalog_position_id = old_position.catalog_position_id;

CREATE TABLE lbc_equipment_new (
    lbc_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT uk_lbc_equipment UNIQUE (lbc_id, equipment_id),
    CONSTRAINT chk_lbc_equipment_quantity CHECK (quantity >= 1),
    CONSTRAINT fk_lbc_equipment_lbc_v8
        FOREIGN KEY (lbc_id) REFERENCES lbc (id) ON DELETE CASCADE,
    CONSTRAINT fk_lbc_equipment_equipment_v8
        FOREIGN KEY (equipment_id) REFERENCES equipment (id)
        ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB;

INSERT INTO lbc_equipment_new (lbc_id, equipment_id, quantity)
SELECT old_position.lbc_id, equipment.id, old_position.quantity
FROM lbc_equipment old_position
JOIN equipment
    ON equipment.legacy_catalog_position_id = old_position.catalog_position_id;

DROP TABLE lc_equipment;
DROP TABLE lbc_equipment;

RENAME TABLE
    lc_equipment_new TO lc_equipment,
    lbc_equipment_new TO lbc_equipment;

-- Функциональная конфигурация теперь хранится явно в таблицах LC и LBC.
ALTER TABLE lc
    ADD COLUMN display_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN payment_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN printer_purpose VARCHAR(50) NOT NULL DEFAULT 'ABSENT';

ALTER TABLE lbc
    ADD COLUMN display_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN payment_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN printer_purpose VARCHAR(50) NOT NULL DEFAULT 'ABSENT';

-- Восстанавливаем функциональные назначения из фактически установленного оборудования.
UPDATE lc
LEFT JOIN (
    SELECT
        lc_equipment.lc_id,
        MAX(CASE WHEN equipment.functional_type = 'DISPLAY'
                 THEN equipment.purpose END) AS display_purpose,
        MAX(CASE WHEN equipment.category = 'PAYMENT_TERMINAL'
                 THEN 1 ELSE 0 END) AS has_card_payment,
        MAX(CASE WHEN equipment.category = 'CASH_ACCEPTOR'
                 THEN 1 ELSE 0 END) AS has_cash_payment,
        MAX(CASE WHEN equipment.functional_type = 'PRINTER'
                 THEN 1 ELSE 0 END) AS has_printer
    FROM lc_equipment
    JOIN equipment ON equipment.id = lc_equipment.equipment_id
    GROUP BY lc_equipment.lc_id
) selected ON selected.lc_id = lc.id
SET
    lc.display_purpose = COALESCE(selected.display_purpose, 'NONE'),
    lc.payment_purpose = CASE
        WHEN selected.has_card_payment = 1 AND selected.has_cash_payment = 1
            THEN 'CARDS_AND_CASH'
        WHEN selected.has_card_payment = 1 THEN 'CARDS'
        WHEN selected.has_cash_payment = 1 THEN 'CASH'
        ELSE 'NONE'
    END,
    lc.printer_purpose = CASE
        WHEN selected.has_printer = 1 THEN 'PRESENT'
        ELSE 'ABSENT'
    END;

UPDATE lbc
LEFT JOIN (
    SELECT
        lbc_equipment.lbc_id,
        MAX(CASE WHEN equipment.functional_type = 'DISPLAY'
                 THEN equipment.purpose END) AS display_purpose,
        MAX(CASE WHEN equipment.category = 'PAYMENT_TERMINAL'
                 THEN 1 ELSE 0 END) AS has_card_payment,
        MAX(CASE WHEN equipment.category = 'CASH_ACCEPTOR'
                 THEN 1 ELSE 0 END) AS has_cash_payment,
        MAX(CASE WHEN equipment.functional_type = 'PRINTER'
                 THEN 1 ELSE 0 END) AS has_printer
    FROM lbc_equipment
    JOIN equipment ON equipment.id = lbc_equipment.equipment_id
    GROUP BY lbc_equipment.lbc_id
) selected ON selected.lbc_id = lbc.id
SET
    lbc.display_purpose = COALESCE(selected.display_purpose, 'NONE'),
    lbc.payment_purpose = CASE
        WHEN selected.has_card_payment = 1 AND selected.has_cash_payment = 1
            THEN 'CARDS_AND_CASH'
        WHEN selected.has_card_payment = 1 THEN 'CARDS'
        WHEN selected.has_cash_payment = 1 THEN 'CASH'
        ELSE 'NONE'
    END,
    lbc.printer_purpose = CASE
        WHEN selected.has_printer = 1 THEN 'PRESENT'
        ELSE 'ABSENT'
    END;

-- Назначения доступа выводятся из установленных считывателей.
CREATE TABLE lc_access_purpose (
    lc_id BIGINT NOT NULL,
    access_purpose VARCHAR(50) NOT NULL,
    CONSTRAINT pk_lc_access_purpose PRIMARY KEY (lc_id, access_purpose),
    CONSTRAINT fk_lc_access_purpose_lc
        FOREIGN KEY (lc_id) REFERENCES lc (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO lc_access_purpose (lc_id, access_purpose)
SELECT DISTINCT lc_equipment.lc_id, equipment.purpose
FROM lc_equipment
JOIN equipment ON equipment.id = lc_equipment.equipment_id
WHERE equipment.functional_type = 'ACCESS';

CREATE TABLE lbc_access_purpose (
    lbc_id BIGINT NOT NULL,
    access_purpose VARCHAR(50) NOT NULL,
    CONSTRAINT pk_lbc_access_purpose PRIMARY KEY (lbc_id, access_purpose),
    CONSTRAINT fk_lbc_access_purpose_lbc
        FOREIGN KEY (lbc_id) REFERENCES lbc (id) ON DELETE CASCADE
) ENGINE=InnoDB;

INSERT INTO lbc_access_purpose (lbc_id, access_purpose)
SELECT DISTINCT lbc_equipment.lbc_id, equipment.purpose
FROM lbc_equipment
JOIN equipment ON equipment.id = lbc_equipment.equipment_id
WHERE equipment.functional_type = 'ACCESS';

-- Удаляем прежние представления функциональной конфигурации и каталог.
DROP TABLE lc_access_method;
DROP TABLE lc_print_option;
DROP TABLE lbc_access_method;
DROP TABLE lbc_print_option;

ALTER TABLE lc DROP COLUMN payment;
ALTER TABLE lbc DROP COLUMN payment;

DROP TABLE equipment_catalog;

-- Убираем технический ключ переноса: в новой модели используется Long id.
ALTER TABLE equipment
    DROP INDEX uk_equipment_legacy_catalog_position,
    DROP COLUMN legacy_catalog_position_id;
