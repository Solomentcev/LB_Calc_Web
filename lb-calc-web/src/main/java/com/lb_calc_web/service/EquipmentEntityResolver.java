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
     * Восстанавливает доменную модель оборудования по идентификатору каталога.
     *
     * @param id идентификатор физической модели
     * @return доменная модель оборудования
     * @throws IllegalArgumentException если идентификатор некорректен
     * @throws NoSuchElementException если запись каталога не найдена
     */
    public Equipment resolveDomain(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Идентификатор модели оборудования должен быть положительным");
        }
        EquipmentEntity entity = equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"));
        return EquipmentEntityMapper.toDomain(entity);
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
        if (!persisted.equals(equipment)
                || !persisted.getFunctionalType().equals(equipment.getFunctionalType())
                || !persisted.getPurpose().equals(equipment.getPurpose())
                || !persisted.getCategory().equals(equipment.getCategory())
                || !persisted.getName().equals(equipment.getName())
                || !persisted.getDescription().equals(equipment.getDescription())
                || persisted.getWidth() != equipment.getWidth()
                || persisted.getHeight() != equipment.getHeight()
                || persisted.getDepth() != equipment.getDepth()) {
            throw new IllegalStateException(
                    "Доменная модель оборудования не соответствует записи каталога с id=" + id
            );
        }
        return entity;
    }
}
