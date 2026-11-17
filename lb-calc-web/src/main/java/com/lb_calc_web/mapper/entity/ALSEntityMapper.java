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

/**
 * Преобразует агрегат ALS между доменной и persistence-моделями.
 *
 * <p>Порядок модулей сохраняется отдельными записями связи
 * ALSModuleEntity и не зависит от идентификаторов модулей.</p>
 */
public final class ALSEntityMapper {

    private ALSEntityMapper() {
    }

    /**
     * Преобразует JPA-сущность ALS в доменный агрегат.
     *
     * @param entity JPA-сущность ALS
     * @return доменный агрегат
     */
    public static ALS toDomain(ALSEntity entity) {
        Objects.requireNonNull(
                entity,
                "ALSEntity не должен быть null"
        );

        List<Module> modules = entity.getModules().stream()
                .sorted(Comparator.comparingInt(ALSModuleEntity::getModuleOrder))
                .map(ALSModuleEntity::getModule)
                .map(ModuleEntityMapper::toDomain)
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

    /**
     * Создаёт новую JPA-сущность ALS.
     *
     * @param domain доменный агрегат
     * @param moduleResolver разрешает доменный модуль в сохранённую Entity
     * @return новая JPA-сущность
     */
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

    /**
     * Переносит состояние доменного агрегата в существующую Entity.
     *
     * <p>Persistence ID ALS и разрешённых модулей не изменяются.
     * Связи модулей создаются в физическом порядке слева направо.</p>
     *
     * @param domain доменный агрегат
     * @param entity существующая JPA-сущность
     * @param moduleResolver разрешает доменный модуль в сохранённую Entity
     */
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
            ModuleEntity moduleEntity = Objects.requireNonNull(
                    moduleResolver.apply(modules.get(i)),
                    "moduleResolver вернул null"
            );

            ALSModuleEntity association = new ALSModuleEntity();
            association.setAls(entity);
            association.setModule(moduleEntity);
            association.setModuleOrder(i);
            entity.getModules().add(association);
        }
    }

    /**
     * Преобразует сущность конкретного модуля в доменную модель.
     *
     * @param entity JPA-сущность модуля
     * @return доменный модуль
     * @deprecated Используйте {@link ModuleEntityMapper#toDomain(ModuleEntity)}.
     */
    @Deprecated
    public static Module toDomainModule(ModuleEntity entity) {
        return ModuleEntityMapper.toDomain(entity);
    }

    /**
     * Создаёт новую сущность конкретного типа модуля.
     *
     * @param module доменный модуль
     * @return новая JPA-сущность
     * @deprecated Используйте {@link ModuleEntityMapper#toEntity(Module)}.
     */
    @Deprecated
    public static ModuleEntity createModuleEntity(Module module) {
        return ModuleEntityMapper.toEntity(module);
    }
}
