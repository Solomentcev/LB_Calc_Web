package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.service.validation.ValidationResult;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.service.validation.ValidationSizeException;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.mapper.entity.LBEntityMapper;
import com.lb_calc_web.repository.LBRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LBServiceTest {

    private FindOrCreateLockService findOrCreateLockService;
    private LBRepository lbRepository;
    private MockEnvironment environment;
    private LBService lbService;

    @BeforeEach
    void setUp() {
        lbRepository = mock(LBRepository.class);
        findOrCreateLockService = mock(FindOrCreateLockService.class);
        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.width.default", "500")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("size.count.cells.default", "3")
                .withProperty("size.door.thickness.default", "20")
                .withProperty("lb.type.default", "TYPE1")
                .withProperty("lb.direction.default", "RIGHT")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");
        lbService = new LBService(
                lbRepository,
                findOrCreateLockService,
                environment,
                TestDataFactory.createSizeValidator(),
                TestDataFactory.createSizeBounds()
        );
    }

    @Test
    void createLB_shouldCreateValidDefault() {
        LB result = lbService.createLB();
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(1940, result.getHeight());
        assertEquals(500, result.getWidth());
        assertEquals("TYPE1", result.getTypeLb().getType());
        assertEquals(com.lb_calc_web.domain.attributes.DirectionDoorOpening.RIGHT,
                result.getDirectionDoorOpening());
        assertTrue(result.getCellHeight() > 0);
        assertTrue(result.getCellDepth() > 0);
        assertNotNull(result.getDescription());
    }

    @Test
    void saveLB_shouldReturnExistingByConfiguration() {
        LB domain = TestDataFactory.validLB(null);
        LBEntity existing = LBEntityMapper.toEntity(domain);
        LBEntity existingWithId = spy(existing);
        doReturn(7L).when(existingWithId).getId();
        when(lbRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                anyString(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), anyInt(), any(), any()
        )).thenReturn(List.of(existingWithId));

        LB result = lbService.saveLB(domain);

        assertEquals(7L, result.getId());
        verify(lbRepository, never()).save(any());
    }

    @Test
    void saveLB_shouldPersistNewConfiguration() {
        LB domain = TestDataFactory.validLB(null);
        LBEntity saved = spy(LBEntityMapper.toEntity(domain));
        doReturn(9L).when(saved).getId();
        when(lbRepository.findCandidates(
                anyInt(), anyInt(), anyInt(), anyInt(), anyInt(),
                anyString(), anyInt(), anyInt(), anyInt(), anyInt(),
                any(), anyInt(), any(), any()
        )).thenReturn(List.of());
        when(lbRepository.save(any(LBEntity.class))).thenReturn(saved);

        LB result = lbService.saveLB(domain);

        assertEquals(9L, result.getId());
        assertEquals(9L, domain.getId());
        verify(lbRepository).save(any(LBEntity.class));
    }

    @Test
    void saveLB_invalid_shouldThrowValidationException() {
        LBDTO dto = TestDataFactory.validLBDTO(null);
        dto.setHeight(10);
        LB domain = LBDtoMapper.toDomain(dto);

        assertThrows(ValidationSizeException.class, () -> lbService.saveLB(domain));
        verify(lbRepository, never()).save(any());
    }

    @Test
    void saveLB_widthBelowMinimum_shouldThrowValidationException() {
        LBDTO dto = TestDataFactory.validLBDTO(null);
        dto.setWidth(159);
        LB domain = LBDtoMapper.toDomain(dto);

        assertThrows(ValidationSizeException.class, () -> lbService.saveLB(domain));
        verify(lbRepository, never()).save(any());
    }

    @Test
    void findById_existing_shouldReturnDomain() {
        LB domain = TestDataFactory.validLB(null);
        LBEntity entity = spy(LBEntityMapper.toEntity(domain));
        doReturn(1L).when(entity).getId();
        when(lbRepository.findById(1L)).thenReturn(Optional.of(entity));

        LB result = lbService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_missing_shouldThrow() {
        when(lbRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> lbService.findById(1L));
    }

    @Test
    void findAll_shouldReturnSortedById() {
        LB first = TestDataFactory.validLB(1L);
        LB second = TestDataFactory.validLB(2L);
        LBEntity firstEntity = spy(LBEntityMapper.toEntity(first));
        LBEntity secondEntity = spy(LBEntityMapper.toEntity(second));
        doReturn(1L).when(firstEntity).getId();
        doReturn(2L).when(secondEntity).getId();
        when(lbRepository.findAll()).thenReturn(List.of(secondEntity, firstEntity));

        List<LB> result = lbService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }
}
