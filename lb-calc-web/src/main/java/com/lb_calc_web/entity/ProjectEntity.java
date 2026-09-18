package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA-сущность проекта.
 *
 * <p>Представляет запись проекта в таблице {@code project}.</p>
 *
 * <p>Сущность отвечает только за persistence-представление проекта
 * и не содержит бизнес-логики. Бизнес-операции с составом проекта
 * выполняются в {@code domain.model.Project}.</p>
 *
 * <p>Связь проекта с ALS реализована через отдельную сущность
 * {@link ProjectALSEntity}, поскольку вместе со ссылкой на ALS
 * необходимо хранить количество данной конфигурации.</p>
 */
@Entity
@Table(name = "project")
public class ProjectEntity {

    /**
     * Идентификатор проекта.
     *
     * <p>Генерируется базой данных.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название проекта.
     */
    @Column(nullable = false)
    private String name;

    /**
     * Описание проекта.
     *
     * <p>Хранится как TEXT, поскольку длина описания
     * заранее не ограничивается небольшим размером.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Компания, для которой создан проект.
     */
    @Column(nullable = false)
    private String company;

    /**
     * Дата создания проекта.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    /**
     * Сотрудник, создавший проект.
     *
     * <p>Связь lazy, поскольку данные сотрудника не всегда
     * нужны при работе с проектом.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "created_by_id",
            nullable = false
    )
    private EmployeeEntity createdBy;

    /**
     * Дата последнего изменения проекта.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    /**
     * Сотрудник, выполнивший последнее изменение проекта.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "updated_by_id",
            nullable = false
    )
    private EmployeeEntity updatedBy;

    /**
     * Связи проекта с ALS.
     *
     * <p>Каждая запись {@link ProjectALSEntity} содержит
     * конкретную ALSEntity и её количество в проекте.</p>
     *
     * <p>{@code cascade = ALL} позволяет сохранять и удалять
     * дочерние записи вместе с проектом.</p>
     *
     * <p>{@code orphanRemoval = true} означает, что связь,
     * удалённая из этой коллекции, должна быть удалена из БД.</p>
     */
    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProjectALSEntity> alsEntries =
            new ArrayList<>();

    /**
     * Конструктор для JPA.
     */
    public ProjectEntity() {
    }

    /**
     * Возвращает идентификатор проекта.
     *
     * @return идентификатор
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает название проекта.
     *
     * @return название
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название проекта.
     *
     * @param name название
     * @throws NullPointerException если name равен null
     */
    public void setName(String name) {
        this.name = Objects.requireNonNull(
                name,
                "Название проекта не должно быть null"
        );
    }

    /**
     * Возвращает описание проекта.
     *
     * @return описание
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание проекта.
     *
     * @param description описание
     */
    public void setDescription(String description) {
        this.description = description;
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
     * Устанавливает компанию проекта.
     *
     * @param company компания
     * @throws NullPointerException если company равен null
     */
    public void setCompany(String company) {
        this.company = Objects.requireNonNull(
                company,
                "Компания не должна быть null"
        );
    }

    /**
     * Возвращает дату создания.
     *
     * @return дата создания
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату создания.
     *
     * @param createdAt дата создания
     * @throws NullPointerException если createdAt равен null
     */
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Дата создания не должна быть null"
        );
    }

    /**
     * Возвращает сотрудника, создавшего проект.
     *
     * @return создатель проекта
     */
    public EmployeeEntity getCreatedBy() {
        return createdBy;
    }

    /**
     * Устанавливает создателя проекта.
     *
     * @param createdBy создатель
     * @throws NullPointerException если createdBy равен null
     */
    public void setCreatedBy(EmployeeEntity createdBy) {
        this.createdBy = Objects.requireNonNull(
                createdBy,
                "Создатель проекта не должен быть null"
        );
    }

    /**
     * Возвращает дату последнего изменения.
     *
     * @return дата изменения
     */
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату последнего изменения.
     *
     * @param updatedAt дата изменения
     * @throws NullPointerException если updatedAt равен null
     */
    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "Дата изменения не должна быть null"
        );
    }

    /**
     * Возвращает сотрудника, выполнившего последнее изменение.
     *
     * @return автор изменения
     */
    public EmployeeEntity getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Устанавливает автора последнего изменения.
     *
     * @param updatedBy автор изменения
     * @throws NullPointerException если updatedBy равен null
     */
    public void setUpdatedBy(EmployeeEntity updatedBy) {
        this.updatedBy = Objects.requireNonNull(
                updatedBy,
                "Автор изменения не должен быть null"
        );
    }

    /**
     * Возвращает связи проекта с ALS.
     *
     * <p>Коллекция является JPA-managed коллекцией.
     * Её не следует заменять новым экземпляром списка.
     * Для изменения состава необходимо использовать
     * {@link List#clear()} и {@link List#add(Object)}.</p>
     *
     * @return managed-коллекция связей с ALS
     */
    public List<ProjectALSEntity> getAlsEntries() {
        return alsEntries;
    }

    /**
     * Сравнивает сущности по persistence-идентификатору.
     *
     * <p>Бизнес-конфигурация проекта здесь не сравнивается.
     * Для JPA-сущности identity определяется её ID.</p>
     *
     * @param o объект для сравнения
     * @return true, если сущности имеют одинаковый ненулевой ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectEntity that = (ProjectEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    /**
     * Возвращает стабильный hashCode для JPA-сущности.
     *
     * <p>HashCode не зависит от изменяемых полей сущности,
     * что позволяет безопасно использовать entity в коллекциях.</p>
     *
     * @return hashCode класса сущности
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Возвращает строковое представление сущности.
     *
     * <p>Коллекция {@code alsEntries} намеренно не включается,
     * чтобы не провоцировать загрузку LAZY-связи при логировании.</p>
     *
     * @return строковое представление проекта
     */
    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdAt=" + createdAt +
                ", createdById=" + (
                createdBy == null
                        ? null
                        : createdBy.getId()
        ) +
                ", updatedAt=" + updatedAt +
                ", updatedById=" + (
                updatedBy == null
                        ? null
                        : updatedBy.getId()
        ) +
                '}';
    }
}