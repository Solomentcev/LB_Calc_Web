package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.ModuleEntityRepository;
import com.lb_calc_web.repository.ProjectALSRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ALSServiceTest {

    private ALSRepository alsRepository;
    private ProjectALSRepository projectALSRepository;
    private ModuleEntityRepository moduleRepository;
    private LBService lbService;
    private LCService lcService;
    private LBCService lbcService;
    private FindOrCreateLockService lockService;
    private EquipmentEntityResolver equipmentResolver;
    private ALSService service;

    @BeforeEach
    void setUp() {
        alsRepository = mock(ALSRepository.class);
        projectALSRepository = mock(ProjectALSRepository.class);
        moduleRepository = mock(ModuleEntityRepository.class);
        lbService = mock(LBService.class);
        lcService = mock(LCService.class);
        lbcService = mock(LBCService.class);
        lockService = mock(FindOrCreateLockService.class);
        equipmentResolver = mock(EquipmentEntityResolver.class);

        MockEnvironment environment = new MockEnvironment()
                .withProperty("size.height.default", "1940")
                .withProperty("size.depth.default", "500")
                .withProperty("size.frame.upper.default", "50")
                .withProperty("size.frame.bottom.default", "50")
                .withProperty("als.color.body.default", "Blue")
                .withProperty("als.color.door.default", "White")
                .withProperty("als.position.control-module.default", "LEFT")
                .withProperty("als.position.lc.default", "LEFT")
                .withProperty("lb.type.TYPE1.delta-width", "100")
                .withProperty("lb.type.TYPE1.shelf-thick", "5")
                .withProperty("lb.type.TYPE1.service-zone-width", "40");

        service = new ALSService(
                alsRepository, projectALSRepository, moduleRepository,
                lbService, lcService, lbcService, lockService,
                environment, TestDataFactory.createSizeValidator(), equipmentResolver);
    }

    @Test
    void createALS_shouldCreateControlAndStorageModules() {
        when(lcService.createLC(anyInt(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(TestDataFactory.validLC(10L));
        when(lbService.createLB(anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(TestDataFactory.validLB(20L));

        ALS result = service.createALS();

        assertNotNull(result.getControlModule());
        assertEquals(1, result.getStorageModules().size());
        assertEquals(2, result.getModules().size());
    }

    @Test
    void deleteALS_shouldRejectConfigurationUsedByProject() {
        ALSEntity entity = mock(ALSEntity.class);
        when(alsRepository.findById(15L)).thenReturn(Optional.of(entity));
        when(projectALSRepository.existsByAls_Id(15L)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class, () -> service.deleteALS(15L));

        assertTrue(exception.getMessage().contains("используется в проектах"));
        verify(alsRepository, never()).delete(any(ALSEntity.class));
    }

    @Test
    void findById_shouldRestoreDomainFromEntity() {
        ALSEntity entity = spy(ALSEntityMapper.toEntity(
                TestDataFactory.validALS(15L),
                module -> ModuleEntityMapper.toEntity(module, equipment -> null)));
        doReturn(15L).when(entity).getId();
        when(alsRepository.findById(15L)).thenReturn(Optional.of(entity));

        ALS result = service.findById(15L);

        assertEquals(TestDataFactory.validALS(15L), result);
    }

    @Test
    void saveALS_shouldReuseExistingIdenticalConfiguration() {
        ALS domain = TestDataFactory.validALS(1L);
        ALSEntity existing = spy(ALSEntityMapper.toEntity(
                TestDataFactory.validALS(77L),
                module -> ModuleEntityMapper.toEntity(module, equipment -> null)));
        doReturn(77L).when(existing).getId();
        when(alsRepository.findCandidates(anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(List.of(existing));

        ALS result = service.saveALS(domain);

        assertSame(domain, result);
        assertEquals(existing.getId(), result.getId());
        verify(alsRepository, never()).save(any());
    }

    @Test
    void restoreDomain_shouldConvertDtoWithoutLegacyCatalogDependencies() {
        ALS result = service.restoreDomain(TestDataFactory.validALSDTO(3L));

        assertNotNull(result);
        assertEquals(2, result.getModules().size());
        verifyNoInteractions(equipmentResolver);
    }
}
