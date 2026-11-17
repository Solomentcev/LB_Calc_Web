package com.lb_calc_web.repository;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.entity.ModuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleEntityRepository
        extends JpaRepository<ModuleEntity, Long> {

    List<ModuleEntity> findByHeightAndWidthAndDepth
            AndUpperFrameAndBottomFrameAndColorBodyAndColorDoor(
            int height,
            int width,
            int depth,
            int upperFrame,
            int bottomFrame,
            Colors colorBody,
            Colors colorDoor
    );
}
