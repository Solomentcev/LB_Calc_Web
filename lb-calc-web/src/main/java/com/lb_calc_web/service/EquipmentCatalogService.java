package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.EquipmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Управляет каталогом физических моделей оборудования. */
@Service
public class EquipmentCatalogService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentCatalogService.class);

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
        if (filter.id() != null) {
            specifications.add((root, query, builder) ->
                    builder.equal(root.get("id"), filter.id()));
        }
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

    /** Добавляет спецификацию текстового поиска без учёта регистра. */
    private void addTextFilter(
            List<Specification<EquipmentEntity>> specifications,
            String attribute,
            String value
    ) {
        if (value == null || value.isBlank()) {
            return;
        }
        String pattern = "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
        specifications.add((root, query, builder) ->
                builder.like(builder.lower(root.get(attribute)), pattern));
    }

    /** Добавляет нижнюю и верхнюю границы фильтрации числового поля. */
    private void addRangeFilter(
            List<Specification<EquipmentEntity>> specifications,
            String attribute,
            Integer minimum,
            Integer maximum
    ) {
        if (minimum != null) {
            specifications.add((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.<Integer>get(attribute), minimum));
        }
        if (maximum != null) {
            specifications.add((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.<Integer>get(attribute), maximum));
        }
    }

    /** Возвращает все модели оборудования из каталога. */
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

    /** Возвращает идентификаторы моделей, выбранных по умолчанию в каталоге. */
    @Transactional(readOnly = true)
    public java.util.Set<Long> findDefaultModelIds() {
        return equipmentRepository.findAllByDefaultModelTrue().stream()
                .map(EquipmentEntity::getId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    /** Находит модель оборудования по идентификатору. */
    @Transactional(readOnly = true)
    public Equipment findById(Long id) {
        return EquipmentEntityMapper.toDomain(findEntity(id));
    }

    /** Сохраняет модель оборудования после проверки её параметров. */
    @Transactional
    public Equipment save(Equipment equipment) {
        Objects.requireNonNull(equipment, "Модель оборудования не должна быть null");

        EquipmentEntity entity;
        EquipmentFunctionalType previousType = null;
        String previousPurpose = null;
        boolean wasDefault = false;
        if (equipment.getId() == null) {
            entity = EquipmentEntity.create();
            entity.setDefaultModel(!equipmentRepository
                    .existsByFunctionalTypeAndPurposeAndDefaultModelTrue(
                            equipment.getFunctionalType(), equipment.getPurpose().toString()));
        } else {
            entity = findEntity(equipment.getId());
            if (equipmentRepository.countModuleReferencesByEquipmentId(equipment.getId()) > 0
                    && hasChanged(equipment, entity)) {
                throw new IllegalStateException(
                        "Нельзя изменить модель оборудования, пока она установлена в LC/LBC. "
                                + "Создайте новую модель и замените оборудование в конфигурациях");
            }
            previousType = entity.getFunctionalType();
            previousPurpose = entity.getPurpose();
            wasDefault = entity.isDefaultModel();
            if (previousType != equipment.getFunctionalType()
                    || !previousPurpose.equals(equipment.getPurpose().toString())) {
                entity.setDefaultModel(false);
            }
        }

        EquipmentEntityMapper.updateEntity(equipment, entity);
        if (wasDefault && (previousType != entity.getFunctionalType()
                || !previousPurpose.equals(entity.getPurpose()))) {
            equipmentRepository.findFirstByFunctionalTypeAndPurposeOrderByIdAsc(
                    previousType, previousPurpose
            ).ifPresent(replacement -> {
                replacement.setDefaultModel(true);
                equipmentRepository.saveAndFlush(replacement);
            });
        }
        if (!entity.isDefaultModel()
                && !equipmentRepository.existsByFunctionalTypeAndPurposeAndDefaultModelTrue(
                        entity.getFunctionalType(), entity.getPurpose())) {
            entity.setDefaultModel(true);
        }
        EquipmentEntity saved = equipmentRepository.saveAndFlush(entity);
        logger.info("Модель оборудования сохранена: id={}, functionalType={}, purpose={}",
                saved.getId(), saved.getFunctionalType(), saved.getPurpose());
        return EquipmentEntityMapper.toDomain(saved);
    }

    /** Удаляет модель оборудования по идентификатору. */
    @Transactional
    public void delete(Long id) {
        EquipmentEntity entity = findEntity(id);
        if (equipmentRepository.countModuleReferencesByEquipmentId(id) > 0) {
            throw new IllegalStateException(
                    "Нельзя удалить модель оборудования: она установлена в одном или нескольких модулях");
        }
        boolean wasDefault = entity.isDefaultModel();
        var functionalType = entity.getFunctionalType();
        String purpose = entity.getPurpose();
        equipmentRepository.delete(entity);
        equipmentRepository.flush();

        if (wasDefault) {
            equipmentRepository.findFirstByFunctionalTypeAndPurposeOrderByIdAsc(
                    functionalType, purpose
            ).ifPresent(replacement -> {
                replacement.setDefaultModel(true);
                equipmentRepository.saveAndFlush(replacement);
            });
        }
        logger.info("Модель оборудования удалена: id={}, functionalType={}, purpose={}",
                id, functionalType, purpose);
    }

    /** Проверяет, изменились ли характеристики модели, используемой в модулях. */
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

    /** Находит сущность оборудования и проверяет корректность идентификатора. */
    private EquipmentEntity findEntity(Long id) {
        if (id == null || id <= 0) {
            throw new NoSuchElementException("Модель оборудования с id=" + id + " не найдена");
        }
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Модель оборудования с id=" + id + " не найдена"));
    }
}
