package com.lb_calc_web.repository;

import com.lb_calc_web.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с проектами.
 */
@Repository
public interface ProjectRepository
        extends JpaRepository<ProjectEntity, Long>, JpaSpecificationExecutor<ProjectEntity> {

    /** Загружает авторов проекта одним запросом при постраничной выборке. */
    @Override
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    Page<ProjectEntity> findAll(Specification<ProjectEntity> specification, Pageable pageable);

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