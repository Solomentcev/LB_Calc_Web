package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LBCDTO {

    private Long id;

    private String name;
    private String description;

    private String type;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private int shelfThick;
    private int deltaWidth;
    private int serviceZoneWidth;
    private int doorThickness;

    private int countCells;

    private double heightCell;
    private int widthCell;
    private int depthCell;

    private String directionDoorOpening;

    private String colorDoor;
    private String colorBody;

    private String display;
    private String barReader;
    private String payment;

    private boolean printer;
    private boolean rfidReader;

    private Integer displayQuantity;
    private Integer barReaderQuantity;
    private Integer printerQuantity;
    private Integer rfidReaderQuantity;

    private int displayWidth;
    private int displayHeight;

    private List<EquipmentSelectionDTO> equipmentSelections;
    private String displayPurpose;
    private List<String> accessPurposes = new ArrayList<>();
    private String paymentPurpose;
    private String printerPurpose;

    private List<String> accessMethods = new ArrayList<>();
    private List<String> printOptions = new ArrayList<>();

    @JsonIgnore
    private String stringLBCImage;

    public LBCDTO() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public int getShelfThick() {
        return shelfThick;
    }

    public void setShelfThick(int shelfThick) {
        this.shelfThick = shelfThick;
    }

    public int getDeltaWidth() {
        return deltaWidth;
    }

    public void setDeltaWidth(int deltaWidth) {
        this.deltaWidth = deltaWidth;
    }

    public int getServiceZoneWidth() {
        return serviceZoneWidth;
    }

    public void setServiceZoneWidth(int serviceZoneWidth) {
        this.serviceZoneWidth = serviceZoneWidth;
    }

    public int getDoorThickness() {
        return doorThickness;
    }

    public void setDoorThickness(int doorThickness) {
        this.doorThickness = doorThickness;
    }

    public int getCountCells() {
        return countCells;
    }

    public void setCountCells(int countCells) {
        this.countCells = countCells;
    }

    public double getHeightCell() {
        return heightCell;
    }

    public void setHeightCell(double heightCell) {
        this.heightCell = heightCell;
    }

    public int getWidthCell() {
        return widthCell;
    }

    public void setWidthCell(int widthCell) {
        this.widthCell = widthCell;
    }

    public int getDepthCell() {
        return depthCell;
    }

    public void setDepthCell(int depthCell) {
        this.depthCell = depthCell;
    }

    public String getDirectionDoorOpening() {
        return directionDoorOpening;
    }

    public void setDirectionDoorOpening(String directionDoorOpening) {
        this.directionDoorOpening = directionDoorOpening;
    }

    public String getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(String colorDoor) {
        this.colorDoor = colorDoor;
    }

    public String getColorBody() {
        return colorBody;
    }

    public void setColorBody(String colorBody) {
        this.colorBody = colorBody;
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
        accessPurposes = values == null ? new ArrayList<>() : new ArrayList<>(values);
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

    public String getStringLBCImage() {
        return stringLBCImage;
    }

    public void setStringLBCImage(String stringLBCImage) {
        this.stringLBCImage = stringLBCImage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LBCDTO lbcdto = (LBCDTO) o;

        return height == lbcdto.height
                && width == lbcdto.width
                && depth == lbcdto.depth
                && upperFrame == lbcdto.upperFrame
                && bottomFrame == lbcdto.bottomFrame
                && shelfThick == lbcdto.shelfThick
                && deltaWidth == lbcdto.deltaWidth
                && serviceZoneWidth == lbcdto.serviceZoneWidth
                && doorThickness == lbcdto.doorThickness
                && countCells == lbcdto.countCells
                && Double.compare(heightCell, lbcdto.heightCell) == 0
                && widthCell == lbcdto.widthCell
                && depthCell == lbcdto.depthCell
                && printer == lbcdto.printer
                && rfidReader == lbcdto.rfidReader
                && Objects.equals(displayQuantity, lbcdto.displayQuantity)
                && Objects.equals(barReaderQuantity, lbcdto.barReaderQuantity)
                && Objects.equals(printerQuantity, lbcdto.printerQuantity)
                && Objects.equals(rfidReaderQuantity, lbcdto.rfidReaderQuantity)
                && Objects.equals(type, lbcdto.type)
                && Objects.equals(
                directionDoorOpening,
                lbcdto.directionDoorOpening
        )
                && Objects.equals(colorDoor, lbcdto.colorDoor)
                && Objects.equals(colorBody, lbcdto.colorBody)
                && Objects.equals(display, lbcdto.display)
                && Objects.equals(displayPurpose, lbcdto.displayPurpose)
                && Objects.equals(accessPurposes, lbcdto.accessPurposes)
                && Objects.equals(paymentPurpose, lbcdto.paymentPurpose)
                && Objects.equals(printerPurpose, lbcdto.printerPurpose)
                && Objects.equals(barReader, lbcdto.barReader)
                && Objects.equals(payment, lbcdto.payment)
                && Objects.equals(equipmentSelections, lbcdto.equipmentSelections)
                && Objects.equals(accessMethods, lbcdto.accessMethods)
                && Objects.equals(printOptions, lbcdto.printOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                type,
                height,
                width,
                depth,
                upperFrame,
                bottomFrame,
                shelfThick,
                deltaWidth,
                serviceZoneWidth,
                doorThickness,
                countCells,
                heightCell,
                widthCell,
                depthCell,
                directionDoorOpening,
                colorDoor,
                colorBody,
                display,
                barReader,
                payment,
                printer,
                rfidReader,
                displayQuantity,
                barReaderQuantity,
                printerQuantity,
                rfidReaderQuantity,
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
        return "LBCDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", shelfThick=" + shelfThick +
                ", deltaWidth=" + deltaWidth +
                ", serviceZoneWidth=" + serviceZoneWidth +
                ", doorThickness=" + doorThickness +
                ", countCells=" + countCells +
                ", heightCell=" + heightCell +
                ", widthCell=" + widthCell +
                ", depthCell=" + depthCell +
                ", directionDoorOpening='" + directionDoorOpening + '\'' +
                ", colorDoor='" + colorDoor + '\'' +
                ", colorBody='" + colorBody + '\'' +
                ", display='" + display + '\'' +
                ", barReader='" + barReader + '\'' +
                ", payment='" + payment + '\'' +
                ", printer=" + printer +
                ", rfidReader=" + rfidReader +
                ", displayQuantity=" + displayQuantity +
                ", barReaderQuantity=" + barReaderQuantity +
                ", printerQuantity=" + printerQuantity +
                ", rfidReaderQuantity=" + rfidReaderQuantity +
                ", equipmentSelections=" + equipmentSelections +
                ", accessMethods=" + accessMethods +
                ", printOptions=" + printOptions +
                '}';
    }
}