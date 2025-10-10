package com.lb_calc_web.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectALSKey implements Serializable {
    @Column(name = "project_id")
    private Integer projectId;
    @Column(name = "als_id")
    private Integer alsId;

    public ProjectALSKey() {
    }

    public ProjectALSKey(Integer projectId, Integer alsId) {
        this.projectId = projectId;
        this.alsId = alsId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getAlsId() {
        return alsId;
    }

    public void setAlsId(Integer alsId) {
        this.alsId = alsId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProjectALSKey that = (ProjectALSKey) o;
        return Objects.equals(getProjectId(), that.getProjectId()) && Objects.equals(getAlsId(), that.getAlsId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProjectId(), getAlsId());
    }

    @Override
    public String toString() {
        return "ProjectALSKey{" +
                "projectId=" + projectId +
                ", alsId=" + alsId +
                '}';
    }
}
