package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.Objects;
import java.util.Set;

/**
 * Функциональные возможности автоматизированной системы хранения.
 *
 * <p>Конфигурация принадлежит ALS и синхронизируется с её единственным
 * управляющим модулем. Состав установленного оборудования хранится отдельно.</p>
 */
public final class FunctionalConfiguration {

    private final DisplayPurpose display;
    private final Set<AccessPurpose> access;
    private final PaymentPurpose payment;
    private final PrinterPurpose printer;

    /** Создаёт функциональную конфигурацию с проверкой обязательных назначений. */
    public FunctionalConfiguration(
            DisplayPurpose display,
            Set<AccessPurpose> access,
            PaymentPurpose payment,
            PrinterPurpose printer
    ) {
        this.display = Objects.requireNonNull(display, "Назначение дисплея не должно быть null");
        Objects.requireNonNull(access, "Назначения доступа не должны быть null");
        this.access = Set.copyOf(access);
        this.payment = Objects.requireNonNull(payment, "Назначение оплаты не должно быть null");
        this.printer = Objects.requireNonNull(printer, "Назначение принтера не должно быть null");
    }

    public DisplayPurpose getDisplay() { return display; }
    public Set<AccessPurpose> getAccess() { return access; }
    public PaymentPurpose getPayment() { return payment; }
    public PrinterPurpose getPrinter() { return printer; }

    /** @return независимая копия функциональной конфигурации */
    public FunctionalConfiguration copy() {
        return new FunctionalConfiguration(display, access, payment, printer);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FunctionalConfiguration other)) return false;
        return display == other.display
                && access.equals(other.access)
                && payment == other.payment
                && printer == other.printer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, access, payment, printer);
    }

    @Override
    public String toString() {
        return "FunctionalConfiguration{" +
                "display=" + display +
                ", access=" + access +
                ", payment=" + payment +
                ", printer=" + printer +
                '}';
    }
}
