package com.lb_calc_web.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO проекта.
 *
 * <p>Используется для передачи данных проекта между
 * controller- и service-слоями, а также для отображения
 * проекта в пользовательском интерфейсе.</p>
 *
 * <p>DTO не содержит бизнес-логики. Он только хранит
 * данные, необходимые для работы с проектом.</p>
 */
public class ProjectDTO {

    /**
     * Идентификатор проекта.
     *
     * <p>Для нового проекта может быть {@code null} или {@code 0},
     * в зависимости от сценария создания.</p>
     */
    private Long id;

    /**
     * Название проекта.
     */
    private String name;

    /**
     * Описание проекта.
     *
     * <p>Формируется на основании состава проекта
     * и является производным значением.</p>
     */
    private String description;

    /**
     * Компания, для которой создаётся проект.
     */
    private String company;

    /**
     * Сотрудник, создавший проект.
     */
    private EmployeeDTO createdBy;

    /**
     * Дата создания проекта.
     */
    private LocalDate createdAt;

    /**
     * Сотрудник, выполнивший последнее изменение проекта.
     */
    private EmployeeDTO updatedBy;

    /**
     * Дата последнего изменения проекта.
     */
    private LocalDate updatedAt;

    /**
     * Список ALS, входящих в проект.
     *
     * <p>Список используется как представление состава проекта
     * для UI. Порядок ALS внутри проекта бизнес-значения не имеет.</p>
     *
     * <p>Из списка рассчитывается количество одинаковых
     * конфигураций ALS.</p>
     */
    private List<ALSDTO> alsList =
            new ArrayList<>();

    /**
     * Количество одинаковых конфигураций ALS.
     *
     * <p>Поле является производным представлением {@link #alsList}
     * и используется UI для отображения количества одинаковых ALS.</p>
     *
     * <p>При преобразовании DTO в domain это поле не должно
     * использоваться как источник истины. Количество должно
     * рассчитываться из фактического состава {@link #alsList}.</p>
     *
     * <p>Поле не сериализуется в JSON, поскольку оно используется
     * текущим серверным UI как дополнительное представление данных.</p>
     */
    @JsonIgnore
    private Map<ALSDTO, Integer> quantityALS =
            new LinkedHashMap<>();

    /**
     * Возвращает идентификатор проекта.
     *
     * @return идентификатор проекта
     */
    public Long getId() {
        return id;
    }

    /**
     * Устанавливает идентификатор проекта.
     *
     * @param id идентификатор проекта
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Возвращает название проекта.
     *
     * @return название проекта
     */
    public String getName() {
        return name;
    }

    /**
     * Устанавливает название проекта.
     *
     * @param name название проекта
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Возвращает описание проекта.
     *
     * @return описание проекта
     */
    public String getDescription() {
        return description;
    }

    /**
     * Устанавливает описание проекта.
     *
     * @param description описание проекта
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Возвращает компанию проекта.
     *
     * @return компания
     */
    public String getCompany() {
        return company;
    }

    /**
     * Устанавливает компанию проекта.
     *
     * @param company компания
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Возвращает сотрудника, создавшего проект.
     *
     * @return создатель проекта
     */
    public EmployeeDTO getCreatedBy() {
        return createdBy;
    }

    /**
     * Устанавливает сотрудника, создавшего проект.
     *
     * @param createdBy создатель проекта
     */
    public void setCreatedBy(EmployeeDTO createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Возвращает дату создания проекта.
     *
     * @return дата создания
     */
    public LocalDate getCreatedAt() {
        return createdAt;
    }

    /**
     * Устанавливает дату создания проекта.
     *
     * @param createdAt дата создания
     */
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Возвращает сотрудника, выполнившего последнее изменение.
     *
     * @return автор последнего изменения
     */
    public EmployeeDTO getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Устанавливает сотрудника, выполнившего последнее изменение.
     *
     * @param updatedBy автор последнего изменения
     */
    public void setUpdatedBy(EmployeeDTO updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * Возвращает дату последнего изменения.
     *
     * @return дата последнего изменения
     */
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Устанавливает дату последнего изменения.
     *
     * @param updatedAt дата последнего изменения
     */
    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Возвращает список ALS проекта.
     *
     * <p>Список остаётся изменяемым, поскольку DTO используется
     * при редактировании проекта и связывается с Thymeleaf-формой.</p>
     *
     * @return список ALS
     */
    public List<ALSDTO> getAlsList() {
        return alsList;
    }

    /**
     * Устанавливает список ALS проекта.
     *
     * <p>При передаче {@code null} устанавливается пустой список.</p>
     *
     * @param alsList список ALS
     */
    public void setAlsList(List<ALSDTO> alsList) {
        this.alsList =
                alsList == null
                        ? new ArrayList<>()
                        : alsList;
    }

    /**
     * Возвращает количество одинаковых конфигураций ALS.
     *
     * <p>Карта используется как производное представление
     * состава проекта для UI.</p>
     *
     * @return конфигурации ALS и их количество
     */
    public Map<ALSDTO, Integer> getQuantityALS() {
        return quantityALS;
    }

    /**
     * Устанавливает количество одинаковых конфигураций ALS.
     *
     * <p>Обычно карта формируется mapper-ом на основании
     * {@link #alsList}.</p>
     *
     * @param quantityALS конфигурации ALS и их количество
     */
    public void setQuantityALS(
            Map<ALSDTO, Integer> quantityALS
    ) {
        this.quantityALS =
                quantityALS == null
                        ? new LinkedHashMap<>()
                        : quantityALS;
    }

    /**
     * Возвращает строковое представление проекта.
     *
     * @return строковое представление DTO
     */
    @Override
    public String toString() {
        return "ProjectDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", company='" + company + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt=" + createdAt +
                ", updatedBy=" + updatedBy +
                ", updatedAt=" + updatedAt +
                ", alsList=" + alsList +
                ", quantityALS=" + quantityALS +
                '}';
    }
}