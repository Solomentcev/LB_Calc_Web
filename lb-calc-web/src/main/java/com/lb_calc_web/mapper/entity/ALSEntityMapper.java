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

/**
 * Маппер между доменной моделью ALS и JPA-сущностями.
 *
 * <p>Отвечает за:
 * <ul>
 *     <li>преобразование ALS -> ALSEntity;</li>
 *     <li>преобразование ALSEntity -> ALS;</li>
 *     <li>восстановление физического порядка модулей;</li>
 *     <li>создание ALSModuleEntity для связи ALS с модулями;</li>
 *     <li>полиморфное преобразование LC / LB / LBC.</li>
 * </ul>
 */
public class ALSEntityMapper {

    private final LCEntityMapper lcMapper;
    private final LBEntityMapper lbMapper;
    private final LBCEntityMapper lbcMapper;

    public ALSEntityMapper(
            LCEntityMapper lcMapper,
            LBEntityMapper lbMapper,
            LBCEntityMapper lbcMapper
    ) {
        this.lcMapper = Objects.requireNonNull(lcMapper);
        this.lbMapper = Objects.requireNonNull(lbMapper);
        this.lbcMapper = Objects.requireNonNull(lbcMapper);
    }

    /**
     * Преобразует JPA-сущность ALS в доменную модель.
     *
     * <p>Порядок модулей определяется moduleOrder в ALSModuleEntity.</p>
     */
    public ALS toDomain(ALSEntity entity) {
        Objects.requireNonNull(
                entity,
                "ALSEntity не должен быть null"
        );

        List<Module> modules = entity.getModules().stream()
                .sorted(Comparator.comparingInt(ALSModuleEntity::getModuleOrder))
                .map(ALSModuleEntity::getModule)
                .map(this::toDomainModule)
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
     * <p>Для каждого доменного модуля создаётся новая ModuleEntity.
     * Этот метод подходит для построения нового графа сущностей.</p>
     */
    public ALSEntity toEntity(ALS domain) {
        Objects.requireNonNull(
                domain,
                "ALS не должен быть null"
        );

        return toEntity(
                domain,
                this::toNewModuleEntity
        );
    }

    /**
     * Создаёт новую JPA-сущность ALS с использованием переданного
     * резолвера модулей.
     *
     * <p>Резолвер нужен в случае, когда модуль уже существует в БД
     * и его необходимо повторно связать с ALS, а не создавать новый.</p>
     *
     * <p>Именно сервисный слой должен решать, какой ModuleEntity
     * соответствует конкретному доменному Module.</p>
     */
    public ALSEntity toEntity(
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

    /**
     * Обновляет существующую сущность ALS.
     */
    public void updateEntity(
            ALS domain,
            ALSEntity entity
    ) {
        Objects.requireNonNull(
                domain,
                "ALS не должен быть null"
        );
        Objects.requireNonNull(
                entity,
                "ALSEntity не должен быть null"
        );

        updateEntity(
                domain,
                entity,
                this::toNewModuleEntity
        );
    }

    /**
     * Обновляет существующую сущность ALS.
     *
     * <p>Переданный moduleResolver определяет, какой persistence-модуль
     * должен использоваться для каждого доменного модуля.</p>
     */
    public void updateEntity(
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

        /*
         * Порядок модулей в Domain ALS является источником истины.
         * Поэтому moduleOrder назначается заново начиная с 0.
         */
        entity.getModules().clear();

        List<Module> domainModules = domain.getModules();

        for (int i = 0; i < domainModules.size(); i++) {
            Module domainModule = domainModules.get(i);

            ModuleEntity moduleEntity = Objects.requireNonNull(
                    moduleResolver.apply(domainModule),
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
     * Преобразует конкретный ModuleEntity в соответствующий
     * доменный объект.
     */
    private Module toDomainModule(ModuleEntity entity) {
        if (entity instanceof LBCEntity lbcEntity) {
            return lbcMapper.toDomain(lbcEntity);
        }

        if (entity instanceof LBEntity lbEntity) {
            return lbMapper.toDomain(lbEntity);
        }

        if (entity instanceof LCEntity lcEntity) {
            return lcMapper.toDomain(lcEntity);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип ModuleEntity: "
                        + entity.getClass().getName()
        );
    }

    /**
     * Создаёт новую persistence-сущность из доменного модуля.
     */
    private ModuleEntity toNewModuleEntity(Module module) {
        if (module instanceof LBC lbc) {
            return lbcMapper.toEntity(lbc);
        }

        if (module instanceof LB lb) {
            return lbMapper.toEntity(lb);
        }

        if (module instanceof LC lc) {
            return lcMapper.toEntity(lc);
        }

        throw new IllegalArgumentException(
                "Неподдерживаемый тип Module: "
                        + module.getClass().getName()
        );
    }
}
