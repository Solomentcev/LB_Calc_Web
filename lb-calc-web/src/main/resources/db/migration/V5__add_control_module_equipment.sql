-- Базовая комплектация управляющего модуля и оборудование оплаты.
-- Размеры конкретных моделей заполняются через управление каталогом.
INSERT INTO equipment_catalog
(catalog_position_id, category, name, description, width, height, depth,
 display_width, display_height, minimum_module_width, supports_barcode_1d,
 supports_barcode_2d, supports_rfid, supports_print, active, is_default)
VALUES
('COMPUTER_STANDARD', 'COMPUTER', 'Компьютер', 'Базовый компьютер управляющего модуля; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
('LOCK_CONTROLLER_STANDARD', 'LOCK_CONTROLLER', 'Контроллер замков', 'Базовый контроллер замков управляющего модуля; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
('POWER_SUPPLY_STANDARD', 'POWER_SUPPLY', 'Блок питания', 'Базовый блок питания управляющего модуля; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
('UPS_STANDARD', 'UPS', 'Источник бесперебойного питания', 'Базовый источник бесперебойного питания управляющего модуля; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
('PAYMENT_TERMINAL_STANDARD', 'PAYMENT_TERMINAL', 'Банковский терминал', 'Базовая позиция банковского терминала; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE),
('CASH_ACCEPTOR_STANDARD', 'CASH_ACCEPTOR', 'Купюроприёмник', 'Базовая позиция купюроприёмника; характеристики конкретной модели задаются в каталоге.', 0, 0, 0, 0, 0, 0, FALSE, FALSE, FALSE, FALSE, TRUE, TRUE);
