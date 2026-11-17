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

/**
 * JPA-сущность модуля управления LC.
 *
 * <p>Общие параметры модуля наследуются от {@link ModuleEntity}.</p>
 */
@Entity
@Table(name = "lc")
public class LCEntity extends ModuleEntity {

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

    @Column(name = "display_quantity", nullable = false)
    private int displayQuantity;

    @Column(name = "bar_reader_quantity", nullable = false)
    private int barReaderQuantity;

    /**
     * Полный набор установленных сканеров штрихкода с количеством каждой позиции.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "lc_bar_reader",
            joinColumns = @JoinColumn(name = "lc_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_lc_bar_reader",
                    columnNames = {"lc_id", "catalog_position_id"}
            )
    )
    private Set<EquipmentQuantityEmbeddable> barReaders = new HashSet<>();

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

    @Column(name = "printer_quantity", nullable = false)
    private int printerQuantity;

    @Column(name = "rfid_reader_quantity", nullable = false)
    private int rfidReaderQuantity;

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

    public int getDisplayQuantity() { return displayQuantity; }

    public void setDisplayQuantity(int quantity) {
        if (quantity > 1) {
            throw new IllegalArgumentException(
                    "Количество дисплеев в модуле должно быть равно 1"
            );
        }
        this.displayQuantity = requireNonNegativeQuantity(quantity);
    }

    public int getBarReaderQuantity() { return barReaderQuantity; }

    public Set<EquipmentQuantityEmbeddable> getBarReaders() {
        return Set.copyOf(barReaders);
    }

    public void setBarReaders(Set<EquipmentQuantityEmbeddable> barReaders) {
        this.barReaders.clear();
        if (barReaders != null) {
            this.barReaders.addAll(barReaders);
        }
    }

    public void setBarReaderQuantity(int quantity) {
        this.barReaderQuantity = requireNonNegativeQuantity(quantity);
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
        if (!printer) {
            this.printerQuantity = 0;
        } else if (printerQuantity == 0) {
            this.printerQuantity = 1;
        }
    }

    public boolean isRfidReader() {
        return rfidReader;
    }

    public void setRfidReader(boolean rfidReader) {
        this.rfidReader = rfidReader;
        if (!rfidReader) {
            this.rfidReaderQuantity = 0;
        } else if (rfidReaderQuantity == 0) {
            this.rfidReaderQuantity = 1;
        }
    }

    public int getPrinterQuantity() { return printerQuantity; }

    public void setPrinterQuantity(int quantity) {
        this.printerQuantity = requireNonNegativeQuantity(quantity);
        this.printer = quantity > 0;
    }

    public int getRfidReaderQuantity() { return rfidReaderQuantity; }

    public void setRfidReaderQuantity(int quantity) {
        this.rfidReaderQuantity = requireNonNegativeQuantity(quantity);
        this.rfidReader = quantity > 0;
    }

    private int requireNonNegativeQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException(
                    "Количество оборудования не может быть отрицательным");
        }
        return quantity;
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
                ", display='" + display + '\'' +
                ", barReader='" + barReader + '\'' +
                ", displayQuantity=" + displayQuantity +
                ", barReaderQuantity=" + barReaderQuantity +
                ", barReaders=" + barReaders +
                ", printerQuantity=" + printerQuantity +
                ", rfidReaderQuantity=" + rfidReaderQuantity +
                ", payment=" + payment +
                ", printer=" + printer +
                ", rfidReader=" + rfidReader +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}