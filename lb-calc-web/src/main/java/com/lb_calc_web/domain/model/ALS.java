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
                    ).copy()
            );
        }

        recalculate();
    }

    /**
     * Создаёт полную независимую копию ALS.
     *
     * <p>Каждый модуль и его изменяемое внутреннее состояние
     * копируются отдельно. Копия может изменяться без влияния
     * на исходную ALS.</p>
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
     * Возвращает независимые копии модулей ALS в физическом порядке.
     *
     * <p>Изменение возвращённых объектов не влияет на агрегат.</p>
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
     * <p>Для добавления нового модуля хранения с автоматическим
     * определением стороны следует использовать
     * {@link #addStorageModule(StorageModule)}.</p>
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
     * <p>Если управляющий модуль находится слева, новый модуль
     * добавляется справа от него; если справа — слева. При центральном
     * положении модуль добавляется на менее заполненную сторону.
     * При равном количестве модулей хранения он добавляется справа.</p>
     *
     * @param module добавляемый модуль хранения
     * @throws IllegalArgumentException если передан не самостоятельный
     *                                  модуль хранения
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
     * <p>Если в ALS присутствуют одинаковые модули и требуется удалить
     * конкретную физическую позицию, следует использовать
     * {@link #removeModuleAt(int)}.</p>
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
     * Удаляет модуль хранения по его физическому индексу в списке ALS.
     *
     * <p>Удаление управляющего модуля запрещено. Для удаления LBC
     * требуется отдельная доменная операция, поскольку он выполняет
     * одновременно две роли.</p>
     *
     * @param moduleIndex физический индекс модуля слева направо
     * @throws IndexOutOfBoundsException если индекс выходит за границы
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
     * <p>При исключении исходный агрегат и его модули остаются
     * неизменными.</p>
     *
     * @param operation операция изменения кандидата
     */
    private void mutate(java.util.function.Consumer<ALS> operation) {
        ALS candidate = copy();
        operation.accept(candidate);
        candidate.recalculate();
        replaceState(candidate);
    }

    /**
     * Переносит проверенное состояние кандидата в текущий агрегат.
     */
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
     * <p>Замена проверяется на независимой копии ALS. Если после замены
     * нарушается структура агрегата или расчёт характеристик модулей,
     * исходная конфигурация остаётся неизменной.</p>
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
     * @return модуль управления
     * @throws IllegalStateException если модуль управления отсутствует
     */
    public ControlModule getControlModule() {
        return (ControlModule) getControlModuleAsModule().copy();
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
                .map(module -> (StorageModule) module.copy())
                .toList();
    }

    /**
     * Возвращает общее количество ячеек во всех storage-модулях ALS.
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
     * Возвращает минимальную глубину ячейки среди модулей хранения ALS.
     *
     * @return минимальная глубина ячейки, мм, или {@code 0}, если ячеек нет
     */
    public int getDepthCell() {
        return modules.stream()
                .filter(StorageModule.class::isInstance)
                .map(StorageModule.class::cast)
                .mapToInt(StorageModule::getCellDepth)
                .min()
                .orElse(0);
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
        PositionControlModule position = getPositionControlModule();

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
                        + " шт.;\n"
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