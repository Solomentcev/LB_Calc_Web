package com.lb_calc_web.model;

import com.lb_calc_web.model.utils.BarReader;
import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.DisplayLC;
import com.lb_calc_web.model.utils.Payment;
import jakarta.persistence.*;
import org.hibernate.type.YesNoConverter;

import java.util.Objects;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "height","width","depth",
        "display","bar_reader","payment","printer","rfid_reader","color_body"}) })
public class LC {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private int height;
    private int width;
    private int depth;
    private int upperFrame;
    private int bottomFrame;
    @Enumerated(EnumType.STRING)
    private DisplayLC display;
    @Enumerated(EnumType.STRING)
    private BarReader barReader;
    @Enumerated(EnumType.STRING)
    private Payment payment;
    @Convert(converter = YesNoConverter.class)
    private boolean printer;
    @Convert(converter = YesNoConverter.class)
    private boolean rfidReader;
    @Enumerated(EnumType.STRING)
    private Colors colorBody;

    public LC() {
    }

    public LC(String name, String description, int height, int width, int depth,
              DisplayLC display, BarReader barReader, Payment payment, boolean printer, boolean rfidReader, Colors colorBody,
              int upperFrame, int bottomFrame) {
        this.name = name;
        this.description = description;
        this.height = height;
        this.width = width;
        this.depth = depth;
        this.display = display;
        this.barReader = barReader;
        this.payment = payment;
        this.printer = printer;
        this.rfidReader = rfidReader;
        this.colorBody = colorBody;
        this.upperFrame = upperFrame;
        this.bottomFrame = bottomFrame;
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

    public DisplayLC getDisplay() {
        return display;
    }

    public void setDisplay(DisplayLC display) {
        this.display = display;
    }

    public BarReader getBarReader() {
        return barReader;
    }

    public void setBarReader(BarReader barReader) {
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

    public Colors getColorBody() {
        return colorBody;
    }

    public void setColorBody(Colors colorBody) {
        this.colorBody = colorBody;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LC lc = (LC) o;
        return height == lc.height && width == lc.width && depth == lc.depth && printer == lc.printer && rfidReader == lc.rfidReader
                && Objects.equals(name, lc.name) && display == lc.display && barReader == lc.barReader && payment == lc.payment
                && colorBody == lc.colorBody
                && upperFrame == lc.upperFrame && bottomFrame == lc.bottomFrame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(height, width, depth, display, barReader, payment, printer, rfidReader, colorBody, upperFrame, bottomFrame);
    }

    @Override
    public String toString() {
        return "LC{" +
                "id=" + id +
                ", name='" + name +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", display=" + display +
                ", barReader=" + barReader +
                ", payment=" + payment +
                ", printer=" + printer +
                ", rfidReader=" + rfidReader +
                ", colorBody=" + colorBody +
                '}';
    }
}
