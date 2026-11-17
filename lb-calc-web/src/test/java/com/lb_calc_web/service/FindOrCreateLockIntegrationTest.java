package com.lb_calc_web.service;

import com.lb_calc_web.TestDataFactory;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.repository.LBRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет блокировку Find-or-Create на реальной тестовой БД.
 */
@SpringBootTest
@ActiveProfiles("test")
class FindOrCreateLockIntegrationTest {

    @Autowired
    private FindOrCreateLockService lockService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private LBService lbService;

    @Autowired
    private LBRepository lbRepository;

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
    void saveLB_shouldReuseConfigurationWhenSnapshotPredatesLock() throws Exception {
        TransactionTemplate transactionTemplate =
                new TransactionTemplate(transactionManager);

        LBDTO firstDto = TestDataFactory.validLBDTO(0L);
        firstDto.setColorBody("Red");
        firstDto.setColorDoor("Green");

        LBDTO secondDto = TestDataFactory.validLBDTO(0L);
        secondDto.setColorBody("Red");
        secondDto.setColorDoor("Green");

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch secondSnapshotReady = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<LBDTO> first = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        lockService.acquire();
                        firstLocked.countDown();
                        await(secondSnapshotReady);
                        return lbService.saveLB(firstDto);
                    })
            );

            assertTrue(firstLocked.await(5, TimeUnit.SECONDS));

            Future<LBDTO> second = executor.submit(() ->
                    transactionTemplate.execute(status -> {
                        // Создаём REPEATABLE READ snapshot до ожидания mutex.
                        lbRepository.count();
                        secondSnapshotReady.countDown();
                        return lbService.saveLB(secondDto);
                    })
            );

            LBDTO firstResult = first.get(10, TimeUnit.SECONDS);
            LBDTO secondResult = second.get(10, TimeUnit.SECONDS);

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
