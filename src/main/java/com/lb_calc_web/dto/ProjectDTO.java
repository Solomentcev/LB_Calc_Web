package com.lb_calc_web.dto;

import java.time.LocalDate;
import java.util.*;

public class ProjectDTO {
    private int id;
    private String name;
    private String description;
    private String company;
    private LocalDate createdDate;
    private LocalDate updatedDate;
    private List<ALSDTO> alsList = new ArrayList<>();
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

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDate updatedDate) {
        this.updatedDate = updatedDate;
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
        return Objects.equals(getCompany(), that.getCompany()) && Objects.equals(getCreatedDate(), that.getCreatedDate())
                && Objects.deepEquals(getQuantityALS(), that.getQuantityALS());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCompany(), getCreatedDate(), getQuantityALS());
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdDate=" + createdDate +"\n"+
                ", alsList=" + alsList +"\n"+
                ", quantityALS=" + quantityALS +
                '}';
    }
}
