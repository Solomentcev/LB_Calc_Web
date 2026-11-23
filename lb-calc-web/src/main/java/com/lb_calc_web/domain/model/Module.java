package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;

import java.util.Objects;

public abstract class Module {

    /** Идентификатор сохранённого модуля. */
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Переносит идентификатор в независимую копию модуля. */
    protected <T extends Module> T copyIdentityTo(T copy) {
        copy.setId(id);
        return copy;
    }

    private String name;
    private String description;

    private int height;
    private int width;
    private int depth;

    private int upperFrame;
    private int bottomFrame;

    private Colors colorBody;
    private Colors colorDoor;

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getDepth() {
        return depth;
    }

    protected void setDepth(int depth) {
        this.depth = depth;
    }

    public int getUpperFrame() {
        return upperFrame;
    }

    protected void setUpperFrame(int upperFrame) {
        this.upperFrame = upperFrame;
    }

    public int getBottomFrame() {
        return bottomFrame;
    }

    protected void setBottomFrame(int bottomFrame) {
        this.bottomFrame = bottomFrame;
    }

    public Colors getColorBody() {
        return colorBody;
    }

    protected void setColorBody(Colors colorBody) {
        this.colorBody = Objects.requireNonNull(
                colorBody,
                "Цвет корпуса не должен быть null"
        );
    }

    public Colors getColorDoor() {
        return colorDoor;
    }

    protected void setColorDoor(Colors colorDoor) {
        this.colorDoor = Objects.requireNonNull(
                colorDoor,
                "Цвет дверей не должен быть null"
        );
    }

    /**
     * Создаёт независимую копию конфигурации модуля.
     *
     * @return копия модуля без общих изменяемых внутренних объектов
     */
    public abstract Module copy();

    public abstract void recalculate();
}