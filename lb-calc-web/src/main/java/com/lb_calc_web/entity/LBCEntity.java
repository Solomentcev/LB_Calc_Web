package com.lb_calc_web.entity;

import com.lb_calc_web.domain.attributes.AccessMethod;
import com.lb_calc_web.domain.attributes.Payment;
import com.lb_calc_web.domain.attributes.PrintOption;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.util.EnumSet;
import java.util.Set;

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
     * Дисплей.
     */
    @Column(name = "display")
    private String display;

    /**
     * Сканер штрихкода.
     */
    @Column(name = "bar_reader")
    private String barReader;

    /**
     * Способ оплаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment")
    private Payment payment;

    /**
     * Наличие принтера.
     */
    @Column(name = "printer", nullable = false)
    private boolean printer;

    /**
     * Наличие RFID-считывателя.
     */
    @Column(name = "rfid_reader", nullable = false)
    private boolean rfidReader;

    /**
     * Допустимые способы доступа.
     */
    @ElementCollection
    @CollectionTable(
            name = "lbc_access_method",
            joinColumns = @JoinColumn(name = "lbc_id")
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
            name = "lbc_print_option",
            joinColumns = @JoinColumn(name = "lbc_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "print_option")
    private Set<PrintOption> printOptions =
            EnumSet.noneOf(PrintOption.class);

    /**
     * Конструктор для JPA.
     */
    public LBCEntity() {
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getBarReader() {
        return barReader;
    }

    public void setBarReader(String barReader) {
        this.barReader = barReader;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public boolean isPrinter() {
        return printer;
    }

    public void setPrinter(boolean printer) {
        this.printer = printer;
    }

    public boolean isRfidReader() {
        return rfidReader;
    }

    public void setRfidReader(boolean rfidReader) {
        this.rfidReader = rfidReader;
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
                ", display='" + display + '\'' +
                ", barReader='" + barReader + '\'' +
                ", payment=" + payment +
                ", printer=" + printer +
                ", rfidReader=" + rfidReader +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}