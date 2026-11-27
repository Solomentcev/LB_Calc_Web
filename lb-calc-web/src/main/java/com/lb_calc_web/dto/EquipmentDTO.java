package com.lb_calc_web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.lb_calc_web.domain.equipment.EquipmentCategory;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Данные физической модели оборудования для интерфейса и REST API. */
@Schema(description = "Модель физического оборудования из каталога, включая функциональный тип, назначение и габариты.")
public class EquipmentDTO {

    private Long id;

    @NotNull(message = "Укажите функциональный тип оборудования")
    private EquipmentFunctionalType functionalType;

    @NotBlank(message = "Укажите назначение оборудования")
    @Size(max = 50, message = "Назначение не должно превышать 50 символов")
    private String purpose;

    @NotNull(message = "Укажите категорию оборудования")
    private EquipmentCategory category;

    @NotBlank(message = "Название оборудования не должно быть пустым")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String name;

    @Size(max = 4000, message = "Описание не должно превышать 4000 символов")
    private String description = "";

    @Min(value = 0, message = "Ширина не может быть отрицательной")
    private int width;

    @Min(value = 0, message = "Высота не может быть отрицательной")
    private int height;

    @Min(value = 0, message = "Глубина не может быть отрицательной")
    private int depth;

    /** Количество выбранных экземпляров; заполняется только в представлении комплектации. */
    private Integer quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EquipmentFunctionalType getFunctionalType() { return functionalType; }
    public void setFunctionalType(EquipmentFunctionalType functionalType) { this.functionalType = functionalType; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public EquipmentCategory getCategory() { return category; }
    public void setCategory(EquipmentCategory category) { this.category = category; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
