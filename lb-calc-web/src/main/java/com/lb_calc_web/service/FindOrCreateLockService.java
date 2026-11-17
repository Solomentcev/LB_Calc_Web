package com.lb_calc_web.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Синхронизирует операции Find-or-Create между всеми экземплярами приложения.
 *
 * <p>Блокировка строки БД действует до завершения внешней транзакции
 * и координирует создание конфигураций ALS, LB, LC и LBC.</p>
 */
@Service
public class FindOrCreateLockService {

    private static final String LOCK_SQL =
            "SELECT id FROM find_or_create_lock WHERE id = 1 FOR UPDATE";

    private final EntityManager entityManager;

    public FindOrCreateLockService(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(
                entityManager,
                "EntityManager не должен быть null"
        );
    }

    /**
     * Получает общую блокировку Find-or-Create.
     *
     * <p>Метод должен вызываться внутри транзакции сохранения конфигурации.
     * Блокировка автоматически освобождается при commit или rollback.</p>
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void acquire() {
        entityManager.createNativeQuery(LOCK_SQL).getSingleResult();
    }
}
