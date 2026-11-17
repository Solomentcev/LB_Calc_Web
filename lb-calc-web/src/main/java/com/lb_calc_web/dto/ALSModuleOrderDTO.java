package com.lb_calc_web.dto;

import java.util.Objects;

/**
 * Ссылка на модуль в физическом порядке ALS.
 *
 * <p>Индекс указывает на элемент соответствующего списка DTO:
 * lbList для LB и индекс 0 для единственного управляющего модуля LC/LBC.</p>
 */
public class ALSModuleOrderDTO {

    private String type;
    private int index;

    public ALSModuleOrderDTO() {
    }

    public ALSModuleOrderDTO(String type, int index) {
        this.type = type;
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ALSModuleOrderDTO that = (ALSModuleOrderDTO) object;
        return index == that.index && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, index);
    }
}
