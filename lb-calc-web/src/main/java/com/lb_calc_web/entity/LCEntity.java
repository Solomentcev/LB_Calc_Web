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

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * JPA-сущность модуля управления LC.
 *
 * <p>Общие параметры модуля наследуются от {@link ModuleEntity}.</p>
 */
@Entity
@Table(name = "lc")
public class LCEntity extends ModuleEntity {

    /**
     * Физический состав оборудования модуля с количеством каждой позиции.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lc_equipment",
            joinColumns = @JoinColumn(name = "lc_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lc_equipment",
                    columnNames = {"lc_id", "catalog_position_id"}
            )
    )
    private Set<EquipmentQuantityEmbeddable> equipment = new HashSet<>();

    /**
     * Способ оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment")
    private Payment payment;



    /**
     * Допустимые способы доступа.
     */
    @ElementCollection
    @CollectionTable(
            name = "lc_access_method",
            joinColumns = @JoinColumn(name = "lc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "access_method")
    private Set<AccessMethod> accessMethods =
            EnumSet.noneOf(AccessMethod.class);

    /**
     * Варианты печати.
     */
    @ElementCollection
    @CollectionTable(
            name = "lc_print_option",
            joinColumns = @JoinColumn(name = "lc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "print_option")
    private Set<PrintOption> printOptions =
            EnumSet.noneOf(PrintOption.class);

    /**
     * Конструктор для JPA.
     */
    public LCEntity() {
    }

    public Set<EquipmentQuantityEmbeddable> getEquipment() {
        return Set.copyOf(equipment);
    }

    public void setEquipment(Set<EquipmentQuantityEmbeddable> equipment) {
        Objects.requireNonNull(
                equipment,
                "Состав оборудования LC не должен быть null"
        );

        Set<String> catalogPositionIds = new HashSet<>();
        for (EquipmentQuantityEmbeddable item : equipment) {
            Objects.requireNonNull(
                    item,
                    "Позиция оборудования LC не должна быть null"
            );
            if (!catalogPositionIds.add(item.getCatalogPositionId())) {
                throw new IllegalArgumentException(
                        "Позиция оборудования LC не может быть указана несколько раз: "
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
        this.payment = payment;
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
        return "LCEntity{" +
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
                ", equipment=" + equipment +
                ", payment=" + payment +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}