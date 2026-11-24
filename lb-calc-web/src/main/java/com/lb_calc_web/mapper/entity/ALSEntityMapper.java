package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.Module;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.entity.ALSModuleEntity;
import com.lb_calc_web.entity.ModuleEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Преобразует агрегат ALS между доменной и persistence-моделями. */
public final class ALSEntityMapper {

    private ALSEntityMapper() {
    }

    public static ALS toDomain(ALSEntity entity) {
        Objects.requireNonNull(entity, "ALSEntity не должен быть null");

        List<Module> modules = entity.getModules().stream()
                .sorted(Comparator.comparingInt(ALSModuleEntity::getModuleOrder))
                .map(ALSModuleEntity::getModule)
                .map(ModuleEntityMapper::toDomain)
                .toList();

        ALS domain = new ALS(
                entity.getHeight(),
                entity.getDepth(),
                entity.getUpperFrame(),
                entity.getBottomFrame(),
                entity.getColorBody(),
                entity.getColorDoor(),
                modules
        );

        if (!domain.getName().equals(entity.getName())
                || !domain.getDescription().equals(entity.getDescription())
                || domain.getWidth() != entity.getWidth()) {
            throw new IllegalStateException(
                    "Производные характеристики ALS в БД не соответствуют составу модулей"
            );
        }

        domain.setId(entity.getId());
        return domain;
    }

    public static ALSEntity toEntity(
            ALS domain,
            Function<Module, ModuleEntity> moduleResolver
    ) {
        Objects.requireNonNull(domain, "ALS не должен быть null");
        Objects.requireNonNull(moduleResolver, "moduleResolver не должен быть null");

        ALSEntity entity = new ALSEntity();
        updateEntity(domain, entity, moduleResolver);
        return entity;
    }

    public static void updateEntity(
            ALS domain,
            ALSEntity entity,
            Function<Module, ModuleEntity> moduleResolver
    ) {
        Objects.requireNonNull(domain, "ALS не должен быть null");
        Objects.requireNonNull(entity, "ALSEntity не должен быть null");
        Objects.requireNonNull(moduleResolver, "moduleResolver не должен быть null");

        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setWidth(domain.getWidth());
        entity.setHeight(domain.getHeight());
        entity.setDepth(domain.getDepth());
        entity.setUpperFrame(domain.getUpperFrame());
        entity.setBottomFrame(domain.getBottomFrame());
        entity.setColorBody(domain.getColorBody());
        entity.setColorDoor(domain.getColorDoor());

        List<ModuleEntity> moduleEntities = domain.getModules().stream()
                .map(module -> Objects.requireNonNull(
                        moduleResolver.apply(module),
                        "moduleResolver вернул null"
                ))
                .toList();

        entity.synchronizeModules(moduleEntities);
    }
}
