-- Модель по умолчанию определяется внутри конкретного функционального назначения.
ALTER TABLE equipment
    ADD COLUMN is_default BOOLEAN NOT NULL DEFAULT FALSE;

-- Переносим ранее согласованные значения по умолчанию из старого каталога.
-- Альтернативные модели с тем же назначением остаются доступными для выбора.
UPDATE equipment
SET is_default = TRUE
WHERE (functional_type = 'DISPLAY' AND purpose = 'INCH_10' AND name = 'LC10')
   OR (functional_type = 'ACCESS' AND purpose = 'QR_CODE'
       AND name = 'Сканер штрихкода 2D')
   OR (functional_type = 'ACCESS' AND purpose = 'RFID'
       AND name = 'RFID-считыватель')
   OR (functional_type = 'PRINTER' AND purpose = 'PRESENT'
       AND name = 'Принтер');

-- Для одного назначения допускается не более одной модели по умолчанию.
ALTER TABLE equipment
    ADD COLUMN default_functional_purpose VARCHAR(110)
        GENERATED ALWAYS AS (
            CASE WHEN is_default = TRUE
                 THEN CONCAT(functional_type, ':', purpose)
                 ELSE NULL
            END
        ) STORED;

CREATE UNIQUE INDEX uk_equipment_default_functional_purpose
    ON equipment (default_functional_purpose);
