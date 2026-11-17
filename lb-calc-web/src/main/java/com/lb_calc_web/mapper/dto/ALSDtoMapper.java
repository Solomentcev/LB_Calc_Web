package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ALSModuleOrderDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LBCDTO;

import java.util.function.Function;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
                ? restoreLegacyOrder(dto, lbTypeResolver, lbcTypeResolver)
                : restoreExplicitOrder(dto, lbTypeResolver, lbcTypeResolver);

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
            Function<LBCDTO, TypeLb> lbcTypeResolver
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
                ? LBCDtoMapper.toDomain(dto.getLBC(), lbcTypeResolver.apply(dto.getLBC()))
                : LCDtoMapper.toDomain(dto.getLC());

        return insertControlModule(storageModules, controlModule, dto.getPositionControlModule());
    }

    /**
     * Восстанавливает точный физический порядок по явным ссылкам DTO.
     */
    private static List<Module> restoreExplicitOrder(
            ALSDTO dto,
            Function<LBDTO, TypeLb> lbTypeResolver,
            Function<LBCDTO, TypeLb> lbcTypeResolver
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
                    modules.add(LCDtoMapper.toDomain(dto.getLC()));
                }
                case "LBC" -> {
                    if (item.getIndex() != 0 || dto.getLBC() == null || controlModuleUsed) {
                        throw new IllegalStateException("moduleOrder содержит недопустимую ссылку на LBC");
                    }
                    controlModuleUsed = true;
                    modules.add(LBCDtoMapper.toDomain(dto.getLBC(), lbcTypeResolver.apply(dto.getLBC())));
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
