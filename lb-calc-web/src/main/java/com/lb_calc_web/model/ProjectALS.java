package com.lb_calc_web.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity(name="project_als")
public class ProjectALS {
    @EmbeddedId
    private ProjectALSKey  id;
    @ManyToOne
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;
    @ManyToOne
    @MapsId("alsId")
    @JoinColumn(name = "als_id")
    private ALS als;

    private int quantity;

    public ProjectALS() {
    }

    public ProjectALS(Project project, ALS als, int quantity) {
        this.project = project;
        this.als = als;
        this.quantity = quantity;
        this.id=new ProjectALSKey(project.getId(), als.getId());
    }

    public ProjectALSKey getId() {
        return id;
    }

    public void setId(ProjectALSKey id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public ALS getAls() {
        return als;
    }

    public void setAls(ALS als) {
        this.als = als;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ProjectALS{" +
                "id=" + id +
                ", project=" + project.getId() +
                ", als=" + als.getId() +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectALS that = (ProjectALS) o;
        return getQuantity() == that.getQuantity() && Objects.equals(getProject().getId(), that.getProject().getId()) && getAls().getId().equals(that.getAls().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProject().getId(), getAls().getId(), getQuantity());
    }
}
