package com.lb_calc_web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
@Embeddable
public class ALSLBKey implements Serializable {
    @Column(name = "als_id")
    private Long alsId;

    @Column(name = "lb_id")
    private Long lbId;

    public ALSLBKey(Long alsId, Long lbId) {
        this.alsId = alsId;
        this.lbId = lbId;
    }

    public ALSLBKey() {

    }

    public Long getAlsId() {
        return alsId;
    }

    public void setAlsId(Long alsId) {
        this.alsId = alsId;
    }

    public Long getLbId() {
        return lbId;
    }

    public void setLbId(Long lbId) {
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
