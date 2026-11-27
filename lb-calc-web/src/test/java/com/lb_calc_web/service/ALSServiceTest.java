package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.ControlConfiguration;
import com.lb_calc_web.domain.attributes.TypeLb;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.mapper.dto.ALSDtoMapper;
import com.lb_calc_web.entity.ALSEntity;
import com.lb_calc_web.mapper.entity.ALSEntityMapper;
import com.lb_calc_web.mapper.entity.ModuleEntityMapper;
import com.lb_calc_web.repository.ALSRepository;
import com.lb_calc_web.repository.EquipmentRepository;
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
    private EquipmentRepository equipmentRepository;
    private EquipmentCatalogService equipmentCatalogService;
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
        equipmentRepository = mock(EquipmentRepository.class);
        equipmentCatalogService = mock(EquipmentCatalogService.class);

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
                environment, TestDataFactory.createSizeValidator(), equipmentRepository,
                equipmentCatalogService, TestDataFactory.createSizeBounds());
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
    void replaceWithNewLBC_shouldPreserveConfigurationAndCreateNewModuleIdentity() {
        ALS als = TestDataFactory.validALS(9L);
        var current = TestDataFactory.validLBC(42L);
        als.replaceControlModule(current);

        var replacement = TestDataFactory.validLBC(null);
        when(lbcService.createLBC(anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(replacement);

        ALS result = service.replaceWithNewLBC(als);

        assertSame(als, result);
        assertNotSame(current, result.getControlModule());
        assertNull(result.getLBC().getId());
        assertEquals(current.getControlConfiguration(), result.getLBC().getControlConfiguration());
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
    void saveALS_shouldUpdateExistingEntityInsteadOfCreatingAnotherOne() {
        ALS domain = TestDataFactory.validALS(77L);
        ALSEntity existing = ALSEntityMapper.toEntity(
                TestDataFactory.validALS(77L),
                module -> ModuleEntityMapper.toEntity(module, equipment -> null)
        );
        List<com.lb_calc_web.entity.ModuleEntity> modules = existing.getModules().stream()
                .map(com.lb_calc_web.entity.ALSModuleEntity::getModule)
                .toList();

        when(alsRepository.findById(77L)).thenReturn(Optional.of(existing));
        when(alsRepository.findCandidates(anyInt(), anyInt(), anyInt(), anyInt(), any(), any()))
                .thenReturn(List.of());
        when(moduleRepository.findCandidates(anyInt(), anyInt(), anyInt(), anyInt(),
                any(), any())).thenReturn(modules);
        when(alsRepository.save(any(ALSEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ALS saved = service.saveALS(domain);

        assertSame(domain, saved);
        assertEquals(77L, saved.getId());
        verify(alsRepository).save(same(existing));
    }

    @Test
    void restoreDomain_shouldPreserveStoredTextsWhenConfigurationIsUnchanged() {
        ALS source = TestDataFactory.validALS(15L);
        ALSEntity entity = spy(ALSEntityMapper.toEntity(
                source,
                module -> ModuleEntityMapper.toEntity(module, equipment -> null)
        ));
        doReturn(15L).when(entity).getId();
        entity.setName("Сохранённое название АКХ");
        entity.setDescription("Сохранённое описание АКХ");
        entity.getModules().forEach(module -> {
            module.getModule().setName("Сохранённое название модуля");
            module.getModule().setDescription("Сохранённое описание модуля");
        });
        when(alsRepository.findById(15L)).thenReturn(Optional.of(entity));

        ALS existing = service.findById(15L);
        var dto = com.lb_calc_web.mapper.dto.ALSDtoMapper.toDto(existing);
        ALS restored = service.preserveExistingModules(existing,
                restoreAlsDto(dto, existing.getControlModule().getControlConfiguration()));

        assertEquals("Сохранённое название АКХ", restored.getName());
        assertEquals("Сохранённое описание АКХ", restored.getDescription());
        assertTrue(restored.getModules().stream().allMatch(module ->
                "Сохранённое название модуля".equals(module.getName())
                        && "Сохранённое описание модуля".equals(module.getDescription())));
    }

    @Test
    void restoreDomain_shouldUpdateOnlyTextsOfChangedModules() {
        ALS source = TestDataFactory.validALS(16L);
        ALSEntity entity = spy(ALSEntityMapper.toEntity(
                source,
                module -> ModuleEntityMapper.toEntity(module, equipment -> null)
        ));
        doReturn(16L).when(entity).getId();
        entity.setName("Сохранённое название АКХ");
        entity.setDescription("Сохранённое описание АКХ");
        entity.getModules().forEach(module -> {
            module.getModule().setName("Сохранённое название модуля");
            module.getModule().setDescription("Сохранённое описание модуля");
        });
        when(alsRepository.findById(16L)).thenReturn(Optional.of(entity));

        ALS existing = service.findById(16L);
        var dto = com.lb_calc_web.mapper.dto.ALSDtoMapper.toDto(existing);
        dto.getFunctionalConfiguration().setDisplay(
                com.lb_calc_web.domain.equipment.DisplayPurpose.INCH_19
        );

        ALS restored = service.preserveExistingModules(existing,
                restoreAlsDto(dto, existing.getControlModule().getControlConfiguration()));

        assertEquals("Сохранённое название модуля", restored.getStorageModules().get(0).getName());
        assertEquals("Сохранённое описание модуля", restored.getStorageModules().get(0).getDescription());
        assertTrue(restored.getLC().getName().contains("дисплей 19″"));
        assertNotEquals("Сохранённое название АКХ", restored.getName());
    }

    @Test
    void restoreDomain_shouldConvertDtoWithoutLegacyCatalogDependencies() {
        var dto = TestDataFactory.validALSDTO(3L);
        dto.setId(null);
        ALS result = restoreAlsDto(dto, null);

        assertNotNull(result);
        assertEquals(3, result.getModules().size());
    }
    private ALS restoreAlsDto(ALSDTO dto, ControlConfiguration fallback) {
        return ALSDtoMapper.toDomain(
                dto,
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                value -> new TypeLb(value.getType(), value.getDeltaWidth(),
                        value.getShelfThick(), value.getServiceZoneWidth()),
                equipmentId -> {
                    throw new IllegalStateException(
                            "Оборудование в этом тесте не должно загружаться");
                },
                fallback,
                TestDataFactory.createSizeBounds()
        );
    }

}
