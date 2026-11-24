package com.lb_calc_web.entity;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/** JPA-сущность модуля управления LC. */
@Entity
@Table(name = "lc")
public class LCEntity extends ModuleEntity {

    @BatchSize(size = 50)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lc_equipment",
            joinColumns = @JoinColumn(name = "lc_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lc_equipment",
                    columnNames = {"lc_id", "equipment_id"}
            )
    )
    private Set<EquipmentQuantityEmbeddable> equipment = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "display_purpose", nullable = false, length = 50)
    private DisplayPurpose displayPurpose = DisplayPurpose.NONE;

    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(
            name = "lc_access_purpose",
            joinColumns = @JoinColumn(name = "lc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "access_purpose", nullable = false, length = 50)
    private Set<AccessPurpose> accessPurposes = EnumSet.noneOf(AccessPurpose.class);

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_purpose", nullable = false, length = 50)
    private PaymentPurpose paymentPurpose = PaymentPurpose.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "printer_purpose", nullable = false, length = 50)
    private PrinterPurpose printerPurpose = PrinterPurpose.ABSENT;

    protected LCEntity() {
    }

    public Set<EquipmentQuantityEmbeddable> getEquipment() {
        return Set.copyOf(equipment);
    }

    public void setEquipment(Set<EquipmentQuantityEmbeddable> equipment) {
        Objects.requireNonNull(equipment, "Состав оборудования LC не должен быть null");
        Set<Long> equipmentIds = new HashSet<>();
        for (EquipmentQuantityEmbeddable item : equipment) {
            Objects.requireNonNull(item, "Позиция оборудования LC не должна быть null");
            Long id = Objects.requireNonNull(
                    item.getEquipment().getId(),
                    "Физическая модель оборудования должна быть сохранена до привязки к LC");
            if (!equipmentIds.add(id)) {
                throw new IllegalArgumentException(
                        "Модель оборудования LC не может быть указана несколько раз: " + id);
            }
        }
        this.equipment.clear();
        this.equipment.addAll(equipment);
    }

    public FunctionalConfiguration getFunctionalConfiguration() {
        return new FunctionalConfiguration(
                displayPurpose, accessPurposes, paymentPurpose, printerPurpose);
    }

    public void setFunctionalConfiguration(FunctionalConfiguration configuration) {
        Objects.requireNonNull(configuration, "Функциональная конфигурация LC не должна быть null");
        displayPurpose = configuration.getDisplay();
        accessPurposes.clear();
        accessPurposes.addAll(configuration.getAccess());
        paymentPurpose = configuration.getPayment();
        printerPurpose = configuration.getPrinter();
    }

    @Override
    public String toString() {
        return "LCEntity{" +
                "id=" + getId() +
                ", name='" + getName() + ''' +
                ", functionalConfiguration=" + getFunctionalConfiguration() +
                ", equipment=" + equipment +
                '}';
    }
}
