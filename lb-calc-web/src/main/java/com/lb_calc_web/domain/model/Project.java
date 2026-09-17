package com.lb_calc_web.domain.model;

import com.lb_calc_web.domain.attributes.Role;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Project {

    private String name;
    private String description;
    private String company;

    private LocalDate createdAt;
    private LocalDate updatedAt;

    private Employee createdBy;
    private Employee updatedBy;

    private final Map<ALS, Integer> quantityALS = new LinkedHashMap<>();

    public Project(
            String name,
            String company,
            LocalDate createdAt,
            Employee createdBy
    ) {
        this.name = Objects.requireNonNull(name);
        this.company = Objects.requireNonNull(company);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = createdAt;
        this.createdBy = Objects.requireNonNull(createdBy);
        this.updatedBy = createdBy;

        updateDescription();
    }

    public void addALS(ALS als) {
        Objects.requireNonNull(als);

        quantityALS.merge(als, 1, Integer::sum);

        update();
    }

    public void removeALS(ALS als) {
        Integer quantity = quantityALS.get(als);

        if (quantity == null) {
            return;
        }

        if (quantity > 1) {
            quantityALS.put(als, quantity - 1);
        } else {
            quantityALS.remove(als);
        }

        update();
    }

    public void replaceALS(ALS oldALS, ALS newALS) {
        Objects.requireNonNull(oldALS);
        Objects.requireNonNull(newALS);

        Integer quantity = quantityALS.remove(oldALS);

        if (quantity == null) {
            throw new IllegalArgumentException(
                    "ALS не найден в проекте"
            );
        }

        quantityALS.put(newALS, quantity);

        update();
    }

    public int getQuantity(ALS als) {
        return quantityALS.getOrDefault(als, 0);
    }

    public Map<ALS, Integer> getQuantityALS() {
        return Collections.unmodifiableMap(quantityALS);
    }

    public void update() {
        updatedAt = LocalDate.now();
        updateDescription();
    }

    private void updateDescription() {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<ALS, Integer> entry : quantityALS.entrySet()) {
            builder
                    .append(entry.getKey().getName())
                    .append(" - ")
                    .append(entry.getValue())
                    .append(" шт.\n");
        }

        description = builder.toString();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCompany() {
        return company;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public Employee getCreatedBy() {
        return createdBy;
    }

    public Employee getUpdatedBy() {
        return updatedBy;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    public void setUpdatedBy(Employee updatedBy) {
        this.updatedBy = Objects.requireNonNull(updatedBy);
        update();
    }
}