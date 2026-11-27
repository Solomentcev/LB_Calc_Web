package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.mapper.dto.LBDtoMapper;
import com.lb_calc_web.repository.LBRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет блокировку Find-or-Create на реальной тестовой БД.
 */
@SpringBootTest
class FindOrCreateLockIntegrationTest {

    @Autowired
    private FindOrCreateLockService lockService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private LBService lbService;

    @Autowired
    private LBRepository lbRepository;

    @Autowired
    private ALSService alsService;

    @Autowired
    private LCService lcService;

    @Autowired
    private LBCService lbcService;

    @Autowired
    private EquipmentCatalogService equipmentCatalogService;

    @Test
    void acquire_shouldSerializeConcurrentTransactions() throws Exception {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        CountDownLatch firstAcquired = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondAcquired = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<?> first = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        lockService.acquire();
                        firstAcquired.countDown();
                        await(releaseFirst);
                    })
            );

            assertTrue(firstAcquired.await(5, TimeUnit.SECONDS));

            Future<?> second = executor.submit(() ->
                    transactionTemplate.executeWithoutResult(status -> {
                        lockService.acquire();
                        secondAcquired.countDown();
                    })
            );

            assertFalse(
                    secondAcquired.await(200, TimeUnit.MILLISECONDS),
                    "Вторая транзакция не должна получить блокировку до завершения первой"
            );

            releaseFirst.countDown();

            assertTrue(
                    secondAcquired.await(5, TimeUnit.SECONDS),
                    "Вторая транзакция должна получить блокировку после первой"
            );

            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void saveConfigurations_shouldExecuteLockingCandidateQueries() {
        ALS als = alsService.saveALS(TestDataFactory.validALS(0L));
        ALS lbcAls = alsService.saveALS(
                TestDataFactory.restoreALS(TestDataFactory.validLBCALSDTO(0L))
        );
        LC lcDomain = TestDataFactory.validLC(0L);
        configureEquipment(lcDomain);
        LC lc = lcService.saveLC(lcDomain);

        LBC lbcDomain = TestDataFactory.validLBC(0L);
        configureEquipment(lbcDomain);
        LBC lbc = lbcService.saveLBC(lbcDomain);

        assertNotNull(als);
        assertNotNull(lbcAls);
        assertNotNull(lc);
        assertNotNull(lbc);
    }

    @Test
    void saveLB_shouldReuseConfigurationWhenSnapshotPredatesLock() throws Exception {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        LBDTO firstDto = TestDataFactory.validLBDTO(0L);
        firstDto.setColorBody("Red");
        firstDto.setColorDoor("Green");

        LBDTO secondDto = TestDataFactory.validLBDTO(0L);
        secondDto.setColorBody("Red");
        secondDto.setColorDoor("Green");

        LB firstLB = TestDataFactory.restoreLB(firstDto);
        LB secondLB = TestDataFactory.restoreLB(secondDto);

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch secondSnapshotReady = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<LB> first = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        lockService.acquire();
                        firstLocked.countDown();
                        await(secondSnapshotReady);
                        return lbService.saveLB(firstLB);
                    })
            );

            assertTrue(firstLocked.await(5, TimeUnit.SECONDS));

            Future<LB> second = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        // Создаём REPEATABLE READ snapshot до ожидания mutex.
                        lbRepository.count();
                        secondSnapshotReady.countDown();
                        return lbService.saveLB(secondLB);
                    })
            );

            LB firstResult = first.get(10, TimeUnit.SECONDS);
            LB secondResult = second.get(10, TimeUnit.SECONDS);

            assertTrue(firstResult.getId() > 0);
            assertEquals(
                    firstResult.getId(),
                    secondResult.getId(),
                    "Вторая транзакция должна переиспользовать запись первой"
            );
        } finally {
            secondSnapshotReady.countDown();
            executor.shutdownNow();
        }
    }

    /** Подбирает физические модели каталога для функциональных требований модуля. */
    private void configureEquipment(com.lb_calc_web.domain.model.ControlModule module) {
        var configuration = module.getControlConfiguration();
        equipmentCatalogService.configureFunctionalEquipment(configuration);
        module.setControlConfiguration(configuration);
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Истекло время ожидания освобождения блокировки"
                );
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Ожидание блокировки было прервано",
                    exception
            );
        }
    }
}
