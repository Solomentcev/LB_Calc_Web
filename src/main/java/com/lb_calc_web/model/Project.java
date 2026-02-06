package com.lb_calc_web.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String description;
    private String company;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    @OneToMany(mappedBy = "project",
             orphanRemoval = true
           )
    private Set<ProjectALS> quantityALS = new HashSet<>();
       public Project() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public Set<ProjectALS> getQuantityALS() {
        return quantityALS;
    }

    public void setQuantityALS(Set<ProjectALS> quantityALS) {
        this.quantityALS = quantityALS;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdDate=" + createdDate +"\n"+
                ", quantityALS=" + quantityALS +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(getDescription(), project.getDescription()) && Objects.equals(getCompany(), project.getCompany())
                && Objects.equals(getCreatedDate(), project.getCreatedDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getCompany(), getCreatedDate());
    }
}
