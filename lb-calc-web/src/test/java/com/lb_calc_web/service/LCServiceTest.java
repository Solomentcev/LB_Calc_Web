package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.entity.LCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.dto.LCDtoMapper;
import com.lb_calc_web.mapper.entity.LCEntityMapper;
import com.lb_calc_web.repository.LCRepository;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LCServiceTest {

    private FindOrCreateLockService findOrCreateLockService;
    private LCRepository lcRepository;
    private MockEnvironment environment;
    private LCService lcService;
    private EquipmentCatalogService equipmentCatalogService;
    private EquipmentRequirementResolver equipmentRequirementResolver;

    @BeforeEach
    void setUp() {

        lcRepository = mock(LCRepository.class);

        findOrCreateLockService = mock(FindOrCreateLockService.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);
        equipmentRequirementResolver = mock(EquipmentRequirementResolver.class);
        when(equipmentRequirementResolver.resolve(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(equipmentCatalogService.resolve(anyString()))
                .thenAnswer(invocation -> EquipmentCatalog.resolve(invocation.getArgument(0)));
        when(equipmentCatalogService.findDomainCatalogResolver())
                .thenReturn(EquipmentCatalog::findByCatalogPositionId);
        when(equipmentCatalogService.findExistingConfigurationResolver(anySet()))
                .thenReturn(EquipmentCatalog::resolve);

        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("lc.display.default", "LC10")
                .withProperty("lc.bar-reader.default", "Без сканера")
                .withProperty("lc.payment.default", "NONE")
                .withProperty("lc.printer.default", "false")
                .withProperty("lc.rfid-reader.default", "true")
                .withProperty("lc.access-methods.default", "RFID")
                .withProperty("lc.color.body.default", "Blue")
                .withProperty("lc.color.door.default", "White");

        lcService =
                new LCService(
                        lcRepository,
                        findOrCreateLockService,
                        environment,
                        TestDataFactory.createSizeValidator(),
                        equipmentCatalogService,
                        equipmentRequirementResolver
                );
    }

    @Test
    void createLC_shouldCreateValidDefault() {
        LCDTO result = lcService.createLC();

        assertNotNull(result);
        assertEquals(1940, result.getHeight());
        assertEquals(500, result.getDepth());
        assertEquals("LC10", result.getDisplay());
        assertEquals(1, result.getDisplayQuantity());
        assertEquals(0, result.getBarReaderQuantity());
        assertEquals(0, result.getPrinterQuantity());
        assertEquals(1, result.getRfidReaderQuantity());
        assertTrue(result.getAccessMethods().contains("RFID"));
        assertTrue(result.getWidth() > 0);
        assertNotNull(result.getDescription());
    }

    @Test
    void saveLC_shouldPreserveExistingCatalogEquipmentWhenFormOmitsSelections() {
        LCEntity existing = new LCEntity();
        existing.setEquipment(java.util.Set.of(
                new com.lb_calc_web.entity.EquipmentQuantityEmbeddable("DISPLAY_LC17", 1),
                new com.lb_calc_web.entity.EquipmentQuantityEmbeddable("MODEM_CUSTOM", 2)
        ));
        when(lcRepository.findById(1L)).thenReturn(Optional.of(existing));
        com.lb_calc_web.domain.equipment.CatalogEquipment modem =
                new com.lb_calc_web.domain.equipment.CatalogEquipment(
                        "MODEM_CUSTOM", "MODEM", "Модем", "", 0, 0, 0,
                        0, 0, 0, false, false, false, false
                );
        when(equipmentCatalogService.findExistingConfigurationResolver(anySet()))
                .thenAnswer(invocation -> id -> "MODEM_CUSTOM".equals(id)
                        ? modem
                        : EquipmentCatalog.resolve(id));

        LCDTO dto = TestDataFactory.validLCDTO(1L);
        dto.setColorBody(null);

        assertThrows(ValidationSizeException.class, () -> lcService.saveLC(dto));

        verify(equipmentCatalogService).findExistingConfigurationResolver(anySet());
        verify(lcRepository, times(1)).findById(1L);
    }

    @Test
    void saveLC_missingRequiredColor_shouldReturnValidationError() {
        LCDTO dto = TestDataFactory.validLCDTO(42L);
        dto.setColorBody(null);

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> lcService.saveLC(dto)
        );

        var error = exception.getValidationResult().getErrors().get(0);
        assertEquals("LC", error.getObjectType());
        assertEquals(42L, error.getObjectId());
        assertEquals("domainModel", error.getField());
        verify(lcRepository, never()).save(any());
    }

    @Test
    void findAll_shouldReturnSortedById() {
        LCEntity first =
                spy(
                        LCEntityMapper.toEntity(
                                LCDtoMapper.toDomain(
                                        TestDataFactory.validLCDTO(1L)
                                )
                        )
                );
        LCEntity second =
                spy(
                        LCEntityMapper.toEntity(
                                LCDtoMapper.toDomain(
                                        TestDataFactory.validLCDTO(2L)
                                )
                        )
                );

        doReturn(1L).when(first).getId();
        doReturn(2L).when(second).getId();

        when(lcRepository.findAll())
                .thenReturn(List.of(second, first));

        List<LCDTO> result = lcService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }

    @Test
    void findById_existing_shouldReturnDto() {
        LCEntity entity =
                spy(
                        LCEntityMapper.toEntity(
                                LCDtoMapper.toDomain(
                                        TestDataFactory.validLCDTO(1L)
                                )
                        )
                );

        doReturn(1L).when(entity).getId();

        when(lcRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        LCDTO result = lcService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_invalidId_shouldRejectWithoutRepositoryCall() {
        assertThrows(NoSuchElementException.class, () -> lcService.findById(0L));
        assertThrows(NoSuchElementException.class, () -> lcService.findById(null));
        verify(lcRepository, never()).findById(any());
    }

    @Test
    void findById_missing_shouldThrow() {
        when(lcRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> lcService.findById(1L)
        );
    }

    @Test
    void saveLC_shouldReturnExistingByConfiguration() {
        LCDTO dto = TestDataFactory.validLCDTO(1L);

        LCEntity existing =
                LCEntityMapper.toEntity(
                        LCDtoMapper.toDomain(dto)
                );

        when(lcRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), any()
        )).thenReturn(List.of(existing));

        LCDTO result = lcService.saveLC(dto);

        assertNotNull(result);
        verify(lcRepository, never()).save(any());
    }

    @Test
    void saveLC_shouldPersistNewConfiguration() {
        LCDTO dto = TestDataFactory.validLCDTO(1L);
        LCEntity saved =
                LCEntityMapper.toEntity(
                        LCDtoMapper.toDomain(dto)
                );

        when(lcRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), any()
        )).thenReturn(List.of());
        when(lcRepository.save(any(LCEntity.class)))
                .thenReturn(saved);

        LCDTO result = lcService.saveLC(dto);

        assertNotNull(result);
        verify(lcRepository).save(any(LCEntity.class));
    }

    @Test
    void saveLC_widthBelowEquipmentRequirement_shouldThrowValidationException() {
        LCDTO dto = TestDataFactory.validLCDTO(1L);
        dto.setWidth(100);

        assertThrows(
                ValidationSizeException.class,
                () -> lcService.saveLC(dto)
        );

        verify(lcRepository, never()).save(any());
    }

    @Test
    void saveLC_invalid_shouldThrowValidationException() {
        LCDTO dto = TestDataFactory.validLCDTO(1L);
        dto.setHeight(10);

        assertThrows(
                ValidationSizeException.class,
                () -> lcService.saveLC(dto)
        );

        verify(lcRepository, never()).save(any());
    }
}
