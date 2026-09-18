package com.lb_calc_web.domain.equipment;

import java.util.Objects;

public abstract class Equipment {

    private final String name;

    protected Equipment(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Название оборудования не должно быть пустым"
            );
        }

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public abstract int getDepth();

    public abstract String getDescription();

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Equipment that = (Equipment) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), name);
    }
}