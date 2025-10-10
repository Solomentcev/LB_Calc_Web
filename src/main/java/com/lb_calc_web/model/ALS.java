package com.lb_calc_web.model;

import com.lb_calc_web.model.utils.Colors;
import com.lb_calc_web.model.utils.PositionLC;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "description","height","width","depth",
        "upper_frame","bottom_frame","count_cells","depth_cell",
        "positionlc","color_body","color_door", "lc_id"}) })
public class ALS {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private int height;
    private int depth;
    private int width;
    private int upperFrame;
    private int bottomFrame;
    private int depthCell;
    private int countCells;
    @ManyToOne
    @JoinColumn(name = "lc_id")
    private LC lc;
    private PositionLC positionLC;
    @Enumerated(EnumType.STRING)
    private Colors colorDoor;
    @Enumerated(EnumType.STRING)
    private Colors colorBody;
    @Transient
    private List<LB> lbList=new ArrayList<>();
    @OneToMany(mappedBy = "als",
            orphanRemoval = true)
    private Set<ALSLB> quantityLB=new HashSet<>();
    @OneToMany(mappedBy = "als")
    private Set<ProjectALS> quantityALS = new HashSet<>();
    public ALS() {
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

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
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

    public int getDepthCell() {
        return depthCell;
    }

    public void setDepthCell(int depthCell) {
        this.depthCell = depthCell;
    }

    public int getCountCells() {
        return countCells;
    }

    public void setCountCells(int countCells) {
        this.countCells = countCells;
    }

    public LC getLc() {
        return lc;
    }

    public void setLc(LC lc) {
        this.lc = lc;
    }

    public PositionLC getPositionLC() {
        return positionLC;
    }

    public void setPositionLC(PositionLC positionLC) {
        this.positionLC = positionLC;
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    public void setColorDoor(Colors colorDoor) {
        this.colorDoor = colorDoor;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    public void setColorBody(Colors colorBody) {
        this.colorBody = colorBody;
    }

    public List<LB> getLbList() {
        return lbList;
    }

    public Set<ALSLB> getQuantityLB() {
        return quantityLB;
    }

    public void setQuantityLB(Set<ALSLB> quantityLB) {
        this.quantityLB = quantityLB;
    }

    public void setLbList(List<LB> lbList) {
        this.lbList = lbList;
    }

    public Set<ProjectALS> getQuantityALS() {
        return quantityALS;
    }

    public void setQuantityALS(Set<ProjectALS> quantityALS) {
        this.quantityALS = quantityALS;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALS als = (ALS) o;
        return height == als.height && depth == als.depth && width == als.width && upperFrame == als.upperFrame
                && bottomFrame == als.bottomFrame && depthCell == als.depthCell && countCells == als.countCells
                && Objects.equals(lc, als.lc) && positionLC == als.positionLC
                && colorDoor == als.colorDoor && colorBody == als.colorBody
                && description.equals(als.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, height, depth, width, upperFrame, bottomFrame, depthCell, countCells,
                lc, positionLC, colorDoor, colorBody);
    }

    @Override
    public String toString() {
        return "ALS{" +
                "id=" + id +
                ", name='" + name +
                ", height=" + height +
                ", depth=" + depth +
                ", width=" + width +
             //   ", upperFrame=" + upperFrame +
             //   ", bottomFrame=" + bottomFrame +
              //  ", depthCell=" + depthCell +
                ", countCells=" + countCells +"\n"+
                ", positionLC=" + positionLC +
                ", colorDoor=" + colorDoor +
                ", colorBody=" + colorBody +
               // ", lc=" + lc.getId() +
                '}';
    }
}
