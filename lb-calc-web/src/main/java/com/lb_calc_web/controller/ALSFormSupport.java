package com.lb_calc_web.controller;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.EquipmentCatalogService;

import java.util.Objects;

/** Подготавливает управляющий модуль ALS к обработке формы агрегата. */
final class ALSFormSupport {

    private ALSFormSupport() {
    }

    /**
     * Передаёт функциональную конфигурацию ALS управляющему модулю
     * и пересобирает строки оборудования с сохранением совместимых выборов.
     *
     * @param als DTO редактируемой конфигурации ALS
     * @param equipmentCatalogService сервис каталога оборудования
     */
    static void synchronizeFunctionalConfiguration(
            ALSDTO als,
            EquipmentCatalogService equipmentCatalogService
    ) {
        Objects.requireNonNull(als, "ALS не должна быть null");
        Objects.requireNonNull(equipmentCatalogService,
                "Сервис каталога оборудования не должен быть null");

        if (als.getFunctionalConfiguration() == null) {
            throw new IllegalArgumentException(
                    "Функциональная конфигурация ALS не должна быть null");
        }

        if (als.getLC() != null) {
            LCDTO controlModule = als.getLC();
            controlModule.setFunctionalConfiguration(als.getFunctionalConfiguration());
            controlModule.setEquipmentSelections(equipmentCatalogService.prepareSelections(
                    als.getFunctionalConfiguration(), controlModule.getEquipmentSelections()));
            controlModule.setEquipmentSelectionsProvided(true);
            return;
        }

        if (als.getLBC() != null) {
            LBCDTO controlModule = als.getLBC();
            controlModule.setFunctionalConfiguration(als.getFunctionalConfiguration());
            controlModule.setEquipmentSelections(equipmentCatalogService.prepareSelections(
                    als.getFunctionalConfiguration(), controlModule.getEquipmentSelections()));
            controlModule.setEquipmentSelectionsProvided(true);
            return;
        }

        throw new IllegalStateException(
                "В ALS отсутствует управляющий модуль LC или LBC");
    }
}
