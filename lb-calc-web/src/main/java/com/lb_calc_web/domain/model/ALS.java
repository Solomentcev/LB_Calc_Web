package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.PositionControlModule;

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

    /** Наименование ALS, формируемое автоматически. */
    private String name;

    /** Описание ALS, формируемое автоматически. */
    private String description;

    /** Высота ALS, мм. */
    private int height;

    /** Общая ширина ALS, мм. */
    private int width;

    /** Глубина ALS, мм. */
    private int depth;

    /** Размер верхней рамы, мм. */
    private int upperFrame;

    /** Размер нижней рамы, мм. */
    private int bottomFrame;

    /** Цвет корпуса ALS. */
    private Colors colorBody;

    /** Цвет дверей ALS. */
    private Colors colorDoor;

    /** Физический порядок модулей слева направо. */
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

        Objects.requireNonNull(modules, "Список модулей не должен быть null");

        for (Module module : modules) {
            this.modules.add(Objects.requireNonNull(
                    module,
                    "Модуль не должен быть null"
            ).copy());
        }

        recalculate();
    }

    /**
     * Создаёт полную независимую копию ALS.
     *
     * <p>Конструктор самостоятельно копирует каждый переданный модуль,
     * поэтому передача внутреннего списка не нарушает инкапсуляцию
     * и не требует предварительного копирования.</p>
     *
     * @return независимая копия ALS
     */
    public ALS copy() {
        return new ALS(
                height,
                depth,
                upperFrame,
                bottomFrame,
                colorBody,
                colorDoor,
                modules
        );
    }

    /** @return наименование ALS */
    public String getName() {
        return name;
    }

    /** @return описание ALS */
    public String getDescription() {
        return description;
    }

    /** @return высота ALS, мм */
    public int getHeight() {
        return height;
    }

    /** @return общая ширина ALS, мм */
    public int getWidth() {
        return width;
    }

    /** @return глубина ALS, мм */
    public int getDepth() {
        return depth;
    }

    /** @return размер верхней рамы, мм */
    public int getUpperFrame() {
        return upperFrame;
    }

    /** @return размер нижней рамы, мм */
    public int getBottomFrame() {
        return bottomFrame;
    }

    /** @return цвет корпуса */
    public Colors getColorBody() {
        return colorBody;
    }

    /** @return цвет дверей */
    public Colors getColorDoor() {
        return colorDoor;
    }

    /**
     * Возвращает независимые копии модулей ALS в физическом порядке.
     *
     * @return неизменяемый список копий модулей
     */
    public List<Module> getModules() {
        return modules.stream()
                .map(Module::copy)
                .toList();
    }

    /**
     * Добавляет независимую копию модуля в конец ALS.
     *
     * @param module добавляемый модуль
     */
    public void addModule(Module module) {
        addModule(modules.size(), module);
    }

    /**
     * Вставляет независимую копию модуля в заданную позицию.
     *
     * @param index позиция модуля
     * @param module добавляемый модуль
     */
    public void addModule(int index, Module module) {
        Objects.requireNonNull(module, "Модуль не должен быть null");
        Module moduleCopy = module.copy();

        mutate(candidate -> candidate.modules.add(index, moduleCopy));
    }

    /**
     * Добавляет модуль хранения со стороны, определяемой положением
     * управляющего модуля.
     *
     * @param module добавляемый модуль хранения
     * @throws IllegalArgumentException если передан LBC
     */
    public void addStorageModule(StorageModule module) {
        Objects.requireNonNull(module, "Модуль хранения не должен быть null");

        if (module instanceof ControlModule) {
            throw new IllegalArgumentException(
                    "Для добавления LBC требуется отдельная операция замены "
                            + "модуля управления"
            );
        }

        Module moduleCopy = module.copy();

        mutate(candidate -> {
            int controlIndex = candidate.getControlModuleIndex();
            PositionControlModule position =
                    candidate.getPositionControlModule();

            int insertionIndex;

            switch (position) {
                case LEFT -> insertionIndex = controlIndex + 1;
                case RIGHT -> insertionIndex = controlIndex;
                case CENTER -> {
                    int leftCount = controlIndex;
                    int rightCount =
                            candidate.modules.size() - controlIndex - 1;
                    insertionIndex = leftCount < rightCount
                            ? controlIndex
                            : controlIndex + 1;
                }
                default -> throw new IllegalStateException(
                        "Неизвестное положение модуля управления"
                );
            }

            candidate.modules.add(insertionIndex, moduleCopy);
        });
    }

    /**
     * Удаляет первый модуль, равный указанному по конфигурации.
     *
     * @param module конфигурация удаляемого модуля
     * @return {@code true}, если равный модуль найден и удалён
     */
    public boolean removeModule(Module module) {
        Objects.requireNonNull(module, "Модуль не должен быть null");

        for (int i = 0; i < modules.size(); i++) {
            if (modules.get(i).equals(module)) {
                removeModuleAt(i);
                return true;
            }
        }

        return false;
    }

    /**
     * Удаляет модуль хранения по его физическому индексу.
     *
     * @param moduleIndex физический индекс модуля слева направо
     * @throws IllegalArgumentException если выбран управляющий модуль
     */
    public void removeModuleAt(int moduleIndex) {
        Module current = modules.get(moduleIndex);

        if (current instanceof ControlModule) {
            throw new IllegalArgumentException(
                    "Нельзя удалить управляющий модуль отдельно от его роли"
            );
        }

        mutate(candidate -> candidate.modules.remove(moduleIndex));
    }

    /**
     * Заменяет модуль по физическому индексу, сохраняя его доменные роли.
     *
     * @param moduleIndex физический индекс модуля слева направо
     * @param replacement новый модуль
     * @throws IllegalArgumentException если замена меняет роли модуля
     */
    public void replaceModuleAt(int moduleIndex, Module replacement) {
        Objects.requireNonNull(replacement, "Новый модуль не должен быть null");

        Module current = modules.get(moduleIndex);

        boolean currentControl = current instanceof ControlModule;
        boolean replacementControl = replacement instanceof ControlModule;
        boolean currentStorage = current instanceof StorageModule;
        boolean replacementStorage = replacement instanceof StorageModule;

        if (currentControl != replacementControl
                || currentStorage != replacementStorage) {
            throw new IllegalArgumentException(
                    "При замене модуля необходимо сохранить его доменные роли"
            );
        }

        Module replacementCopy = replacement.copy();
        mutate(candidate -> candidate.modules.set(moduleIndex, replacementCopy));
    }

    /**
     * Выполняет изменение на независимом кандидате и публикует его
     * состояние только после успешной проверки и пересчёта.
     *
     * @param operation операция изменения кандидата
     */
    private void mutate(java.util.function.Consumer<ALS> operation) {
        ALS candidate = copy();
        operation.accept(candidate);
        candidate.recalculate();
        replaceState(candidate);
    }

    /** Переносит проверенное состояние кандидата в текущий агрегат. */
    private void replaceState(ALS candidate) {
        modules.clear();
        candidate.modules.stream()
                .map(Module::copy)
                .forEach(modules::add);

        width = candidate.width;
        name = candidate.name;
        description = candidate.description;
    }

    /**
     * Заменяет управляющий модуль, сохраняя его физическую позицию.
     *
     * @param controlModule новый управляющий модуль
     */
    public void replaceControlModule(ControlModule controlModule) {
        Objects.requireNonNull(
                controlModule,
                "Модуль управления не должен быть null"
        );

        if (!(controlModule instanceof Module replacement)) {
            throw new IllegalArgumentException(
                    "Управляющий модуль должен быть модулем ALS"
            );
        }

        Module replacementCopy = replacement.copy();

        mutate(candidate -> {
            int controlIndex = candidate.getControlModuleIndex();
            candidate.modules.set(controlIndex, replacementCopy);
        });
    }

    /**
     * Возвращает единственный модуль управления.
     *
     * @return независимая копия модуля управления
     */
    public ControlModule getControlModule() {
        return (ControlModule) getControlModuleAsModule().copy();
    }

    /**
     * Возвращает storage-модули в их физическом порядке.
     *
     * @return неизменяемый список независимых копий storage-модулей
     */
    public List<StorageModule> getStorageModules() {
        return modules.stream()
                .filter(StorageModule.class::isInstance)
                .map(StorageModule.class::cast)
                .map(module -> (StorageModule) module.copy())
                .toList();
    }

    /**
     * Возвращает общее количество ячеек без создания копий модулей.
     *
     * @return количество ячеек
     */
    public int getCountCells() {
        return modules.stream()
                .filter(StorageModule.class::isInstance)
                .map(StorageModule.class::cast)
                .mapToInt(StorageModule::getCountCells)
                .sum();
    }

    /**
     * Возвращает количество модулей, равных указанному модулю.
     *
     * @param module модуль для поиска
     * @return количество равных модулей
     */
    public int getCountSameModules(Module module) {
        Objects.requireNonNull(module, "Модуль не должен быть null");

        return (int) modules.stream()
                .filter(module::equals)
                .count();
    }

    /**
     * Возвращает положение модуля управления.
     *
     * @return положение модуля управления
     */
    public PositionControlModule getPositionControlModule() {
        int controlIndex = getControlModuleIndex();

        if (modules.size() == 1) {
            return PositionControlModule.CENTER;
        }

        if (controlIndex == 0) {
            return PositionControlModule.LEFT;
        }

        if (controlIndex == modules.size() - 1) {
            return PositionControlModule.RIGHT;
        }

        return PositionControlModule.CENTER;
    }

    /**
     * Обратная совместимость со старым именем.
     */
    @Deprecated
    public com.lb_calc_web.domain.attributes.PositionLC getPositionLC() {
        return com.lb_calc_web.domain.attributes.PositionLC.valueOf(
                getPositionControlModule().name()
        );
    }

    /**
     * Пересчитывает производные характеристики ALS и входящих модулей.
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

    /** Передаёт модулям общие конструктивные параметры ALS. */
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

    /** Пересчитывает общую ширину ALS. */
    private void recalculateWidth() {
        width = modules.stream()
                .mapToInt(Module::getWidth)
                .sum();
    }

    /**
     * Рассчитывает направления открытия дверей на основании
     * физического положения модулей.
     */
    private void recalculateDoorDirections() {
        int controlIndex = getControlModuleIndex();
        PositionControlModule position = getPositionControlModule();

        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);

            if (!(module instanceof StorageModule storageModule)) {
                continue;
            }

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

    /** Нумерует ячейки последовательно слева направо. */
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

    /** Формирует производные наименование и описание ALS. */
    private void updateNameAndDescription() {
        int countCells = getCountCells();
        int storageModuleCount = getStorageModuleCount();

        name = "АКХ на " + countCells + " ячеек";

        Module controlModule = getControlModuleAsModule();

        description =
                "АКХ на "
                        + countCells
                        + " ячеек, ВхШхГ, мм: "
                        + height + "x"
                        + width + "x"
                        + depth
                        + "; Цвет: "
                        + colorBody + "/"
                        + colorDoor
                        + "; Модулей хранения: "
                        + storageModuleCount
                        + " шт.;\\n"
                        + controlModule.getDescription();
    }

    /**
     * Возвращает количество storage-модулей без создания их копий.
     *
     * @return количество модулей хранения
     */
    private int getStorageModuleCount() {
        return (int) modules.stream()
                .filter(StorageModule.class::isInstance)
                .count();
    }

    /**
     * Возвращает control-модуль как базовый Module.
     *
     * @return control-модуль
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
     * Проверяет структуру ALS: один модуль управления и минимум
     * один модуль хранения.
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
