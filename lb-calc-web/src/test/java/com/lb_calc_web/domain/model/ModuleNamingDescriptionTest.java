package com.lb_calc_web.domain.model;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет пользовательские названия и описания модулей и агрегатов. */
class ModuleNamingDescriptionTest {

    @Test
    void lbNameAndDescription_shouldContainStorageCharacteristics() {
        ALS als = TestDataFactory.validALS(101L);
        StorageModule left = als.getStorageModules().get(0);

        assertTrue(left.getName().startsWith("Модуль хранения: 3 яч."));
        assertTrue(left.getName().contains("TYPE1"));
        assertTrue(left.getName().contains("влево"));
        assertTrue(left.getName().contains("1940×500×500 мм"));
        assertTrue(left.getName().contains("синий/белый"));
        assertTrue(left.getName().contains("синий/белый"));

        assertTrue(left.getDescription().contains("Размеры ячейки (В×Ш×Г)"));
        assertTrue(left.getDescription().contains("Верхняя рама: 50 мм"));
        assertTrue(left.getDescription().contains("Направление открытия дверей: влево"));
        assertTrue(left.getDescription().contains("Толщина дверцы: 20 мм"));
    }

    @Test
    void lcNameAndDescription_shouldContainDisplayAndFunctionalPurposes() {
        LC lc = TestDataFactory.validALS(102L).getLC();

        assertTrue(lc.getName().startsWith("Модуль управления: дисплей 10″"));
        assertTrue(lc.getName().contains("RFID"));
        assertTrue(lc.getName().contains("1940×500×500 мм"));
        assertTrue(lc.getDescription().contains("Функциональные назначения:"));
        assertTrue(lc.getDescription().contains("RFID"));
        assertTrue(lc.getDescription().contains("Установленное оборудование:"));
    }

    @Test
    void lbcNameAndDescription_shouldContainStorageAndControlCharacteristics() {
        ALS als = ALSDtoMapper.toDomain(TestDataFactory.validLBCALSDTO(103L));
        LBC lbc = als.getLBC();

        assertTrue(lbc.getName().startsWith(
                "Модуль хранения и управления: 3 яч., дисплей 10″"));
        assertTrue(lbc.getName().contains("TYPE1"));
        assertTrue(lbc.getName().contains("влево"));
        assertTrue(lbc.getName().contains("1940×500×500 мм"));
        assertTrue(lbc.getDescription().contains("Размеры ячейки (В×Ш×Г)"));
        assertTrue(lbc.getDescription().contains("Функциональные назначения:"));
    }

    @Test
    void alsNameAndDescription_shouldContainModuleCountAndPhysicalComposition() {
        ALS als = TestDataFactory.validALS(104L);

        assertTrue(als.getName().startsWith("АКХ: 6 яч., 2 мод."));
        assertTrue(als.getName().contains("дисплей 10″"));
        assertTrue(als.getName().contains("RFID"));
        assertTrue(als.getDescription().contains("Количество модулей хранения: 2"));
        assertTrue(als.getDescription().contains("Состав модулей слева направо:"));
        assertTrue(als.getDescription().contains("Направление открытия дверей: влево"));
        assertTrue(als.getDescription().contains("Направление открытия дверей: вправо"));
    }

    @Test
    void projectDescription_shouldContainDetailedAlsCompositionAndQuantity() {
        Employee employee = new Employee(
                "Тест",
                "Тестов",
                "module-description@example.test",
                LocalDate.of(2026, 1, 10),
                com.lb_calc_web.domain.attributes.Role.ROLE_MANAGER
        );
        ALS als = TestDataFactory.validALS(105L);
        Project project = new Project(
                "Тестовый проект",
                "Компания",
                LocalDate.of(2026, 1, 10),
                employee
        );

        project.addALS(als);
        project.addALS(als);

        assertTrue(project.getDescription().contains("АКХ на 6 ячеек"));
        assertTrue(project.getDescription().contains("2 шт."));
        assertTrue(project.getDescription().contains("Состав модулей слева направо:"));
        assertTrue(project.getDescription().contains("Размеры ячейки (В×Ш×Г)"));
    }

    @Test
    void absentOptionalControlFunctions_shouldNotAppearInNames() {
        ALS als = ALSDtoMapper.toDomain(TestDataFactory.validLBCALSDTO(106L));
        FunctionalConfiguration configuration = new FunctionalConfiguration(
                com.lb_calc_web.domain.equipment.DisplayPurpose.NONE,
                java.util.Set.of(),
                com.lb_calc_web.domain.equipment.PaymentPurpose.NONE,
                com.lb_calc_web.domain.equipment.PrinterPurpose.ABSENT
        );
        als.setFunctionalConfiguration(configuration);

        String name = als.getName();
        assertTrue(!name.contains("дисплей"));
        assertTrue(!name.contains("доступ:"));
        assertTrue(!name.contains("оплата:"));
        assertTrue(!name.contains("печать:"));
    }
}
