-- Историческая миграция. Изменение компенсируется миграцией V7.
UPDATE equipment_catalog
SET active = FALSE, is_default = FALSE
WHERE catalog_position_id IN (
    'COMPUTER_STANDARD',
    'POWER_SUPPLY_STANDARD',
    'UPS_STANDARD'
);
