-- COMPUTER, POWER_SUPPLY и UPS не являются обязательными функциями LC/LBC.
-- Существующие ссылки сохраняются для истории; новые конфигурации их не выбирают.
UPDATE equipment_catalog
SET active = FALSE, is_default = FALSE
WHERE catalog_position_id IN (
    'COMPUTER_STANDARD',
    'POWER_SUPPLY_STANDARD',
    'UPS_STANDARD'
);
