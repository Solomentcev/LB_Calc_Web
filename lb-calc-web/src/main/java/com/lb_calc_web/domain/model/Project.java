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
    private String company;

    /**
     * Дата создания проекта.
     */
    private LocalDate createdAt;

    /**
     * Дата последнего изменения проекта.
     */
    private LocalDate updatedAt;

    /**
     * Сотрудник, создавший проект.
     */
    private Employee createdBy;

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
                als,
                1,
                Integer::sum
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
                quantityALS.get(als);

        if (quantity == null) {
            return;
        }

        if (quantity > 1) {
            quantityALS.put(
                    als,
                    quantity - 1
            );
        } else {
            quantityALS.remove(als);
        }

        update();
    }

    /**
     * Заменяет одну конфигурацию ALS другой.
     *
     * <p>Количество старой конфигурации переносится на новую.
     * Если новая конфигурация уже присутствует в проекте,
     * её существующее количество увеличивается.</p>
     *
     * @param oldALS конфигурация ALS, которую необходимо заменить
     * @param newALS новая конфигурация ALS
     * @throws NullPointerException если один из параметров равен null
     * @throws IllegalArgumentException если oldALS отсутствует в проекте
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

        Integer quantity =
                quantityALS.remove(oldALS);

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "ALS не найден в проекте"
            );
        }

        quantityALS.merge(
                newALS,
                quantity,
                Integer::sum
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

        return quantityALS.getOrDefault(
                als,
                0
        );
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
        return Collections.unmodifiableMap(
                quantityALS
        );
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
        StringBuilder builder =
                new StringBuilder();

        for (Map.Entry<ALS, Integer> entry :
                quantityALS.entrySet()) {

            builder
                    .append(entry.getKey().getName())
                    .append(" - ")
                    .append(entry.getValue())
                    .append(" шт.\n");
        }

        description =
                builder.toString();
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
                    als,
                    quantity,
                    Integer::sum
            );
        }

        project.updateDescription();

        return project;
    }
}