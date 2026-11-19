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


    @Test
    void findActiveNamesByCategory_shouldIncludeNoDisplayAndOnlyActiveCatalogModels() {
        EquipmentCatalogEntity active = new EquipmentCatalogEntity(
                "DISPLAY_CUSTOM", "DISPLAY", "Дисплей пользовательский", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, true);
        when(repository.findAllByCategoryAndActiveTrueOrderByNameAsc("DISPLAY"))
                .thenReturn(List.of(active));

        List<String> result = service.findActiveNamesByCategory("DISPLAY");

        assertEquals(List.of("Без дисплея", "Дисплей пользовательский"), result);
        verify(repository).findAllByCategoryAndActiveTrueOrderByNameAsc("DISPLAY");
    }

    @Test
    void findActiveNamesByCategory_shouldIncludeNoBarcodeReader() {
        when(repository.findAllByCategoryAndActiveTrueOrderByNameAsc("BAR_READER"))
                .thenReturn(List.of());

        List<String> result = service.findActiveNamesByCategory("BAR_READER");

        assertEquals(List.of("Без сканера"), result);
    }


    @Test
    void findDomainCatalogResolver_shouldResolveInactiveHistoricalEquipmentFromSingleCatalogLoad() {
        EquipmentCatalogEntity inactive = new EquipmentCatalogEntity(
                "DISPLAY_OLD", "DISPLAY", "Старый дисплей", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, false);
        when(repository.findAll()).thenReturn(List.of(inactive));

        var resolver = service.findDomainCatalogResolver();

        assertEquals("Старый дисплей", resolver.apply("DISPLAY_OLD").getName());
        verify(repository).findAll();
        verify(repository, never()).findById("DISPLAY_OLD");
    }

    @Test
    void findDomainCatalogResolver_shouldRejectUnknownEquipmentId() {
        when(repository.findAll()).thenReturn(List.of());

        var resolver = service.findDomainCatalogResolver();

        assertThrows(IllegalArgumentException.class,
                () -> resolver.apply("UNKNOWN"));
    }


    @Test
    void findExistingConfigurationResolver_shouldAllowRetainedInactiveEquipment() {
        EquipmentCatalogEntity inactive = new EquipmentCatalogEntity(
                "DISPLAY_OLD", "DISPLAY", "Старый дисплей", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, false);
        when(repository.findAll()).thenReturn(List.of(inactive));

        var resolver = service.findExistingConfigurationResolver(
                java.util.Set.of("DISPLAY_OLD"));

        assertEquals("DISPLAY_OLD", resolver.apply("DISPLAY_OLD").getCatalogPositionId());
        verify(repository).findAll();
        verify(repository, never()).findById("DISPLAY_OLD");
    }

    @Test
    void findExistingConfigurationResolver_shouldRejectNewSelectionOfInactiveEquipment() {
        EquipmentCatalogEntity inactive = new EquipmentCatalogEntity(
                "DISPLAY_OLD", "DISPLAY", "Старый дисплей", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, false);
        when(repository.findAll()).thenReturn(List.of(inactive));

        var resolver = service.findExistingConfigurationResolver(java.util.Set.of());

        assertThrows(IllegalArgumentException.class,
                () -> resolver.apply("DISPLAY_OLD"));
    }

    @Test
    void create_shouldRejectInactiveDefaultEquipment() {
        EquipmentCatalogEntity item = new EquipmentCatalogEntity(
                "DISPLAY_OLD", "DISPLAY", "Старый дисплей", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, false);
        item.setDefaultPosition(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(item));
        verify(repository, never()).save(any());
    }


    @Test
    void findExistingConfigurationResolver_shouldPreferRetainedModelWhenNamesMatch() {
        EquipmentCatalogEntity inactive = new EquipmentCatalogEntity(
                "DISPLAY_OLD", "DISPLAY", "Одинаковое название", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, false);
        EquipmentCatalogEntity active = new EquipmentCatalogEntity(
                "DISPLAY_NEW", "DISPLAY", "Одинаковое название", "", 0, 0, 0,
                0, 0, 300, false, false, false, false, true);
        when(repository.findAll()).thenReturn(List.of(inactive, active));

        var resolver = service.findExistingConfigurationResolver(
                java.util.Set.of("DISPLAY_OLD"));

        assertEquals("DISPLAY_OLD", resolver.apply("Одинаковое название").getCatalogPositionId());
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
