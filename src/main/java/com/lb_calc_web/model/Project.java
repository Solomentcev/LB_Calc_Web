package com.lb_calc_web.model;

import com.lb_calc_web.model.user.Employee;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String company;
    private LocalDate createdAt;
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Employee createdBy;
    private LocalDate updatedAt;
    @ManyToOne
    @JoinColumn(name = "updated_by_id")
    private Employee updatedBy;
    @OneToMany(mappedBy = "project",
             orphanRemoval = true
           )
    private Set<ProjectALS> quantityALS = new HashSet<>();

    public Employee getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Employee updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Employee createdBy) {
        this.createdBy = createdBy;
    }

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        this.company = company;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Set<ProjectALS> getQuantityALS() {
        return quantityALS;
    }

    public void setQuantityALS(Set<ProjectALS> quantityALS) {
        this.quantityALS = quantityALS;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +"\n"+
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                ", quantityALS=" + quantityALS +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(getDescription(), project.getDescription()) && Objects.equals(getCompany(), project.getCompany())
                && Objects.equals(getCreatedAt(), project.getCreatedAt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getCompany(), getCreatedAt());
    }
}
