package com.lb_calc_web.dto;

import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;

import java.util.EnumSet;
import java.util.Set;
import java.util.Objects;

/** Транспортное представление функциональных назначений ALS и управляющего модуля. */
public class FunctionalConfigurationDTO {

    private DisplayPurpose display;
    private Set<AccessPurpose> access;
    private PaymentPurpose payment;
    private PrinterPurpose printer;

    public DisplayPurpose getDisplay() { return display; }
    public void setDisplay(DisplayPurpose display) { this.display = display; }

    public Set<AccessPurpose> getAccess() { return access == null ? null : Set.copyOf(access); }
    public void setAccess(Set<AccessPurpose> access) {
        this.access = access == null ? null
                : access.isEmpty() ? EnumSet.noneOf(AccessPurpose.class) : EnumSet.copyOf(access);
    }

    public PaymentPurpose getPayment() { return payment; }
    public void setPayment(PaymentPurpose payment) { this.payment = payment; }

    public PrinterPurpose getPrinter() { return printer; }
    public void setPrinter(PrinterPurpose printer) { this.printer = printer; }
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof FunctionalConfigurationDTO other)) return false;
        return display == other.display && Objects.equals(access, other.access)
                && payment == other.payment && printer == other.printer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(display, access, payment, printer);
    }
}
