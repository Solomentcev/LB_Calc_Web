package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * JPA-сущность связи между проектом и ALS.
 *
 * <p>Представляет строку таблицы {@code project_als}.</p>
 *
 * <p>Сущность хранит:</p>
 *
 * <ul>
 *     <li>проект;</li>
 *     <li>конкретную конфигурацию ALS;</li>
 *     <li>количество данной конфигурации ALS в проекте.</li>
 * </ul>
 *
 * <p>Одна пара {@code project + als} может существовать
 * только один раз. Это обеспечивается уникальным ограничением
 * {@code uk_project_als}.</p>
 */
@Entity
@Table(
        name = "project_als",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_als",
                        columnNames = {
                                "project_id",
                                "als_id"
                        }
                )
        }
)
public class ProjectALSEntity {

    /**
     * Идентификатор записи связи.
     *
     * <p>Это ID самой связи, а не ID проекта или ALS.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Проект, которому принадлежит данная ALS.
     *
     * <p>Обратная сторона связи с {@link ProjectEntity}.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private ProjectEntity project;

    /**
     * ALS, входящая в проект.
     *
     * <p>Ссылка на существующую {@link ALSEntity}.
     * ALS не является дочерней сущностью проекта.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "als_id",
            nullable = false
    )
    private ALSEntity als;

    /**
     * Количество данной конфигурации ALS в проекте.
     */
    @Column(nullable = false)
    private int quantity;

    /**
     * Конструктор для JPA.
     */
    public ProjectALSEntity() {
    }

    /**
     * Возвращает идентификатор связи.
     *
     * @return идентификатор
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает проект.
     *
     * @return проект
     */
    public ProjectEntity getProject() {
        return project;
    }

    /**
     * Устанавливает проект.
     *
     * @param project проект
     * @throws NullPointerException если project равен null
     */
    public void setProject(ProjectEntity project) {
        this.project = Objects.requireNonNull(
                project,
                "Проект не должен быть null"
        );
    }

    /**
     * Возвращает ALS.
     *
     * @return ALS
     */
    public ALSEntity getAls() {
        return als;
    }

    /**
     * Устанавливает ALS.
     *
     * @param als ALS
     * @throws NullPointerException если als равен null
     */
    public void setAls(ALSEntity als) {
        this.als = Objects.requireNonNull(
                als,
                "ALS не должна быть null"
        );
    }

    /**
     * Возвращает количество ALS.
     *
     * @return количество
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Устанавливает количество ALS.
     *
     * @param quantity количество
     * @throws IllegalArgumentException если quantity меньше 1
     */
    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество ALS должно быть больше нуля"
            );
        }

        this.quantity = quantity;
    }

    /**
     * Сравнивает записи связи по persistence ID.
     *
     * @param o объект для сравнения
     * @return true, если ID одинаковый и не равен null
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectALSEntity that =
                (ProjectALSEntity) o;

        if (id == null || that.id == null) {
            return false;
        }

        return id.equals(that.id);
    }

    /**
     * Возвращает стабильный hashCode сущности.
     *
     * @return hashCode класса
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Возвращает строковое представление записи связи.
     *
     * <p>Используются только идентификаторы связанных сущностей,
     * поэтому логирование не требует загрузки их полной конфигурации.</p>
     *
     * @return строковое представление связи
     */
    @Override
    public String toString() {
        return "ProjectALSEntity{" +
                "id=" + id +
                ", projectId=" + (
                project == null
                        ? null
                        : project.getId()
        ) +
                ", alsId=" + (
                als == null
                        ? null
                        : als.getId()
        ) +
                ", quantity=" + quantity +
                '}';
    }
}