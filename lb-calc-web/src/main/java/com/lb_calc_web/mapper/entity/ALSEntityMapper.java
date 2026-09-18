package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.ALSModuleEntity;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.entity.ModuleEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class ALSEntityMapper {

    private ALSEntityMapper() {
    }

    public static ALS toDomain(ALSEntity entity) {
        Objects.requireNonNull(
                entity,
                "ALSEntity не должен быть null"
        );

        List<Module> modules = entity.getModules().stream()
                .sorted(
                        Comparator.comparingInt(
                                ALSModuleEntity::getModuleOrder
                        )
                )
                .map(ALSModuleEntity::getModule)
                .map(ALSEntityMapper::toDomainModule)
                .toList();

        return new ALS(
                entity.getHeight(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                entity.getColorBody(),
                entity.getColorDoor(),
                modules
        );
    }

    public static ALSEntity toEntity(
            ALS domain,
            Function<Module, ModuleEntity> moduleResolver
    ) {
        Objects.requireNonNull(
                domain,
                "ALS не должен быть null"
        );

        Objects.requireNonNull(
                moduleResolver,
                "moduleResolver не должен быть null"
        );

        ALSEntity entity = new ALSEntity();

        updateEntity(
                domain,
                entity,
                moduleResolver
        );

        return entity;
    }

    public static void updateEntity(
            ALS domain,
            ALSEntity entity,
            Function<Module, ModuleEntity> moduleResolver
    ) {
        Objects.requireNonNull(
                domain,
                "ALS не должен быть null"
        );

        Objects.requireNonNull(
                entity,
                "ALSEntity не должен быть null"
        );

        Objects.requireNonNull(
                moduleResolver,
                "moduleResolver не должен быть null"
        );

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        entity.setHeight(domain.getHeight());
        entity.setWidth(domain.getWidth());
        entity.setDepth(domain.getDepth());

        entity.setUpperFrame(domain.getUpperFrame());
        entity.setBottomFrame(domain.getBottomFrame());

        entity.setColorBody(domain.getColorBody());
        entity.setColorDoor(domain.getColorDoor());

        entity.getModules().clear();

        List<Module> modules = domain.getModules();

        for (int i = 0; i < modules.size(); i++) {

            Module domainModule = modules.get(i);

            ModuleEntity moduleEntity =
                    Objects.requireNonNull(
                            moduleResolver.apply(domainModule),
                            "moduleResolver вернул null"
                    );

            ALSModuleEntity association =
                    new ALSModuleEntity();

            association.setAls(entity);
            association.setModule(moduleEntity);
            association.setModuleOrder(i);

            entity.getModules().add(association);
        }
    }

    public static Module toDomainModule(
            ModuleEntity entity
    ) {
        if (entity instanceof LBCEntity lbcEntity) {
            return LBCEntityMapper.toDomain(lbcEntity);
        }

        if (entity instanceof LBEntity lbEntity) {
            return LBEntityMapper.toDomain(lbEntity);
        }

        if (entity instanceof LCEntity lcEntity) {
            return LCEntityMapper.toDomain(lcEntity);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип ModuleEntity: "
                        + entity.getClass().getName()
        );
    }

    private static ModuleEntity toNewModuleEntity(
            Module module
    ) {
        if (module instanceof LBC lbc) {
            return LBCEntityMapper.toEntity(lbc);
        }

        if (module instanceof LB lb) {
            return LBEntityMapper.toEntity(lb);
        }

        if (module instanceof LC lc) {
            return LCEntityMapper.toEntity(lc);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип Module: "
                        + module.getClass().getName()
        );
    }

    public static ModuleEntity createModuleEntity(
            Module module
    ) {
        return toNewModuleEntity(module);
    }
}