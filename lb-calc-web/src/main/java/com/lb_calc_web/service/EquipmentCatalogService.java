package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.repository.EquipmentCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Управляет каталогом оборудования и его доступностью для новых конфигураций. */
@Service
@Transactional(readOnly = true)
public class EquipmentCatalogService {
    private final EquipmentCatalogRepository repository;

    public EquipmentCatalogService(EquipmentCatalogRepository repository) {
        this.repository = repository;
    }

    public List<EquipmentCatalogEntity> findAll() {
        return repository.findAllByOrderByCategoryAscNameAsc();
    }

    public Map<String, Equipment> findDomainCatalog() {
        return repository.findAll().stream().collect(Collectors.toUnmodifiableMap(
                EquipmentCatalogEntity::getCatalogPositionId, this::toDomain));
    }

    /**
     * Создаёт разрешитель на основе единовременно загруженного каталога.
     * В карту входят и деактивированные позиции, необходимые историческим конфигурациям.
     */
    public Function<String, Equipment> findDomainCatalogResolver() {
        Map<String, Equipment> catalog = findDomainCatalog();
        return id -> {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("ID позиции оборудования не должен быть пустым");
            }
            Equipment equipment = catalog.get(id);
            if (equipment == null) {
                throw new IllegalArgumentException(
                        "Позиция оборудования не найдена в каталоге: " + id);
            }
            return equipment;
        };
    }

    public Equipment resolveById(String id) {
        return repository.findById(id).map(this::toDomain).orElseThrow(() ->
                new IllegalArgumentException("Позиция оборудования не найдена в каталоге: " + id));
    }

    /**
     * Создаёт разрешитель для редактирования конфигурации на основе одного
     * чтения каталога. Деактивированные позиции разрешены только если их ID
     * присутствовал в исходной сохранённой комплектации.
     */
    public Function<String, Equipment> findExistingConfigurationResolver(Set<String> retainedIds) {
        Set<String> retained = retainedIds == null ? Set.of() : Set.copyOf(retainedIds);
        Map<String, EquipmentCatalogEntity> catalog = repository.findAll().stream()
                .collect(Collectors.toUnmodifiableMap(
                        EquipmentCatalogEntity::getCatalogPositionId,
                        Function.identity()
                ));
        Map<String, List<EquipmentCatalogEntity>> activeByName = catalog.values().stream()
                .filter(EquipmentCatalogEntity::isActive)
                .collect(Collectors.groupingBy(
                        item -> item.getName().toLowerCase(java.util.Locale.ROOT)
                ));

        return idOrName -> {
            if (idOrName == null || idOrName.isBlank()) {
                return null;
            }
            if (Display.NONE_ID.equals(idOrName) || Display.NONE.getName().equals(idOrName)) {
                return Display.NONE;
            }
            if (BarReader.NONE_ID.equals(idOrName) || BarReader.NONE.getName().equals(idOrName)) {
                return BarReader.NONE;
            }

            EquipmentCatalogEntity byId = catalog.get(idOrName);
            if (byId != null) {
                if (!byId.isActive() && !retained.contains(idOrName)) {
                    throw new IllegalArgumentException(
                            "Деактивированную позицию нельзя выбрать для новой конфигурации: " + idOrName);
                }
                return toDomain(byId);
            }

            List<EquipmentCatalogEntity> retainedMatches = catalog.values().stream()
                    .filter(item -> retained.contains(item.getCatalogPositionId()))
                    .filter(item -> item.getName().equalsIgnoreCase(idOrName))
                    .toList();
            if (retainedMatches.size() == 1) {
                return toDomain(retainedMatches.get(0));
            }
            if (retainedMatches.size() > 1) {
                throw new IllegalArgumentException(
                        "Название оборудования неоднозначно в исходной конфигурации: " + idOrName);
            }

            List<EquipmentCatalogEntity> matches = activeByName.getOrDefault(
                    idOrName.toLowerCase(java.util.Locale.ROOT), List.of());
            if (matches.size() == 1) {
                return toDomain(matches.get(0));
            }
            if (matches.size() > 1) {
                throw new IllegalArgumentException(
                        "Название оборудования неоднозначно, используйте ID каталога: " + idOrName);
            }
            throw new IllegalArgumentException(
                    "Активная позиция оборудования не найдена в каталоге: " + idOrName);
        };
    }

    public Equipment resolve(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) return null;
        if (Display.NONE_ID.equals(idOrName) || Display.NONE.getName().equals(idOrName)) {
            return Display.NONE;
        }
        if (BarReader.NONE_ID.equals(idOrName) || BarReader.NONE.getName().equals(idOrName)) {
            return BarReader.NONE;
        }

        var byId = repository.findById(idOrName);
        if (byId.isPresent()) {
            EquipmentCatalogEntity item = byId.get();
            if (!item.isActive()) {
                throw new IllegalArgumentException(
                        "Деактивированную позицию нельзя выбрать для новой конфигурации: " + idOrName);
            }
            return toDomain(item);
        }

        List<EquipmentCatalogEntity> matches = repository.findAllByActiveTrueOrderByCategoryAscNameAsc()
                .stream()
                .filter(item -> item.getName().equalsIgnoreCase(idOrName))
                .toList();
        if (matches.size() == 1) {
            return toDomain(matches.get(0));
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException(
                    "Название оборудования неоднозначно, используйте ID каталога: " + idOrName);
        }
        throw new IllegalArgumentException(
                "Активная позиция оборудования не найдена в каталоге: " + idOrName);
    }

    /**
     * Разрешает позицию для редактирования существующей конфигурации.
     * Деактивированная модель допускается только если она уже присутствовала
     * в исходной сохранённой комплектации.
     */
    public Equipment resolveForExistingConfiguration(String idOrName, Set<String> existingIds) {
        return findExistingConfigurationResolver(existingIds).apply(idOrName);
    }

    /** Преобразует запись каталога в независимую доменную позицию. */
    public Equipment toDomain(EquipmentCatalogEntity item) {
        return new Equipment(item.getCatalogPositionId(), item.getCategory(),
                item.getName(), item.getDescription(), item.getWidth(), item.getHeight(),
                item.getDepth(), item.getMinimumModuleWidth(), item.getDisplayWidth(),
                item.getDisplayHeight(), item.isSupportsBarcode1d(),
                item.isSupportsBarcode2d(), item.isSupportsRfid(), item.isSupportsPrint());
    }

    public List<EquipmentCatalogEntity> findActive() {
        return repository.findAllByActiveTrueOrderByCategoryAscNameAsc();
    }

    public Equipment findActiveDefault(String category) {
        List<EquipmentCatalogEntity> defaults =
                repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(category);
        if (defaults.size() != 1) {
            throw new IllegalStateException(
                    "Для категории " + category
                            + " должна быть ровно одна активная модель по умолчанию; найдено: "
                            + defaults.size());
        }
        return toDomain(defaults.get(0));
    }

    public EquipmentCatalogEntity get(String id) {
        return repository.findById(id).orElseThrow(
                () -> new java.util.NoSuchElementException("Оборудование не найдено: " + id));
    }

    @Transactional
    public EquipmentCatalogEntity create(EquipmentCatalogEntity item) {
        if (item == null) {
            throw new IllegalArgumentException("Позиция оборудования не должна быть null");
        }
        if (item.isDefaultPosition() && !item.isActive()) {
            throw new IllegalArgumentException("Деактивированная модель не может быть моделью по умолчанию");
        }
        validateCatalogData(item.getCategory(), item.isSupportsBarcode1d(),
                item.isSupportsBarcode2d(), item.isSupportsRfid(), item.isSupportsPrint());
        if (repository.existsById(item.getCatalogPositionId())) {
            throw new IllegalArgumentException("ID оборудования уже существует: "
                    + item.getCatalogPositionId());
        }
        List<EquipmentCatalogEntity> activeDefaults =
                repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                        item.getCategory());
        if (activeDefaults.size() > 1) {
            throw new IllegalStateException(
                    "В категории уже нарушено правило единственной модели по умолчанию: "
                            + item.getCategory());
        }
        if (item.isActive() && item.isDefaultPosition() && !activeDefaults.isEmpty()) {
            throw new IllegalArgumentException(
                    "Для категории уже задана активная модель по умолчанию: " + item.getCategory());
        }
        if (item.isActive() && !item.isDefaultPosition() && activeDefaults.isEmpty()) {
            throw new IllegalArgumentException(
                    "Первая активная модель категории должна быть назначена моделью по умолчанию: "
                            + item.getCategory());
        }
        return repository.save(item);
    }

    @Transactional
    public EquipmentCatalogEntity update(String id, String category, String name,
                                         String description, int width, int height, int depth,
                                         int displayWidth, int displayHeight, int minimumModuleWidth,
                                         boolean barcode1d, boolean barcode2d,
                                         boolean rfid, boolean print) {
        validateCatalogData(category, barcode1d, barcode2d, rfid, print);
        EquipmentCatalogEntity item = get(id);
        if (item.isActive() && !item.getCategory().equals(category)) {
            List<EquipmentCatalogEntity> targetDefaults =
                    repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(category);
            if (item.isDefaultPosition()) {
                if (!hasOtherActiveDefault(item.getCategory(), id)) {
                    throw new IllegalStateException(
                            "Перед переносом модели назначьте другую модель по умолчанию для категории: "
                                    + item.getCategory());
                }
                if (!targetDefaults.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Для новой категории уже задана активная модель по умолчанию: " + category);
                }
            } else if (targetDefaults.size() != 1) {
                throw new IllegalStateException(
                        "В целевой категории должна быть ровно одна активная модель по умолчанию: "
                                + category);
            }
        }
        if (repository.isUsedInConfigurations(id)
                && hasImmutableCharacteristicsChanged(item, category, name, width, height, depth,
                displayWidth, displayHeight, minimumModuleWidth,
                barcode1d, barcode2d, rfid, print)) {
            throw new IllegalStateException(
                    "Нельзя изменять категорию, название, габариты и совместимость оборудования, "
                            + "которое используется в сохранённых конфигурациях: " + id);
        }
        item.update(category, name, description, width, height, depth, displayWidth,
                displayHeight, minimumModuleWidth, barcode1d, barcode2d, rfid, print);
        return item;
    }

    private void validateCatalogData(String category, boolean barcode1d,
                                      boolean barcode2d, boolean rfid, boolean print) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("Категория оборудования обязательна");
        }
        boolean valid = switch (category) {
            case "DISPLAY" -> !barcode1d && !barcode2d && !rfid && !print;
            case "BAR_READER" -> (barcode1d || barcode2d) && !rfid && !print;
            case "PRINTER" -> print && !barcode1d && !barcode2d && !rfid;
            case "RFID_READER" -> rfid && !barcode1d && !barcode2d && !print;
            case "COMPUTER", "LOCK_CONTROLLER", "POWER_SUPPLY", "UPS",
                    "PAYMENT_TERMINAL", "CASH_ACCEPTOR", "MODEM", "ROUTER", "ANTENNA" ->
                    !barcode1d && !barcode2d && !rfid && !print;
            default -> !barcode1d && !barcode2d && !rfid && !print;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Характеристики оборудования не соответствуют категории " + category);
        }
    }

    private boolean hasImmutableCharacteristicsChanged(
            EquipmentCatalogEntity item, String category, String name,
            int width, int height, int depth,
            int displayWidth, int displayHeight, int minimumModuleWidth,
            boolean barcode1d, boolean barcode2d, boolean rfid, boolean print) {
        return !item.getCategory().equals(category)
                || !item.getName().equals(name)
                || item.getWidth() != width
                || item.getHeight() != height
                || item.getDepth() != depth
                || item.getDisplayWidth() != displayWidth
                || item.getDisplayHeight() != displayHeight
                || item.getMinimumModuleWidth() != minimumModuleWidth
                || item.isSupportsBarcode1d() != barcode1d
                || item.isSupportsBarcode2d() != barcode2d
                || item.isSupportsRfid() != rfid
                || item.isSupportsPrint() != print;
    }

    private boolean hasOtherActiveDefault(String category, String excludedId) {
        return repository
                .findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(category)
                .stream()
                .anyMatch(item -> !item.getCatalogPositionId().equals(excludedId));
    }

    @Transactional
    public void delete(String id) {
        EquipmentCatalogEntity item = get(id);
        if (repository.isUsedInConfigurations(id)) {
            throw new IllegalStateException(
                    "Нельзя удалить позицию оборудования, используемую в конфигурациях: " + id);
        }
        if (item.isActive() && item.isDefaultPosition()) {
            throw new IllegalStateException(
                    "Перед удалением активной модели по умолчанию назначьте другую модель: " + id);
        }
        repository.delete(item);
    }

    @Transactional
    public EquipmentCatalogEntity setDefault(String id, boolean defaultPosition) {
        EquipmentCatalogEntity item = get(id);
        if (!defaultPosition && item.isActive() && item.isDefaultPosition()
                && !hasOtherActiveDefault(item.getCategory(), id)) {
            throw new IllegalStateException(
                    "Нельзя снять признак модели по умолчанию с единственной активной модели категории: "
                            + item.getCategory());
        }
        if (defaultPosition && !item.isActive()) {
            throw new IllegalArgumentException(
                    "Деактивированная модель не может быть моделью по умолчанию: " + id);
        }
        if (defaultPosition) {
            repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                            item.getCategory())
                    .stream()
                    .filter(existing -> !existing.getCatalogPositionId().equals(id))
                    .forEach(existing -> existing.setDefaultPosition(false));
            repository.flush();
        }
        item.setDefaultPosition(defaultPosition);
        return item;
    }

    @Transactional
    public EquipmentCatalogEntity setActive(String id, boolean active) {
        EquipmentCatalogEntity item = get(id);
        if (!active && item.isActive() && item.isDefaultPosition()
                && !hasOtherActiveDefault(item.getCategory(), id)) {
            throw new IllegalStateException(
                    "Перед деактивацией модели по умолчанию назначьте другую активную модель категории: "
                            + item.getCategory());
        }
        if (active) {
            List<EquipmentCatalogEntity> activeDefaults =
                    repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                            item.getCategory());
            if (activeDefaults.size() > 1) {
                throw new IllegalStateException(
                        "В категории уже нарушено правило единственной модели по умолчанию: "
                                + item.getCategory());
            }
            boolean anotherDefaultExists = activeDefaults.stream()
                    .anyMatch(existing -> !existing.getCatalogPositionId().equals(id));
            if (item.isDefaultPosition() && anotherDefaultExists) {
                throw new IllegalArgumentException(
                        "Для категории уже есть активная модель по умолчанию: " + item.getCategory());
            }
            if (!item.isDefaultPosition() && activeDefaults.isEmpty()) {
                throw new IllegalStateException(
                        "Перед активацией модели назначьте активную модель по умолчанию для категории: "
                                + item.getCategory());
            }
            item.activate();
        } else {
            item.deactivate();
        }
        return item;
    }
}
