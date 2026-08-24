package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.mapper.LBMapper;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.repository.LBRepository;
import com.lb_calc_web.service.util.SizeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LBServiceTest {

    @Mock
    private LBRepository lbRepository;

    @InjectMocks
    private LBService lbService;

    private LBDTO testDTO;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();
        testDTO = TestDataFactory.validLBDTO(1L);
    }

    // -----------------------------
    // createLB
    // -----------------------------

    @Test
    void createLB_shouldCreateDefaultLB() {
        LBDTO lb = lbService.createLB();

        assertNotNull(lb);
        assertEquals(1940, lb.getHeight());
        assertEquals(500, lb.getWidth());
        assertEquals("TYPE1", lb.getType());
        assertNotNull(lb.getDescription());
    }

    // -----------------------------
    // updateLBsizeAndDescription
    // -----------------------------

    @Test
    void updateLBsize_shouldCalculateFieldsCorrectly() {
        testDTO.setCountCells(4);

        lbService.updateLBsizeAndDescription(testDTO);

        assertTrue(testDTO.getHeightCell() > 0);
        assertTrue(testDTO.getWidthCell() > 0);
        assertTrue(testDTO.getDepthCell() > 0);
        assertTrue(testDTO.getDescription().contains("Модуль хранения"));
    }

    @Test
    void updateLBsize_shouldHandleInvalidType() {
        testDTO.setType("INVALID");

        assertDoesNotThrow(() ->
                lbService.updateLBsizeAndDescription(testDTO)
        );

        assertNotNull(testDTO.getDescription());
    }

    // -----------------------------
    // saveLB (existing)
    // -----------------------------

    @Test
    void saveLB_shouldReturnExisting_whenFound() {
        LB lbEntity = LBMapper.toLB(testDTO);

        when(lbRepository.findOne(any())).thenReturn(Optional.of(lbEntity));

        LBDTO result = lbService.saveLB(testDTO);

        assertNotNull(result);
        verify(lbRepository, never()).save(any());
    }

    // -----------------------------
    // saveLB (new)
    // -----------------------------

    @Test
    void saveLB_shouldSaveNew_whenNotFound() {
        LB saved = LBMapper.toLB(testDTO);
        saved.setId(1L);

        when(lbRepository.findOne(any())).thenReturn(Optional.empty());
        when(lbRepository.save(any())).thenReturn(saved);

        LBDTO result = lbService.saveLB(testDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(lbRepository).save(any());
    }
    // -----------------------------
    // saveLB - invalid LB (SizeValidator)
    // -----------------------------
    @Test
    void saveLB_shouldThrowValidationSizeException_whenInvalid() {
        LBDTO invalidLB = lbService.createLB();
        invalidLB.setHeight(10);
        try (MockedStatic<SizeValidator> validatorMockedStatic = mockStatic(SizeValidator.class)) {
            ValidationResult invalidResult = new ValidationResult("LB", invalidLB.getId());
            invalidResult.addError("height", "too small", invalidLB.getHeight(), 100, 3000);
            validatorMockedStatic.when(() -> SizeValidator.validateLB(any())).thenReturn(invalidResult);

            ValidationSizeException exception = assertThrows(ValidationSizeException.class,
                    () -> lbService.saveLB(invalidLB));

            assertEquals(1, exception.getValidationResult().getErrorCount());
        }
    }

    // -----------------------------
    // findById
    // -----------------------------

    @Test
    void findById_shouldReturnLB() {
        LB lb = LBMapper.toLB(testDTO);
        lb.setId(1L);

        when(lbRepository.findById(1L)).thenReturn(Optional.of(lb));

        LBDTO result = lbService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(lbRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                lbService.findById(1L)
        );
    }

    // -----------------------------
    // findAll
    // -----------------------------

    @Test
    void findAll_shouldReturnSortedList() {
        LB lb1 = LBMapper.toLB(testDTO);
        lb1.setId(2L);

        LB lb2 = LBMapper.toLB(testDTO);
        lb2.setId(1L);

        when(lbRepository.findAll()).thenReturn(List.of(lb1, lb2));

        List<LBDTO> result = lbService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() < result.get(1).getId());
    }

    // -----------------------------
    // getOptionalLB
    // -----------------------------

    @Test
    void getOptionalLB_shouldCallRepository() {
        LB lb = LBMapper.toLB(testDTO);

        when(lbRepository.findOne(any())).thenReturn(Optional.of(lb));

        Optional<LB> result = lbService.getOptionalLB(lb);

        assertTrue(result.isPresent());
        verify(lbRepository).findOne(any());
    }
}