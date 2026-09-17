package com.lb_calc_web.repository;

import com.lb_calc_web.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с проектами.
 */
@Repository
public interface ProjectRepository
        extends JpaRepository<ProjectEntity, Long> {

    /**
     * Возвращает проекты вместе с сотрудниками,
     * создавшими и изменившими проект.
     *
     * @return список проектов
     */
    @Query("""
            SELECT DISTINCT p
            FROM ProjectEntity p
            LEFT JOIN FETCH p.createdBy
            LEFT JOIN FETCH p.updatedBy
            ORDER BY p.updatedAt DESC, p.id DESC
            """)
    List<ProjectEntity> findAllWithUsers();
}