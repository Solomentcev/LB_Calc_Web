package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Управляет каталогом физических моделей оборудования. */
@Service
public class EquipmentCatalogService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentCatalogService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = Objects.requireNonNull(equipmentRepository);
    }

    @Transactional(readOnly = true)
    public List<Equipment> findAll() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentEntityMapper::toDomain)
                .sorted(Comparator.comparing(Equipment::getFunctionalType)
                        .thenComparing(Equipment::getCategory)
                        .thenComparing(Equipment::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Equipment::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return EquipmentEntityMapper.toDomain(findEntity(id));
    }

    @Transactional
    public Equipment save(Equipment equipment) {
        Objects.requireNonNull(equipment, "Модель оборудования не должна быть null");

        EquipmentEntity entity;
        if (equipment.getId() == null) {
            entity = EquipmentEntity.create();
        } else {
            entity = findEntity(equipment.getId());
            if (equipmentRepository.countModuleReferencesByEquipmentId(equipment.getId()) > 0
                    && hasChanged(equipment, entity)) {
                throw new IllegalStateException(
                        "Нельзя изменить модель оборудования, пока она установлена в LC/LBC. "
                                + "Создайте новую модель и замените оборудование в конфигурациях");
            }
        }

        EquipmentEntityMapper.updateEntity(equipment, entity);
        return EquipmentEntityMapper.toDomain(equipmentRepository.saveAndFlush(entity));
    }

    @Transactional
    public void delete(Long id) {
        EquipmentEntity entity = findEntity(id);
        if (equipmentRepository.countModuleReferencesByEquipmentId(id) > 0) {
            throw new IllegalStateException(
                    "Нельзя удалить модель оборудования: она установлена в одном или нескольких модулях");
        }
        equipmentRepository.delete(entity);
        equipmentRepository.flush();
    }

    private boolean hasChanged(Equipment equipment, EquipmentEntity entity) {
        return equipment.getFunctionalType() != entity.getFunctionalType()
                || !equipment.getPurpose().toString().equals(entity.getPurpose())
                || equipment.getCategory() != entity.getCategory()
                || !equipment.getName().equals(entity.getName())
                || !equipment.getDescription().equals(entity.getDescription())
                || equipment.getWidth() != entity.getWidth()
                || equipment.getHeight() != entity.getHeight()
                || equipment.getDepth() != entity.getDepth();
    }

    private EquipmentEntity findEntity(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("Модель оборудования с id=" + id + " не найдена");
        }
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"));
    }
}
