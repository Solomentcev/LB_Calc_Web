package com.lb_calc_web.entity;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/** JPA-сущность комбинированного модуля LBC. */
@Entity
@Table(name = "lbc")
public class LBCEntity extends StorageModuleEntity {

    @BatchSize(size = 50)
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lbc_equipment",
            joinColumns = @JoinColumn(name = "lbc_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lbc_equipment",
                    columnNames = {"lbc_id", "equipment_id"}
            )
    )
    private Set<EquipmentQuantityEmbeddable> equipment = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "display_purpose", nullable = false, length = 50)
    private DisplayPurpose displayPurpose = DisplayPurpose.NONE;

    @BatchSize(size = 50)
    @ElementCollection
    @CollectionTable(
            name = "lbc_access_purpose",
            joinColumns = @JoinColumn(name = "lbc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "access_purpose", nullable = false, length = 50)
    private Set<AccessPurpose> accessPurposes = EnumSet.noneOf(AccessPurpose.class);

    /** Стабильное строковое представление набора Access для сортировки каталога. */
    @org.hibernate.annotations.Formula(
            "(select group_concat(ap.access_purpose order by ap.access_purpose separator ', ') "
                    + "from lbc_access_purpose ap where ap.lbc_id = {alias}.id)"
    )
    private String accessSort;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_purpose", nullable = false, length = 50)
    private PaymentPurpose paymentPurpose = PaymentPurpose.NONE;

    @Enumerated(EnumType.STRING)
    @Column(name = "printer_purpose", nullable = false, length = 50)
    private PrinterPurpose printerPurpose = PrinterPurpose.ABSENT;

    protected LBCEntity() {
    }

    /** Создаёт новую JPA-сущность LBC для заполнения маппером. */
    public static LBCEntity create() {
        return new LBCEntity();
    }

    public Set<EquipmentQuantityEmbeddable> getEquipment() {
        return Set.copyOf(equipment);
    }

    public void setEquipment(Set<EquipmentQuantityEmbeddable> equipment) {
        Objects.requireNonNull(equipment, "Состав оборудования LBC не должен быть null");
        Set<Long> equipmentIds = new HashSet<>();
        for (EquipmentQuantityEmbeddable item : equipment) {
            Objects.requireNonNull(item, "Позиция оборудования LBC не должна быть null");
            Long id = Objects.requireNonNull(
                    item.getEquipment().getId(),
                    "Физическая модель оборудования должна быть сохранена до привязки к LBC");
            if (!equipmentIds.add(id)) {
                throw new IllegalArgumentException(
                        "Модель оборудования LBC не может быть указана несколько раз: " + id);
            }
        }
        this.equipment.clear();
        this.equipment.addAll(equipment);
    }

    public String getAccessSort() {
        return accessSort;
    }

    public DisplayPurpose getDisplayPurpose() {
        return displayPurpose;
    }

    public void setDisplayPurpose(DisplayPurpose displayPurpose) {
        this.displayPurpose = Objects.requireNonNull(displayPurpose,
                "Назначение дисплея не должно быть null");
    }

    public Set<AccessPurpose> getAccessPurposes() {
        return Set.copyOf(accessPurposes);
    }

    public void setAccessPurposes(Set<AccessPurpose> accessPurposes) {
        Objects.requireNonNull(accessPurposes, "Назначения доступа не должны быть null");
        this.accessPurposes.clear();
        this.accessPurposes.addAll(accessPurposes);
    }

    public PaymentPurpose getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(PaymentPurpose paymentPurpose) {
        this.paymentPurpose = Objects.requireNonNull(paymentPurpose,
                "Назначение оплаты не должно быть null");
    }

    public PrinterPurpose getPrinterPurpose() {
        return printerPurpose;
    }

    public void setPrinterPurpose(PrinterPurpose printerPurpose) {
        this.printerPurpose = Objects.requireNonNull(printerPurpose,
                "Назначение принтера не должно быть null");
    }

    @Override
    public String toString() {
        return "LBCEntity{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", displayPurpose=" + displayPurpose +
                ", accessPurposes=" + accessPurposes +
                ", paymentPurpose=" + paymentPurpose +
                ", printerPurpose=" + printerPurpose +
                ", equipment=" + equipment +
                '}';
    }
}
