package com.lb_calc_web.service;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.domain.equipment.CatalogEquipment;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.domain.equipment.BarReader;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.lb_calc_web.repository.EquipmentCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public EquipmentCatalogEntity get(String id) {
        return repository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Оборудование не найдено: " + id));
    }

    @Transactional
    public EquipmentCatalogEntity create(EquipmentCatalogEntity item) {
        if (repository.existsById(item.getCatalogPositionId())) {
            throw new IllegalArgumentException("ID оборудования уже существует: "
                    + item.getCatalogPositionId());
        }
        return repository.save(item);
    }

    @Transactional
    public EquipmentCatalogEntity update(String id, String category, String name,
                                         String description, int width, int height, int depth,
                                         int displayWidth, int displayHeight, int minimumModuleWidth,
                                         boolean barcode1d, boolean barcode2d,
                                         boolean rfid, boolean print) {
        EquipmentCatalogEntity item = get(id);
        if (repository.isUsedInConfigurations(id)
                && hasPhysicalCharacteristicsChanged(item, category, width, height, depth,
                displayWidth, displayHeight, minimumModuleWidth,
                barcode1d, barcode2d, rfid, print)) {
            throw new IllegalStateException(
                    "Нельзя изменять категорию, габариты и совместимость оборудования, "
                            + "которое используется в сохранённых конфигурациях: " + id);
        }
        item.update(category, name, description, width, height, depth, displayWidth,
                displayHeight, minimumModuleWidth, barcode1d, barcode2d, rfid, print);
        return item;
    }

    private boolean hasPhysicalCharacteristicsChanged(
            EquipmentCatalogEntity item, String category,
            int width, int height, int depth,
            int displayWidth, int displayHeight, int minimumModuleWidth,
            boolean barcode1d, boolean barcode2d, boolean rfid, boolean print) {
        return !item.getCategory().equals(category)
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
    public EquipmentCatalogEntity setActive(String id, boolean active) {
        EquipmentCatalogEntity item = get(id);
        if (active) item.activate(); else item.deactivate();
        return item;
    }
}
