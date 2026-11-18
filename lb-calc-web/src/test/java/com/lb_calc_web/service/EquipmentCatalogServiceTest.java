package com.lb_calc_web.service;

import com.lb_calc_web.entity.EquipmentCatalogEntity;
import com.lb_calc_web.repository.EquipmentCatalogRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentCatalogServiceTest {

    private final EquipmentCatalogRepository repository = mock(EquipmentCatalogRepository.class);
    private final EquipmentCatalogService service = new EquipmentCatalogService(repository);

    @Test
    void setActive_shouldRejectDeactivatingOnlyActiveDefault() {
        EquipmentCatalogEntity item = defaultItem("COMPUTER_1");
        when(repository.findById("COMPUTER_1")).thenReturn(Optional.of(item));
        when(repository
                .findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc("COMPUTER"))
                .thenReturn(List.of(item));

        assertThrows(IllegalStateException.class,
                () -> service.setActive("COMPUTER_1", false));

        assertTrue(item.isActive());
        assertTrue(item.isDefaultPosition());
    }

    @Test
    void setDefault_shouldRejectRemovingOnlyActiveDefault() {
        EquipmentCatalogEntity item = defaultItem("COMPUTER_1");
        when(repository.findById("COMPUTER_1")).thenReturn(Optional.of(item));
        when(repository
                .findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc("COMPUTER"))
                .thenReturn(List.of(item));

        assertThrows(IllegalStateException.class,
                () -> service.setDefault("COMPUTER_1", false));

        assertTrue(item.isDefaultPosition());
    }

    @Test
    void setDefault_shouldReplacePreviousDefault() {
        EquipmentCatalogEntity current = defaultItem("COMPUTER_1");
        EquipmentCatalogEntity replacement = item("COMPUTER_2");
        when(repository.findById("COMPUTER_2")).thenReturn(Optional.of(replacement));
        when(repository
                .findAllByCategoryAndActiveTrueAndDefaultPositionTrueOrderByNameAsc("COMPUTER"))
                .thenReturn(List.of(current));

        service.setDefault("COMPUTER_2", true);

        assertFalse(current.isDefaultPosition());
        assertTrue(replacement.isDefaultPosition());
        verify(repository).flush();
    }

    private EquipmentCatalogEntity defaultItem(String id) {
        EquipmentCatalogEntity item = item(id);
        item.setDefaultPosition(true);
        return item;
    }

    private EquipmentCatalogEntity item(String id) {
        return new EquipmentCatalogEntity(id, "COMPUTER", id, "", 0, 0, 0,
                0, 0, 0, false, false, false, false, true);
    }
}
