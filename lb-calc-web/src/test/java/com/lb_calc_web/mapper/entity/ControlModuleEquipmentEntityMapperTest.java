package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Проверяет сохранение физического состава оборудования LC/LBC.
 */
class ControlModuleEquipmentEntityMapperTest {

    @Test
    void lc_shouldPreserveAllEquipmentPositionsAndQuantities() {
        LC domain = withBarReaders(TestDataFactory.validLC(1L), 1);

        LCEntity entity = LCEntityMapper.toEntity(domain);
        LC restored = LCEntityMapper.toDomain(entity);

        assertEquals(
                domain.getControlConfiguration().getEquipmentConfiguration()
                        .getAllItems().size(),
                entity.getEquipment().size()
        );
        assertEquals(2, entity.getEquipment().stream()
                .filter(item -> item.getCatalogPositionId().startsWith("BAR_READER"))
                .count());
        assertEquals(domain, restored);
    }

    @Test
    void lbc_shouldPreserveAllEquipmentPositionsAndQuantities() {
        LBC domain = withBarReaders(TestDataFactory.validLBC(1L), 1);

        LBCEntity entity = LBCEntityMapper.toEntity(domain);
        LBC restored = LBCEntityMapper.toDomain(entity);

        assertEquals(
                domain.getControlConfiguration().getEquipmentConfiguration()
                        .getAllItems().size(),
                entity.getEquipment().size()
        );
        assertEquals(2, entity.getEquipment().stream()
                .filter(item -> item.getCatalogPositionId().startsWith("BAR_READER"))
                .count());
        assertEquals(domain, restored);
    }

    @Test
    void lbc_shouldPersistEveryEquipmentPositionAndQuantity() {
        LBC domain = withBarReaders(TestDataFactory.validLBC(1L), 3);
        EquipmentConfiguration configuration =
                domain.getControlConfiguration().getEquipmentConfiguration();

        LBCEntity entity = LBCEntityMapper.toEntity(domain);

        assertEquals(
                configuration.getAllItems().size(),
                entity.getEquipment().size()
        );
        assertEquals(
                2,
                entity.getEquipment().stream()
                        .filter(item -> item.getCatalogPositionId()
                                .equals(BarReader.READER_1D_ID))
                        .findFirst().orElseThrow().getQuantity()
        );
        assertEquals(
                3,
                entity.getEquipment().stream()
                        .filter(item -> item.getCatalogPositionId()
                                .equals(BarReader.READER_2D_ID))
                        .findFirst().orElseThrow().getQuantity()
        );
        assertEquals(domain, LBCEntityMapper.toDomain(entity));
    }

    @Test
    void equipmentValue_shouldIncludeQuantity() {
        EquipmentQuantityEmbeddable one =
                new EquipmentQuantityEmbeddable("BAR_READER_1D", 1);
        EquipmentQuantityEmbeddable two =
                new EquipmentQuantityEmbeddable("BAR_READER_1D", 2);

        assertNotEquals(one, two);
        assertEquals(2, new HashSet<>(List.of(one, two)).size());

        assertThrows(
                IllegalArgumentException.class,
                () -> new LCEntity().setEquipment(Set.of(one, two))
        );
    }

    @Test
    void lc_shouldPersistEveryEquipmentPositionAndQuantity() {
        LC domain = withBarReaders(TestDataFactory.validLC(1L), 3);
        EquipmentConfiguration configuration =
                domain.getControlConfiguration().getEquipmentConfiguration();

        LCEntity entity = LCEntityMapper.toEntity(domain);

        assertEquals(
                configuration.getAllItems().size(),
                entity.getEquipment().size()
        );
        assertEquals(
                2,
                entity.getEquipment().stream()
                        .filter(item -> item.getCatalogPositionId()
                                .equals(BarReader.READER_1D_ID))
                        .findFirst().orElseThrow().getQuantity()
        );
        assertEquals(domain, LCEntityMapper.toDomain(entity));
    }

    private LC withBarReaders(LC source, int secondReaderQuantity) {
        EquipmentConfiguration equipment =
                source.getControlConfiguration().getEquipmentConfiguration();
        equipment.addRequiredEquipment(BarReader.READER_1D, 2);
        equipment.addAdditionalEquipment(
                BarReader.READER_2D,
                secondReaderQuantity
        );

        ControlConfiguration configuration = new ControlConfiguration(
                source.getControlConfiguration().getPayment(),
                source.getControlConfiguration().getAccessMethods(),
                source.getControlConfiguration().getPrintOptions(),
                equipment
        );

        return new LC(
                source.getHeight(),
                source.getWidth(),
                source.getDepth(),
                source.getUpperFrame(),
                source.getBottomFrame(),
                source.getColorBody(),
                source.getColorDoor(),
                configuration
        );
    }

    private LBC withBarReaders(LBC source, int secondReaderQuantity) {
        EquipmentConfiguration equipment =
                source.getControlConfiguration().getEquipmentConfiguration();
        equipment.addRequiredEquipment(BarReader.READER_1D, 2);
        equipment.addAdditionalEquipment(
                BarReader.READER_2D,
                secondReaderQuantity
        );

        ControlConfiguration configuration = new ControlConfiguration(
                source.getControlConfiguration().getPayment(),
                source.getControlConfiguration().getAccessMethods(),
                source.getControlConfiguration().getPrintOptions(),
                equipment
        );

        return new LBC(
                source.getHeight(),
                source.getWidth(),
                source.getDepth(),
                source.getUpperFrame(),
                source.getBottomFrame(),
                source.getColorBody(),
                source.getColorDoor(),
                source.getTypeLb(),
                source.getCountCells(),
                source.getDirectionDoorOpening(),
                source.getDoorThickness(),
                configuration
        );
    }
}
