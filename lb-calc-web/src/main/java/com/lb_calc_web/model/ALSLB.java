package com.lb_calc_web.model;

import jakarta.persistence.*;

@Entity(name="als_lb")
public class ALSLB {
    @EmbeddedId( )
    private ALSLBKey id;
    @ManyToOne

    @MapsId("alsId")
    @JoinColumn(name = "als_id")
    private ALS als;
    @ManyToOne

    @MapsId("lbId")
    @JoinColumn(name = "lb_id")
    private LB lb;

    private int quantity;

    public ALSLB() {
    }

    public ALSLB( ALS als, LB lb, int quantity) {
        this.id = new ALSLBKey(als.getId(), lb.getId());
        this.als = als;
        this.lb = lb;
        this.quantity = quantity;
    }

    public ALSLBKey getId() {
        return id;
    }

    public void setId(ALSLBKey id) {
        this.id = id;
    }

    public ALS getAls() {
        return als;
    }

    public void setAls(ALS als) {
        this.als = als;
    }

    public LB getLb() {
        return lb;
    }

    public void setLb(LB lb) {
        this.lb = lb;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ALSLB{" +
                "id=" + id +
                ", alsID=" + als.getId() +
                ", lbID=" + lb.getId() +
                ", quantity=" + quantity +
                '}';
    }
}
