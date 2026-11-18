-- Отменяет побочный эффект V6: базовые позиции каталога остаются доступными.
-- Если для категории уже назначена другая активная модель по умолчанию,
-- восстановленная позиция не получает второй признак is_default.
UPDATE equipment_catalog
SET active = TRUE
WHERE catalog_position_id IN (
    'COMPUTER_STANDARD',
    'POWER_SUPPLY_STANDARD',
    'UPS_STANDARD'
);

UPDATE equipment_catalog AS target
SET is_default = NOT EXISTS (
    SELECT 1
    FROM (
        SELECT category, catalog_position_id
        FROM equipment_catalog
        WHERE active = TRUE AND is_default = TRUE
    ) AS active_defaults
    WHERE active_defaults.category = target.category
      AND active_defaults.catalog_position_id <> target.catalog_position_id
)
WHERE target.catalog_position_id IN (
    'COMPUTER_STANDARD',
    'POWER_SUPPLY_STANDARD',
    'UPS_STANDARD'
);
