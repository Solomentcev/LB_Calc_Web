package com.lb_calc_web.dto;

import com.lb_calc_web.model.utils.BarReader;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DisplayLC;
import com.lb_calc_web.model.utils.Payment;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Transient;
import org.hibernate.type.YesNoConverter;

public class LCDTO {
    private int id;
    private String name;
    private String description;
    private int height;
    private int width;
    private int depth;
    private String display;
    private String barReader;
    private String payment;
    private boolean printer;
    private boolean rfidReader;
    private String colorBody;

    public LCDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
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

    public String getColorBody() {
        return colorBody;
    }

    public void setColorBody(String colorBody) {
        this.colorBody = colorBody;
    }
}
