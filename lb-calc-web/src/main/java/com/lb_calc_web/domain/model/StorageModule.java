package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Базовый класс для модулей хранения.
 *
 * <p>StorageModule содержит конструктивный тип, направление открытия
 * дверей и набор ячеек. Количество ячеек определяется размером списка
 * {@link #getCells()} и отдельно в domain не хранится.</p>
 *
 * <p>Номера ячеек не являются свойством самого storage-модуля.
 * Они назначаются агрегатом {@link ALS} с учётом положения модуля
 * внутри конкретного ALS.</p>
 *
 * <p>Размеры ячейки являются производными значениями и рассчитываются
 * методом {@link #recalculateCells()}.</p>
 */
public abstract class StorageModule extends Module {

    /**
     * Конструктивный тип модуля хранения.
     */
    private TypeLb typeLb;

    /**
     * Рассчитанная высота ячейки, мм.
     */
    private int cellHeight;

    /**
     * Рассчитанная ширина ячейки, мм.
     */
    private int cellWidth;

    /**
     * Рассчитанная глубина ячейки, мм.
     */
    private int cellDepth;

    /**
     * Толщина дверцы, используемая при расчёте глубины ячейки, мм.
     *
     * <p>Значение не задаётся в domain как default.
     * Default берётся из конфигурации приложения.</p>
     */
    private final int doorThickness;

    /**
     * Направление открытия дверей storage-модуля.
     */
    private DirectionDoorOpening directionDoorOpening;

    /**
     * Ячейки модуля в локальном порядке.
     */
    private final List<StorageCell> cells = new ArrayList<>();

    /**
     * Создаёт storage-модуль.
     *
     * @param doorThickness толщина дверцы, используемая
     *                      при расчёте глубины ячейки, мм
     * @throws IllegalArgumentException если толщина дверцы
     *                                  меньше или равна нулю
     */
    protected StorageModule(int doorThickness) {
        if (doorThickness <= 0) {
            throw new IllegalArgumentException(
                    "Толщина дверцы должна быть положительной"
            );
        }

        this.doorThickness = doorThickness;
    }

    /**
     * Возвращает конструктивный тип модуля.
     *
     * @return тип LB
     */
    public TypeLb getTypeLb() {
        return typeLb;
    }

    /**
     * Устанавливает конструктивный тип модуля.
     *
     * @param typeLb конструктивный тип
     * @throws NullPointerException если тип не задан
     */
    public void setTypeLb(TypeLb typeLb) {
        this.typeLb = Objects.requireNonNull(
                typeLb,
                "Тип модуля хранения не может быть null"
        );
    }

    /**
     * Возвращает толщину дверцы, используемую в расчётах.
     *
     * @return толщина дверцы, мм
     */
    public int getDoorThickness() {
        return doorThickness;
    }

    /**
     * Возвращает рассчитанную высоту ячейки.
     *
     * @return высота ячейки, мм
     */
    public int getCellHeight() {
        return cellHeight;
    }

    /**
     * Возвращает рассчитанную ширину ячейки.
     *
     * @return ширина ячейки, мм
     */
    public int getCellWidth() {
        return cellWidth;
    }

    /**
     * Возвращает рассчитанную глубину ячейки.
     *
     * @return глубина ячейки, мм
     */
    public int getCellDepth() {
        return cellDepth;
    }

    /**
     * Возвращает направление открытия дверей.
     *
     * @return направление открытия дверей
     */
    public DirectionDoorOpening getDirectionDoorOpening() {
        return directionDoorOpening;
    }

    /**
     * Устанавливает направление открытия дверей.
     *
     * @param directionDoorOpening направление открытия дверей
     * @throws NullPointerException если направление не задано
     */
    public void setDirectionDoorOpening(
            DirectionDoorOpening directionDoorOpening
    ) {
        this.directionDoorOpening = Objects.requireNonNull(
                directionDoorOpening,
                "Направление открытия дверей не может быть null"
        );
    }

    /**
     * Возвращает ячейки модуля в их локальном порядке.
     *
     * <p>Список доступен только для чтения.</p>
     *
     * @return неизменяемый список ячеек
     */
    public List<StorageCell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    /**
     * Возвращает количество ячеек модуля.
     *
     * <p>Количество определяется фактическим количеством объектов
     * {@link StorageCell}.</p>
     *
     * @return количество ячеек
     */
    public int getCountCells() {
        return cells.size();
    }

    /**
     * Устанавливает количество ячеек.
     *
     * <p>При увеличении создаются новые ячейки, при уменьшении
     * удаляются последние. Номера ячеек здесь не назначаются.
     * После изменения конфигурации выполняется пересчёт модуля.</p>
     *
     * @param countCells новое количество ячеек
     * @throws IllegalArgumentException если количество меньше единицы
     */
    public void setCountCells(int countCells) {
        setCells(createCells(countCells));
        recalculate();
    }

    /**
     * Заменяет набор ячеек.
     *
     * <p>Метод предназначен для внутреннего использования
     * storage-модулем и его наследниками.</p>
     *
     * @param cells новый список ячеек
     * @throws NullPointerException если список не задан
     */
    protected void setCells(List<StorageCell> cells) {
        Objects.requireNonNull(
                cells,
                "Список ячеек не может быть null"
        );

        this.cells.clear();
        this.cells.addAll(cells);
    }

    /**
     * Создаёт список пустых ячеек.
     *
     * <p>Номера ячеек не задаются. Их нумерацию выполняет ALS.</p>
     *
     * @param countCells количество ячеек
     * @return список новых ячеек
     * @throws IllegalArgumentException если количество меньше единицы
     */
    protected List<StorageCell> createCells(int countCells) {
        if (countCells < 1) {
            throw new IllegalArgumentException(
                    "Количество ячеек должно быть больше нуля"
            );
        }

        List<StorageCell> cells = new ArrayList<>(countCells);

        for (int i = 0; i < countCells; i++) {
            cells.add(new StorageCell());
        }

        return cells;
    }

    /**
     * Устанавливает рассчитанные размеры ячейки.
     *
     * @param cellHeight высота ячейки, мм
     * @param cellWidth ширина ячейки, мм
     * @param cellDepth глубина ячейки, мм
     * @throws IllegalArgumentException если один из размеров
     *                                  меньше или равен нулю
     */
    protected void setCellDimensions(
            int cellHeight,
            int cellWidth,
            int cellDepth
    ) {
        if (cellHeight <= 0) {
            throw new IllegalArgumentException(
                    "Высота ячейки должна быть положительной"
            );
        }

        if (cellWidth <= 0) {
            throw new IllegalArgumentException(
                    "Ширина ячейки должна быть положительной"
            );
        }

        if (cellDepth <= 0) {
            throw new IllegalArgumentException(
                    "Глубина ячейки должна быть положительной"
            );
        }

        this.cellHeight = cellHeight;
        this.cellWidth = cellWidth;
        this.cellDepth = cellDepth;
    }

    /**
     * Пересчитывает геометрические параметры ячеек.
     *
     * <p>Расчёт использует:</p>
     * <ul>
     *     <li>высоту модуля;</li>
     *     <li>верхнюю и нижнюю рамы;</li>
     *     <li>количество ячеек;</li>
     *     <li>толщину полки из {@link TypeLb};</li>
     *     <li>ширину и глубину модуля;</li>
     *     <li>добавочную ширину из {@link TypeLb};</li>
     *     <li>толщину дверцы.</li>
     * </ul>
     *
     * @throws IllegalStateException если отсутствуют ячейки
     *                               или конструктивный тип
     */
    protected void recalculateStorageCells() {
        int countCells = getCountCells();

        if (countCells == 0) {
            throw new IllegalStateException(
                    "Модуль хранения должен содержать хотя бы одну ячейку"
            );
        }

        if (typeLb == null) {
            throw new IllegalStateException(
                    "Тип модуля хранения не задан"
            );
        }

        int cellWidth =
                getWidth() - typeLb.getDeltaWidth();

        int cellDepth =
                getDepth() - doorThickness;

        double usableHeight =
                getHeight()
                        - getUpperFrame()
                        - getBottomFrame()
                        - ((countCells - 1) * typeLb.getShelfThick());

        double cellHeight =
                usableHeight / countCells;

        setCellDimensions(
                (int) cellHeight,
                cellWidth,
                cellDepth
        );
    }

    /**
     * Пересчитывает размеры ячеек конкретного storage-модуля.
     */
    public abstract void recalculateCells();

    /**
     * Пересчитывает все производные параметры конкретного модуля.
     */
    @Override
    public abstract void recalculate();
}
