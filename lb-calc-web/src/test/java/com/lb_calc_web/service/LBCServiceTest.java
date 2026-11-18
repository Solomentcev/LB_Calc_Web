package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.entity.LBCEntity;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.entity.LBCEntityMapper;
import com.lb_calc_web.repository.LBCRepository;
import com.lb_calc_web.domain.equipment.EquipmentCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LBCServiceTest {

    private FindOrCreateLockService findOrCreateLockService;
    private LBCRepository lbcRepository;
    private MockEnvironment environment;
    private LBCService lbcService;
    private EquipmentCatalogService equipmentCatalogService;
    private EquipmentRequirementResolver equipmentRequirementResolver;

    @BeforeEach
    void setUp() {

        lbcRepository = mock(LBCRepository.class);

        findOrCreateLockService = mock(FindOrCreateLockService.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);
        equipmentRequirementResolver = mock(EquipmentRequirementResolver.class);
        when(equipmentRequirementResolver.resolve(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(equipmentCatalogService.resolve(anyString()))
                .thenAnswer(invocation -> EquipmentCatalog.resolve(invocation.getArgument(0)));
        when(equipmentCatalogService.resolveById(anyString()))
                .thenAnswer(invocation -> EquipmentCatalog.findByCatalogPositionId(invocation.getArgument(0)));

        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.width.default", "500")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("size.count.cells.default", "3")
                .withProperty("size.door.thickness.default", "20")
                .withProperty("lbc.type.default", "TYPE1")
                .withProperty("lbc.display.default", "LC10")
                .withProperty("lbc.bar-reader.default", "Без сканера")
                .withProperty("lbc.payment.default", "NONE")
                .withProperty("lbc.printer.default", "false")
                .withProperty("lbc.rfid-reader.default", "true")
                .withProperty("lbc.color.body.default", "Blue")
                .withProperty("lbc.color.door.default", "White")
                .withProperty("lbc.direction.default", "LEFT")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");

        lbcService =
                new LBCService(
                        lbcRepository,
                        findOrCreateLockService,
                        environment,
                        TestDataFactory.createSizeValidator(),
                        equipmentCatalogService,
                        equipmentRequirementResolver
                );
    }

    @Test
    void createLBC_shouldCreateValidDefault() {
        LBCDTO result =
                lbcService.createLBC();

        assertNotNull(result);
        assertEquals(1940, result.getHeight());
        assertEquals(500, result.getWidth());
        assertEquals(3, result.getCountCells());
        assertEquals("TYPE1", result.getType());
        assertEquals("LC10", result.getDisplay());
        assertEquals(1, result.getDisplayQuantity());
        assertEquals(0, result.getBarReaderQuantity());
        assertEquals(0, result.getPrinterQuantity());
        assertEquals(1, result.getRfidReaderQuantity());
        assertNotNull(result.getStringLBCImage());
    }

    @Test
    void saveLBC_shouldPersistNewConfiguration() {
        LBCDTO dto =
                TestDataFactory.validLBCDTO(1L);

        LBCEntity saved =
                LBCEntityMapper.toEntity(
                        TestDataFactory.validLBC(1L)
                );

        when(lbcRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), anyString(), anyInt(), anyInt(), anyInt(),
                anyInt(), any(), anyInt(), any()
        )).thenReturn(List.of());

        when(lbcRepository.save(any(LBCEntity.class)))
                .thenReturn(saved);

        LBCDTO result =
                lbcService.saveLBC(dto);

        assertNotNull(result);
        verify(lbcRepository)
                .save(any(LBCEntity.class));
    }

    @Test
    void saveLBC_shouldReturnExistingByConfiguration() {
        LBCDTO dto =
                TestDataFactory.validLBCDTO(1L);

        LBCEntity existing =
                LBCEntityMapper.toEntity(
                        TestDataFactory.validLBC(1L)
                );

        when(lbcRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any(), anyString(), anyInt(), anyInt(), anyInt(),
                anyInt(), any(), anyInt(), any()
        )).thenReturn(List.of(existing));

        LBCDTO result =
                lbcService.saveLBC(dto);

        assertNotNull(result);
        verify(
                lbcRepository,
                never()
        ).save(any());
    }

    @Test
    void saveLBC_widthBelowEquipmentRequirement_shouldThrowValidationException() {
        LBCDTO dto = TestDataFactory.validLBCDTO(1L);
        dto.setWidth(159);

        assertThrows(
                ValidationSizeException.class,
                () -> lbcService.saveLBC(dto)
        );

        verify(lbcRepository, never()).save(any());
    }

    @Test
    void saveLBC_invalid_shouldThrowValidationException() {
        LBCDTO dto =
                TestDataFactory.validLBCDTO(1L);

        dto.setHeight(10);

        assertThrows(
                ValidationSizeException.class,
                () -> lbcService.saveLBC(dto)
        );

        verify(
                lbcRepository,
                never()
        ).save(any());
    }

    @Test
    void saveLBC_missingRequiredColor_shouldReturnValidationError() {
        LBCDTO dto = TestDataFactory.validLBCDTO(43L);
        dto.setColorBody(null);

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> lbcService.saveLBC(dto)
        );

        var error = exception.getValidationResult().getErrors().get(0);
        assertEquals("LBC", error.getObjectType());
        assertEquals(43L, error.getObjectId());
        assertEquals("domainModel", error.getField());
        verify(lbcRepository, never()).save(any());
    }

    @Test
    void findById_existing_shouldReturnDto() {
        LBCEntity entity =
                spy(
                        LBCEntityMapper.toEntity(
                                TestDataFactory.validLBC(1L)
                        )
                );

        doReturn(1L)
                .when(entity)
                .getId();

        when(lbcRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        LBCDTO result =
                lbcService.findById(1L);

        assertEquals(
                1L,
                result.getId()
        );
        assertNotNull(
                result.getStringLBCImage()
        );
    }

    @Test
    void findById_missing_shouldThrow() {
        when(lbcRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> lbcService.findById(1L)
        );
    }

    @Test
    void findAll_shouldReturnSortedById() {
        LBCEntity first =
                spy(
                        LBCEntityMapper.toEntity(
                                TestDataFactory.validLBC(1L)
                        )
                );
        LBCEntity second =
                spy(
                        LBCEntityMapper.toEntity(
                                TestDataFactory.validLBC(2L)
                        )
                );

        doReturn(1L).when(first).getId();
        doReturn(2L).when(second).getId();

        when(lbcRepository.findAll())
                .thenReturn(
                        List.of(
                                second,
                                first
                        )
                );

        List<LBCDTO> result =
                lbcService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }
}
