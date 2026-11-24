package com.lb_calc_web.service;

import com.lb_calc_web.domain.equipment.*;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.entity.EquipmentEntity;
import com.lb_calc_web.mapper.entity.EquipmentEntityMapper;
import com.lb_calc_web.repository.EquipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentCatalogServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    private EquipmentCatalogService service;

    @BeforeEach
    void setUp() {
        service = new EquipmentCatalogService(equipmentRepository);
    }

    @Test
    void save_shouldCreateNewCatalogEntity() {
        Equipment equipment = equipment(null);
        EquipmentEntity persisted = EquipmentEntity.create();
        EquipmentEntityMapper.updateEntity(equipment, persisted);

        when(equipmentRepository.saveAndFlush(any(EquipmentEntity.class))).thenReturn(persisted);

        Equipment result = service.save(equipment);

        assertEquals(equipment.getName(), result.getName());
        verify(equipmentRepository).saveAndFlush(any(EquipmentEntity.class));
    }

    @Test
    void save_shouldUpdateExistingCatalogEntity() {
        Equipment equipment = equipment(12L);
        EquipmentEntity entity = EquipmentEntity.create();
        EquipmentEntityMapper.updateEntity(equipment, entity);
        when(equipmentRepository.findById(12L)).thenReturn(Optional.of(entity));
        when(equipmentRepository.saveAndFlush(entity)).thenReturn(entity);

        Equipment result = service.save(equipment);

        assertEquals("Scanner", result.getName());
        verify(equipmentRepository).saveAndFlush(entity);
    }

    @Test
    void delete_shouldRejectEquipmentUsedByControlModule() {
        EquipmentEntity entity = EquipmentEntity.create();
        when(equipmentRepository.findById(12L)).thenReturn(Optional.of(entity));
        when(equipmentRepository.countModuleReferencesByEquipmentId(12L)).thenReturn(1L);

        assertThrows(IllegalStateException.class, () -> service.delete(12L));
        verify(equipmentRepository, never()).delete(any(EquipmentEntity.class));
    }

    @Test
    void delete_shouldRemoveUnusedEquipment() {
        EquipmentEntity entity = EquipmentEntity.create();
        when(equipmentRepository.findById(12L)).thenReturn(Optional.of(entity));
        when(equipmentRepository.countModuleReferencesByEquipmentId(12L)).thenReturn(0L);

        service.delete(12L);

        verify(equipmentRepository).delete(entity);
        verify(equipmentRepository).flush();
    }

    private Equipment equipment(Long id) {
        return new Equipment(id, EquipmentFunctionalType.ACCESS, AccessPurpose.QR_CODE,
                EquipmentCategory.BAR_READER, "Scanner", "2D scanner", 100, 50, 30);
    }
}
