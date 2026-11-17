-- Идентичность оборудования определяется ID каталожной позиции.
-- Количество установленного оборудования хранится отдельно.

ALTER TABLE lc
    ADD COLUMN display_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN bar_reader_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN printer_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN rfid_reader_quantity INT NOT NULL DEFAULT 0;

ALTER TABLE lbc
    ADD COLUMN display_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN bar_reader_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN printer_quantity INT NOT NULL DEFAULT 0,
    ADD COLUMN rfid_reader_quantity INT NOT NULL DEFAULT 0;

UPDATE lc
SET display = CASE display
    WHEN 'LC10' THEN 'DISPLAY_LC10'
    WHEN 'LC17' THEN 'DISPLAY_LC17'
    WHEN 'LC19' THEN 'DISPLAY_LC19'
    WHEN 'Без дисплея' THEN NULL
    ELSE display
END,
bar_reader = CASE bar_reader
    WHEN 'Сканер штрихкода 1D' THEN 'BAR_READER_1D'
    WHEN 'Сканер штрихкода 2D' THEN 'BAR_READER_2D'
    WHEN 'Без сканера' THEN NULL
    ELSE bar_reader
END;

UPDATE lbc
SET display = CASE display
    WHEN 'LC10' THEN 'DISPLAY_LC10'
    WHEN 'LC17' THEN 'DISPLAY_LC17'
    WHEN 'LC19' THEN 'DISPLAY_LC19'
    WHEN 'Без дисплея' THEN NULL
    ELSE display
END,
bar_reader = CASE bar_reader
    WHEN 'Сканер штрихкода 1D' THEN 'BAR_READER_1D'
    WHEN 'Сканер штрихкода 2D' THEN 'BAR_READER_2D'
    WHEN 'Без сканера' THEN NULL
    ELSE bar_reader
END;

UPDATE lc
SET display_quantity = IF(display IS NULL, 0, 1),
    bar_reader_quantity = IF(bar_reader IS NULL, 0, 1),
    printer_quantity = IF(printer, 1, 0),
    rfid_reader_quantity = IF(rfid_reader, 1, 0);

UPDATE lbc
SET display_quantity = IF(display IS NULL, 0, 1),
    bar_reader_quantity = IF(bar_reader IS NULL, 0, 1),
    printer_quantity = IF(printer, 1, 0),
    rfid_reader_quantity = IF(rfid_reader, 1, 0);

ALTER TABLE lc
    ADD CONSTRAINT chk_lc_display_quantity CHECK (display_quantity BETWEEN 0 AND 1),
    ADD CONSTRAINT chk_lc_bar_reader_quantity CHECK (bar_reader_quantity >= 0),
    ADD CONSTRAINT chk_lc_printer_quantity CHECK (printer_quantity >= 0),
    ADD CONSTRAINT chk_lc_rfid_reader_quantity CHECK (rfid_reader_quantity >= 0);

ALTER TABLE lbc
    ADD CONSTRAINT chk_lbc_display_quantity CHECK (display_quantity BETWEEN 0 AND 1),
    ADD CONSTRAINT chk_lbc_bar_reader_quantity CHECK (bar_reader_quantity >= 0),
    ADD CONSTRAINT chk_lbc_printer_quantity CHECK (printer_quantity >= 0),
    ADD CONSTRAINT chk_lbc_rfid_reader_quantity CHECK (rfid_reader_quantity >= 0);
