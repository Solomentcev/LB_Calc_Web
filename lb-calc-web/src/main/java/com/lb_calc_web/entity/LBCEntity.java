package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.BatchSize;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * JPA-сущность комбинированного модуля LBC.
 *
 * <p>LBC является одновременно модулем хранения и модулем управления,
 * поэтому наследует storage-параметры от {@link StorageModuleEntity}
 * и содержит дополнительные параметры управления.</p>
 */
@Entity
@Table(name = "lbc")
public class LBCEntity extends StorageModuleEntity {

    /**
     * Физический состав оборудования модуля с количеством каждой позиции.
     */
    @BatchSize(size = 50)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lbc_equipment",
            joinColumns = @JoinColumn(name = "lbc_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lbc_equipment",
                    columnNames = {"lbc_id", "catalog_position_id"}
            )
    )
    private Set<EquipmentQuantityEmbeddable> equipment = new HashSet<>();

    /**
     * Способ оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment", nullable = false, length = 50)
    private Payment payment;



    /**
     * Допустимые способы доступа.
     */
    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(
            name = "lbc_access_method",
            joinColumns = @JoinColumn(name = "lbc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "access_method", nullable = false, length = 50)
    private Set<AccessMethod> accessMethods =
            EnumSet.noneOf(AccessMethod.class);

    /**
     * Варианты печати.
     */
    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(
            name = "lbc_print_option",
            joinColumns = @JoinColumn(name = "lbc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "print_option", nullable = false, length = 50)
    private Set<PrintOption> printOptions =
            EnumSet.noneOf(PrintOption.class);

    /**
     * Конструктор для JPA.
     */
    public LBCEntity() {
    }

    public Set<EquipmentQuantityEmbeddable> getEquipment() {
        return Set.copyOf(equipment);
    }

    public void setEquipment(Set<EquipmentQuantityEmbeddable> equipment) {
        Objects.requireNonNull(
                equipment,
                "Состав оборудования LBC не должен быть null"
        );

        Set<String> catalogPositionIds = new HashSet<>();
        for (EquipmentQuantityEmbeddable item : equipment) {
            Objects.requireNonNull(
                    item,
                    "Позиция оборудования LBC не должна быть null"
            );
            if (!catalogPositionIds.add(item.getCatalogPositionId())) {
                throw new IllegalArgumentException(
                        "Позиция оборудования LBC не может быть указана несколько раз: "
                                + item.getCatalogPositionId()
                );
            }
        }

        this.equipment.clear();
        this.equipment.addAll(equipment);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = Objects.requireNonNull(
                payment,
                "Способ оплаты не должен быть null"
        );
    }



    public Set<AccessMethod> getAccessMethods() {
        return Set.copyOf(accessMethods);
    }

    public void setAccessMethods(Set<AccessMethod> accessMethods) {
        this.accessMethods.clear();

        if (accessMethods != null) {
            this.accessMethods.addAll(accessMethods);
        }
    }

    public Set<PrintOption> getPrintOptions() {
        return Set.copyOf(printOptions);
    }

    public void setPrintOptions(Set<PrintOption> printOptions) {
        this.printOptions.clear();

        if (printOptions != null) {
            this.printOptions.addAll(printOptions);
        }
    }

    @Override
    public String toString() {
        return "LBCEntity{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", height=" + getHeight() +
                ", width=" + getWidth() +
                ", depth=" + getDepth() +
                ", upperFrame=" + getUpperFrame() +
                ", bottomFrame=" + getBottomFrame() +
                ", colorBody=" + getColorBody() +
                ", colorDoor=" + getColorDoor() +
                ", type='" + getType() + '\'' +
                ", deltaWidth=" + getDeltaWidth() +
                ", shelfThick=" + getShelfThick() +
                ", serviceZoneWidth=" + getServiceZoneWidth() +
                ", doorThickness=" + getDoorThickness() +
                ", directionDoorOpening=" + getDirectionDoorOpening() +
                ", countCells=" + getCountCells() +
                ", heightCell=" + getHeightCell() +
                ", widthCell=" + getWidthCell() +
                ", depthCell=" + getDepthCell() +
                ", payment=" + payment +
                '}';
    }
}