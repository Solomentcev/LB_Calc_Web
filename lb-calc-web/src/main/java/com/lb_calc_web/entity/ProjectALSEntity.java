package com.lb_calc_web.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "project_als",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_project_als",
                        columnNames = {"project_id", "als_id"}
                )
        }
)
public class ProjectALSEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false
    )
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "als_id",
            nullable = false
    )
    private ALSEntity als;

    @Column(nullable = false)
    private int quantity;

    public ProjectALSEntity() {
    }

    public Long getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = Objects.requireNonNull(
                project,
                "Проект не должен быть null"
        );
    }

    public ALSEntity getAls() {
        return als;
    }

    public void setAls(ALSEntity als) {
        this.als = Objects.requireNonNull(
                als,
                "ALS не должна быть null"
        );
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException(
                    "Количество ALS должно быть больше нуля"
            );
        }

        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectALSEntity that = (ProjectALSEntity) o;

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
        return "ProjectALSEntity{" +
                "id=" + id +
                ", projectId=" + (
                project == null ? null : project.getId()
        ) +
                ", alsId=" + (
                als == null ? null : als.getId()
        ) +
                ", quantity=" + quantity +
                '}';
    }
}