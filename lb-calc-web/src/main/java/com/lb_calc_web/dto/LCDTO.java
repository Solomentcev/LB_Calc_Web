package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LCDTO {

    private Long id;
    private String name;
    private String description;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private String display;
    private String barReader;
    private String payment;

    private boolean printer;
    private boolean rfidReader;

    private String colorBody;
    private String colorDoor;

    private List<String> accessMethods = new ArrayList<>();
    private List<String> printOptions = new ArrayList<>();

    @JsonIgnore
    private String stringLCImage;

    public LCDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public String getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(String colorDoor) {
        this.colorDoor = colorDoor;
    }

    public List<String> getAccessMethods() {
        return accessMethods;
    }

    public void setAccessMethods(List<String> accessMethods) {
        this.accessMethods = accessMethods == null
                ? new ArrayList<>()
                : new ArrayList<>(accessMethods);
    }

    public List<String> getPrintOptions() {
        return printOptions;
    }

    public void setPrintOptions(List<String> printOptions) {
        this.printOptions = printOptions == null
                ? new ArrayList<>()
                : new ArrayList<>(printOptions);
    }

    public String getStringLCImage() {
        return stringLCImage;
    }

    public void setStringLCImage(String stringLCImage) {
        this.stringLCImage = stringLCImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LCDTO lcdto = (LCDTO) o;

        return height == lcdto.height
                && width == lcdto.width
                && depth == lcdto.depth
                && upperFrame == lcdto.upperFrame
                && bottomFrame == lcdto.bottomFrame
                && printer == lcdto.printer
                && rfidReader == lcdto.rfidReader
                && Objects.equals(display, lcdto.display)
                && Objects.equals(barReader, lcdto.barReader)
                && Objects.equals(payment, lcdto.payment)
                && Objects.equals(colorBody, lcdto.colorBody)
                && Objects.equals(colorDoor, lcdto.colorDoor)
                && Objects.equals(accessMethods, lcdto.accessMethods)
                && Objects.equals(printOptions, lcdto.printOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                height,
                width,
                depth,
                upperFrame,
                bottomFrame,
                display,
                barReader,
                payment,
                printer,
                rfidReader,
                colorBody,
                colorDoor,
                accessMethods,
                printOptions
        );
    }

    @Override
    public String toString() {
        return "LCDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", display='" + display + '\'' +
                ", barReader='" + barReader + '\'' +
                ", payment='" + payment + '\'' +
                ", printer=" + printer +
                ", rfidReader=" + rfidReader +
                ", colorBody='" + colorBody + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}