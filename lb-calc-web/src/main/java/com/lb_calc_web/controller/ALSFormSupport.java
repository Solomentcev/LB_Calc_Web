package com.lb_calc_web.controller;

import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.FunctionalConfiguration;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.service.EquipmentCatalogService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Подготавливает комплектацию управляющего модуля ALS. */
final class ALSFormSupport {

    private ALSFormSupport() {
    }

    /** Синхронизирует функциональные назначения ALS и физическую комплектацию управляющего модуля. */
    static void synchronizeFunctionalConfiguration(
            ALSDTO als,
            EquipmentCatalogService equipmentCatalogService
    ) {
        Objects.requireNonNull(als, "ALS не должна быть null");
        FunctionalConfigurationDTO functional = Objects.requireNonNull(
                als.getFunctionalConfiguration(), "Функциональная конфигурация ALS не должна быть null");
        if (als.getLC() != null) {
            ModuleEquipmentFormSupport.applySelection(als.getLC());
            als.getLC().setFunctionalConfiguration(functional);
            configure(als.getLC().getEquipment(),
                    als.getLC()::setEquipment, als.getLC()::setEquipmentProvided,
                    als.getLC()::setFunctionalConfiguration, functional, equipmentCatalogService);
            return;
        }
        if (als.getLBC() != null) {
            ModuleEquipmentFormSupport.applySelection(als.getLBC());
            als.getLBC().setFunctionalConfiguration(functional);
            configure(als.getLBC().getEquipment(),
                    als.getLBC()::setEquipment, als.getLBC()::setEquipmentProvided,
                    als.getLBC()::setFunctionalConfiguration, functional, equipmentCatalogService);
            return;
        }
        throw new IllegalStateException("В ALS отсутствует управляющий модуль LC или LBC");
    }

    static void configure(LCDTO dto, EquipmentCatalogService service) {
        configure(dto.getEquipment(), dto::setEquipment,
                dto::setEquipmentProvided, dto::setFunctionalConfiguration,
                dto.getFunctionalConfiguration(), service);
    }

    static void configure(LBCDTO dto, EquipmentCatalogService service) {
        configure(dto.getEquipment(), dto::setEquipment,
                dto::setEquipmentProvided, dto::setFunctionalConfiguration,
                dto.getFunctionalConfiguration(), service);
    }

    private static void configure(
            Map<Long, Integer> selectedIds,
            java.util.function.Consumer<Map<Long, Integer>> equipmentSetter,
            java.util.function.Consumer<Boolean> providedSetter,
            java.util.function.Consumer<FunctionalConfigurationDTO> functionalSetter,
            FunctionalConfigurationDTO functionalDto,
            EquipmentCatalogService service
    ) {
        Objects.requireNonNull(service, "Сервис каталога оборудования не должен быть null");
        FunctionalConfiguration functional = new FunctionalConfiguration(
                functionalDto.getDisplay() == null ? DisplayPurpose.NONE : functionalDto.getDisplay(),
                functionalDto.getAccess() == null ? Set.of() : functionalDto.getAccess(),
                functionalDto.getPayment() == null ? PaymentPurpose.NONE : functionalDto.getPayment(),
                functionalDto.getPrinter() == null ? PrinterPurpose.ABSENT : functionalDto.getPrinter());
        Map<Equipment, Integer> selected = new LinkedHashMap<>();
        if (selectedIds != null) {
            selectedIds.forEach((id, quantity) -> selected.put(service.findById(id), quantity));
        }
        ControlConfiguration configuration = new ControlConfiguration(functional, selected);
        configuration.configureEquipment(functional, service.findAll(), service.findDefaultModels());

        FunctionalConfiguration normalized = configuration.getFunctionalConfiguration();
        FunctionalConfigurationDTO normalizedDto = new FunctionalConfigurationDTO();
        normalizedDto.setDisplay(normalized.getDisplay());
        normalizedDto.setAccess(normalized.getAccess());
        normalizedDto.setPayment(normalized.getPayment());
        normalizedDto.setPrinter(normalized.getPrinter());
        functionalSetter.accept(normalizedDto);

        Map<Long, Integer> result = new LinkedHashMap<>();
        configuration.getEquipment().forEach((item, quantity) -> result.put(item.getId(), quantity));
        equipmentSetter.accept(result);
        providedSetter.accept(true);
    }
}
