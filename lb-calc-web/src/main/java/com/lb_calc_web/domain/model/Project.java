package com.lb_calc_web.domain.model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Доменная модель проекта.
 *
 * <p>Проект объединяет набор конфигураций {@link ALS}.
 * Одинаковые конфигурации ALS не хранятся отдельными записями —
 * для них хранится количество.</p>
 *
 * <p>Например:</p>
 * <pre>
 * ALS №1 → 3 шт.
 * ALS №2 → 1 шт.
 * </pre>
 *
 * <p>Состав проекта, количество ALS и описание проекта относятся
 * к бизнес-логике и поэтому находятся в domain-слое.</p>
 *
 * <p>Класс не зависит от Spring, JPA, DTO и Repository.</p>
 */
public class Project {

    /** Идентификатор сохранённого проекта. */
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Название проекта.
     */
    private String name;

    /**
     * Автоматически формируемое описание проекта.
     */
    private String description;

    /**
     * Компания, для которой создаётся проект.
     */
    private final String company;

    /**
     * Дата создания проекта.
     */
    private final LocalDate createdAt;

    /**
     * Дата последнего изменения проекта.
     */
    private LocalDate updatedAt;

    /**
     * Сотрудник, создавший проект.
     */
    private final Employee createdBy;

    /**
     * Сотрудник, выполнивший последнее изменение.
     */
    private Employee updatedBy;

    /**
     * Конфигурации ALS проекта и их количество.
     *
     * <p>Порядок ALS внутри проекта не имеет бизнес-значения.
     * LinkedHashMap используется для стабильного порядка обхода
     * при формировании отображаемого представления.</p>
     */
    private final Map<ALS, Integer> quantityALS =
            new LinkedHashMap<>();

    /**
     * Создаёт новый проект.
     *
     * <p>При создании updatedAt и updatedBy устанавливаются
     * такими же, как createdAt и createdBy, поскольку создание
     * проекта одновременно является его первым изменением.</p>
     *
     * @param name название проекта
     * @param company компания
     * @param createdAt дата создания
     * @param createdBy сотрудник, создавший проект
     * @throws NullPointerException если обязательный параметр равен null
     */
    public Project(
            String name,
            String company,
            LocalDate createdAt,
            Employee createdBy
    ) {
        this.name = Objects.requireNonNull(
                name,
                "Название проекта не должно быть null"
        );

        this.company = Objects.requireNonNull(
                company,
                "Компания не должна быть null"
        );

        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Дата создания не должна быть null"
        );

        this.createdBy = Objects.requireNonNull(
                createdBy,
                "Создатель проекта не должен быть null"
        );

        this.updatedAt = createdAt;
        this.updatedBy = createdBy;

        updateDescription();
    }

    /**
     * Добавляет одну единицу указанной конфигурации ALS в проект.
     *
     * <p>Если такая конфигурация уже существует,
     * её количество увеличивается на единицу.</p>
     *
     * @param als конфигурация ALS
     * @throws NullPointerException если ALS равен null
     */
    public void addALS(ALS als) {
        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        quantityALS.merge(
                als.copy(),
                1,
                Math::addExact
        );

        update();
    }

    /**
     * Удаляет одну единицу указанной конфигурации ALS.
     *
     * <p>Если количество больше единицы, оно уменьшается на единицу.
     * Если количество равно единице, конфигурация полностью удаляется
     * из проекта.</p>
     *
     * <p>Если такой ALS отсутствует в проекте, метод ничего не меняет.</p>
     *
     * @param als конфигурация ALS
     * @throws NullPointerException если ALS равен null
     */
    public void removeALS(ALS als) {
        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        Integer quantity =
                findQuantity(als);

        if (quantity == null) {
            return;
        }

        if (quantity > 1) {
            replaceQuantity(als, quantity - 1);
        } else {
            removeConfiguration(als);
        }

        update();
    }

    /**
     * Заменяет одну единицу конфигурации ALS другой.
     *
     * <p>Если исходная конфигурация представлена в проекте несколькими
     * одинаковыми экземплярами, количество уменьшается только на единицу.
     * Остальные экземпляры сохраняются. Если новая конфигурация уже есть
     * в проекте, её количество увеличивается на единицу.</p>
     *
     * @param oldALS конфигурация ALS, которую необходимо заменить
     * @param newALS новая конфигурация ALS
     * @throws NullPointerException если один из параметров равен null
     * @throws IllegalArgumentException если исходная конфигурация отсутствует
     */
    public void replaceALS(
            ALS oldALS,
            ALS newALS
    ) {
        Objects.requireNonNull(
                oldALS,
                "Старый ALS не должен быть null"
        );

        Objects.requireNonNull(
                newALS,
                "Новый ALS не должен быть null"
        );

        Integer quantity = findQuantity(oldALS);

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "ALS не найден в проекте"
            );
        }

        /*
         * Проверяем переполнение до изменения исходного состава.
         * Это сохраняет атомарность операции замены.
         */
        if (!oldALS.equals(newALS)) {
            Math.addExact(getQuantity(newALS), 1);
        }

        if (quantity > 1) {
            replaceQuantity(oldALS, quantity - 1);
        } else {
            removeConfiguration(oldALS);
        }

        quantityALS.merge(
                newALS.copy(),
                1,
                Math::addExact
        );

        update();
    }

    /**
     * Возвращает количество указанной конфигурации ALS в проекте.
     *
     * @param als конфигурация ALS
     * @return количество ALS или {@code 0}, если конфигурация отсутствует
     * @throws NullPointerException если ALS равен null
     */
    public int getQuantity(ALS als) {
        Objects.requireNonNull(
                als,
                "ALS не должен быть null"
        );

        Integer quantity = findQuantity(als);
        return quantity == null ? 0 : quantity;
    }

    /**
     * Возвращает состав проекта.
     *
     * <p>Возвращаемая карта доступна только для чтения.
     * Изменять состав проекта необходимо через методы
     * {@link #addALS(ALS)}, {@link #removeALS(ALS)}
     * и {@link #replaceALS(ALS, ALS)}.</p>
     *
     * @return неизменяемое представление конфигураций ALS и их количества
     */
    public Map<ALS, Integer> getQuantityALS() {
        Map<ALS, Integer> snapshot = new LinkedHashMap<>();

        quantityALS.forEach((als, quantity) ->
                snapshot.put(als.copy(), quantity)
        );

        return Collections.unmodifiableMap(snapshot);
    }

    /**
     * Устанавливает название проекта.
     *
     * <p>Изменение названия само по себе не пересчитывает
     * автоматически другие производные данные проекта.</p>
     *
     * @param name новое название
     * @throws NullPointerException если название равно null
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(
                name,
                "Название проекта не должно быть null"
        );

        update();
    }

    /**
     * Устанавливает сотрудника, выполнившего изменение проекта.
     *
     * <p>Изменение автора автоматически обновляет updatedAt.</p>
     *
     * @param updatedBy сотрудник, выполнивший изменение
     * @throws NullPointerException если сотрудник равен null
     */
    public void setUpdatedBy(Employee updatedBy) {
        this.updatedBy = Objects.requireNonNull(
                updatedBy,
                "Автор изменения не должен быть null"
        );

        update();
    }

    /**
     * Возвращает название проекта.
     *
     * @return название проекта
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает автоматически сформированное описание проекта.
     *
     * @return описание проекта
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает компанию проекта.
     *
     * @return компания
     */
    public String getCompany() {
        return company;
    }

    /**
     * Возвращает дату создания проекта.
     *
     * @return дата создания
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Возвращает дату последнего изменения проекта.
     *
     * @return дата последнего изменения
     */
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Возвращает сотрудника, создавшего проект.
     *
     * @return создатель проекта
     */
    public Employee getCreatedBy() {
        return createdBy;
    }

    /**
     * Возвращает сотрудника, выполнившего последнее изменение.
     *
     * @return автор последнего изменения
     */
    public Employee getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Ищет количество конфигурации без зависимости от hashCode
     * переданного извне объекта.
     */
    private Integer findQuantity(ALS target) {
        return quantityALS.entrySet().stream()
                .filter(entry -> entry.getKey().equals(target))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * Удаляет конфигурацию по её бизнес-содержимому.
     */
    private Integer removeConfiguration(ALS target) {
        var iterator = quantityALS.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<ALS, Integer> entry = iterator.next();

            if (entry.getKey().equals(target)) {
                Integer quantity = entry.getValue();
                iterator.remove();
                return quantity;
            }
        }

        return null;
    }

    /**
     * Обновляет количество, сохраняя внутренний ключ неизменным.
     */
    private void replaceQuantity(ALS target, int quantity) {
        for (Map.Entry<ALS, Integer> entry : quantityALS.entrySet()) {
            if (entry.getKey().equals(target)) {
                entry.setValue(quantity);
                return;
            }
        }

        throw new IllegalStateException(
                "Конфигурация ALS не найдена при обновлении количества"
        );
    }

    /**
     * Фиксирует изменение проекта.
     *
     * <p>При изменении проекта обновляется дата последнего изменения
     * и пересчитывается описание.</p>
     */
    private void update() {
        updatedAt = LocalDate.now();
        updateDescription();
    }

    /**
     * Пересчитывает описание проекта на основании его состава.
     *
     * <p>Описание не является самостоятельным источником данных.
     * Оно полностью строится из {@link #quantityALS}.</p>
     */
    private void updateDescription() {
        StringBuilder builder = new StringBuilder();
        int totalQuantity = 0;
        int totalCells = 0;
        int index = 1;

        var configurations = quantityALS.entrySet().stream()
                .sorted(java.util.Comparator
                        .comparing((Map.Entry<ALS, Integer> entry) -> entry.getKey().getName())
                        .thenComparing(entry -> entry.getKey().getDescription()))
                .toList();

        for (Map.Entry<ALS, Integer> entry : configurations) {
            ALS als = entry.getKey();
            int quantity = entry.getValue();
            int cellCount = als.getCountCells();

            if (!builder.isEmpty()) {
                builder.append("\n\n");
            }

            builder.append(index++)
                    .append(". ")
                    .append(als.getName())
                    .append(" — ")
                    .append(quantity)
                    .append(" шт.\n   ")
                    .append(formatCellCount(cellCount));

            if (quantity > 1) {
                builder.append(" в каждой АКХ");
            }
            builder.append('.');

            totalQuantity = Math.addExact(totalQuantity, quantity);
            totalCells = Math.addExact(
                    totalCells,
                    Math.multiplyExact(cellCount, quantity)
            );
        }

        if (!builder.isEmpty()) {
            builder.append("\n\n");
        }

        builder.append("Всего: ")
                .append(totalQuantity)
                .append(" АКХ, ")
                .append(formatCellCount(totalCells))
                .append('.');

        description = builder.toString();
    }

    /**
     * Формирует количество ячеек с правильной формой существительного.
     *
     * @param count количество ячеек
     * @return количество и слово «ячейка» в соответствующей форме
     */
    private String formatCellCount(int count) {
        return count + " " + formatQuantityWord(
                count,
                "ячейка",
                "ячейки",
                "ячеек"
        );
    }

    /**
     * Выбирает форму существительного для числительного.
     *
     * @param count количество
     * @param singular форма для одного предмета
     * @param paucal форма для двух, трёх или четырёх предметов
     * @param plural форма для остальных значений
     * @return подходящая форма существительного
     */
    private String formatQuantityWord(
            int count,
            String singular,
            String paucal,
            String plural
    ) {
        int lastTwoDigits = Math.abs(count % 100);
        int lastDigit = Math.abs(count % 10);

        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return plural;
        }
        if (lastDigit == 1) {
            return singular;
        }
        if (lastDigit >= 2 && lastDigit <= 4) {
            return paucal;
        }
        return plural;
    }

    /**
     * Восстанавливает проект из сохранённого состояния.
     *
     * <p>Метод используется persistence/application-слоем,
     * когда объект необходимо собрать из уже существующих данных БД.</p>
     *
     * <p>В отличие от обычного конструктора метод позволяет восстановить
     * исходные значения updatedAt и updatedBy, не считая восстановление
     * новым изменением проекта.</p>
     *
     * @param name название проекта
     * @param company компания
     * @param createdAt дата создания
     * @param createdBy создатель
     * @param updatedAt дата последнего изменения
     * @param updatedBy автор последнего изменения
     * @param quantityALS состав проекта
     * @return восстановленный проект
     * @throws NullPointerException если обязательное значение равно null
     * @throws IllegalArgumentException если количество ALS меньше единицы
     */
    public static Project restore(
            String name,
            String company,
            LocalDate createdAt,
            Employee createdBy,
            LocalDate updatedAt,
            Employee updatedBy,
            Map<ALS, Integer> quantityALS
    ) {
        Objects.requireNonNull(
                name,
                "Название проекта не должно быть null"
        );

        Objects.requireNonNull(
                company,
                "Компания не должна быть null"
        );

        Objects.requireNonNull(
                createdAt,
                "Дата создания не должна быть null"
        );

        Objects.requireNonNull(
                createdBy,
                "Создатель проекта не должен быть null"
        );

        Objects.requireNonNull(
                updatedAt,
                "Дата изменения не должна быть null"
        );

        Objects.requireNonNull(
                updatedBy,
                "Автор изменения не должен быть null"
        );

        Objects.requireNonNull(
                quantityALS,
                "Состав ALS не должен быть null"
        );

        Project project =
                new Project(
                        name,
                        company,
                        createdAt,
                        createdBy
                );

        project.updatedAt =
                updatedAt;

        project.updatedBy =
                updatedBy;

        for (Map.Entry<ALS, Integer> entry :
                quantityALS.entrySet()) {

            ALS als =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "ALS не должен быть null"
                    );

            Integer quantity =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "Количество ALS не должно быть null"
                    );

            if (quantity < 1) {
                throw new IllegalArgumentException(
                        "Количество ALS должно быть больше нуля"
                );
            }

            project.quantityALS.merge(
                    als.copy(),
                    quantity,
                    Math::addExact
            );
        }

        project.updateDescription();

        return project;
    }
}