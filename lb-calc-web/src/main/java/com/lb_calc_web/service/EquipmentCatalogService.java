package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.EquipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    /**
     * Возвращает страницу оборудования с независимыми фильтрами по столбцам.
     * Все условия применяются в БД до сортировки и пагинации.
     *
     * @param filter параметры фильтрации
     * @param pageable сортировка и параметры страницы
     * @return отфильтрованная страница оборудования
     */
    @Transactional(readOnly = true)
    public Page<Equipment> findPage(EquipmentCatalogFilter filter, Pageable pageable) {
        Objects.requireNonNull(filter, "Фильтры каталога оборудования не должны быть null");
        Objects.requireNonNull(pageable, "Параметры страницы не должны быть null");

        List<Specification<EquipmentEntity>> specifications = new ArrayList<>();
        addTextFilter(specifications, "name", filter.name());
        addTextFilter(specifications, "purpose", filter.purpose());
        addTextFilter(specifications, "description", filter.description());

        if (filter.functionalType() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("functionalType"), filter.functionalType()));
        }
        if (filter.category() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("category"), filter.category()));
        }

        addRangeFilter(specifications, "width", filter.widthMin(), filter.widthMax());
        addRangeFilter(specifications, "height", filter.heightMin(), filter.heightMax());
        addRangeFilter(specifications, "depth", filter.depthMin(), filter.depthMax());

        Specification<EquipmentEntity> specification = specifications.stream()
                .reduce(Specification::and)
                .orElse((root, query, builder) -> builder.conjunction());

        return equipmentRepository.findAll(specification, pageable)
                .map(EquipmentEntityMapper::toDomain);
    }

    private void addTextFilter(
            List<Specification<EquipmentEntity>> specifications,
            String attribute,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        String pattern = "%" + value.trim().toLowerCase() + "%";
        specifications.add((root, query, builder) ->
                builder.like(builder.lower(root.get(attribute)), pattern));
    }

    private void addRangeFilter(
            List<Specification<EquipmentEntity>> specifications,
            String attribute,
            Integer minimum,
            Integer maximum
    ) {
        if (minimum != null) {
            specifications.add((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get(attribute), minimum));
        }
        if (maximum != null) {
            specifications.add((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get(attribute), maximum));
        }
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
