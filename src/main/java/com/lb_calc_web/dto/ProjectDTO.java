package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.*;

public class ProjectDTO {
    private int id;
    private String name;
    private String description;
    private String company;
    private EmployeeDTO createdBy;
    private LocalDate createdAt;
    private EmployeeDTO updatedBy;
    private LocalDate updatedAt;
    private List<ALSDTO> alsList = new ArrayList<>();
    @JsonIgnore
    private Map<ALSDTO, Integer> quantityALS =new HashMap<>();

    public ProjectDTO() {
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

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ALSDTO> getAlsList() {
        return alsList;
    }

    public void setAlsList(List<ALSDTO> alsList) {
        this.alsList = alsList;
    }

    public Map<ALSDTO, Integer> getQuantityALS() {
        return quantityALS;
    }

    public void setQuantityALS(Map<ALSDTO, Integer> quantityALS) {
        this.quantityALS = quantityALS;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectDTO that = (ProjectDTO) o;
        return Objects.equals(getCompany(), that.getCompany()) && Objects.equals(getCreatedAt(), that.getCreatedAt())
                && Objects.deepEquals(getQuantityALS(), that.getQuantityALS());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompany(), getCreatedAt(), getQuantityALS());
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +"\n"+
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +"\n"+
                ", alsList=" + alsList +"\n"+
                ", quantityALS=" + quantityALS +
                '}';
    }

    public EmployeeDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(EmployeeDTO owner) {
        this.createdBy = owner;
    }

    public EmployeeDTO getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(EmployeeDTO updatedBy) {
        this.updatedBy = updatedBy;
    }
}
