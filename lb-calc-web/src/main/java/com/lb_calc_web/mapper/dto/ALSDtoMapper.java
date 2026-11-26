package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.dto.ALSModuleOrderDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.mapper.dto.LCDtoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует транспортную модель ALS в доменную конфигурацию и обратно. */
public final class ALSDtoMapper {

    private ALSDtoMapper() {
    }

    /** Преобразует доменную конфигурацию ALS в DTO для представления. */
    public static ALSDTO toDto(ALS domain) {
        if (domain == null) {
            return null;
        }

        ALSDTO dto = new ALSDTO();
        dto.setId(domain.getId());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setHeight(domain.getHeight());
        dto.setWidth(domain.getWidth());
        dto.setDepth(domain.getDepth());
        dto.setUpperFrame(domain.getUpperFrame());
        dto.setBottomFrame(domain.getBottomFrame());
        dto.setDepthCell(domain.getDepthCell());
        dto.setCountCells(domain.getCountCells());
        dto.setColorBody(domain.getColorBody().name());
        dto.setColorDoor(domain.getColorDoor().name());
        dto.setPositionControlModule(domain.getPositionControlModule().name());
        dto.setFunctionalConfiguration(toDto(domain.getFunctionalConfiguration()));

        List<LBDTO> lbList = new ArrayList<>();
        List<ALSModuleOrderDTO> moduleOrder = new ArrayList<>();

        for (Module module : domain.getModules()) {
            if (module instanceof LC lc) {
                dto.setLC(LCDtoMapper.toDto(lc));
                moduleOrder.add(new ALSModuleOrderDTO("LC", 0));
            } else if (module instanceof LBC lbc) {
                dto.setLBC(LBCDtoMapper.toDto(lbc));
                moduleOrder.add(new ALSModuleOrderDTO("LBC", 0));
            } else if (module instanceof LB lb) {
                LBDTO lbDto = LBDtoMapper.toDto(lb);
                int index = lbList.size();
                lbList.add(lbDto);
                moduleOrder.add(new ALSModuleOrderDTO("LB", index));
            }
        }

        dto.setLbList(lbList);
        dto.setModuleOrder(moduleOrder);
        return dto;
    }

    private static FunctionalConfigurationDTO toDto(FunctionalConfiguration configuration) {
        FunctionalConfigurationDTO dto = new FunctionalConfigurationDTO();
        dto.setDisplay(configuration.getDisplay());
        dto.setAccess(configuration.getAccess());
        dto.setPayment(configuration.getPayment());
        dto.setPrinter(configuration.getPrinter());
        return dto;
    }

    /** Обновляет в DTO вычисляемые геометрические характеристики ALS. */
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
        target.setFunctionalConfiguration(calculated.getFunctionalConfiguration());
        target.setModuleOrder(calculated.getModuleOrder());
    }

    /** Находит позицию физического экземпляра LB в порядке модулей ALS. */
    public static int findPhysicalLbIndex(ALS domain, int lbIndex) {
        Objects.requireNonNull(domain, "ALS не должна быть null");
        int current = 0;
        List<Module> modules = domain.getModules();
        for (int index = 0; index < modules.size(); index++) {
            if (modules.get(index) instanceof LB) {
                if (current == lbIndex) {
                    return index;
                }
                current++;
            }
        }
        throw new IllegalStateException(
                "Не удалось сопоставить LB DTO с модулем доменной ALS");
    }

    /** Восстанавливает ALS с настройками геометрии LC приложения. */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<Long, com.lb_calc_web.domain.equipment.Equipment> equipmentLookup,
            com.lb_calc_web.domain.model.ControlConfiguration fallbackConfiguration,
            int minimumWidth, int displaySideClearance, int widthRoundingStep,
            int displayCenterHeight, int displayTopClearance,
            int lbcTechnicalCellMinWidth, int lbcTechnicalCellMinHeight,
            int lbcEquipmentClearance, int lbcDisplayCenterHeight, int storageCellMinHeight
    ) {
        return toDomain(dto, lbTypeResolver, lbcTypeResolver, equipmentLookup,
                fallbackConfiguration, minimumWidth, displaySideClearance, widthRoundingStep,
                displayCenterHeight, displayTopClearance, 200, 200, 20, 1300, 85);
    }

    /** Восстанавливает ALS с настройками физической геометрии LC и LBC. */
    public static ALS toDomain(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<Long, com.lb_calc_web.domain.equipment.Equipment> equipmentLookup,
            com.lb_calc_web.domain.model.ControlConfiguration fallbackConfiguration,
            int minimumWidth, int displaySideClearance, int widthRoundingStep,
            int displayCenterHeight, int displayTopClearance,
            int lbcTechnicalCellMinWidth, int lbcTechnicalCellMinHeight,
            int lbcEquipmentClearance, int lbcDisplayCenterHeight, int storageCellMinHeight
    ) {
        if (dto == null) {
            return null;
        }
        Objects.requireNonNull(lbTypeResolver, "lbTypeResolver не должен быть null");
        Objects.requireNonNull(lbcTypeResolver, "lbcTypeResolver не должен быть null");
        Objects.requireNonNull(equipmentLookup, "equipmentLookup не должен быть null");

        if (dto.getLC() != null && dto.getLBC() != null) {
            throw new IllegalStateException("ALS не может одновременно содержать LC и LBC");
        }
        if (dto.getLC() == null && dto.getLBC() == null) {
            throw new IllegalStateException("ALS должен содержать модуль управления");
        }

        List<Module> modules = dto.getModuleOrder() == null
                ? restoreLegacyOrder(
                        dto, lbTypeResolver, lbcTypeResolver, equipmentLookup, fallbackConfiguration,
                        minimumWidth, displaySideClearance, widthRoundingStep,
                        displayCenterHeight, displayTopClearance,
                        lbcTechnicalCellMinWidth, lbcTechnicalCellMinHeight,
                        lbcEquipmentClearance, lbcDisplayCenterHeight, storageCellMinHeight)
                : restoreExplicitOrder(
                        dto, lbTypeResolver, lbcTypeResolver, equipmentLookup, fallbackConfiguration,
                        minimumWidth, displaySideClearance, widthRoundingStep,
                        displayCenterHeight, displayTopClearance,
                        lbcTechnicalCellMinWidth, lbcTechnicalCellMinHeight,
                        lbcEquipmentClearance, lbcDisplayCenterHeight, storageCellMinHeight);

        ALS domain = new ALS(
                dto.getHeight(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                com.lb_calc_web.domain.attributes.Colors.valueOf(dto.getColorBody()),
                com.lb_calc_web.domain.attributes.Colors.valueOf(dto.getColorDoor()),
                modules
        );
        domain.setId(dto.getId() == null || dto.getId() <= 0 ? null : dto.getId());

        if (dto.getFunctionalConfiguration() != null) {
            FunctionalConfigurationDTO functional = dto.getFunctionalConfiguration();
            FunctionalConfiguration current = domain.getFunctionalConfiguration();
            domain.setFunctionalConfiguration(new FunctionalConfiguration(
                    functional.getDisplay() == null ? current.getDisplay() : functional.getDisplay(),
                    functional.getAccess() == null ? current.getAccess() : functional.getAccess(),
                    functional.getPayment() == null ? current.getPayment() : functional.getPayment(),
                    functional.getPrinter() == null ? current.getPrinter() : functional.getPrinter()
            ));
        }

        if (dto.getModuleOrder() != null
                && dto.getPositionControlModule() != null
                && !dto.getPositionControlModule().isBlank()
                && !domain.getPositionControlModule().name().equals(dto.getPositionControlModule())) {
            throw new IllegalStateException(
                    "positionControlModule не соответствует физическому порядку moduleOrder");
        }
        return domain;
    }

    private static List<Module> restoreLegacyOrder(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<Long, com.lb_calc_web.domain.equipment.Equipment> equipmentLookup,
            com.lb_calc_web.domain.model.ControlConfiguration fallbackConfiguration,
            int minimumWidth, int displaySideClearance, int widthRoundingStep,
            int displayCenterHeight, int displayTopClearance
    ) {
        List<LBDTO> lbDtos = dto.getLbList() == null ? List.of() : dto.getLbList();
        List<Module> modules = new ArrayList<>();
        for (LBDTO lbDto : lbDtos) {
            if (lbDto == null) {
                throw new IllegalStateException("Список LB содержит пустой элемент");
            }
            modules.add(LBDtoMapper.toDomain(lbDto, lbTypeResolver.apply(lbDto)));
        }

        Module control = dto.getLBC() != null
                ? LBCDtoMapper.toDomain(dto.getLBC(), lbcTypeResolver.apply(dto.getLBC()), equipmentLookup,
                        fallbackConfiguration, lbcTechnicalCellMinWidth, lbcTechnicalCellMinHeight,
                        lbcEquipmentClearance, lbcDisplayCenterHeight, storageCellMinHeight)
                : LCDtoMapper.toDomain(
                        dto.getLC(), equipmentLookup, fallbackConfiguration,
                        minimumWidth, displaySideClearance, widthRoundingStep,
                        displayCenterHeight, displayTopClearance);
        return insertControlModule(modules, control, dto.getPositionControlModule());
    }

    private static List<Module> restoreExplicitOrder(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver,
            Function<Long, com.lb_calc_web.domain.equipment.Equipment> equipmentLookup,
            com.lb_calc_web.domain.model.ControlConfiguration fallbackConfiguration,
            int minimumWidth, int displaySideClearance, int widthRoundingStep,
            int displayCenterHeight, int displayTopClearance
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
                        throw new IllegalStateException(
                                "moduleOrder содержит недопустимый или повторный индекс LB");
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
                    modules.add(LCDtoMapper.toDomain(
                            dto.getLC(), equipmentLookup, fallbackConfiguration,
                            minimumWidth, displaySideClearance, widthRoundingStep,
                            displayCenterHeight, displayTopClearance));
                }
                case "LBC" -> {
                    if (item.getIndex() != 0 || dto.getLBC() == null || controlModuleUsed) {
                        throw new IllegalStateException("moduleOrder содержит недопустимую ссылку на LBC");
                    }
                    controlModuleUsed = true;
                    modules.add(LBCDtoMapper.toDomain(
                            dto.getLBC(), lbcTypeResolver.apply(dto.getLBC()), equipmentLookup, fallbackConfiguration,
                            lbcTechnicalCellMinWidth, lbcTechnicalCellMinHeight,
                            lbcEquipmentClearance, lbcDisplayCenterHeight, storageCellMinHeight));
                }
                default -> throw new IllegalStateException(
                        "Неизвестный тип модуля в moduleOrder: " + item.getType());
            }
        }

        if (!controlModuleUsed) {
            throw new IllegalStateException("moduleOrder не содержит управляющий модуль");
        }
        for (boolean used : usedLbIndexes) {
            if (!used) {
                throw new IllegalStateException(
                        "moduleOrder не содержит ссылку на каждый LB из lbList");
            }
        }
        return modules;
    }

    private static List<Module> insertControlModule(
            List<Module> storageModules,
            Module controlModule,
            String position
    ) {
        List<Module> modules = new ArrayList<>(storageModules);
        PositionControlModule controlPosition = PositionControlModule.valueOf(
                position == null || position.isBlank()
                        ? PositionControlModule.CENTER.name()
                        : position
        );

        int index = switch (controlPosition) {
            case LEFT -> 0;
            case RIGHT -> modules.size();
            case CENTER -> {
                if (modules.size() == 1) {
                    throw new IllegalStateException(
                            "Положение CENTER недопустимо для ALS с одним LB");
                }
                yield modules.size() / 2;
            }
        };
        modules.add(index, controlModule);
        return modules;
    }
}
