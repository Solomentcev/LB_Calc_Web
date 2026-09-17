package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionLC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Автоматизированная система хранения.
 *
 * <p>ALS является агрегатом, объединяющим модули в физическом порядке
 * слева направо.</p>
 *
 * <p>Допустимая структура ALS:</p>
 * <ul>
 *     <li>ровно один {@link ControlModule};</li>
 *     <li>как минимум один {@link StorageModule}.</li>
 * </ul>
 *
 * <p>Единственным источником истины для расположения модулей является
 * список {@link #modules}. Позиция модуля управления, направления
 * открытия дверей и номера ячеек вычисляются на основании этого списка.</p>
 *
 * <p>Ширина ALS определяется как сумма ширин всех входящих в него
 * модулей.</p>
 */
public class ALS {

    /**
     * Наименование ALS.
     *
     * <p>Формируется автоматически.</p>
     */
    private String name;

    /**
     * Описание ALS.
     *
     * <p>Формируется автоматически.</p>
     */
    private String description;

    /**
     * Высота ALS, мм.
     */
    private int height;

    /**
     * Общая ширина ALS, мм.
     *
     * <p>Рассчитывается как сумма ширин модулей.</p>
     */
    private int width;

    /**
     * Глубина ALS, мм.
     */
    private int depth;

    /**
     * Размер верхней рамы, мм.
     */
    private int upperFrame;

    /**
     * Размер нижней рамы, мм.
     */
    private int bottomFrame;

    /**
     * Цвет корпуса ALS.
     */
    private Colors colorBody;

    /**
     * Цвет дверей ALS.
     */
    private Colors colorDoor;

    /**
     * Физический порядок модулей слева направо.
     */
    private final List<Module> modules = new ArrayList<>();

    /**
     * Создаёт ALS с заданной конфигурацией.
     *
     * @param height высота ALS, мм
     * @param depth глубина ALS, мм
     * @param upperFrame размер верхней рамы, мм
     * @param bottomFrame размер нижней рамы, мм
     * @param colorBody цвет корпуса
     * @param colorDoor цвет дверей
     * @param modules модули в физическом порядке слева направо
     * @throws NullPointerException если обязательное значение отсутствует
     * @throws IllegalStateException если структура ALS недопустима
     */
    public ALS(
            int height,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor,
            List<Module> modules
    ) {
        this.height = height;
        this.depth = depth;
        this.upperFrame = upperFrame;
        this.bottomFrame = bottomFrame;
        this.colorBody = Objects.requireNonNull(
                colorBody,
                "Цвет корпуса не должен быть null"
        );
        this.colorDoor = Objects.requireNonNull(
                colorDoor,
                "Цвет дверей не должен быть null"
        );

        Objects.requireNonNull(
                modules,
                "Список модулей не должен быть null"
        );

        for (Module module : modules) {
            this.modules.add(
                    Objects.requireNonNull(
                            module,
                            "Модуль не должен быть null"
                    )
            );
        }

        recalculate();
    }

    /**
     * Возвращает наименование ALS.
     *
     * @return наименование ALS
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает описание ALS.
     *
     * @return описание ALS
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает высоту ALS.
     *
     * @return высота ALS, мм
     */
    public int getHeight() {
        return height;
    }

    /**
     * Возвращает общую ширину ALS.
     *
     * @return ширина ALS, мм
     */
    public int getWidth() {
        return width;
    }

    /**
     * Возвращает глубину ALS.
     *
     * @return глубина ALS, мм
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Возвращает размер верхней рамы.
     *
     * @return размер верхней рамы, мм
     */
    public int getUpperFrame() {
        return upperFrame;
    }

    /**
     * Возвращает размер нижней рамы.
     *
     * @return размер нижней рамы, мм
     */
    public int getBottomFrame() {
        return bottomFrame;
    }

    /**
     * Возвращает цвет корпуса.
     *
     * @return цвет корпуса
     */
    public Colors getColorBody() {
        return colorBody;
    }

    /**
     * Возвращает цвет дверей.
     *
     * @return цвет дверей
     */
    public Colors getColorDoor() {
        return colorDoor;
    }

    /**
     * Возвращает модули ALS в физическом порядке слева направо.
     *
     * @return неизменяемый список модулей
     */
    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /**
     * Добавляет модуль в конец ALS.
     *
     * @param module добавляемый модуль
     * @throws NullPointerException если модуль не задан
     * @throws IllegalStateException если после добавления
     *                               структура ALS становится недопустимой
     */
    public void addModule(Module module) {
        Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        );

        modules.add(module);

        try {
            recalculate();
        } catch (RuntimeException e) {
            modules.remove(modules.size() - 1);
            throw e;
        }
    }

    /**
     * Вставляет модуль в заданную позицию.
     *
     * <p>Индекс {@code 0} соответствует крайнему левому положению.</p>
     *
     * @param index позиция модуля
     * @param module добавляемый модуль
     * @throws NullPointerException если модуль не задан
     * @throws IndexOutOfBoundsException если индекс недопустим
     * @throws IllegalStateException если после добавления
     *                               структура ALS становится недопустимой
     */
    public void addModule(int index, Module module) {
        Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        );

        modules.add(index, module);

        try {
            recalculate();
        } catch (RuntimeException e) {
            modules.remove(index);
            throw e;
        }
    }

    /**
     * Удаляет конкретный экземпляр модуля из ALS.
     *
     * <p>Удаление выполняется по идентичности объекта, а не через
     * {@link Object#equals(Object)}, поскольку в ALS могут находиться
     * несколько одинаковых модулей.</p>
     *
     * @param module удаляемый экземпляр модуля
     * @return {@code true}, если именно этот экземпляр был удалён
     * @throws NullPointerException если модуль не задан
     * @throws IllegalStateException если после удаления
     *                               структура ALS становится недопустимой
     */
    public boolean removeModule(Module module) {
        Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        );

        int index = findModuleIndexByIdentity(module);

        if (index < 0) {
            return false;
        }

        Module removedModule = modules.remove(index);

        try {
            recalculate();
            return true;
        } catch (RuntimeException e) {
            modules.add(index, removedModule);
            throw e;
        }
    }

    /**
     * Возвращает единственный модуль управления.
     *
     * @return модуль управления
     * @throws IllegalStateException если модуль управления отсутствует
     */
    public ControlModule getControlModule() {
        for (Module module : modules) {
            if (module instanceof ControlModule controlModule) {
                return controlModule;
            }
        }

        throw new IllegalStateException(
                "В ALS отсутствует модуль управления"
        );
    }

    /**
     * Возвращает storage-модули в их физическом порядке.
     *
     * @return неизменяемый список storage-модулей
     */
    public List<StorageModule> getStorageModules() {
        return modules.stream()
                .filter(StorageModule.class::isInstance)
                .map(StorageModule.class::cast)
                .toList();
    }

    /**
     * Возвращает общее количество ячеек во всех storage-модулях ALS.
     *
     * @return количество ячеек
     */
    public int getCountCells() {
        return getStorageModules().stream()
                .mapToInt(StorageModule::getCountCells)
                .sum();
    }

    /**
     * Возвращает количество модулей, равных указанному модулю.
     *
     * <p>Сравнение выполняется по {@link Object#equals(Object)}.</p>
     *
     * @param module модуль для поиска
     * @return количество равных модулей
     * @throws NullPointerException если модуль не задан
     */
    public int getCountSameModules(Module module) {
        Objects.requireNonNull(
                module,
                "Модуль не должен быть null"
        );

        return (int) modules.stream()
                .filter(module::equals)
                .count();
    }

    /**
     * Возвращает положение модуля управления.
     *
     * <p>Положение определяется индексом control-модуля
     * в физическом порядке ALS.</p>
     *
     * <p>Для ALS, состоящего только из одного LBC, положение
     * считается центральным.</p>
     *
     * @return положение модуля управления
     */
    public PositionLC getPositionLC() {
        int controlIndex = getControlModuleIndex();

        if (modules.size() == 1) {
            return PositionLC.CENTER;
        }

        if (controlIndex == 0) {
            return PositionLC.LEFT;
        }

        if (controlIndex == modules.size() - 1) {
            return PositionLC.RIGHT;
        }

        return PositionLC.CENTER;
    }

    /**
     * Пересчитывает производные характеристики ALS и входящих модулей.
     *
     * <p>Последовательность расчёта:</p>
     * <ol>
     *     <li>проверяется структура ALS;</li>
     *     <li>общие параметры ALS передаются модулям;</li>
     *     <li>определяются направления открытия дверей;</li>
     *     <li>пересчитываются модули;</li>
     *     <li>перенумеровываются ячейки;</li>
     *     <li>пересчитывается общая ширина ALS;</li>
     *     <li>обновляются наименование и описание ALS.</li>
     * </ol>
     */
    public void recalculate() {
        validateStructure();

        synchronizeModules();
        recalculateDoorDirections();

        for (Module module : modules) {
            module.recalculate();
        }

        renumberCells();
        recalculateWidth();
        updateNameAndDescription();
    }

    /**
     * Передаёт модулям общие конструктивные параметры ALS.
     */
    private void synchronizeModules() {
        for (Module module : modules) {
            module.setHeight(height);
            module.setDepth(depth);
            module.setUpperFrame(upperFrame);
            module.setBottomFrame(bottomFrame);
            module.setColorBody(colorBody);
            module.setColorDoor(colorDoor);
        }
    }

    /**
     * Пересчитывает общую ширину ALS.
     */
    private void recalculateWidth() {
        width = modules.stream()
                .mapToInt(Module::getWidth)
                .sum();
    }

    /**
     * Рассчитывает направления открытия дверей.
     *
     * <p>Для обычных storage-модулей:</p>
     * <ul>
     *     <li>слева от control-модуля — двери открываются влево;</li>
     *     <li>справа от control-модуля — двери открываются вправо.</li>
     * </ul>
     *
     * <p>Для LBC, который одновременно является control- и
     * storage-модулем:</p>
     * <ul>
     *     <li>слева — двери открываются вправо;</li>
     *     <li>по центру — двери открываются влево;</li>
     *     <li>справа — двери открываются влево.</li>
     * </ul>
     */
    private void recalculateDoorDirections() {
        int controlIndex = getControlModuleIndex();
        PositionLC position = getPositionLC();

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            if (!(module instanceof StorageModule storageModule)) {
                continue;
            }

            /*
             * LBC одновременно является ControlModule и StorageModule,
             * поэтому для него действует отдельное правило.
             */
            if (module instanceof ControlModule) {
                switch (position) {
                    case LEFT ->
                            storageModule.setDirectionDoorOpening(
                                    DirectionDoorOpening.RIGHT
                            );

                    case CENTER, RIGHT ->
                            storageModule.setDirectionDoorOpening(
                                    DirectionDoorOpening.LEFT
                            );
                }

                continue;
            }

            /*
             * Обычный storage-модуль открывается в сторону,
             * противоположную положению control-модуля относительно него.
             */
            if (i < controlIndex) {
                storageModule.setDirectionDoorOpening(
                        DirectionDoorOpening.LEFT
                );
            } else if (i > controlIndex) {
                storageModule.setDirectionDoorOpening(
                        DirectionDoorOpening.RIGHT
                );
            }
        }
    }

    /**
     * Возвращает индекс control-модуля.
     *
     * @return индекс control-модуля
     * @throws IllegalStateException если control-модуль отсутствует
     */
    private int getControlModuleIndex() {
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i) instanceof ControlModule) {
                return i;
            }
        }

        throw new IllegalStateException(
                "В ALS отсутствует модуль управления"
        );
    }

    /**
     * Нумерует ячейки последовательно слева направо.
     *
     * <p>Нумерация начинается с единицы и зависит от физического
     * порядка модулей в ALS.</p>
     */
    private void renumberCells() {
        int number = 1;

        for (Module module : modules) {
            if (!(module instanceof StorageModule storageModule)) {
                continue;
            }

            for (StorageCell cell : storageModule.getCells()) {
                cell.setNumber(number++);
            }
        }
    }

    /**
     * Формирует производные наименование и описание ALS.
     */
    private void updateNameAndDescription() {
        name = "АКХ на " + getCountCells() + " ячеек";

        Module controlModule = getControlModuleAsModule();

        description =
                "АКХ на "
                        + getCountCells()
                        + " ячеек, ВхШхГ, мм: "
                        + height + "x"
                        + width + "x"
                        + depth
                        + "; Цвет: "
                        + colorBody + "/"
                        + colorDoor
                        + "; Модулей хранения: "
                        + getStorageModules().size()
                        + " шт.;\n"
                        + controlModule.getDescription();
    }

    /**
     * Возвращает control-модуль как базовый Module.
     *
     * <p>В текущей domain-модели каждый ControlModule также является
     * Module (LC или LBC), поэтому поиск выполняется среди списка
     * Module без изменения интерфейса ControlModule.</p>
     *
     * @return control-модуль
     * @throws IllegalStateException если control-модуль отсутствует
     */
    private Module getControlModuleAsModule() {
        for (Module module : modules) {
            if (module instanceof ControlModule) {
                return module;
            }
        }

        throw new IllegalStateException(
                "В ALS отсутствует модуль управления"
        );
    }

    /**
     * Проверяет структуру ALS.
     *
     * <p>В ALS должен находиться ровно один ControlModule
     * и хотя бы один StorageModule.</p>
     *
     * @throws IllegalStateException если структура ALS недопустима
     */
    private void validateStructure() {
        long controlModules = modules.stream()
                .filter(ControlModule.class::isInstance)
                .count();

        long storageModules = modules.stream()
                .filter(StorageModule.class::isInstance)
                .count();

        if (controlModules != 1) {
            throw new IllegalStateException(
                    "ALS должен содержать ровно один модуль управления"
            );
        }

        if (storageModules == 0) {
            throw new IllegalStateException(
                    "ALS должен содержать хотя бы один модуль хранения"
            );
        }
    }

    /**
     * Ищет индекс конкретного экземпляра модуля.
     *
     * <p>Используется сравнение по {@code ==}, а не по equals(),
     * поскольку одинаковые модули могут присутствовать в ALS
     * одновременно как разные экземпляры.</p>
     *
     * @param module искомый экземпляр
     * @return индекс экземпляра или {@code -1}, если он отсутствует
     */
    private int findModuleIndexByIdentity(Module module) {
        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i) == module) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Возвращает подробное строковое представление ALS.
     *
     * @return строковое представление ALS
     */
    @Override
    public String toString() {
        return "ALS{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", height=" + height +
                ", width=" + width +
                ", depth=" + depth +
                ", upperFrame=" + upperFrame +
                ", bottomFrame=" + bottomFrame +
                ", colorBody=" + colorBody +
                ", colorDoor=" + colorDoor +
                ", positionLC=" + getPositionLC() +
                ", countCells=" + getCountCells() +
                ", modules=" + modules +
                '}';
    }

    /**
     * Сравнивает ALS по конфигурации.
     *
     * <p>Порядок модулей имеет значение, поскольку определяет
     * физическую конфигурацию ALS.</p>
     *
     * <p>Ширина, наименование, описание и положение control-модуля
     * отдельно не сравниваются, поскольку являются производными
     * значениями.</p>
     *
     * @param o объект для сравнения
     * @return {@code true}, если конфигурации ALS совпадают
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ALS other)) {
            return false;
        }

        return height == other.height
                && depth == other.depth
                && upperFrame == other.upperFrame
                && bottomFrame == other.bottomFrame
                && colorBody == other.colorBody
                && colorDoor == other.colorDoor
                && Objects.equals(modules, other.modules);
    }

    /**
     * Возвращает хэш-код конфигурации ALS.
     *
     * @return хэш-код конфигурации
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                height,
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                modules
        );
    }
}