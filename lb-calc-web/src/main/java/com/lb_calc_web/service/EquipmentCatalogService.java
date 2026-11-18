package com.lb_calc_web.service;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.domain.equipment.CatalogEquipment;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.BarReader;
import java.util.Map;
import java.util.stream.Collectors;
import com.lb_calc_web.repository.EquipmentCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

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

    public Equipment resolveById(String id) {
        return repository.findById(id).map(this::toDomain).orElseThrow(() ->
                new IllegalArgumentException("Позиция оборудования не найдена в каталоге: " + id));
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
        if (idOrName == null || idOrName.isBlank()) return null;
        if (existingIds != null && !existingIds.isEmpty()) {
            List<Equipment> retained = existingIds.stream()
                    .map(this::resolveById)
                    .filter(item -> item.getCatalogPositionId().equals(idOrName)
                            || item.getName().equalsIgnoreCase(idOrName))
                    .toList();
            if (retained.size() == 1) return retained.get(0);
            if (retained.size() > 1) {
                throw new IllegalArgumentException(
                        "Позиция оборудования неоднозначна в исходной конфигурации: " + idOrName);
            }
        }
        return resolve(idOrName);
    }

    /** Преобразует запись каталога в независимую доменную позицию. */
    public Equipment toDomain(EquipmentCatalogEntity item) {
        return new CatalogEquipment(item.getCatalogPositionId(), item.getCategory(),
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
                () -> new IllegalArgumentException("Оборудование не найдено: " + id));
    }

    @Transactional
    public EquipmentCatalogEntity create(EquipmentCatalogEntity item) {
        validateCatalogData(item.getCategory(), item.isSupportsBarcode1d(),
                item.isSupportsBarcode2d(), item.isSupportsRfid(), item.isSupportsPrint());
        if (repository.existsById(item.getCatalogPositionId())) {
            throw new IllegalArgumentException("ID оборудования уже существует: "
                    + item.getCatalogPositionId());
        }
        if (item.isActive() && item.isDefaultPosition()
                && !repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                        item.getCategory()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Для категории уже задана активная модель по умолчанию: " + item.getCategory());
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
        if (item.isActive() && item.isDefaultPosition()
                && !item.getCategory().equals(category)
                && !repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                        category).isEmpty()) {
            throw new IllegalArgumentException(
                    "Для новой категории уже задана активная модель по умолчанию: " + category);
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
        if (active && item.isDefaultPosition()
                && repository.findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc(
                        item.getCategory()).stream()
                        .anyMatch(existing -> !existing.getCatalogPositionId().equals(id))) {
            throw new IllegalArgumentException(
                    "Для категории уже есть активная модель по умолчанию: " + item.getCategory());
        }
        if (active) item.activate(); else item.deactivate();
        return item;
    }
}
