package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;

import java.util.Objects;

/**
 * Базовая доменная модель физического модуля автоматизированной камеры хранения.
 *
 * <p>Содержит общие идентификационные, геометрические и визуальные характеристики.
 * Специфические параметры и правила пересчёта определяются конкретными типами модулей.</p>
 */
public abstract class Module {

    /** Идентификатор сохранённого модуля. */
    private Long id;

    /** @return идентификатор сохранённого модуля или {@code null} для нового */
    public Long getId() {
        return id;
    }

    /** @param id идентификатор, назначенный хранилищем */
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

    /**
     * Создаёт независимую копию модуля с сохранёнными пользовательскими текстами.
     *
     * <p>Используется при восстановлении ранее сохранённого состояния.</p>
     *
     * @param source исходный модуль
     * @param name сохранённое наименование
     * @param description сохранённое описание
     * @return независимая копия модуля с указанными текстами
     */
    public static Module restoreText(Module source, String name, String description) {
        Objects.requireNonNull(source, "Исходный модуль не должен быть null");
        Module restored = source.copy();
        restored.setName(name);
        // Нормализуем старые описания, в которых переносы были сохранены как "\\n".
        restored.setDescription(description == null ? null : description.replace("\\n", "\n"));
        return restored;
    }

    /** Копирует пользовательские тексты в независимую копию модуля. */
    protected final <T extends Module> T copyTextTo(T copy) {
        copy.setName(name);
        copy.setDescription(description);
        return copy;
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

    /** Пересчитывает производные характеристики после изменения исходных параметров. */
    public abstract void recalculate();
}