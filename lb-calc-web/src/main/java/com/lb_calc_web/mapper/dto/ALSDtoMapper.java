package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ALSModuleOrderDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.EquipmentSelectionDTO;
import com.lb_calc_web.dto.validation.ValidationResult;

import java.util.function.Function;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ALSDtoMapper {

    private ALSDtoMapper() {
    }

    public static ALSDTO toDto(ALS domain) {
        if (domain == null) {
            return null;
        }

        ALSDTO dto = new ALSDTO();

        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());

        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());

        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());

        dto.setDepthCell(domain.getDepthCell());

        dto.setCountCells(
                domain.getCountCells()
        );

        dto.setColorBody(
                domain.getColorBody().name()
        );

        dto.setColorDoor(
                domain.getColorDoor().name()
        );

        dto.setPositionControlModule(
                domain.getPositionControlModule().name()
        );

        List<LBDTO> lbList = new ArrayList<>();
        Map<LBDTO, Integer> quantityLB = new LinkedHashMap<>();
        List<ALSModuleOrderDTO> moduleOrder = new ArrayList<>();

        for (Module module : domain.getModules()) {
            if (module instanceof LC lc) {
                dto.setLC(LCDtoMapper.toDto(lc));
                moduleOrder.add(new ALSModuleOrderDTO("LC", 0));
                continue;
            }
            if (module instanceof LBC lbc) {
                dto.setLBC(LBCDtoMapper.toDto(lbc));
                moduleOrder.add(new ALSModuleOrderDTO("LBC", 0));
                continue;
            }
            if (module instanceof LB lb) {
                LBDTO lbDto = LBDtoMapper.toDto(lb);
                int lbIndex = lbList.size();
                lbList.add(lbDto);
                moduleOrder.add(new ALSModuleOrderDTO("LB", lbIndex));
                quantityLB.merge(lbDto, 1, Integer::sum);
            }
        }

        dto.setLbList(lbList);
        dto.setQuantityLB(quantityLB);
        dto.setModuleOrder(moduleOrder);

        return dto;
    }

    /**
     * Переносит вычисленные доменной моделью характеристики в существующий DTO.
     */
    public static void updateCalculatedFields(ALSDTO target, ALS domain) {
        Objects.requireNonNull(target, "Целевой ALSDTO не должен быть null");
        Objects.requireNonNull(domain, "ALS не должна быть null");
        ALSDTO calculated = toDto(domain);
        target.setName(calculated.getName());
        target.setDescription(calculated.getDescription());
        target.setWidth(calculated.getWidth());
        target.setCountCells(calculated.getCountCells());
        target.setDepthCell(calculated.getDepthCell());
        target.setPositionControlModule(calculated.getPositionControlModule());
        target.setModuleOrder(calculated.getModuleOrder());
    }

    /**
     * Переносит persistence ID ALS и её модулей в результаты валидации.
     */
    public static void assignValidationObjectIds(List<ValidationResult> results, ALS domain, ALSDTO source) {
        if (results.isEmpty()) return;
        results.get(0).setObjectId(source.getId());
        List<LBDTO> sourceLBs = source.getLbList() == null ? List.of()
                : source.getLbList().stream().filter(Objects::nonNull).toList();
        int resultIndex = 1, lbIndex = 0;
        for (Module module : domain.getModules()) {
            Long moduleId = null;
            if (module instanceof LBC && source.getLBC() != null) moduleId = source.getLBC().getId();
            else if (module instanceof LC && source.getLC() != null) moduleId = source.getLC().getId();
            else if (module instanceof LB) {
                if (lbIndex < sourceLBs.size()) moduleId = sourceLBs.get(lbIndex).getId();
                lbIndex++;
            }
            if (resultIndex < results.size()) results.get(resultIndex).setObjectId(moduleId);
            resultIndex++;
        }
    }

    /**
     * Находит физический индекс LB в доменном списке модулей по индексу DTO.
     */
    public static int findPhysicalLbIndex(ALS domain, int lbIndex) {
        Objects.requireNonNull(domain, "ALS не должна быть null");
        int currentLbIndex = 0;
        List<Module> modules = domain.getModules();
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i) instanceof LB) {
                if (currentLbIndex == lbIndex) return i;
                currentLbIndex++;
            }
        }
        throw new IllegalStateException("Не удалось сопоставить LB DTO с модулем доменной ALS");
    }

    public static ALS toDomain(ALSDTO dto) {
        return toDomain(
                dto,
                lbDto -> new TypeLb(
                        lbDto.getType(),
                        lbDto.getDeltaWidth(),
                        lbDto.getShelfThick(),
                        lbDto.getServiceZoneWidth()
                ),
                lbcDto -> new TypeLb(
                        lbcDto.getType(),
                        lbcDto.getDeltaWidth(),
                        lbcDto.getShelfThick(),
                        lbcDto.getServiceZoneWidth()
                )
        );
    }

    /**
     * Преобразует DTO в ALS, используя конструктивные типы,
     * разрешённые application-слоем.
     *
     * @param dto транспортное представление ALS
     * @param lbTypeResolver разрешает тип обычного модуля хранения
     * @param lbcTypeResolver разрешает тип комбинированного модуля
     * @return доменная конфигурация ALS
     */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver
    ) {
        return toDomain(dto, lbTypeResolver, lbcTypeResolver, EquipmentCatalog::resolve);
    }

    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver
    ) {
        return toDomain(
                dto,
                lbTypeResolver,
                lbcTypeResolver,
                equipmentResolver,
                dto == null || dto.getLC() == null ? null : dto.getLC().getEquipmentSelections(),
                dto == null || dto.getLBC() == null ? null : dto.getLBC().getEquipmentSelections()
        );
    }

    /**
     * Преобразует ALS, используя подготовленный состав оборудования управляющего модуля.
     * DTO и вложенные DTO остаются неизменными.
     */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver,
            List<EquipmentSelectionDTO> lcEquipmentSelections,
            List<EquipmentSelectionDTO> lbcEquipmentSelections
    ) {
        if (dto == null) {
            return null;
        }

        java.util.Objects.requireNonNull(
                lbTypeResolver,
                "lbTypeResolver не должен быть null"
        );
        java.util.Objects.requireNonNull(
                lbcTypeResolver,
                "lbcTypeResolver не должен быть null"
        );
        java.util.Objects.requireNonNull(
                equipmentResolver,
                "equipmentResolver не должен быть null"
        );

        Colors colorBody = Colors.valueOf(
                dto.getColorBody()
        );

        Colors colorDoor = Colors.valueOf(
                dto.getColorDoor()
        );

        if (dto.getLC() != null && dto.getLBC() != null) {
            throw new IllegalStateException(
                    "ALS не может одновременно содержать LC и LBC"
            );
        }
        if (dto.getLC() == null && dto.getLBC() == null) {
            throw new IllegalStateException(
                    "ALS должен содержать модуль управления"
            );
        }

        List<Module> modules = dto.getModuleOrder() == null
                ? restoreLegacyOrder(dto, lbTypeResolver, lbcTypeResolver, equipmentResolver, lcEquipmentSelections, lbcEquipmentSelections)
                : restoreExplicitOrder(dto, lbTypeResolver, lbcTypeResolver, equipmentResolver, lcEquipmentSelections, lbcEquipmentSelections);

        ALS domain = new ALS(
                dto.getHeight(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                colorBody,
                colorDoor,
                modules
        );

        if (dto.getModuleOrder() != null
                && dto.getPositionControlModule() != null
                && !dto.getPositionControlModule().isBlank()
                && !domain.getPositionControlModule().name()
                        .equals(dto.getPositionControlModule())) {
            throw new IllegalStateException(
                    "positionControlModule не соответствует физическому порядку moduleOrder"
            );
        }
        return domain;
    }

    /**
     * Восстанавливает физический порядок по прежнему DTO-контракту.
     */
    private static List<Module> restoreLegacyOrder(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver,
            List<EquipmentSelectionDTO> lcEquipmentSelections,
            List<EquipmentSelectionDTO> lbcEquipmentSelections
    ) {
        List<LBDTO> lbDtos = dto.getLbList() == null ? List.of() : dto.getLbList();
        List<Module> storageModules = new ArrayList<>();
        for (LBDTO lbDto : lbDtos) {
            if (lbDto == null) {
                throw new IllegalStateException("Список LB содержит пустой элемент");
            }
            storageModules.add(LBDtoMapper.toDomain(lbDto, lbTypeResolver.apply(lbDto)));
        }

        Module controlModule = dto.getLBC() != null
                ? LBCDtoMapper.toDomain(dto.getLBC(), lbcTypeResolver.apply(dto.getLBC()), lbcEquipmentSelections, equipmentResolver)
                : LCDtoMapper.toDomain(dto.getLC(), lcEquipmentSelections, equipmentResolver);

        return insertControlModule(storageModules, controlModule, dto.getPositionControlModule());
    }

    /**
     * Восстанавливает точный физический порядок по явным ссылкам DTO.
     */
    private static List<Module> restoreExplicitOrder(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<String, Equipment> equipmentResolver
    ) {
        List<LBDTO> lbDtos = dto.getLbList() == null ? List.of() : dto.getLbList();
        boolean[] usedLbIndexes = new boolean[lbDtos.size()];
        boolean controlModuleUsed = false;
        List<Module> modules = new ArrayList<>();

        for (ALSModuleOrderDTO item : dto.getModuleOrder()) {
            if (item == null || item.getType() == null || item.getIndex() < 0) {
                throw new IllegalStateException("moduleOrder содержит некорректную ссылку на модуль");
            }
            switch (item.getType()) {
                case "LB" -> {
                    int index = item.getIndex();
                    if (index >= lbDtos.size() || usedLbIndexes[index]) {
                        throw new IllegalStateException("moduleOrder содержит недопустимый или повторный индекс LB");
                    }
                    LBDTO lbDto = lbDtos.get(index);
                    if (lbDto == null) {
                        throw new IllegalStateException("Список LB содержит пустой элемент");
                    }
                    usedLbIndexes[index] = true;
                    modules.add(LBDtoMapper.toDomain(lbDto, lbTypeResolver.apply(lbDto)));
                }
                case "LC" -> {
                    if (item.getIndex() != 0 || dto.getLC() == null || controlModuleUsed) {
                        throw new IllegalStateException("moduleOrder содержит недопустимую ссылку на LC");
                    }
                    controlModuleUsed = true;
                    modules.add(LCDtoMapper.toDomain(dto.getLC(), equipmentResolver));
                }
                case "LBC" -> {
                    if (item.getIndex() != 0 || dto.getLBC() == null || controlModuleUsed) {
                        throw new IllegalStateException("moduleOrder содержит недопустимую ссылку на LBC");
                    }
                    controlModuleUsed = true;
                    modules.add(LBCDtoMapper.toDomain(dto.getLBC(), lbcTypeResolver.apply(dto.getLBC()), equipmentResolver));
                }
                default -> throw new IllegalStateException("Неизвестный тип модуля в moduleOrder: " + item.getType());
            }
        }

        if (!controlModuleUsed) {
            throw new IllegalStateException("moduleOrder не содержит управляющий модуль");
        }
        for (boolean used : usedLbIndexes) {
            if (!used) {
                throw new IllegalStateException("moduleOrder не содержит ссылку на каждый LB из lbList");
            }
        }
        return modules;
    }

    public static List<ALSDTO> toDtoList(
            List<ALS> domains
    ) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(ALSDtoMapper::toDto)
                .toList();
    }

    public static List<ALS> toDomainList(
            List<ALSDTO> dtos
    ) {
        if (dtos == null) {
            return List.of();
        }

        return dtos.stream()
                .map(ALSDtoMapper::toDomain)
                .toList();
    }

    private static List<Module> insertControlModule(
            List<Module> storageModules,
            Module controlModule,
            String position
    ) {
        List<Module> modules = new ArrayList<>(
                storageModules
        );

        PositionControlModule controlPosition =
                PositionControlModule.valueOf(
                        position == null || position.isBlank()
                                ? PositionControlModule.CENTER.name()
                                : position
                );

        int index;

        switch (controlPosition) {
            case LEFT -> index = 0;

            case RIGHT -> index = modules.size();

            case CENTER -> {
                if (modules.size() == 1) {
                    throw new IllegalStateException(
                            "Положение CENTER недопустимо для ALS с одним LB"
                    );
                }

                index = modules.size() / 2;
            }

            default -> throw new IllegalStateException(
                    "Неизвестное положение модуля управления: " + controlPosition
            );
        }

        modules.add(
                index,
                controlModule
        );

        return modules;
    }
}
