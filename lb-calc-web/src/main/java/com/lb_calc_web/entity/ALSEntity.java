package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Objects;

/**
 * JPA-сущность автоматизированной системы хранения ALS.
 *
 * <p>ALS содержит модули в физическом порядке слева направо.
 * Положение каждого модуля хранится в {@link ALSModuleEntity},
 * поскольку один и тот же модуль может использоваться
 * в разных ALS с разным расположением.</p>
 */
@Entity
@org.hibernate.annotations.BatchSize(size = 50)
@Table(name = "als")
public class ALSEntity {

    /**
     * Идентификатор ALS.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование ALS.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Описание ALS.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /**
     * Высота ALS, мм.
     */
    @Column(nullable = false)
    private int height;

    /**
     * Общая ширина ALS, мм.
     *
     * <p>Рассчитывается доменной моделью
     * как сумма ширин модулей.</p>
     */
    @Column(nullable = false)
    private int width;

    /**
     * Глубина ALS, мм.
     */
    @Column(nullable = false)
    private int depth;

    /**
     * Размер верхней рамы, мм.
     */
    @Column(nullable = false)
    private int upperFrame;

    /**
     * Размер нижней рамы, мм.
     */
    @Column(nullable = false)
    private int bottomFrame;

    /**
     * Цвет корпуса.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Colors colorBody;

    /**
     * Цвет дверей.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Colors colorDoor;

    /** Функциональное назначение дисплея ALS. */
    @Enumerated(EnumType.STRING)
    @Column(name = "display_purpose", nullable = false, length = 50)
    private DisplayPurpose displayPurpose = DisplayPurpose.NONE;

    /** Функциональные назначения доступа ALS. */
    @org.hibernate.annotations.BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(
            name = "als_access_purpose",
            joinColumns = @JoinColumn(name = "als_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "access_purpose", nullable = false, length = 50)
    private Set<AccessPurpose> accessPurposes = EnumSet.noneOf(AccessPurpose.class);

    /** Стабильное строковое представление набора Access для сортировки каталога. */
    @org.hibernate.annotations.Formula(
            "(select group_concat(ap.access_purpose order by ap.access_purpose separator ', ') "
                    + "from als_access_purpose ap where ap.als_id = {alias}.id)"
    )
    private String accessSort;

    /** Общее количество ячеек во всех модулях хранения ALS. */
    @org.hibernate.annotations.Formula(
            "((select coalesce(sum(lb_module.count_cells), 0) "
                    + "from als_module am join lb lb_module on lb_module.id = am.module_id "
                    + "where am.als_id = {alias}.id) "
                    + "+ (select coalesce(sum(lbc_module.count_cells), 0) "
                    + "from als_module am join lbc lbc_module on lbc_module.id = am.module_id "
                    + "where am.als_id = {alias}.id))"
    )
    private int countCells;

    /** Положение управляющего модуля, вычисляемое по физическому порядку модулей ALS. */
    @org.hibernate.annotations.Formula(
            "(case "
                    + "when (select count(*) from als_module am where am.als_id = {alias}.id) = 1 "
                    + "then 'CENTER' "
                    + "when (select min(am.module_order) from als_module am "
                    + "where am.als_id = {alias}.id "
                    + "and am.module_id in (select lc_module.id from lc lc_module union select lbc_module.id from lbc lbc_module)) = 0 "
                    + "then 'LEFT' "
                    + "when (select min(am.module_order) from als_module am "
                    + "where am.als_id = {alias}.id "
                    + "and am.module_id in (select id from lc union select id from lbc)) = "
                    + "(select count(*) - 1 from als_module am where am.als_id = {alias}.id) "
                    + "then 'RIGHT' else 'CENTER' end)"
    )
    private String positionControlModule;

    /** Функциональное назначение оплаты ALS. */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_purpose", nullable = false, length = 50)
    private PaymentPurpose paymentPurpose = PaymentPurpose.NONE;

    /** Функциональное назначение принтера ALS. */
    @Enumerated(EnumType.STRING)
    @Column(name = "printer_purpose", nullable = false, length = 50)
    private PrinterPurpose printerPurpose = PrinterPurpose.ABSENT;

    /**
     * Модули, входящие в ALS.
     *
     * <p>Список отсортирован по физическому расположению
     * модулей слева направо.</p>
     */
    @org.hibernate.annotations.BatchSize(size = 50)
    @OneToMany(
            mappedBy = "als",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("moduleOrder ASC")
    private List<ALSModuleEntity> modules = new ArrayList<>();

    /**
     * Конструктор для JPA.
     */
    public ALSEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(
                name,
                "Название ALS не должно быть null"
        );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNull(
                description,
                "Описание ALS не должно быть null"
        );
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getUpperFrame() {
        return upperFrame;
    }

    public void setUpperFrame(int upperFrame) {
        this.upperFrame = upperFrame;
    }

    public int getBottomFrame() {
        return bottomFrame;
    }

    public void setBottomFrame(int bottomFrame) {
        this.bottomFrame = bottomFrame;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    public void setColorBody(Colors colorBody) {
        this.colorBody = Objects.requireNonNull(
                colorBody,
                "Цвет корпуса не должен быть null"
        );
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(Colors colorDoor) {
        this.colorDoor = Objects.requireNonNull(
                colorDoor,
                "Цвет дверей не должен быть null"
        );
    }

    /** Возвращает независимую функциональную конфигурацию ALS. */
    public String getAccessSort() {
        return accessSort;
    }

    public String getPositionControlModule() {
        return positionControlModule;
    }

    public int getCountCells() {
        return countCells;
    }

    public FunctionalConfiguration getFunctionalConfiguration() {
        return new FunctionalConfiguration(
                displayPurpose, accessPurposes, paymentPurpose, printerPurpose
        );
    }

    /** Обновляет функциональную конфигурацию ALS. */
    public void setFunctionalConfiguration(FunctionalConfiguration configuration) {
        Objects.requireNonNull(
                configuration,
                "Функциональная конфигурация ALS не должна быть null"
        );
        displayPurpose = configuration.getDisplay();
        accessPurposes.clear();
        accessPurposes.addAll(configuration.getAccess());
        paymentPurpose = configuration.getPayment();
        printerPurpose = configuration.getPrinter();
    }

    public List<ALSModuleEntity> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * Добавляет модуль в указанную физическую позицию ALS.
     *
     * @param module модуль
     * @param moduleOrder позиция слева направо, начиная с нуля
     */
    public void addModule(ModuleEntity module, int moduleOrder) {
        if (moduleOrder != modules.size()) {
            throw new IllegalArgumentException(
                    "Порядок добавляемого модуля должен соответствовать его позиции в ALS"
            );
        }

        ALSModuleEntity association = new ALSModuleEntity();
        association.setAls(this);
        association.setModule(Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        ));
        association.setModuleOrder(moduleOrder);
        modules.add(association);
    }

    /**
     * Синхронизирует связи с модулями, сохраняя неизменившиеся позиции.
     *
     * <p>Связи, у которых изменился модуль или его физическая позиция,
     * заменяются. Это исключает промежуточные конфликты уникального
     * ограничения {@code (als_id, module_order)} при перестановке.</p>
     *
     * @param desiredModules модули в физическом порядке слева направо
     */
    public void synchronizeModules(List<ModuleEntity> desiredModules) {
        Objects.requireNonNull(
                desiredModules,
                "Список модулей не должен быть null"
        );

        /*
         * Сначала переносим существующие связи на временные уникальные позиции.
         * Это исключает конфликт ограничения (als_id, module_order) при перестановке.
         */
        int temporaryOrder = modules.size() + desiredModules.size() + 1;
        for (int index = 0; index < modules.size(); index++) {
            modules.get(index).setModuleOrder(temporaryOrder + index);
        }

        List<ALSModuleEntity> unmatched = new ArrayList<>(modules);
        List<ALSModuleEntity> synchronizedModules = new ArrayList<>();

        for (ModuleEntity desiredModule : desiredModules) {
            ModuleEntity module = Objects.requireNonNull(
                    desiredModule,
                    "Модуль не должен быть null"
            );

            ALSModuleEntity association = unmatched.stream()
                    .filter(candidate -> {
                        ModuleEntity current = candidate.getModule();
                        return current == module
                                || (current.getId() != null
                                && current.getId().equals(module.getId()));
                    })
                    .findFirst()
                    .orElse(null);

            if (association != null) {
                unmatched.remove(association);
                association.setModule(module);
            } else {
                association = new ALSModuleEntity();
                association.setAls(this);
                association.setModule(module);
                modules.add(association);
            }
            synchronizedModules.add(association);
        }

        // Удаляем только связи с модулями, которых больше нет в ALS.
        modules.removeAll(unmatched);

        // Все оставшиеся связи уже находятся на временных позициях.
        // Теперь можно безопасно назначить итоговый физический порядок.
        for (int order = 0; order < synchronizedModules.size(); order++) {
            synchronizedModules.get(order).setModuleOrder(order);
        }

        modules.sort(Comparator.comparingInt(ALSModuleEntity::getModuleOrder));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ALSEntity that = (ALSEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ALSEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", colorBody=" + colorBody +
                ", colorDoor=" + colorDoor +
                '}';
    }
}