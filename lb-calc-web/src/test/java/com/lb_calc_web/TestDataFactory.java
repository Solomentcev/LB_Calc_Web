package com.lb_calc_web;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.domain.equipment.AccessPurpose;
import com.lb_calc_web.domain.equipment.DisplayPurpose;
import com.lb_calc_web.domain.equipment.PaymentPurpose;
import com.lb_calc_web.domain.equipment.PrinterPurpose;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.FunctionalConfigurationDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.mapper.dto.LBCDtoMapper;
import com.lb_calc_web.service.validation.size.ALSSizeValidator;
import com.lb_calc_web.service.validation.size.ModuleSizeValidator;
import com.lb_calc_web.service.validation.size.ProjectSizeValidator;
import com.lb_calc_web.service.validation.size.SizeBounds;
import com.lb_calc_web.service.validation.size.SizeValidator;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    private static TypeLb type1() {
        return new TypeLb(
                "TYPE1",
                100,
                5,
                40
        );
    }

    public static SizeBounds createSizeBounds() {
        Properties properties = loadSizeProperties();
        return new SizeBounds(
                intProperty(properties, "size.frame.upper.min"),
                intProperty(properties, "size.frame.upper.max"),
                intProperty(properties, "size.frame.bottom.min"),
                intProperty(properties, "size.frame.bottom.max"),
                intProperty(properties, "size.height.cell.min"),
                intProperty(properties, "size.height.max"),
                intProperty(properties, "size.height.min"),
                intProperty(properties, "size.height.lc.min"),
                intProperty(properties, "size.height.lc.panel.min"),
                intProperty(properties, "size.count.cells.min"),
                intProperty(properties, "size.count.cells.max"),
                intProperty(properties, "size.width.cell.min"),
                intProperty(properties, "size.width.min"),
                intProperty(properties, "size.width.max"),
                intProperty(properties, "size.depth.cell.min"),
                intProperty(properties, "size.depth.cell.max"),
                intProperty(properties, "size.depth.min"),
                intProperty(properties, "size.depth.max"),
                intProperty(properties, "size.door.thickness.min"),
                intProperty(properties, "size.door.thickness.max"),
                intProperty(properties, "size.type.delta-width.min"),
                intProperty(properties, "size.type.delta-width.max"),
                intProperty(properties, "size.type.shelf-thick.min"),
                intProperty(properties, "size.type.shelf-thick.max"),
                intProperty(properties, "size.type.service-zone-width.min"),
                intProperty(properties, "size.type.service-zone-width.max"),
                intProperty(properties, "size.height.default"),
                intProperty(properties, "size.width.default"),
                intProperty(properties, "size.depth.default"),
                intProperty(properties, "size.frame.upper.default"),
                intProperty(properties, "size.frame.bottom.default"),
                intProperty(properties, "size.count.cells.default"),
                intProperty(properties, "size.door.thickness.default"),
                intProperty(properties, "lc.geometry.width.min"),
                intProperty(properties, "lc.geometry.display.side-clearance"),
                intProperty(properties, "lc.geometry.width.rounding-step"),
                intProperty(properties, "lc.geometry.display.center-height"),
                intProperty(properties, "lc.geometry.display.top-clearance"),
                intProperty(properties, "lbc.geometry.storage-cell.width.min"),
                intProperty(properties, "lbc.geometry.technical-cell.width.min"),
                intProperty(properties, "lbc.geometry.technical-cell.height.min"),
                intProperty(properties, "lbc.geometry.equipment.clearance"),
                intProperty(properties, "lbc.geometry.display.center-height")
        );
    }

    public static SizeValidator createSizeValidator() {
        SizeBounds bounds = createSizeBounds();
        ModuleSizeValidator moduleValidator =
                new ModuleSizeValidator(bounds);
        ALSSizeValidator alsValidator =
                new ALSSizeValidator(bounds, moduleValidator);
        ProjectSizeValidator projectValidator =
                new ProjectSizeValidator(alsValidator);

        return new SizeValidator(
                moduleValidator,
                alsValidator,
                projectValidator
        );
    }

    public static LCDTO validLCDTO(Long id) {
        LCDTO lc = new LCDTO();

        lc.setId(id);
        lc.setHeight(1940);
        lc.setWidth(500);
        lc.setDepth(500);
        lc.setUpperFrame(50);
        lc.setBottomFrame(50);
        lc.setColorBody(Colors.Blue.name());
        lc.setColorDoor(Colors.White.name());

        FunctionalConfigurationDTO functional = new FunctionalConfigurationDTO();
        functional.setDisplay(DisplayPurpose.INCH_10);
        functional.setAccess(java.util.Set.of(AccessPurpose.RFID));
        functional.setPayment(PaymentPurpose.NONE);
        functional.setPrinter(PrinterPurpose.ABSENT);
        lc.setFunctionalConfiguration(functional);

        return lc;
    }

    public static LC validLC(Long id) {
        return com.lb_calc_web.mapper.dto.LCDtoMapper.toDomain(
                validLCDTO(id),
                equipmentId -> {
                    throw new IllegalStateException("Оборудование в тестовой конфигурации не загружается");
                },
                null,
                lcMinimumWidth(),
                lcDisplaySideClearance(),
                lcWidthRoundingStep(),
                lcDisplayCenterHeight(),
                lcDisplayTopClearance()
        );
    }

    public static LBDTO validLBDTO(Long id) {
        LBDTO lb = new LBDTO();

        lb.setId(id);
        lb.setHeight(1940);
        lb.setWidth(500);
        lb.setDepth(500);
        lb.setUpperFrame(50);
        lb.setBottomFrame(50);

        lb.setCountCells(3);
        TypeLb type = type1();
        lb.setType(type.getType());
        lb.setShelfThick(type.getShelfThick());
        lb.setDeltaWidth(type.getDeltaWidth());
        lb.setServiceZoneWidth(type.getServiceZoneWidth());

        lb.setDoorThickness(20);
        lb.setDirectionDoorOpening(
                DirectionDoorOpening.LEFT.name()
        );

        lb.setColorBody(Colors.Blue.name());
        lb.setColorDoor(Colors.White.name());

        return lb;
    }

    public static LB validLB(Long id) {
        return com.lb_calc_web.mapper.dto.LBDtoMapper
                .toDomain(validLBDTO(id));
    }

    public static LBCDTO validLBCDTO(Long id) {
        LBCDTO lbc = new LBCDTO();

        lbc.setId(id);
        lbc.setHeight(1940);
        lbc.setWidth(500);
        lbc.setDepth(500);
        lbc.setUpperFrame(50);
        lbc.setBottomFrame(50);

        lbc.setCountCells(3);
        TypeLb type = type1();
        lbc.setType(type.getType());
        lbc.setShelfThick(type.getShelfThick());
        lbc.setDeltaWidth(type.getDeltaWidth());
        lbc.setServiceZoneWidth(type.getServiceZoneWidth());

        lbc.setDoorThickness(20);
        lbc.setDirectionDoorOpening(
                DirectionDoorOpening.LEFT.name()
        );

        lbc.setColorBody(Colors.Blue.name());
        lbc.setColorDoor(Colors.White.name());

        FunctionalConfigurationDTO functional = new FunctionalConfigurationDTO();
        functional.setDisplay(DisplayPurpose.INCH_10);
        functional.setAccess(java.util.Set.of(AccessPurpose.RFID));
        functional.setPayment(PaymentPurpose.NONE);
        functional.setPrinter(PrinterPurpose.ABSENT);
        lbc.setFunctionalConfiguration(functional);

        return lbc;
    }

    public static LBC validLBC(Long id) {
        return LBCDtoMapper.toDomain(
                validLBCDTO(id),
                type1(),
                equipmentId -> {
                    throw new IllegalStateException("Оборудование в тестовой конфигурации не загружается");
                },
                null,
                lbcTechnicalCellMinWidth(),
                lbcTechnicalCellMinHeight(),
                lbcEquipmentClearance(),
                lbcDisplayCenterHeight(),
                lbcStorageCellMinWidth(),
                storageCellMinHeight()
        );
    }

    public static ALSDTO validALSDTO(Long id) {
        ALSDTO als = new ALSDTO();

        als.setId(id);
        als.setHeight(1940);
        als.setDepth(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setColorBody(Colors.Blue.name());
        als.setColorDoor(Colors.White.name());
        als.setPositionControlModule("CENTER");

        LCDTO lc = validLCDTO(offsetId(id, 100L));
        als.setLC(lc);

        List<LBDTO> lbList = new ArrayList<>();
        LBDTO lb1 = validLBDTO(offsetId(id, 200L));
        LBDTO lb2 = validLBDTO(offsetId(id, 201L));

        lbList.add(lb1);
        lbList.add(lb2);

        als.setLbList(lbList);


        als.setCountCells(
                lb1.getCountCells()
                        + lb2.getCountCells()
        );

        als.setWidth(
                lc.getWidth()
                        + lb1.getWidth()
                        + lb2.getWidth()
        );

        als.setName(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );

        als.setDescription(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );

return als;
    }

    public static ALSDTO validLBCALSDTO(Long id) {
        ALSDTO als = new ALSDTO();

        als.setId(id);
        als.setHeight(1940);
        als.setDepth(500);
        als.setUpperFrame(50);
        als.setBottomFrame(50);
        als.setColorBody(Colors.Blue.name());
        als.setColorDoor(Colors.White.name());
        als.setPositionControlModule("CENTER");
        als.setLBC(validLBCDTO(offsetId(id, 100L)));

        als.setCountCells(
                als.getLBC().getCountCells()
        );
        als.setWidth(
                als.getLBC().getWidth()
        );
        als.setDepthCell(
                als.getLBC().getDepthCell()
        );
        als.setName(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );
        als.setDescription(
                "АКХ на "
                        + als.getCountCells()
                        + " ячеек"
        );
return als;
    }

    public static ALS validALS(Long id) {
        return restoreALS(validALSDTO(id));
    }

    /** Восстанавливает тестовую ALS с геометрическими настройками из properties. */
    public static ALS restoreALS(ALSDTO dto) {
        return ALSDtoMapper.toDomain(
                dto,
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                equipmentId -> {
                    throw new IllegalStateException("Оборудование в тестовой конфигурации не загружается");
                },
                null,
                lcMinimumWidth(),
                lcDisplaySideClearance(),
                lcWidthRoundingStep(),
                lcDisplayCenterHeight(),
                lcDisplayTopClearance(),
                lbcTechnicalCellMinWidth(),
                lbcTechnicalCellMinHeight(),
                lbcEquipmentClearance(),
                lbcDisplayCenterHeight(),
                lbcStorageCellMinWidth(),
                storageCellMinHeight()
        );
    }

    private static Long offsetId(Long id, long offset) {
        return id == null ? null : id + offset;
    }

    public static ProjectDTO validProject(Long id) {
        ProjectDTO project = new ProjectDTO();

        project.setId(id);
        project.setName("TEST_" + id);
        project.setCompany("TEST_COMPANY");
        project.setDescription("TEST_PROJECT");

        project.setAlsList(
                List.of(
                        validALSDTO(offsetId(id, 1L)),
                        validALSDTO(offsetId(id, 2L))
                )
        );

        return project;
    }
    public static int lcMinimumWidth() {
        return createSizeBounds().getLcMinimumWidth();
    }

    public static int lcDisplaySideClearance() {
        return createSizeBounds().getLcDisplaySideClearance();
    }

    public static int lcWidthRoundingStep() {
        return createSizeBounds().getLcWidthRoundingStep();
    }

    public static int lcDisplayCenterHeight() {
        return createSizeBounds().getLcDisplayCenterHeight();
    }

    public static int lcDisplayTopClearance() {
        return createSizeBounds().getLcDisplayTopClearance();
    }

    public static int lbcTechnicalCellMinWidth() {
        return createSizeBounds().getLbcTechnicalCellMinWidth();
    }

    public static int lbcTechnicalCellMinHeight() {
        return createSizeBounds().getLbcTechnicalCellMinHeight();
    }

    public static int lbcEquipmentClearance() {
        return createSizeBounds().getLbcEquipmentClearance();
    }

    public static int lbcDisplayCenterHeight() {
        return createSizeBounds().getLbcDisplayCenterHeight();
    }

    public static int lbcStorageCellMinWidth() {
        return createSizeBounds().getLbcStorageCellMinWidth();
    }

    public static int storageCellMinHeight() {
        return createSizeBounds().getHeightCellMin();
    }

    private static int intProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Не задано свойство " + key);
        }
        return Integer.parseInt(value);
    }

    private static Properties loadSizeProperties() {
        Properties properties = new Properties();
        try (InputStream input = TestDataFactory.class.getClassLoader()
                .getResourceAsStream("size-bounds.properties")) {
            if (input == null) {
                throw new IllegalStateException("Не найден size-bounds.properties");
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось загрузить size-bounds.properties", exception);
        }
    }

}
