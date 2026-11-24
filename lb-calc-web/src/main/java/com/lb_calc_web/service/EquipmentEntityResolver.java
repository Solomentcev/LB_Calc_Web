package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.EquipmentRepository;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Разрешает доменную модель оборудования в существующую JPA-сущность каталога.
 *
 * <p>Оборудование сохраняется независимо от управляющих модулей. Поэтому
 * при сохранении конфигурации модуля разрешается только уже существующая
 * запись каталога; создание новой записи здесь не допускается.</p>
 */
@Component
public class EquipmentEntityResolver {

    private final EquipmentRepository equipmentRepository;

    public EquipmentEntityResolver(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = Objects.requireNonNull(
                equipmentRepository,
                "EquipmentRepository не должен быть null"
        );
    }

    /**
     * Возвращает сохранённую модель оборудования по доменному идентификатору.
     *
     * @param equipment доменная модель оборудования
     * @return существующая JPA-сущность
     * @throws IllegalArgumentException если модель не имеет persistence ID
     * @throws NoSuchElementException если запись каталога не найдена
     */
    public EquipmentEntity resolve(Equipment equipment) {
        Objects.requireNonNull(equipment, "Оборудование не должно быть null");
        Long id = equipment.getId();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Для установки оборудования требуется сохранённая модель с Long id"
            );
        }

        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"
                ));

        Equipment persisted = EquipmentEntityMapper.toDomain(entity);
        if (!persisted.equals(equipment)) {
            throw new IllegalStateException(
                    "Идентификатор оборудования не соответствует сохранённой модели: " + id
            );
        }
        return entity;
    }
}
