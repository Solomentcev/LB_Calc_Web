package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ALSServiceTest {

    private ALSRepository alsRepository;
    private ModuleEntityRepository moduleRepository;
    private LBService lbService;
    private LCService lcService;
    private LBCService lbcService;
    private MockEnvironment environment;
    private ALSService alsService;

    @BeforeEach
    void setUp() {

        alsRepository = mock(ALSRepository.class);
        moduleRepository = mock(ModuleEntityRepository.class);
        lbService = mock(LBService.class);
        lcService = mock(LCService.class);
        lbcService = mock(LBCService.class);

        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("als.color.body.default", "Blue")
                .withProperty("als.color.door.default", "White")
                .withProperty("als.position.control-module.default", "CENTER")
                .withProperty("als.position.lc.default", "CENTER")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");

        alsService =
                new ALSService(
                        alsRepository,
                        moduleRepository,
                        lbService,
                        lcService,
                        lbcService,
                        environment,
                        TestDataFactory.createSizeValidator()
                );
    }

    @Test
    void createALS_shouldCreateLCWithLB() {
        when(lcService.createLC(
                anyInt(), anyInt(), anyInt(), anyInt(), any()
        )).thenReturn(
                TestDataFactory.validLCDTO(10L)
        );

        when(lbService.createLB(
                anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(
                TestDataFactory.validLBDTO(20L)
        );

        ALSDTO result = alsService.createALS();

        assertNotNull(result);
        assertNotNull(result.getLC());
        assertNull(result.getLBC());
        assertEquals(1, result.getLbList().size());
        assertNotNull(result.getStringALSImage());
    }

    @Test
    void saveALS_shouldReturnExistingByConfiguration() {
        ALSDTO dto = TestDataFactory.validALSDTO(1L);
        ALSEntity existing = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(1L),
                ModuleEntityMapper::toEntity
        );

        when(alsRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(existing));

        ALSDTO result = alsService.saveALS(dto);

        assertNotNull(result);
        verify(alsRepository, never()).save(any());
        verify(alsRepository, never()).findAll();
    }

    @Test
    void saveALS_shouldPropagateInvalidPersistedAlsCandidate() {
        ALSDTO dto = TestDataFactory.validALSDTO(1L);
        ALSEntity existing = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(1L),
                ModuleEntityMapper::toEntity
        );
        existing.setWidth(existing.getWidth() + 1);

        when(alsRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(existing));

        assertThrows(
                IllegalStateException.class,
                () -> alsService.saveALS(dto)
        );

        verify(alsRepository, never()).save(any());
        verifyNoInteractions(moduleRepository);
    }

    @Test
    void saveALS_shouldPropagateInvalidPersistedModuleCandidate() {
        ALSDTO dto = TestDataFactory.validALSDTO(1L);
        var domainModule = alsService.toDomain(dto).getModules().get(0);
        var existingModule = ModuleEntityMapper.toEntity(domainModule);
        existingModule.setDescription("Устаревшее описание");

        when(alsRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of());
        when(moduleRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(List.of(existingModule));

        assertThrows(
                IllegalStateException.class,
                () -> alsService.saveALS(dto)
        );

        verify(alsRepository, never()).save(any());
        verify(moduleRepository, never()).save(any());
    }

    @Test
    void saveALS_invalidHeight_shouldThrowBeforeRepositorySave() {
        ALSDTO als = TestDataFactory.validALSDTO(1L);
        als.setHeight(10);

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> alsService.saveALS(als)
        );

        var alsHeightError = exception.getValidationResult()
                .getErrors()
                .stream()
                .filter(error -> "ALS".equals(error.getObjectType())
                        && "height".equals(error.getField()))
                .findFirst()
                .orElseThrow();

        assertEquals(1L, alsHeightError.getObjectId());

        var lbError = exception.getValidationResult()
                .getErrors()
                .stream()
                .filter(error -> "LB".equals(error.getObjectType()))
                .findFirst()
                .orElseThrow();

        assertEquals(201L, lbError.getObjectId());
        verify(alsRepository, never()).save(any());
    }

    @Test
    void saveALS_missingRequiredColor_shouldReturnValidationError() {
        ALSDTO als = TestDataFactory.validALSDTO(51L);
        als.setColorBody(null);

        ValidationSizeException exception = assertThrows(
                ValidationSizeException.class,
                () -> alsService.saveALS(als)
        );

        var error = exception.getValidationResult().getErrors().get(0);
        assertEquals("ALS", error.getObjectType());
        assertEquals(51L, error.getObjectId());
        assertEquals("domainModel", error.getField());
        verify(alsRepository, never()).save(any());
    }

    @Test
    void findLBCInALS_shouldRejectIdNotBelongingToALS() {
        ALSDTO als = TestDataFactory.validLBCALSDTO(1L);

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> alsService.findLBCInALS(als, 999L)
        );
    }

    @Test
    void replaceLC_shouldRejectModuleIdNotBelongingToALS() {
        ALSDTO als = TestDataFactory.validALSDTO(1L);
        LCDTO anotherLC = TestDataFactory.validLCDTO(999L);

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> alsService.replaceLC(als, anotherLC)
        );
    }

    @Test
    void replaceWithNewLBC_shouldReplaceExistingLBC() {
        ALSDTO als = TestDataFactory.validLBCALSDTO(1L);
        when(lbcService.createLBC(
                anyInt(), anyInt(), anyInt(), anyInt(), any(), any()
        )).thenReturn(TestDataFactory.validLBCDTO(0L));

        ALSDTO result = alsService.replaceWithNewLBC(als);

        assertNotNull(result.getLBC());
        assertNull(result.getLC());
    }

    @Test
    void replaceLBC_shouldRejectModuleIdNotBelongingToALS() {
        ALSDTO als = TestDataFactory.validLBCALSDTO(1L);
        LBCDTO anotherLBC = TestDataFactory.validLBCDTO(999L);

        assertThrows(
                java.util.NoSuchElementException.class,
                () -> alsService.replaceLBC(als, anotherLBC)
        );
    }

    @Test
    void replaceLCandSaveALS_shouldReplaceControlModule() {
        ALSDTO als = TestDataFactory.validLBCALSDTO(1L);
        LCDTO lc = TestDataFactory.validLCDTO(2L);

        ALSService spy = spy(alsService);
        doReturn(als).when(spy).saveALS(any(ALSDTO.class));

        ALSDTO result =
                spy.replaceLCandSaveALS(als, lc);

        assertSame(lc, result.getLC());
        assertNull(result.getLBC());
    }

    @Test
    void replaceLBCandSaveALS_shouldReplaceControlAndKeepLBCAsStorage() {
        ALSDTO als = TestDataFactory.validALSDTO(1L);
        LBCDTO lbc = TestDataFactory.validLBCDTO(2L);

        ALSService spy = spy(alsService);
        doReturn(als).when(spy).saveALS(any(ALSDTO.class));

        ALSDTO result =
                spy.replaceLBCandSaveALS(als, lbc);

        assertSame(lbc, result.getLBC());
        assertNull(result.getLC());
    }
}
