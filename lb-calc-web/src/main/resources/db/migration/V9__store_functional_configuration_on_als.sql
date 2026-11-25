-- Функциональные назначения принадлежат ALS и дублируются
-- в управляющем модуле для независимой работы со статистикой и отчётами.
ALTER TABLE als
    ADD COLUMN display_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN payment_purpose VARCHAR(50) NOT NULL DEFAULT 'NONE',
    ADD COLUMN printer_purpose VARCHAR(50) NOT NULL DEFAULT 'ABSENT';

CREATE TABLE als_access_purpose (
    als_id BIGINT NOT NULL,
    access_purpose VARCHAR(50) NOT NULL,
    CONSTRAINT pk_als_access_purpose PRIMARY KEY (als_id, access_purpose),
    CONSTRAINT fk_als_access_purpose_als
        FOREIGN KEY (als_id) REFERENCES als (id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Первично заполняем ALS функциональными назначениями её управляющего модуля.
UPDATE als
JOIN als_module ON als_module.als_id = als.id
JOIN lc ON lc.id = als_module.module_id
SET
    als.display_purpose = lc.display_purpose,
    als.payment_purpose = lc.payment_purpose,
    als.printer_purpose = lc.printer_purpose;

UPDATE als
JOIN als_module ON als_module.als_id = als.id
JOIN lbc ON lbc.id = als_module.module_id
SET
    als.display_purpose = lbc.display_purpose,
    als.payment_purpose = lbc.payment_purpose,
    als.printer_purpose = lbc.printer_purpose;

INSERT INTO als_access_purpose (als_id, access_purpose)
SELECT DISTINCT als_module.als_id, lc_access_purpose.access_purpose
FROM als_module
JOIN lc ON lc.id = als_module.module_id
JOIN lc_access_purpose ON lc_access_purpose.lc_id = lc.id;

INSERT INTO als_access_purpose (als_id, access_purpose)
SELECT DISTINCT als_module.als_id, lbc_access_purpose.access_purpose
FROM als_module
JOIN lbc ON lbc.id = als_module.module_id
JOIN lbc_access_purpose ON lbc_access_purpose.lbc_id = lbc.id;
