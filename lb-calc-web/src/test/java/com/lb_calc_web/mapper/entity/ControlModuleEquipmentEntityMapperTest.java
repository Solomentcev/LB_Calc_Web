package com.lb_calc_web.mapper.entity;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.model.EquipmentConfiguration;
import com.lb_calc_web.entity.EquipmentQuantityEmbeddable;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.entity.LCEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlModuleEquipmentEntityMapperTest {

    @Test
    void lc_shouldPreserveAllBarReaderPositionsAndQuantities() {
        var domain = TestDataFactory.validLC(1L);
        addBarReaders(domain.getControlConfiguration().getEquipmentConfiguration());

        LCEntity entity = LCEntityMapper.toEntity(domain);
        var restored = LCEntityMapper.toDomain(entity);

        assertEquals(2, entity.getBarReaders().size());
        assertEquals(domain, restored);
    }

    @Test
    void lbc_shouldPreserveAllBarReaderPositionsAndQuantities() {
        var domain = TestDataFactory.validLBC(1L);
        addBarReaders(domain.getControlConfiguration().getEquipmentConfiguration());

        LBCEntity entity = LBCEntityMapper.toEntity(domain);
        var restored = LBCEntityMapper.toDomain(entity);

        assertEquals(2, entity.getBarReaders().size());
        assertEquals(domain, restored);
    }


    @Test
    void equipmentPositionIdentity_shouldNotDependOnQuantity() {
        EquipmentQuantityEmbeddable one =
                new EquipmentQuantityEmbeddable("BAR_READER_1D", 1);
        EquipmentQuantityEmbeddable two =
                new EquipmentQuantityEmbeddable("BAR_READER_1D", 2);

        assertNotEquals(one, two);
        assertEquals(2, new HashSet<>(List.of(one, two)).size());

        assertThrows(
                IllegalArgumentException.class,
                () -> new LCEntity().setBarReaders(Set.of(one, two))
        );
    }

    private void addBarReaders(EquipmentConfiguration configuration) {
        configuration.addRequiredEquipment(BarReader.READER_1D, 2);
        configuration.addAdditionalEquipment(BarReader.READER_2D, 1);
    }
}
