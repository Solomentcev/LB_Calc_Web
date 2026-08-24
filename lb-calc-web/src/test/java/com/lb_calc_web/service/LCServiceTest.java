package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LCDTO;
import com.lb_calc_web.handler.ValidationSizeException;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.repository.LCRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LCServiceTest {

    @Mock
    private LCRepository lcRepository;

    @InjectMocks
    private LCService lcService;

    private LCDTO validLC;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();
        validLC = TestDataFactory.validLCDTO(1L);
    }


    // -----------------------------
    // createLC()
    // -----------------------------
    @Test
    void createLC_default_shouldReturnValidObject() {
        LCDTO lc = lcService.createLC();

        assertNotNull(lc);
        assertEquals(1940, lc.getHeight());
        assertEquals(500, lc.getDepth());
        assertNotNull(lc.getName());
        assertNotNull(lc.getDescription());
    }

    // -----------------------------
    // findAll()
    // -----------------------------
    @Test
    void findAll_shouldReturnSortedList() {
        LC lc1 =TestDataFactory.validLC(2L);
        LC lc2 = TestDataFactory.validLC(1L);

        when(lcRepository.findAll()).thenReturn(List.of(lc1, lc2));

        List<LCDTO> result = lcService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.get(0).getId() < result.get(1).getId());
    }

    // -----------------------------
    // findById()
    // -----------------------------
    @Test
    void findById_existing_shouldReturnDTO() {
        LC lc = TestDataFactory.validLC(1L);

        when(lcRepository.findById(1L)).thenReturn(Optional.of(lc));

        LCDTO result = lcService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findById_notFound_shouldThrow() {
        when(lcRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> lcService.findById(1L));
    }

    // -----------------------------
    // saveLC()
    // -----------------------------
    @Test
    void saveLC_valid_new_shouldSave() {
        LC saved = TestDataFactory.validLC(10L);

        when(lcRepository.findOne(any())).thenReturn(Optional.empty());
        when(lcRepository.save(any())).thenReturn(saved);

        LCDTO result = lcService.saveLC(validLC);

        assertNotNull(result);
        assertEquals(10L, result.getId());

        verify(lcRepository).save(any());
    }

    @Test
    void saveLC_existing_shouldReturnExisting() {
        LC existing = TestDataFactory.validLC(5L);

        when(lcRepository.findOne(any())).thenReturn(Optional.of(existing));

        LCDTO result = lcService.saveLC(validLC);

        assertEquals(5L, result.getId());
        verify(lcRepository, never()).save(any());
    }

    @Test
    void saveLC_invalid_shouldThrowValidationException() {
        validLC.setHeight(10); // некорректная высота

        assertThrows(ValidationSizeException.class,
                () -> lcService.saveLC(validLC));

        verify(lcRepository, never()).save(any());
    }

    // -----------------------------
    // updateLCsizeAndDescription()
    // -----------------------------
    @Test
    void updateLCsizeAndDescription_shouldSetFields() {
        lcService.updateLCsizeAndDescription(validLC);

        assertNotNull(validLC.getName());
        assertNotNull(validLC.getDescription());
        assertTrue(validLC.getWidth() > 0);
    }

    // -----------------------------
    // getOptionalLC()
    // -----------------------------
    @Test
    void getOptionalLC_shouldCallRepository() {
        LC lc = new LC();

        when(lcRepository.findOne(any())).thenReturn(Optional.of(lc));

        Optional<LC> result = lcService.getOptionalLC(lc);

        assertTrue(result.isPresent());
        verify(lcRepository).findOne(any());
    }
}