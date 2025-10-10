package com.lb_calc_web.dto;

import java.time.LocalDate;
import java.util.*;

public class ProjectDTO {
    private int id;
    private String name;
    private String description;
    private String company;
    private LocalDate createdDate;
    private List<ALSDTO> alsList = new ArrayList<>();
    private Map<ALSDTO, Integer> uniqueALS=new HashMap<>();

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

    public List<ALSDTO> getAlsList() {
        return alsList;
    }

    public void setAlsList(List<ALSDTO> alsList) {
        this.alsList = alsList;
    }

    public Map<ALSDTO, Integer> getUniqueALS() {
        return uniqueALS;
    }

    public void setUniqueALS(Map<ALSDTO, Integer> uniqueALS) {
        this.uniqueALS = uniqueALS;
    }
}
