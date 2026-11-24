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

    private Integer displayQuantity;
    private Integer barReaderQuantity;
    private Integer printerQuantity;
    private Integer rfidReaderQuantity;

    private String colorBody;
    private String colorDoor;

    private int displayWidth;
    private int displayHeight;

    private List<EquipmentSelectionDTO> equipmentSelections;
    private String displayPurpose;
    private List<String> accessPurposes;
    private String paymentPurpose;
    private String printerPurpose;

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

    public Integer getDisplayQuantity() { return displayQuantity; }
    public void setDisplayQuantity(Integer value) { displayQuantity = value; }
    public Integer getBarReaderQuantity() { return barReaderQuantity; }
    public void setBarReaderQuantity(Integer value) { barReaderQuantity = value; }
    public Integer getPrinterQuantity() { return printerQuantity; }
    public void setPrinterQuantity(Integer value) { printerQuantity = value; }
    public Integer getRfidReaderQuantity() { return rfidReaderQuantity; }
    public void setRfidReaderQuantity(Integer value) { rfidReaderQuantity = value; }

    public int getDisplayWidth() { return displayWidth; }
    public void setDisplayWidth(int value) { displayWidth = value; }
    public int getDisplayHeight() { return displayHeight; }
    public void setDisplayHeight(int value) { displayHeight = value; }

    public List<EquipmentSelectionDTO> getEquipmentSelections() { return equipmentSelections; }

    public void setEquipmentSelections(List<EquipmentSelectionDTO> selections) {
        this.equipmentSelections = selections == null ? null : new ArrayList<>(selections);
    }

    public String getDisplayPurpose() { return displayPurpose; }
    public void setDisplayPurpose(String value) { displayPurpose = value; }

    public List<String> getAccessPurposes() { return accessPurposes; }
    public void setAccessPurposes(List<String> values) {
        accessPurposes = values == null ? null : new ArrayList<>(values);
    }

    public String getPaymentPurpose() { return paymentPurpose; }
    public void setPaymentPurpose(String value) { paymentPurpose = value; }

    public String getPrinterPurpose() { return printerPurpose; }
    public void setPrinterPurpose(String value) { printerPurpose = value; }

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
                && Objects.equals(displayQuantity, lcdto.displayQuantity)
                && Objects.equals(barReaderQuantity, lcdto.barReaderQuantity)
                && Objects.equals(printerQuantity, lcdto.printerQuantity)
                && Objects.equals(rfidReaderQuantity, lcdto.rfidReaderQuantity)
                && Objects.equals(display, lcdto.display)
                && Objects.equals(displayPurpose, lcdto.displayPurpose)
                && Objects.equals(accessPurposes, lcdto.accessPurposes)
                && Objects.equals(paymentPurpose, lcdto.paymentPurpose)
                && Objects.equals(printerPurpose, lcdto.printerPurpose)
                && Objects.equals(barReader, lcdto.barReader)
                && Objects.equals(payment, lcdto.payment)
                && Objects.equals(colorBody, lcdto.colorBody)
                && Objects.equals(colorDoor, lcdto.colorDoor)
                && Objects.equals(equipmentSelections, lcdto.equipmentSelections)
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
                equipmentSelections,
                displayPurpose,
                accessPurposes,
                paymentPurpose,
                printerPurpose,
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
                ", displayQuantity=" + displayQuantity +
                ", barReaderQuantity=" + barReaderQuantity +
                ", printerQuantity=" + printerQuantity +
                ", rfidReaderQuantity=" + rfidReaderQuantity +
                ", colorBody='" + colorBody + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", equipmentSelections=" + equipmentSelections +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}