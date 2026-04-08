package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.model.ALS;
import com.lb_calc_web.repository.ALSRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ALSServiceTest {

    @Mock
    private ALSRepository alsRepository;

    @Mock
    private LBService lbService;

    @Mock
    private LCService lcService;

    @Mock
    private ALSLBService alslbService;

    @InjectMocks
    private ALSService alsService;

    private ALSDTO validALS;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();

        validALS = TestDataFactory.validALSDTO(1L);
    }

    // =============================
    // findAll
    // =============================

    @Test
    void findAll_shouldReturnList() {
        when(alsRepository.findAll()).thenReturn(List.of(new ALS(), new ALS()));

        List<ALSDTO> result = alsService.findAll();

        assertEquals(2, result.size());
    }

    // =============================
    // findById
    // =============================

    @Test
    void findById_existing_shouldReturnDTO() {
        ALS als = TestDataFactory.validALS(1L);

        when(alsRepository.findById(1L)).thenReturn(Optional.of(als));

        ALSDTO result = alsService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_notFound_shouldThrow() {
        when(alsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> alsService.findById(1L));
    }

    // =============================
    // createALS
    // =============================

    @Test
    void createALS_shouldCreateValidStructure() {
        when(lcService.createLC(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(TestDataFactory.validLCDTO(10L));

        when(lbService.createLB(anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(TestDataFactory.validLBDTO(20L));

        ALSDTO result = alsService.createALS();

        assertNotNull(result);
        assertNotNull(result.getLC());
        assertFalse(result.getLbList().isEmpty());
        assertNotNull(result.getName());
    }

    // =============================
    // saveALS SUCCESS
    // =============================

    @Test
    void saveALS_valid_shouldSave() {
        when(lcService.saveLC(any())).thenReturn(TestDataFactory.validLCDTO(10L));
        when(lbService.saveLB(any())).thenReturn(TestDataFactory.validLBDTO(20L));

        when(alsRepository.findOne(any())).thenReturn(Optional.empty());

        ALS saved = TestDataFactory.validALS(100L);

        when(alsRepository.save(any())).thenReturn(saved);

        ALSDTO result = alsService.saveALS(validALS);

        assertEquals(100L, result.getId());
        verify(alsRepository).save(any());
    }

    // =============================
    // saveALS EXISTING
    // =============================

    @Test
    void saveALS_existing_shouldReturnExisting() {
        when(lcService.saveLC(any())).thenReturn(TestDataFactory.validLCDTO(10L));
        when(lbService.saveLB(any())).thenReturn(TestDataFactory.validLBDTO(20L));

        ALS existing = TestDataFactory.validALS(50L);

        when(alsRepository.findOne(any())).thenReturn(Optional.of(existing));

        ALSDTO result = alsService.saveALS(validALS);

        assertEquals(50L, result.getId());
        verify(alsRepository, never()).save(any());
    }

    // =============================
    // saveALS VALIDATION ERROR
    // =============================

    @Test
    void saveALS_invalidALS_shouldThrow() {
        validALS.setHeight(10); // нарушаем валидатор

        when(lcService.saveLC(any())).thenReturn(TestDataFactory.validLCDTO(10L));
        when(lbService.saveLB(any())).thenReturn(TestDataFactory.validLBDTO(20L));

        assertThrows(ValidationSizeException.class,
                () -> alsService.saveALS(validALS));
    }

    @Test
    void saveALS_invalidLC_shouldThrow() {
        ValidationResult lcValidation = new ValidationResult();
        lcValidation.addError("lc", "LC invalid", null, null, null);

        when(lcService.saveLC(any()))
                .thenThrow(new ValidationSizeException(lcValidation));

        when(lbService.saveLB(any()))
                .thenReturn(TestDataFactory.validLBDTO(20L));

        ValidationSizeException ex = assertThrows(
                ValidationSizeException.class,
                () -> alsService.saveALS(validALS)
        );

        assertFalse(ex.getValidationResult().isValid());
        assertEquals(1, ex.getValidationResult().getErrorCount());
    }

    @Test
    void saveALS_invalidLB_shouldThrow() {
        ValidationResult lbValidation = new ValidationResult();
        lbValidation.addError("lb", "LB invalid", null, null, null);

        when(lcService.saveLC(any()))
                .thenReturn(TestDataFactory.validLCDTO(10L)); // LC валидный

        when(lbService.saveLB(any()))
                .thenThrow(new ValidationSizeException(lbValidation));

        ValidationSizeException ex = assertThrows(
                ValidationSizeException.class,
                () -> alsService.saveALS(validALS)
        );

        assertFalse(ex.getValidationResult().isValid());
        assertFalse(ex.getValidationResult().isValid());
        assertTrue(ex.getValidationResult().getErrorCount() > 0);
    }

    // =============================
    // resizeLC
    // =============================

    @Test
    void resizeLC_shouldUpdateFields() {
        ALSDTO result = alsService.resizeLC(validALS);

        LCDTO lc = result.getLC();

        assertEquals(validALS.getHeight(), lc.getHeight());
        assertEquals(validALS.getDepth(), lc.getDepth());
    }

    // =============================
    // resizeLBs
    // =============================

    @Test
    void resizeLBs_shouldUpdateLBs() {
        alsService.resizeLBs(validALS);

        for (LBDTO lb : validALS.getLbList()) {
            assertEquals(validALS.getHeight(), lb.getHeight());
            assertEquals(validALS.getDepth(), lb.getDepth());
        }
    }

    // =============================
    // addLB
    // =============================

    @Test
    void addLB_shouldAddAndUpdateQuantity() {
        int initialSize = validALS.getLbList().size();

        LBDTO newLB = TestDataFactory.validLBDTO(999L);

        alsService.addLB(validALS, newLB);

        assertEquals(initialSize + 1, validALS.getLbList().size());
        assertTrue(validALS.getQuantityLB().containsKey(newLB));
    }

    // =============================
    // deleteLB
    // =============================

    @Test
    void deleteLB_shouldRemoveLB() {
        when(lbService.saveLB(any()))
                .thenReturn(TestDataFactory.validLBDTO(20L));

        when(lcService.saveLC(any()))
                .thenReturn(TestDataFactory.validLCDTO(10L));

        when(alsRepository.save(any()))
                .thenReturn(TestDataFactory.validALS(100L));
        LBDTO lb = validALS.getLbList().get(0);

        when(lbService.findById(lb.getId())).thenReturn(lb);

        ALSService spyService = Mockito.spy(alsService);
        doReturn(validALS).when(spyService).findById(any());

        ALSDTO result = spyService.deleteLBandSaveALS(1L, lb.getId());

        assertFalse(result.getLbList().contains(lb));
    }

    // =============================
    // replaceLB
    // =============================

    @Test
    void replaceLB_shouldReplace() {
        LBDTO oldLB = validALS.getLbList().get(0);
        LBDTO newLB = TestDataFactory.validLBDTO(999L);

        ALSService spyService = Mockito.spy(alsService);
        doReturn(validALS).when(spyService).findById(any());
        doReturn(validALS).when(spyService).saveALS(any());

        List<Object> result = spyService.replaceLBandSaveALS(1L, oldLB.getId(), newLB);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // =============================
    // replaceLC
    // =============================

    @Test
    void replaceLC_shouldReplace() {
        LCDTO newLC = TestDataFactory.validLCDTO(999L);

        ALSService spyService = Mockito.spy(alsService);
        doReturn(validALS).when(spyService).saveALS(any());

        ALSDTO result = spyService.replaceLCandSaveALS(validALS, newLC);

        assertEquals(newLC, result.getLC());
    }

    // =============================
    // getOptionalALS
    // =============================

    @Test
    void getOptionalALS_shouldCallRepository() {
        when(alsRepository.findOne(any())).thenReturn(Optional.of(new ALS()));

        Optional<ALS> result = alsService.getOptionalALS(new ALS());

        assertTrue(result.isPresent());
        verify(alsRepository).findOne(any());
    }
}