package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;

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

        dto.setDepthCell(
                calculateDepthCell(domain)
        );

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

        for (Module module : domain.getModules()) {

            if (module instanceof LC lc) {
                dto.setLC(
                        LCDtoMapper.toDto(lc)
                );
                continue;
            }

            if (module instanceof LBC lbc) {
                dto.setLBC(
                        LBCDtoMapper.toDto(lbc)
                );
                continue;
            }

            if (module instanceof LB lb) {
                LBDTO lbDto = LBDtoMapper.toDto(lb);

                lbList.add(lbDto);

                quantityLB.merge(
                        lbDto,
                        1,
                        Integer::sum
                );
            }
        }

        dto.setLbList(lbList);
        dto.setQuantityLB(quantityLB);

        return dto;
    }

    public static ALS toDomain(ALSDTO dto) {
        if (dto == null) {
            return null;
        }

        Colors colorBody = Colors.valueOf(
                dto.getColorBody()
        );

        Colors colorDoor = Colors.valueOf(
                dto.getColorDoor()
        );

        List<Module> storageModules = new ArrayList<>();

        if (dto.getLbList() != null) {
            for (LBDTO lbDto : dto.getLbList()) {
                if (lbDto != null) {
                    storageModules.add(
                            LBDtoMapper.toDomain(lbDto)
                    );
                }
            }
        }

        Module controlModule;

        if (dto.getLBC() != null) {
            controlModule = LBCDtoMapper.toDomain(
                    dto.getLBC()
            );
        } else if (dto.getLC() != null) {
            controlModule = LCDtoMapper.toDomain(
                    dto.getLC()
            );
        } else {
            throw new IllegalStateException(
                    "ALS должен содержать модуль управления"
            );
        }

        List<Module> modules = insertControlModule(
                storageModules,
                controlModule,
                dto.getPositionControlModule()
        );

        return new ALS(
                dto.getHeight(),
                dto.getDepth(),
                dto.getUpperFrame(),
                dto.getBottomFrame(),
                colorBody,
                colorDoor,
                modules
        );
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

        PositionControlModule position = PositionControlModule.valueOf(
                position
        );

        int index;

        switch (position) {
            case LEFT -> index = 0;

            case RIGHT -> index = modules.size();

            case CENTER -> index = modules.size() / 2;

            default -> throw new IllegalStateException(
                    "Неизвестное положение модуля управления: " + position
            );
        }

        modules.add(
                index,
                controlModule
        );

        return modules;
    }

    private static int calculateDepthCell(
            ALS domain
    ) {
        return domain.getStorageModules()
                .stream()
                .mapToInt(storageModule ->
                        storageModule.getCellDepth()
                )
                .min()
                .orElse(0);
    }
}