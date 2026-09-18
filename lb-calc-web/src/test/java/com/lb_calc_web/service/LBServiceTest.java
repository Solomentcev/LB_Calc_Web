package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.validation.ValidationResult;
import com.lb_calc_web.entity.LBEntity;
import com.lb_calc_web.handler.ValidationSizeException;
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

    private LBRepository lbRepository;
    private MockEnvironment environment;
    private LBService lbService;

    @BeforeEach
    void setUp() {
        TestDataFactory.initSizeValidator();

        lbRepository = mock(LBRepository.class);

        environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.width.default", "500")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("size.count.cells.default", "3")
                .withProperty("size.door.thickness.default", "20")
                .withProperty("lb.type.default", "TYPE1")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");

        lbService =
                new LBService(
                        lbRepository,
                        environment
                );
    }

    @Test
    void createLB_shouldCreateValidDefault() {
        LBDTO result = lbService.createLB();

        assertNotNull(result);
        assertEquals(1940, result.getHeight());
        assertEquals(500, result.getWidth());
        assertEquals("TYPE1", result.getType());
        assertTrue(result.getHeightCell() > 0);
        assertTrue(result.getDepthCell() > 0);
        assertNotNull(result.getDescription());
    }

    @Test
    void saveLB_shouldReturnExistingByConfiguration() {
        LBDTO dto = TestDataFactory.validLBDTO(1L);
        LBEntity existing = LBEntityMapper.toEntity(
                com.lb_calc_web.mapper.dto.LBDtoMapper.toDomain(dto)
        );

        when(lbRepository.findAll())
                .thenReturn(List.of(existing));

        LBDTO result = lbService.saveLB(dto);

        assertNotNull(result);
        verify(lbRepository, never()).save(any());
    }

    @Test
    void saveLB_shouldPersistNewConfiguration() {
        LBDTO dto = TestDataFactory.validLBDTO(1L);
        LBEntity saved =
                LBEntityMapper.toEntity(
                        com.lb_calc_web.mapper.dto.LBDtoMapper.toDomain(dto)
                );

        when(lbRepository.findAll()).thenReturn(List.of());
        when(lbRepository.save(any(LBEntity.class)))
                .thenReturn(saved);

        LBDTO result = lbService.saveLB(dto);

        assertNotNull(result);
        verify(lbRepository).save(any(LBEntity.class));
    }

    @Test
    void saveLB_invalid_shouldThrowValidationException() {
        LBDTO dto = TestDataFactory.validLBDTO(1L);
        dto.setHeight(10);

        assertThrows(
                ValidationSizeException.class,
                () -> lbService.saveLB(dto)
        );

        verify(lbRepository, never()).save(any());
    }

    @Test
    void findById_existing_shouldReturnDto() {
        LBDTO dto = TestDataFactory.validLBDTO(1L);
        LBEntity entity =
                LBEntityMapper.toEntity(
                        com.lb_calc_web.mapper.dto.LBDtoMapper.toDomain(dto)
                );
        LBEntity spyEntity = spy(entity);
        doReturn(1L).when(spyEntity).getId();

        when(lbRepository.findById(1L))
                .thenReturn(Optional.of(spyEntity));

        LBDTO result = lbService.findById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void findById_missing_shouldThrow() {
        when(lbRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> lbService.findById(1L)
        );
    }

    @Test
    void findAll_shouldReturnSortedById() {
        LB first = TestDataFactory.validLB(1L);
        LB second = TestDataFactory.validLB(2L);

        LBEntity firstEntity =
                spy(LBEntityMapper.toEntity(first));
        LBEntity secondEntity =
                spy(LBEntityMapper.toEntity(second));

        doReturn(1L).when(firstEntity).getId();
        doReturn(2L).when(secondEntity).getId();

        when(lbRepository.findAll())
                .thenReturn(List.of(secondEntity, firstEntity));

        List<LBDTO> result = lbService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
    }
}
