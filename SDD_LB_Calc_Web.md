# Software Design Document (SDD): LB_Calc_Web

**Система расчёта конфигураций автоматических камер хранения**

- **Автор:** Denis Solomentsev
- **Дата:** 16 октября 2026
- **Версия:** 1.4 (Финальная, с разделением UI и REST API)
- **Статус:** Утверждено
- **Основа:** ТЗ LB_Calc_Web v1.1 + DDD-спецификация

---

## Содержание

1. Введение
2. Архитектура системы
3. Структура приложения
4. Доменная модель
5. Ключевые алгоритмы
6. API-контракты и эндпоинты
7. Последовательности действий
8. Безопасность и управление пользователями
9. Обработка ошибок
10. Транзакционность и индексы БД
11. Развёртывание и конфигурация

---

# 1. ВВЕДЕНИЕ

## 1.1. Цель документа

Данный SDD описывает техническую реализацию системы LB_Calc_Web. Документ определяет архитектуру, структуру приложения, доменную модель, ключевые алгоритмы, API-контракты (эндпоинты), правила обработки ошибок и политики безопасности на основе утверждённого ТЗ и текущего состояния кодовой базы.

## 1.2. Связь с другими документами

- **ТЗ (Техническое задание):** Описывает бизнес-правила, UI-элементы и пользовательские сценарии.
- **DDD-спецификация:** Описывает детальную структуру агрегатов, Value Objects и инвариантов.
- **Данный SDD:** Описывает КАК правила из ТЗ и DDD реализованы технически в коде.

---

# 2. АРХИТЕКТУРА СИСТЕМЫ

## 2.1. Общая схема

Система построена по микросервисной архитектуре:

- **lb-calc-web** — основное веб-приложение (Spring Boot + Thymeleaf). Отвечает за UI, бизнес-логику, расчёты, работу с БД и аутентификацию.
- **notification-service** — сервис уведомлений. Слушает события из Kafka и отправляет email.
- **Инфраструктура** — MySQL 8.4 (хранение), Kafka (асинхронная коммуникация), Docker/Docker Compose (развёртывание).

## 2.2. Стек технологий

- **Backend:** Java 21, Spring Boot 3.x, Spring Data JPA, Spring Security, Spring Kafka.
- **Frontend:** Thymeleaf (Server-Side Rendering), HTML5, CSS3.
- **Безопасность:** JWT (Access + Refresh tokens в HTTP-only cookies), BCrypt.
- **Инструменты:** Maven, JUnit 5, Mockito, Apache POI (экспорт в Excel), Liquibase/Flyway.

## 2.3. Принципы архитектуры

- **Чистая архитектура:** Зависимости направлены внутрь, к Domain-слою. Domain не знает о Spring, JPA или HTTP.
- **Неизменяемость:** Конфигурации (Equipment, Module, ALS) неизменяемы после сохранения. Используется паттерн Find-or-Create.
- **Разделение ответственности:** Controller (HTTP) → Application Service (оркестрация) → Domain Service (бизнес-правила) → Repository (персистентность).

---

# 3. СТРУКТУРА ПРИЛОЖЕНИЯ

## 3.1. Пакетная структура (com.lb_calc_web)

- **controller** — UI HTTP-контроллеры (приём/отдача запросов, маппинг на Thymeleaf view).
- **controller.api** — REST API контроллеры (для внешних интеграций, возвращают JSON).
- **service** — Application Services (оркестрация транзакций).
- **service.util** — Утилиты (например, SizeValidator).
- **domain.model** — Доменные сущности (ALS, LB, LC, LBC, Project, Equipment).
- **domain.attributes** — Value Objects и Enums (Colors, TypeLb, AccessMethod).
- **domain.service** — Доменные сервисы (EquipmentRequirementResolver, ModuleAllocationResolver).
- **entity** — JPA-сущности (отображение на БД).
- **repository** — Интерфейсы Spring Data JPA.
- **dto** — Data Transfer Objects для передачи данных между слоями.
- **mapper** — Преобразования DTO ↔ Domain ↔ Entity (без бизнес-логики).
- **security** — Конфигурация Spring Security, JWT-сервисы, EmployeePrincipal.
- **event** — Доменные события для Kafka (например, UserEvent).

## 3.2. Правила зависимостей

- Controller вызывает только Service.
- Service вызывает Domain и Repository.
- Domain не зависит ни от чего внешнего (чистая Java).
- Mapper не содержит бизнес-логики и не обращается к БД.
- Repository не содержит бизнес-логики, только persistence-операции.

---

# 4. ДОМЕННАЯ МОДЕЛЬ (Краткая сводка)

Система управляет 4 основными агрегатами:

- **Equipment** — общая доменная абстракция оборудования. Используются отдельные логические каталоги принтеров, мониторов, оборудования оплаты, RFID-, QR- и Bar-считывателей; у каждого каталога собственные характеристики и валидация.
- **Module** — абстрактный корень для LB, LC, LBC. Содержит габариты, цвета и привязанное оборудование. Неизменяем.
- **ALS** — автоматическая локерная система. Содержит бизнес-характеристики (оплата, монитор, принтер, способы доступа), общие цвета и высоту/глубину, положение управляющего модуля (слева/посередине/справа) и упорядоченный список модулей. Ширина ALS — сумма ширин модулей. Неизменяема.
- **Project** — контейнер для ALS-конфигураций клиента. Изменяемый агрегат. Хранит ссылки на ALS и их количество.

*(Детальная структура полей и инвариантов описана в отдельном документе DDD-спецификации).*

---

# 5. КЛЮЧЕВЫЕ АЛГОРИТМЫ

## 5.1. EquipmentRequirementResolver

Преобразует характеристики ALS в требования к типам оборудования и критерии подбора.

- Оплата: NONE / CASH / CASHLESS / CASH_AND_CASHLESS → требование к каталогу оборудования оплаты.
- Монитор: NONE / 10 / 17 / 19 → требование к каталогу мониторов с выбранным размером.
- Принтер: YES → требование к каталогу принтеров.
- Для каждого выбранного способа RFID / QR / BAR → требование к соответствующему каталогу считывателей.
- PIN учитывается как способ доступа и активирует правила обязательности монитора и принтера.
- Для хранения/замков требования формируются по действующим правилам модулей.

## 5.2. Подбор и размещение оборудования

Подбор выполняется по отдельным логическим каталогам: PRINTER, MONITOR, PAYMENT, RFID_READER, QR_READER, BAR_READER. У каждого каталога собственные характеристики и правила валидации; общая абстракция Equipment содержит только общие сведения.

1. Для каждого требования выбирается подходящая активная модель из соответствующего каталога.
2. По умолчанию используется позиция, отмеченная как модель по умолчанию; если её нет — применяется правило выбора первого подходящего кандидата.
3. При редактировании LC/LBC пользователь может заменить выбранную модель на другую подходящую позицию того же каталога.
4. Любая ручная замена повторно проверяется на соответствие требованиям ALS и габаритным ограничениям модуля.
5. Оборудование управляющих каталогов размещается в LC/LBC; оборудование хранения и замков — в LB/LBC согласно конструктивным правилам.

## 5.3. Find-or-Create (Поиск или создание)

Применяется для Equipment, Module, ALS.

1. Получить входные параметры конфигурации.
2. Выполнить расчёты (размеры ячеек) и валидацию (SizeValidator + доменные инварианты).
3. Выполнить SELECT из БД по индексированным скалярным полям (ширина, высота, тип и т.д.).
4. Для каждого кандидата загрузить его набор оборудования (module_equipment).
5. Сравнить множества: если количество и пары (equipmentId, quantity) полностью совпадают → вернуть существующий ID.
6. Если не найдено → выполнить INSERT новой записи в рамках той же транзакции.

## 5.4. Расчёт размеров ячеек (StorageModule.recalculateStorageCells)

- cellWidth = moduleWidth - typeLb.deltaWidth - typeLb.serviceZoneWidth
- cellDepth = moduleDepth - doorThickness
- usableHeight = moduleHeight - upperFrame - bottomFrame - ((countCells - 1) * typeLb.shelfThick)
- cellHeight = (int) (usableHeight / countCells)
- **Инварианты:** cellWidth > 0, cellDepth > 0, cellHeight >= 85.

---

# 6. API-КОНТРАКТЫ И ЭНДПОИНТЫ

Система имеет два типа эндпоинтов:

1. **UI-эндпоинты** — для работы с Thymeleaf-шаблонами, используют Form POST, возвращают HTML-страницы.
2. **REST API-эндпоинты** — для внешних интеграций, SPA-фронтенда, мобильных приложений. Следуют REST-стилю, возвращают JSON.

---

## 6.1. UI-ЭНДПОИНТЫ (Thymeleaf)

*Примечание: UI-эндпоинты используют POST для операций обновления (save, update), что является стандартным подходом для Thymeleaf-приложений с HTML-формами.*

### 6.1.1. Аутентификация и Профиль

| Метод | Путь                      | Описание                               | Возвращает | Доступ        |
|-------|---------------------------|----------------------------------------|------------|---------------|
| GET   | /login                    | Форма входа                            | HTML       | Public        |
| POST  | /login                    | Аутентификация, установка JWT cookies  | Redirect   | Public        |
| GET   | /logout                   | Выход из системы, очистка JWT cookies  | Redirect   | Authenticated |
| GET   | /registration             | Форма регистрации (⚠️ см. раздел 8.3)  | HTML       | Public        |
| POST  | /registration             | Саморегистрация (выдаёт ROLE_MANAGER)  | Redirect   | Public        |
| GET   | /myprofile                | Страница профиля текущего пользователя | HTML       | Authenticated |
| GET   | /myprofile/changepassword | Форма смены своего пароля              | HTML       | Authenticated |
| POST  | /myprofile/changepassword | Обработка смены своего пароля          | Redirect   | Authenticated |

### 6.1.2. Управление сотрудниками (Только ADMIN)

| Метод | Путь                           | Описание                        | Возвращает | Доступ     |
|-------|--------------------------------|---------------------------------|------------|------------|
| GET   | /employees                     | Список всех сотрудников         | HTML       | ROLE_ADMIN |
| GET   | /employees/create              | Форма создания сотрудника       | HTML       | ROLE_ADMIN |
| POST  | /employees/save                | Сохранение нового сотрудника    | Redirect   | ROLE_ADMIN |
| GET   | /employees/{id}                | Форма редактирования сотрудника | HTML       | ROLE_ADMIN |
| POST  | /employees/{id}/update         | Обновление данных сотрудника    | Redirect   | ROLE_ADMIN |
| GET   | /employees/{id}/changepassword | Форма сброса пароля сотрудника  | HTML       | ROLE_ADMIN |
| POST  | /employees/{id}/changepassword | Обработка сброса пароля         | Redirect   | ROLE_ADMIN |

### 6.1.3. Проекты и ALS

| Метод | Путь                                | Описание                      | Возвращает | Доступ        |
|-------|-------------------------------------|-------------------------------|------------|---------------|
| GET   | /projects                           | Список проектов               | HTML       | MANAGER/ADMIN |
| GET   | /projects/create                    | Форма создания проекта        | HTML       | MANAGER/ADMIN |
| POST  | /projects/{id}/save                 | Сохранение/обновление проекта | Redirect   | MANAGER/ADMIN |
| POST  | /projects/{id}/addALS               | Добавление ALS в проект       | Redirect   | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/delete   | Удаление ALS из проекта       | Redirect   | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/quantity | Изменение количества ALS      | Redirect   | MANAGER/ADMIN |
| GET   | /projects/{id}/savetoexcel          | Экспорт проекта в Excel       | Файл XLSX  | MANAGER/ADMIN |

### 6.1.4. Редактирование ALS и модулей (Find-or-Create при изменении)

**Редактирование ALS:**

| Метод | Путь                            | Описание                                        | Возвращает | Доступ        |
|-------|---------------------------------|-------------------------------------------------|------------|---------------|
| GET   | /projects/{pId}/alss/{aId}      | Форма редактирования ALS (загрузка в Draft)     | HTML       | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/save | Сохранение изменений (Find-or-Create новой ALS) | Redirect   | MANAGER/ADMIN |

**Редактирование модулей (LB/LC/LBC):**

| Метод | Путь                                         | Описание                                  | Возвращает | Доступ        |
|-------|----------------------------------------------|-------------------------------------------|------------|---------------|
| GET   | /projects/{pId}/alss/{aId}/lbs/{lbId}        | Форма редактирования LB                   | HTML       | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/lbs/{lbId}/save   | Сохранение изменений LB (Find-or-Create)  | Redirect   | MANAGER/ADMIN |
| GET   | /projects/{pId}/alss/{aId}/lcs/{lcId}        | Форма редактирования LC                   | HTML       | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/lcs/{lcId}/save   | Сохранение изменений LC (Find-or-Create)  | Redirect   | MANAGER/ADMIN |
| GET   | /projects/{pId}/alss/{aId}/lbcs/{lbcId}      | Форма редактирования LBC                  | HTML       | MANAGER/ADMIN |
| POST  | /projects/{pId}/alss/{aId}/lbcs/{lbcId}/save | Сохранение изменений LBC (Find-or-Create) | Redirect   | MANAGER/ADMIN |

### 6.1.5. Draft-конфигуратор (создание ALS с нуля)

| Метод | Путь                                     | Описание                              | Возвращает | Доступ        |
|-------|------------------------------------------|---------------------------------------|------------|---------------|
| GET   | /als/configurator/new                    | Инициализация Draft-конфигуратора     | HTML       | MANAGER/ADMIN |
| POST  | /als/configurator/update-characteristics | Обновление характеристик в Draft      | Redirect   | MANAGER/ADMIN |
| POST  | /als/configurator/add-module             | Добавление модуля в Draft             | Redirect   | MANAGER/ADMIN |
| POST  | /als/configurator/remove-module/{index}  | Удаление модуля из Draft              | Redirect   | MANAGER/ADMIN |
| POST  | /als/configurator/save                   | Финальное сохранение (Find-or-Create) | Redirect   | MANAGER/ADMIN |

---

## 6.2. REST API-ЭНДПОИНТЫ (JSON)

*Все REST API-эндпоинты имеют префикс `/api/v1/` и возвращают JSON. Используют стандартные HTTP-методы (GET, POST, PUT, DELETE).*

### 6.2.1. Аутентификация

| Метод | Путь                 | Описание           | Тело запроса        | Ответ                         |
|-------|----------------------|--------------------|---------------------|-------------------------------|
| POST  | /api/v1/auth/login   | Аутентификация     | `{email, password}` | `{accessToken, refreshToken}` |
| POST  | /api/v1/auth/refresh | Обновление токенов | `{refreshToken}`    | `{accessToken, refreshToken}` |
| POST  | /api/v1/auth/logout  | Выход из системы   | —                   | 204 No Content                |

### 6.2.2. Управление сотрудниками (Только ADMIN)

| Метод  | Путь                            | Описание                | Тело запроса        | Ответ                       |
|--------|---------------------------------|-------------------------|---------------------|-----------------------------|
| GET    | /api/v1/employees               | Список всех сотрудников | —                   | `List<EmployeeDTO>`         |
| GET    | /api/v1/employees/{id}          | Детали сотрудника       | —                   | `EmployeeDTO`               |
| POST   | /api/v1/employees               | Создать сотрудника      | `CreateEmployeeDTO` | `EmployeeDTO` (201 Created) |
| PUT    | /api/v1/employees/{id}          | Обновить сотрудника     | `UpdateEmployeeDTO` | `EmployeeDTO`               |
| DELETE | /api/v1/employees/{id}          | Удалить сотрудника      | —                   | 204 No Content              |
| POST   | /api/v1/employees/{id}/password | Сменить пароль          | `{newPassword}`     | 204 No Content              |

### 6.2.3. Оборудование (Только ADMIN для записи)

| Метод  | Путь                              | Описание                      | Тело запроса            | Ответ                        |
|--------|-----------------------------------|-------------------------------|-------------------------|------------------------------|
| GET    | /api/v1/equipment                 | Список оборудования           | Query: `type`, `active` | `List<EquipmentDTO>`         |
| GET    | /api/v1/equipment/{id}            | Детали оборудования           | —                       | `EquipmentDTO`               |
| POST   | /api/v1/equipment                 | Создать оборудование (ADMIN)  | `CreateEquipmentDTO`    | `EquipmentDTO` (201 Created) |
| PUT    | /api/v1/equipment/{id}            | Обновить оборудование (ADMIN) | `UpdateEquipmentDTO`    | `EquipmentDTO`               |
| DELETE | /api/v1/equipment/{id}            | Удалить оборудование (ADMIN)  | —                       | 204 No Content               |
| POST   | /api/v1/equipment/{id}/deactivate | Деактивировать (ADMIN)        | —                       | 204 No Content               |

### 6.2.4. Проекты

| Метод  | Путь                               | Описание        | Тело запроса       | Ответ                      |
|--------|------------------------------------|-----------------|--------------------|----------------------------|
| GET    | /api/v1/projects                   | Список проектов | Query: `createdBy` | `List<ProjectDTO>`         |
| GET    | /api/v1/projects/{id}              | Детали проекта  | —                  | `ProjectDTO`               |
| POST   | /api/v1/projects                   | Создать проект  | `CreateProjectDTO` | `ProjectDTO` (201 Created) |
| PUT    | /api/v1/projects/{id}              | Обновить проект | `UpdateProjectDTO` | `ProjectDTO`               |
| DELETE | /api/v1/projects/{id}              | Удалить проект  | —                  | 204 No Content             |
| GET    | /api/v1/projects/{id}/export/excel | Экспорт в Excel | —                  | Файл XLSX                  |

### 6.2.5. ALS в проектах

| Метод  | Путь                                      | Описание                | Тело запроса        | Ответ                         |
|--------|-------------------------------------------|-------------------------|---------------------|-------------------------------|
| GET    | /api/v1/projects/{pId}/als                | Список ALS в проекте    | —                   | `List<ProjectALSDTO>`         |
| POST   | /api/v1/projects/{pId}/als                | Добавить ALS в проект   | `{alsId, quantity}` | `ProjectALSDTO` (201 Created) |
| PUT    | /api/v1/projects/{pId}/als/{aId}/quantity | Изменить количество ALS | `{quantity}`        | `ProjectALSDTO`               |
| DELETE | /api/v1/projects/{pId}/als/{aId}          | Удалить ALS из проекта  | —                   | 204 No Content                |

### 6.2.6. Редактирование ALS (Find-or-Create)

| Метод | Путь                             | Описание                        | Тело запроса   | Ответ                |
|-------|----------------------------------|---------------------------------|----------------|----------------------|
| GET   | /api/v1/projects/{pId}/als/{aId} | Получить ALS для редактирования | —              | `ALSDTO`             |
| PUT   | /api/v1/projects/{pId}/als/{aId} | Обновить ALS (Find-or-Create)   | `UpdateALSDTO` | `ALSDTO` (новая ALS) |

### 6.2.7. Модули (LB/LC/LBC)

| Метод  | Путь                 | Описание                                     | Тело запроса                     | Ответ                      |
|--------|----------------------|----------------------------------------------|----------------------------------|----------------------------|
| GET    | /api/v1/modules      | Список модулей                               | Query: `type`, `height`, `width` | `List<ModuleDTO>`          |
| GET    | /api/v1/modules/{id} | Детали модуля                                | —                                | `ModuleDTO`                |
| POST   | /api/v1/modules/lb   | Создать LB (Find-or-Create)                  | `CreateLBDTO`                    | `LBDTO` (201 Created)      |
| POST   | /api/v1/modules/lc   | Создать LC (Find-or-Create)                  | `CreateLCDTO`                    | `LCDTO` (201 Created)      |
| POST   | /api/v1/modules/lbc  | Создать LBC (Find-or-Create)                 | `CreateLBCDTO`                   | `LBCDTO` (201 Created)     |
| PUT    | /api/v1/modules/{id} | Обновить модуль (Find-or-Create)             | `UpdateModuleDTO`                | `ModuleDTO` (новая версия) |
| DELETE | /api/v1/modules/{id} | Удалить модуль (только если не используется) | —                                | 204 No Content             |

### 6.2.8. ALS (библиотека конфигураций)

| Метод  | Путь             | Описание                                  | Тело запроса                       | Ответ                  |
|--------|------------------|-------------------------------------------|------------------------------------|------------------------|
| GET    | /api/v1/als      | Список всех ALS                           | Query: `paymentMode`, `totalCells` | `List<ALSDTO>`         |
| GET    | /api/v1/als/{id} | Детали ALS                                | —                                  | `ALSDTO`               |
| POST   | /api/v1/als      | Создать ALS (Find-or-Create)              | `CreateALSDTO`                     | `ALSDTO` (201 Created) |
| PUT    | /api/v1/als/{id} | Обновить ALS (Find-or-Create)             | `UpdateALSDTO`                     | `ALSDTO` (новая ALS)   |
| DELETE | /api/v1/als/{id} | Удалить ALS (только если не используется) | —                                  | 204 No Content         |

### 6.2.9. Примеры запросов/ответов

**Пример 1: Создание проекта**

Запрос:

    POST /api/v1/projects
    Content-Type: application/json
    Authorization: Bearer {accessToken}

    {
      "name": "ТЦ Мега",
      "description": "Камера хранения для торгового центра",
      "company": "ООО Мега"
    }

Ответ (201 Created):

    {
      "id": 1,
      "name": "ТЦ Мега",
      "description": "Камера хранения для торгового центра",
      "company": "ООО Мега",
      "createdBy": {
        "id": 10,
        "email": "manager@example.com"
      },
      "createdAt": "2026-10-16",
      "alsEntries": []
    }

**Пример 2: Добавление ALS в проект**

Запрос:

    POST /api/v1/projects/1/als
    Content-Type: application/json
    Authorization: Bearer {accessToken}

    {
      "alsId": 15,
      "quantity": 2
    }

Ответ (201 Created):

    {
      "id": 1,
      "projectId": 1,
      "alsId": 15,
      "quantity": 2,
      "als": {
        "id": 15,
        "name": "ALS 32 яч. | PAID | RFID, QR | RAL7035/RAL9003",
        "totalCells": 32,
        "totalWidth": 2400
      }
    }

**Пример 3: Обновление ALS (Find-or-Create)**

Запрос:

    PUT /api/v1/projects/1/als/15
    Content-Type: application/json
    Authorization: Bearer {accessToken}

    {
      "paymentMode": "PAID",
      "connectionMode": "NETWORK",
      "colorBody": "RAL7035",
      "colorDoor": "RAL9003",
      "display": {
        "enabled": true,
        "size": 21.5,
        "touch": true
      },
      "accessMethods": ["RFID", "QR_CODE"],
      "printOption": "REQUIRED",
      "modules": [
        {"type": "LB", "countCells": 16, "direction": "LEFT"},
        {"type": "LC"},
        {"type": "LB", "countCells": 16, "direction": "RIGHT"}
      ]
    }

Ответ (200 OK, создана новая ALS #20):

    {
      "id": 20,
      "name": "ALS 32 яч. | PAID | RFID, QR | RAL7035/RAL9003",
      "totalCells": 32,
      "totalWidth": 2600,
      "modules": [
        {"id": 100, "type": "LB", "countCells": 16},
        {"id": 115, "type": "LC"},
        {"id": 100, "type": "LB", "countCells": 16}
      ]
    }

---

## 6.3. Структура контроллеров

### 6.3.1. UI-контроллеры (пакет `controller`)

- `AuthController` — аутентификация и регистрация
- `EmployeeController` — управление сотрудниками (UI)
- `ProjectController` — проекты и ALS (UI)
- `ALSConfiguratorController` — Draft-конфигуратор (UI)

### 6.3.2. REST API-контроллеры (пакет `controller.api`)

- `AuthApiController` — `/api/v1/auth/**`
- `EmployeeApiController` — `/api/v1/employees/**`
- `EquipmentApiController` — `/api/v1/equipment/**`
- `ProjectApiController` — `/api/v1/projects/**`
- `ALSApiController` — `/api/v1/als/**`
- `ModuleApiController` — `/api/v1/modules/**`

### 6.3.3. Правила для REST API

1. **Версионирование:** Все эндпоинты имеют префикс `/api/v1/`. При изменении контракта создаётся `/api/v2/`.
2. **Аутентификация:** Используется JWT в заголовке `Authorization: Bearer {accessToken}`.
3. **Формат данных:** JSON (Content-Type: application/json).
4. **HTTP-методы:**
    - GET — чтение
    - POST — создание
    - PUT — полное обновление
    - PATCH — частичное обновление (не используется в текущей версии)
    - DELETE — удаление
5. **Коды ответов:**
    - 200 OK — успешное чтение/обновление
    - 201 Created — успешное создание
    - 204 No Content — успешное удаление
    - 400 Bad Request — ошибка валидации
    - 401 Unauthorized — не аутентифицирован
    - 403 Forbidden — нет прав
    - 404 Not Found — ресурс не найден
    - 409 Conflict — нарушение уникальности (например, email уже существует)
    - 500 Internal Server Error — внутренняя ошибка
6. **Обработка ошибок:** Все ошибки возвращаются в формате:

        {
          "timestamp": "2026-10-16T10:30:00",
          "status": 400,
          "error": "Validation Error",
          "message": "Email уже используется",
          "path": "/api/v1/employees"
        }

---

# 7. ПОСЛЕДОВАТЕЛЬНОСТИ ДЕЙСТВИЙ (Сценарии)

## 7.1. Сценарий: Создание новой ALS в проекте (через Draft-конфигуратор)

1. User → ALSConfiguratorController: Инициирует /als/configurator/new.
2. Controller → Session: Создаёт объект DraftALS в HttpSession.
3. User → Controller: Меняет характеристики, нажимает "Сохранить".
4. Controller → ALSService: Вызывает метод сохранения Draft.
5. ALSService → EquipmentRequirementResolver: Получает список требований.
6. ALSService → ModuleAllocationResolver: Распределяет требования по модулям.
7. ALSService → LBService/LCService: Вызывает findOrCreate для каждого модуля.
8. ALSService → ALSRepository: Вызывает findOrCreate для самой ALS.
9. ALSService → ProjectService: Создаёт связь ProjectALS (проект → новая ALS).
10. Controller → User: redirect:/projects/{id} (очистка Draft из сессии).

## 7.2. Сценарий: Редактирование существующей ALS

1. User → ProjectController: Открывает /projects/{pId}/alss/{aId}.
2. Controller: Загружает ALS из БД, создаёт DraftALS-копию в сессии.
3. User → Controller: Меняет характеристики в форме.
4. Controller: Обновляет DraftALS в сессии.
5. User → Controller: Нажимает "Сохранить" (POST /projects/{pId}/alss/{aId}/save).
6. Controller → ProjectService: Передаёт DraftALS.
7. ProjectService → ALSService: Вызывает saveDraftAsNewALS().
8. ALSService → Resolvers: Формирует новые требования.
9. ALSService → Services: Вызывает findOrCreate для модулей и ALS.
10. ProjectService: Обновляет ProjectALS: меняет als_id на новый.
11. Controller → User: redirect:/projects/{pId}. Старая ALS остаётся нетронутой.

## 7.3. Сценарий: Find-or-Create модуля

1. Service получает параметры конфигурации модуля.
2. Service вызывает recalculate() для расчёта производных размеров.
3. Service вызывает SizeValidator и проверяет доменные инварианты.
4. Service → Repository: Выполняет SELECT по скалярным полям (например, width, height, type).
5. Service: Для каждого найденного кандидата загружает список module_equipment.
6. Service: Сравнивает списки оборудования (сортировка по ID, проверка quantity).
7. Service: Если полное совпадение → возвращает существующий объект. Иначе → repository.save(новый_модуль).

## 7.4. Сценарий: Редактирование отдельного модуля (LB) в составе ALS

1. User → ProjectController: Открывает /projects/{pId}/alss/{aId}/lbs/{lbId}.
2. Controller: Загружает ALS и LB, создаёт DraftALS-копию.
3. User → Controller: Меняет параметры LB (например, countCells).
4. User → Controller: Нажимает "Сохранить" (POST /projects/{pId}/alss/{aId}/lbs/{lbId}/save).
5. Controller → ProjectService: Передаёт обновлённый DraftALS.
6. ProjectService → LBService: Вызывает findOrCreate для изменённого LB.
7. ProjectService: Заменяет ссылку на LB в DraftALS на новый ID.
8. ProjectService → ALSService: Вызывает findOrCreate для новой ALS.
9. ProjectService: Обновляет ProjectALS: меняет als_id на новый.
10. Controller → User: redirect:/projects/{pId}/alss/{aId}.

---

# 8. БЕЗОПАСНОСТЬ И УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ

## 8.1. Аутентификация и Сессии

- **Механизм:** JWT (JSON Web Tokens).
- **Хранение:** Access Token и Refresh Token хранятся в HTTP-only, Secure cookies. Это защищает от XSS-кражи токенов.
- **Истечение:** Access Token короткоживущий (например, 15 мин), Refresh Token долгоживущий (например, 7 дней).
- **Выход (Logout):** Эндпоинт /logout принудительно устанавливает Max-Age=0 для cookies токенов и очищает SecurityContextHolder.

## 8.2. Управление паролями

- **Хеширование:** PasswordEncoder (BCrypt) с автоматической генерацией salt. Исходные пароли в БД не хранятся.
- **Смена пароля пользователем:** Доступна через /myprofile/changepassword. Требуется ввод нового пароля и его подтверждение.
- **Сброс пароля администратором:** Админ может принудительно сменить пароль любому сотруднику через /employees/{id}/changepassword.
- **Восстановление пароля (Forgot Password):** В текущей реализации отсутствует self-service восстановление через email. Корпоративная политика: пароль сбрасывает только администратор системы.

## 8.3. ⚠️ КРИТИЧЕСКОЕ ЗАМЕЧАНИЕ ПО РЕГИСТРАЦИИ

- В коде присутствует публичный эндпоинт POST /registration, который автоматически выдаёт роль ROLE_MANAGER любому зарегистрировавшемуся.
- **Требование для Production:** Полностью удалить или скрыть за флагом app.registration.enabled=false публичную регистрацию. Создание учётных записей должно осуществляться исключительно администратором через защищённый эндпоинт /employees/create.

## 8.4. Защита от атак

- **CSRF:** Spring Security CSRF-токен автоматически добавляется во все формы Thymeleaf (<form th:action="...">).
- **XSS:** Thymeleaf по умолчанию экранирует все выводимые переменные (th:text), что предотвращает межсайтовый скриптинг.
- **SQL Injection:** Все запросы к БД выполняются через Spring Data JPA с параметризованными запросами (?1, :name). Конкатенация строк в SQL отсутствует.
- **Авторизация:** Используется двойная защита: конфигурация URL в SecurityFilterChain + аннотации @PreAuthorize("hasAuthority('ROLE_ADMIN')") на методах контроллеров (Defense in Depth).

---

# 9. ОБРАБОТКА ОШИБОК

## 9.1. Типы исключений

- **ValidationSizeException:** Нарушение ограничений размеров. Содержит ValidationResult. Перехватывается контроллером, пользователь возвращается на форму с подсветкой ошибок.
- **AuthException / IllegalArgumentException:** Ошибки аутентификации или несовпадения паролей. Отображаются как flash-сообщения (errorMessage).
- **AccessDeniedException:** Попытка доступа к ресурсу без прав. Возвращает HTTP 403 или редирект на главную.
- **DomainException (например, IllegalStateException):** Нарушение бизнес-инвариантов (например, отрицательная ширина ячейки).
- **EquipmentNotFoundException:** Оборудование не найдено в каталоге. Блокирует сохранение ALS с понятным сообщением.

## 9.2. Глобальный обработчик

Используется @ControllerAdvice для централизованной обработки необработанных исключений, логирования ошибок (SLF4J) и возврата пользовательских страниц ошибок (error.html).

## 9.3. Обработка ошибок в Draft-конфигураторе

При возникновении ошибки во время Find-or-Create:

- Транзакция откатывается полностью (не сохраняются частично созданные модули).
- DraftALS остаётся в HttpSession — пользователь не теряет введённые данные.
- Пользователь возвращается на форму с отображением ошибок.
- Пользователь может исправить параметры и повторить попытку.

---

# 10. ТРАНЗАКЦИОННОСТЬ И ИНДЕКСЫ БД

## 10.1. Границы транзакций

Аннотация @Transactional применяется на уровне Application Service (например, AuthService.createEmployee, ALSService.findOrCreate). Это гарантирует атомарность операций Find-or-Create: если создание связанного оборудования падает с ошибкой, вся транзакция откатывается, не оставляя "мусорных" записей.

## 10.2. Критические секции

**Find-or-Create** — критическая секция, требует защиты от гонок:

- Блокировка на уровне приложения (для одного инстанса).
- Или SELECT ... FOR UPDATE (для нескольких инстансов).

**Обновление ProjectALS** — атомарная операция замены ссылки на ALS.

## 10.3. Уровни изоляции

- Default (READ_COMMITTED в MySQL).
- Для Find-or-Create — SERIALIZABLE или явные блокировки.

## 10.4. Откат транзакций

При любом исключении внутри @Transactional — полный откат:

- Не сохраняются частично созданные модули.
- Не сохраняются частично созданные ALS.
- Draft в сессии остаётся (пользователь может исправить и повторить).

## 10.5. Индексы базы данных (Обязательны для производительности Find-or-Create)

Для обеспечения быстрого поиска дубликатов конфигураций необходимо создать следующие индексы:

    -- Оборудование
    CREATE INDEX idx_equipment_type_active ON equipment(equipment_type_id, is_active);
    CREATE INDEX idx_equipment_default ON equipment(equipment_type_id, is_default_for_type);

    -- Модули (для быстрого отсева кандидатов по габаритам)
    CREATE INDEX idx_module_dimensions ON module(module_type, height, width, depth);

    -- Связи ALS и Проектов
    CREATE INDEX idx_project_als_project ON project_als(project_id);
    CREATE INDEX idx_project_als_als ON project_als(als_id);
    CREATE UNIQUE INDEX uk_project_als ON project_als(project_id, als_id);

    -- ALS по ключевым характеристикам
    CREATE INDEX idx_als_characteristics ON als(payment_mode, monitor_size, printer_required, color_body, color_door);

---

# 11. РАЗВЁРТЫВАНИЕ И КОНФИГУРАЦИЯ

## 11.1. Файлы конфигурации

- **application.properties:** Настройки БД, Kafka, JWT-секретов, времени жизни токенов.
- **size-bounds.properties:** Все ограничения размеров и параметры TypeLb (TYPE1, TYPE2, TYPE3, TYPE4). Загружается через @Value и @PostConstruct в SizeValidator. Изменение требует перезапуска приложения.
- **.env:** Переменные окружения для Docker Compose (пароли БД, хосты Kafka).

## 11.2. Миграции БД

Использовать Liquibase или Flyway. Ключевые миграции для рефакторинга:

1. Создание таблиц equipment_type, equipment, module_equipment.
2. Удаление устаревших таблиц: lc_access_method, lc_print_option, lbc_access_method, lbc_print_option.
3. Добавление полей характеристик (display_enabled, color_body и т.д.) в таблицу als.
4. Создание индексов для Find-or-Create (см. раздел 10.5).

## 11.3. Docker Compose

Контейнеры:

- lb-calc-web (основное приложение).
- notification-service (уведомления).
- mysql (база данных).
- kafka + zookeeper (очередь сообщений).

## 11.4. Мониторинг

- Health-check эндпоинты: /actuator/health.
- Логирование: SLF4J + Logback (уровни INFO для бизнес-операций, ERROR для исключений).
- Асинхронная отправка событий: KafkaTemplate с обработкой CompletableFuture для логирования ошибок доставки без блокировки основного потока.

---

# 📌 Заключение

Документ описывает полную техническую реализацию системы LB_Calc_Web:

- Архитектуру и стек технологий
- Структуру приложения и правила зависимостей
- Ключевые алгоритмы (Find-or-Create, Resolvers, расчёт размеров)
- Все эндпоинты, включая создание, редактирование и удаление ALS/модулей
- Сценарии использования с последовательностями действий
- Политики безопасности и управления пользователями
- Обработку ошибок и транзакционность
- Развёртывание и конфигурацию

Все решения в SDD соответствуют утверждённому ТЗ v1.1 и DDD-спецификации, а также учитывают текущее состояние кодовой базы. Документ готов к использованию в качестве основного руководства при рефакторинге и дальнейшей разработке системы.

---

*Документ утверждён и готов к передаче в разработку.*