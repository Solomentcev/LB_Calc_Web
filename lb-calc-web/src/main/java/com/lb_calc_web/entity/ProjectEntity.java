package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA-сущность проекта.
 *
 * <p>Проект содержит набор ALS и количество каждого ALS.
 * В доменной модели это представлено как {@code Map<ALS, Integer>}.
 * В persistence-слое используется {@link ProjectALSEntity},
 * поскольку количество является свойством связи между проектом и ALS.</p>
 */
@Entity
@Table(name = "project")
public class ProjectEntity {

    /**
     * Идентификатор проекта.
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
     * <p>В Domain оно формируется на основании ALS проекта.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Компания-заказчик.
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
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    private EmployeeEntity createdBy;

    /**
     * Дата последнего изменения проекта.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    /**
     * Сотрудник, выполнивший последнее изменение.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "updated_by_id", nullable = false)
    private EmployeeEntity updatedBy;

    /**
     * ALS, входящие в проект, и их количество.
     */
    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ProjectALSEntity> alsEntries = new ArrayList<>();

    /**
     * Конструктор для JPA.
     */
    protected ProjectEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(
                name,
                "Название проекта не должно быть null"
        );
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = Objects.requireNonNull(
                company,
                "Компания не должна быть null"
        );
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = Objects.requireNonNull(
                createdAt,
                "Дата создания не должна быть null"
        );
    }

    public EmployeeEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeEntity createdBy) {
        this.createdBy = Objects.requireNonNull(
                createdBy,
                "Создатель проекта не должен быть null"
        );
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = Objects.requireNonNull(
                updatedAt,
                "Дата изменения не должна быть null"
        );
    }

    public EmployeeEntity getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(EmployeeEntity updatedBy) {
        this.updatedBy = Objects.requireNonNull(
                updatedBy,
                "Автор изменения не должен быть null"
        );
    }

    public List<ProjectALSEntity> getAlsEntries() {
        return alsEntries;
    }

    public void setAlsEntries(List<ProjectALSEntity> alsEntries) {
        this.alsEntries = Objects.requireNonNull(
                alsEntries,
                "Список ALS не должен быть null"
        );
    }

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

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdAt=" + createdAt +
                ", createdById=" + (
                createdBy == null ? null : createdBy.getId()
        ) +
                ", updatedAt=" + updatedAt +
                ", updatedById=" + (
                updatedBy == null ? null : updatedBy.getId()
        ) +
                ", alsEntries=" + alsEntries +
                '}';
    }
}