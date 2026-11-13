# Domain-Driven Design (DDD) Specification: LB_Calc_Web

**Система расчёта конфигураций и визуализации автоматических камер хранения**

- **Автор:** Denis Solomentsev
- **Дата:** 16 октября 2026
- **Версия:** 3.0 (Финальная, полная)
- **Статус:** Утверждено

---

## Содержание

1. Strategic Design (Стратегический уровень)
2. Tactical Design: Aggregates (Тактический уровень: Агрегаты)
3. Value Objects (Объекты-значения)
4. Domain Services (Доменные сервисы)
5. Domain Events (Доменные события)
6. Business Rules & Invariants (Свод бизнес-правил и инвариантов)

---

# 1. STRATEGIC DESIGN (Стратегический уровень)

## 1.1. Domain Vision Statement (Видение предметной области)

**LB_Calc_Web** — это система для проектирования и расчёта конфигураций автоматических локерных систем (ALS).

**Ключевая ценность:** Автоматизация процесса конфигурирования сложных технических систем с учётом физических ограничений, бизнес-правил и доступного оборудования. Система позволяет менеджерам быстро создавать коммерческие предложения, автоматически подбирая оборудование, рассчитывая физические параметры и генерируя точную визуальную спецификацию на основе строгих доменных данных.

## 1.2. Subdomains (Поддомены)

### Core Domain (Основной поддомен)

**ALS Configuration** — ядро бизнес-ценности:

- Конфигурирование ALS из модулей (LB, LC, LBC).
- Автоматический подбор оборудования на основе бизнес-характеристик.
- Расчёт физических параметров (ячейки, габариты).
- Паттерн Find-or-Create для обеспечения неизменяемости конфигураций.
- Генерация структурированных данных для визуализации схемы ALS.

### Supporting Subdomains (Вспомогательные поддомены)

**Project Management** — управление проектами клиентов (создание, связь с ALS, экспорт спецификаций).

**Employee Management** — управление учётными записями, ролями и аутентификацией сотрудников.

### Generic Subdomain (Общий поддомен)

**Notification** — отправка email-уведомлений и обработка событий (может быть реализована через внешний сервис).

## 1.3. Bounded Contexts & Context Map

    ┌─────────────────────────────────────────────────────────────┐
    │  ┌──────────────────┐                                       │
    │  │  ALS             │                                       │
    │  │  Configuration   │                                       │
    │  └────────┬─────────┘                                       │
    │           │ Customer-Supplier (поставляет ALS)              │
    │           ▼                                                 │
    │  ┌──────────────────┐                                       │
    │  │  Project         │                                       │
    │  │  Management      │                                       │
    │  └────────┬─────────┘                                       │
    │           │ Conformist (использует модель Employee)         │
    │           ▼                                                 │
    │  ┌──────────────────┐                                       │
    │  │  Employee        │                                       │
    │  │  Management      │                                       │
    │  └──────────────────┘                                       │
    │                                                             │
    │  ┌──────────────────┐                                       │
    │  │  Notification    │◄──────┐ (Published Language / Kafka)  │
    │  └──────────────────┘       │                               │
    │  ┌──────────────────┐       │                               │
    │  │  ALS / Employee  ├───────┘ (Публикуют события)           │
    │  └──────────────────┘                                       │
    └─────────────────────────────────────────────────────────────┘

**Типы отношений:**

1. **Customer-Supplier** (ALS Configuration → Project Management): ALS Configuration поставляет конфигурации ALS, Project Management использует их как есть.
2. **Conformist** (Project Management → Employee Management): Project Management подчиняется модели Employee.
3. **Published Language** (через Kafka): ALS Configuration и Employee Management публикуют события, Notification потребляет их.

## 1.4. Ubiquitous Language (Единый язык)

| Термин                   | Определение                                                                                                                                             |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| **ALS**                  | Автоматическая локерная система. Законченная, неизменяемая конфигурация из модулей.                                                                     |
| **Module**               | Физическая единица конструкции: `LB` (хранение), `LC` (управление), `LBC` (комбинированный).                                                            |
| **Equipment**            | Конкретная модель физического устройства из справочника.                                                                                                |
| **Characteristic**       | Бизнес-требование к ALS в целом (например, "режим PAID", "нужен RFID").                                                                                 |
| **Requirement**          | Техническое требование к оборудованию, выведенное из характеристики.                                                                                    |
| **Find-or-Create**       | Паттерн: перед созданием сущности система ищет идентичную по всем значимым полям конфигурацию. Если находит — переиспользует, если нет — создаёт новую. |
| **TypeLb**               | Конструктивный тип модуля хранения, жёстко задающий `deltaWidth`, `shelfThick` и `serviceZoneWidth`.                                                    |
| **DirectionDoorOpening** | Направление открытия дверей (`LEFT` / `RIGHT`). Критично для расчёта позиции сервисной зоны и визуализации.                                             |
| **Specification**        | Итоговый документ (например, Excel), содержащий визуальную схему ALS и табличные данные оборудования.                                                   |
| **Employee**             | Сотрудник системы с ролью (MANAGER или ADMIN) и учётными данными.                                                                                       |
| **Project**              | Контейнер для ALS-конфигураций клиента с указанием количества каждой ALS.                                                                               |
# 2. TACTICAL DESIGN: AGGREGATES (Тактический уровень)

## Aggregate 1: Equipment (Оборудование)

**Корень агрегата:** `Equipment`

**Ответственность:** Управление справочником физических устройств.

**Инварианты:**

- `manufacturer` и `model` не могут быть пустыми.
- Габариты (`width`, `height`, `depth`) должны быть > 0.
- `maxLocks` должен быть > 0, если тип оборудования `LOCK_CONTROLLER`.
- `unitCost` должен быть >= 0.

**Неизменяемые поля (после создания):**

- `equipmentType`, `manufacturer`, `model`, `parameters`
- `width`, `height`, `depth`, `powerConsumption`, `maxLocks`

**Изменяемые поля:**

- `unitCost`, `isActive`, `isDefaultForType`

---

## Aggregate 2: Module (Модуль)

**Корень агрегата:** `Module` (абстрактный).

**Наследники:** `LB`, `LC`, `LBC`.

**Ответственность:** Управление конфигурацией модуля, расчёт производных размеров ячеек и привязка оборудования.

**Инварианты:**

**Для всех модулей:**

- `height`, `width`, `depth` должны быть в допустимых пределах.
- `upperFrame`, `bottomFrame` должны быть > 0.
- Заданы `colorBody` и `colorDoor`.

**Для LB/LBC (StorageModule):**

- `countCells >= 1`
- `cellHeight >= 85` мм
- `cellWidth > 0`
- `cellDepth > 0`
- `typeLb` не может быть null

**Для LC/LBC (ControlModule):**

- `width >= max(equipment.width)` — ширина должна вмещать самое широкое оборудование.
- Должен быть хотя бы один `LOCK_CONTROLLER` (если есть замки в ALS).

**Неизменяемость:** После сохранения в БД модуль не изменяется. Изменение = создание нового модуля (Find-or-Create).

**Ключевая бизнес-логика (Расчёт размеров ячеек):**

- `cellWidth = moduleWidth - typeLb.deltaWidth - typeLb.serviceZoneWidth`
- `cellDepth = moduleDepth - doorThickness`
- `usableHeight = moduleHeight - upperFrame - bottomFrame - ((countCells - 1) * typeLb.shelfThick)`
- `cellHeight = usableHeight / countCells`

---

## Aggregate 3: ALS (Автоматическая локерная система)

**Корень агрегата:** `ALS`

**Ответственность:** Управление бизнес-характеристиками системы и композицией модулей.

**Инварианты:**

- Ровно один управляющий модуль (`LC` или `LBC`).
- Хотя бы один модуль хранения (`LB` или `LBC`).
- `LC` и `LBC` не могут сосуществовать в одной ALS.
- Модули имеют уникальный `moduleOrder` (1, 2, 3...).
- Все модули имеют синхронизированные `height`, `depth`, `colorBody`, `colorDoor`.
- Направления дверей рассчитываются автоматически на основе позиции.

**Неизменяемость:** После сохранения ALS не изменяется. Изменение = создание новой ALS (Find-or-Create).

**Ключевая бизнес-логика:**

1. **Расчёт направлений дверей:**
   - Для `LB`: если модуль слева от управления → `LEFT`, если справа → `RIGHT`.
   - Для `LBC`: если модуль слева от управления → `RIGHT`, если справа → `LEFT`.
2. **Генерация имени:** Формат: `ALS {totalCells} яч. | {paymentMode} | {accessMethods} | {colorBody}/{colorDoor}`.

---

## Aggregate 4: Project (Проект)

**Корень агрегата:** `Project`

**Ответственность:** Управление проектами клиентов. **Единственный полностью изменяемый агрегат.**

**Инварианты:**

- `name` и `company` не могут быть пустыми.
- `createdBy` (ссылка на Employee) не может быть null.
- Уникальность пары `(projectId, alsId)` в связях проекта.

**Изменяемость:**

- Можно изменять метаданные проекта.
- Можно добавлять/удалять связи с ALS.
- Можно изменять `quantity` (количество) ALS в проекте.
- Изменение `quantity` не создаёт новую ALS.

---

## Aggregate 5: Employee (Сотрудник)

**Корень агрегата:** `Employee`

**Ответственность:** Управление учётными записями, ролями и аутентификационными данными.

**Инварианты:**

- `email` должен быть уникальным в системе и соответствовать формату.
- `passwordHash` не может быть пустым.
- `role` не может быть null.
- `firstName` и `lastName` не могут быть пустыми.

**Неизменяемые поля:** `id`, `registrationDate`.

**Изменяемые поля:** `firstName`, `lastName`, `role`, `passwordHash`, `isActive`.

**Ключевая бизнес-логика:**

- **Смена пароля пользователем:** Требует предоставления текущего пароля для проверки. Новый пароль должен отличаться от текущего.
- **Сброс пароля администратором:** Позволяет установить новый пароль без знания старого, но требует, чтобы запрашивающий сотрудник имел роль `ADMIN`.
- **Изменение роли:** Может быть выполнено только сотрудником с ролью `ADMIN`.
# 3. VALUE OBJECTS (Объекты-значения)

## EquipmentType
- `code` (уникальный код: DISPLAY, RFID_READER, LOCK_CONTROLLER)
- `name` (человекочитаемое имя)
- `category` (CONTROL, STORAGE, POWER, NETWORK)

## TypeLb
- `type` (TYPE1, TYPE2, TYPE3, TYPE4)
- `deltaWidth` (добавочная ширина, мм)
- `shelfThick` (толщина полки, мм)
- `serviceZoneWidth` (ширина сервисной зоны, мм)
- Все числовые значения >= 0

## ALSCharacteristics
- `paymentMode` (FREE, PAID)
- `connectionMode` (AUTONOMOUS, NETWORK)
- `display` (enabled, size, touch)
- `accessMethods` (Set<AccessMethod>)
- `printOption` (NONE, REQUIRED)
- `lighting` (enabled)
- `powerSupply` (mainsRequired, upsRequired, backupDuration, requiredPower)
- `powerOutlet` (enabled, quantity)

## Dimensions
- `width`, `height`, `depth` (все > 0)

## CellDimensions
- `width`, `height`, `depth` (height >= 85, width > 0, depth > 0)

## Credentials
- `email` (уникальный, валидный формат)
- `passwordHash` (инкапсулирует логику проверки пароля)

## Enums
- `PaymentMode`: FREE, PAID
- `ConnectionMode`: AUTONOMOUS, NETWORK
- `PrintOption`: NONE, REQUIRED
- `AccessMethod`: RFID, QR_CODE, BARCODE, PIN
- `DirectionDoorOpening`: LEFT, RIGHT
- `Colors`: Blue, White, Gray, Black, RAL7035, RAL9003
- `ModuleType`: LB, LC, LBC
- `EquipmentCategory`: CONTROL, STORAGE, POWER, NETWORK
- `Role`: MANAGER, ADMIN

---

# 4. DOMAIN SERVICES (Доменные сервисы)

Логика, которая не принадлежит одному агрегату, но является частью предметной области.

## 4.1. EquipmentRequirementResolver

**Ответственность:** Преобразует `ALSCharacteristics` в список `EquipmentRequirement`.

**Правила:**

- Если `display.enabled == true` → требование `DISPLAY` с параметрами `size` и `touch`.
- Для каждого `accessMethod` → соответствующий тип оборудования (RFID → RFID_READER и т.д.).
- Если `printOption == REQUIRED` → требование `PRINTER`.
- Если `upsRequired == true` → требование `UPS` с параметром `backupDuration`.
- Для каждого storage-модуля → требование `ELECTRONIC_LOCK` (количество = `countCells`).
- Если `lighting.enabled == true` → требование `LIGHTING` (количество = `countCells` каждого storage-модуля).
- Количество `LOCK_CONTROLLER` = `ceil(totalLocks / controller.maxLocks)`.

## 4.2. ModuleAllocationResolver

**Ответственность:** Распределяет требования по модулям на основе строгих правил.

**Правила размещения:**

- `ELECTRONIC_LOCK`, `LIGHTING` → в каждый `StorageModule` (LB/LBC).
- `DISPLAY`, `RFID_READER`, `QR_READER`, `PRINTER`, `UPS`, `LOCK_CONTROLLER`, `POWER_OUTLET` → в `ControlModule` (LC/LBC).

## 4.3. FindOrCreateService<T>

**Ответственность:** Универсальный сервис поиска или создания сущности по её идентичности.

**Алгоритм:**

1. Получить входные параметры конфигурации.
2. Выполнить расчёты и валидацию.
3. Выполнить поиск в БД по индексированным полям.
4. Для каждого кандидата загрузить набор оборудования.
5. Сравнить множества: если количество и пары (equipmentId, quantity) совпадают → вернуть существующий ID.
6. Если не найдено → создать новую запись.

## 4.4. ALSVisualizationDataProvider

**Ответственность:** Извлекает из агрегата `ALS` структурированные данные, необходимые для генерации визуализации схемы, **не загрязняя домен знаниями о графике**.

**Выходные данные:**

- Для каждого модуля: тип, габариты, HEX-коды цветов, `DirectionDoorOpening`, количество ячеек, толщина полок, высота ячеек, ширина сервисной зоны, позиция X.
- Для ALS: общая ширина, общая высота, список модулей в порядке.
# 5. DOMAIN EVENTS (Доменные события)

События, публикуемые агрегатами при изменении состояния (для аудита или асинхронной обработки через Kafka).

- `ALSCreatedEvent(ALSId, ProjectId, occurredOn)`
- `ModuleReconfiguredEvent(oldModuleId, newModuleId, occurredOn)`
- `ProjectSpecificationExportedEvent(projectId, fileHash, occurredOn)`
- `EmployeeRegisteredEvent(employeeId, email, occurredOn)`
- `EmployeeLoggedInEvent(employeeId, email, occurredOn)`
- `PasswordChangedEvent(employeeId, email, occurredOn)`

**Обработка событий:**

- События `Employee*` отправляются в Kafka и потребляются `notification-service` для отправки email.
- События `ALSCreatedEvent`, `ModuleReconfiguredEvent` могут использоваться для аудита или аналитики.
- `ProjectSpecificationExportedEvent` фиксирует факт генерации спецификации.

---

# 6. BUSINESS RULES & INVARIANTS (Свод бизнес-правил)

## 6.1. Структурные правила ALS

- Ровно один управляющий модуль (LC или LBC).
- Хотя бы один модуль хранения (LB или LBC).
- LC и LBC не могут сосуществовать в одной ALS.
- Модули выстраиваются в единую физическую линию с уникальным порядком.

## 6.2. Правила расчёта размеров

- `cellWidth = moduleWidth - typeLb.deltaWidth - typeLb.serviceZoneWidth`
- `cellDepth = moduleDepth - doorThickness`
- `cellHeight = (moduleHeight - upperFrame - bottomFrame - ((countCells - 1) * shelfThick)) / countCells`
- `cellHeight >= 85` мм (минимальная высота ячейки)
- `cellWidth > 0`, `cellDepth > 0`

## 6.3. Правила размещения оборудования

- Замки и подсветка размещаются в ячейках модулей хранения (LB/LBC).
- Всё остальное оборудование размещается в управляющем модуле (LC/LBC).
- Количество контроллеров замков = `ceil(totalLocks / controller.maxLocks)`.

## 6.4. Правила идентичности

- Модуль идентичен, если совпадают: габариты, цвета, рамы, тип (для storage), направление двери, набор оборудования.
- ALS идентична, если совпадают: все характеристики, упорядоченный список модулей.
- Имя ALS генерируется автоматически и не участвует в идентичности.

## 6.5. Правила неизменяемости

- После сохранения `Equipment`, `Module`, `ALS` не изменяются (кроме `unitCost`, `isActive`, `isDefaultForType` для Equipment).
- Изменение = создание новой конфигурации (Find-or-Create).
- Единственное исключение: изменение `quantity` в `ProjectALS` не создаёт новую ALS.

## 6.6. Правила визуализации

- Визуализация генерируется на основе доменных данных (габариты, цвета, направление дверей, количество ячеек).
- Направление дверей определяет позицию сервисной зоны и полок на схеме.
- Визуализация является вычисляемым представлением доменной модели, а не статичным изображением.

## 6.7. Правила безопасности пользователей

- Смена пароля требует проверки текущего пароля.
- Сброс пароля и изменение ролей доступны исключительно пользователям с ролью `ADMIN`.
- Email должен быть уникальным в системе.

---

*Документ утверждён. Данная спецификация является единственным источником истины для проектирования, рефакторинга и развития доменной модели LB_Calc_Web.*