package com.lb_calc_web.mapper.dto;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.service.util.TypeLbFactory;
import org.springframework.core.env.Environment;

import java.util.Objects;

/**
 * Преобразует DTO конфигурации ALS в доменную модель,
 * разрешая конструктивные типы модулей из конфигурации приложения.
 */
public final class ALSConfigurationMapper {

    private ALSConfigurationMapper() {
    }

    /**
     * Преобразует DTO в доменную ALS с доверенными типами модулей.
     *
     * @param dto DTO конфигурации
     * @param environment конфигурация приложения
     * @return доменная конфигурация ALS
     */
    public static ALS toDomain(ALSDTO dto, Environment environment) {
        Objects.requireNonNull(environment, "Environment не должен быть null");
        return ALSDtoMapper.toDomain(
                dto,
                lbDto -> TypeLbFactory.forLB(environment, lbDto.getType()),
                lbcDto -> TypeLbFactory.forLBC(environment, lbcDto.getType())
        );
    }
}
