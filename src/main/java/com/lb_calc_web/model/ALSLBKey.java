package com.lb_calc_web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class ALSLBKey implements Serializable {
    @Column(name = "als_id")
    private Integer alsId;

    @Column(name = "lb_id")
    private Integer lbId;

    public ALSLBKey() {
    }

    public ALSLBKey(Integer alsId, Integer lbId) {
        this.alsId = alsId;
        this.lbId = lbId;
    }

    public Integer getAlsId() {
        return alsId;
    }

    public void setAlsId(Integer alsId) {
        this.alsId = alsId;
    }

    public Integer getLbId() {
        return lbId;
    }

    public void setLbId(Integer lbId) {
        this.lbId = lbId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ALSLBKey alslbKey = (ALSLBKey) o;
        return Objects.equals(getAlsId(), alslbKey.getAlsId()) && Objects.equals(getLbId(), alslbKey.getLbId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAlsId(), getLbId());
    }

    @Override
    public String toString() {
        return "ALSLBKey{" +
                "alsId=" + alsId +
                ", lbId=" + lbId +
                '}';
    }
}
