package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.ALSEntity;
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
        TestDataFactory.initSizeValidator();

        alsRepository = mock(ALSRepository.class);
        moduleRepository = mock(ModuleEntityRepository.class);
        lbService = mock(LBService.class);
        lcService = mock(LCService.class);
        lbcService = mock(LBCService.class);

        environment = new MockEnvironment()
                .withProperty("als.position.control-module.default", "CENTER")
                .withProperty("als.position.lc.default", "CENTER");

        alsService =
                new ALSService(
                        alsRepository,
                        moduleRepository,
                        lbService,
                        lcService,
                        lbcService,
                        environment
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
    void saveALS_invalidHeight_shouldThrowBeforeRepositorySave() {
        ALSDTO als = TestDataFactory.validALSDTO(1L);
        als.setHeight(10);

        assertThrows(
                RuntimeException.class,
                () -> alsService.saveALS(als)
        );

        verify(alsRepository, never()).save(any());
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
