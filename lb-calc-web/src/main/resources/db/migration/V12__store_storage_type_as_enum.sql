-- Характеристики конструктивного типа теперь определяются enum TypeLb.
-- В таблицах остаётся только идентификатор типа; значения размеров не дублируются.
ALTER TABLE lb
    DROP COLUMN delta_width,
    DROP COLUMN shelf_thick,
    DROP COLUMN service_zone_width;

ALTER TABLE lbc
    DROP COLUMN delta_width,
    DROP COLUMN shelf_thick,
    DROP COLUMN service_zone_width;
