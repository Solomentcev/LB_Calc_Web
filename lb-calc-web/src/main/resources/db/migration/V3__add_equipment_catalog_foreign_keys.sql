-- Каждая сохранённая позиция оборудования должна ссылаться на каталог.
-- Это также запрещает физическое удаление модели, пока она используется
-- хотя бы в одной конфигурации LC или LBC.
ALTER TABLE lc_equipment
    ADD CONSTRAINT fk_lc_equipment_catalog
        FOREIGN KEY (catalog_position_id)
            REFERENCES equipment_catalog (catalog_position_id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT;

ALTER TABLE lbc_equipment
    ADD CONSTRAINT fk_lbc_equipment_catalog
        FOREIGN KEY (catalog_position_id)
            REFERENCES equipment_catalog (catalog_position_id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT;
