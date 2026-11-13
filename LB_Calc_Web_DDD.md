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

**Корневая абстракция:** `Equipment`

**Ответственность:** Общие правила идентификации, доступности и выбора физических устройств. Фактическое ведение оборудования организовано по отдельным логическим каталогам в зависимости от типа и назначения.

**Общие атрибуты:** идентификатор, тип каталога, производитель, модель, габариты (если применимо), стоимость, активность и признак модели по умолчанию.

**Специализированные каталоги:** принтеры, мониторы, оборудование оплаты, RFID-считыватели, QR-считыватели и считыватели штрихкодов. Каждый каталог имеет собственные характеристики и правила валидации. Оборудование других подсистем (например, замки и контроллеры) также может иметь собственные типы и модели.

**Инварианты:**

- Производитель и модель обязательны для каталогов, где эти сведения применимы.
- Габариты проверяются по правилам конкретного типа оборудования.
- Специализированные характеристики валидируются в контексте своего каталога.
- Для одного типа каталога используется не более одной активной модели по умолчанию.

**Неизменяемые характеристики после создания:** тип каталога, производитель, модель и технические характеристики.

**Изменяемые поля:** стоимость, активность и признак модели по умолчанию.

Общая абстракция Equipment не предполагает единую универсальную таблицу со всеми характеристиками всех типов оборудования.

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

**Ответственность:** Управление бизнес-характеристиками системы, требованиями к оборудованию и упорядоченной композицией модулей.

**Инварианты:**

- Ровно один управляющий модуль: `LC` или `LBC`.
- Хотя бы один модуль хранения: `LB` или `LBC`.
- `LC` и `LBC` не могут сосуществовать в одной ALS.
- Модули имеют уникальный `moduleOrder`; порядок задаёт физическое расположение слева направо.
- Расположение управляющего модуля задаётся значением `LEFT`, `CENTER` или `RIGHT` и должно соответствовать фактическому порядку модулей.
- Высота, глубина, цвет корпуса и цвет дверей синхронизируются между всеми модулями ALS.
- Ширина ALS равна сумме ширин всех модулей.
- Размеры отдельного LC/LBC определяются конфигурацией модуля и действующими ограничениями размеров.

**Неизменяемость:** После сохранения ALS не изменяется. Изменение характеристик, оборудования, размеров или порядка модулей создаёт новую конфигурацию ALS (Find-or-Create).

**Ключевая бизнес-логика:**

1. Положение управляющего модуля: слева, посередине или справа. Физический порядок модулей формируется с учётом этого значения.
2. Направления открытия дверей модулей хранения определяются их фактическим положением относительно управляющего модуля и действующими конструктивными правилами.
3. Имя ALS генерируется автоматически и не участвует в идентичности конфигурации.

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

Тип оборудования определяет отдельный логический каталог и собственный набор характеристик. Каталоги текущей модели:

- PRINTER — принтеры;
- MONITOR — мониторы;
- PAYMENT — оборудование оплаты;
- RFID_READER — RFID-считыватели;
- QR_READER — QR-считыватели;
- BAR_READER — считыватели штрихкодов.

У каждого каталога собственные атрибуты, правила валидации и критерии совместимости. Общими могут быть идентификатор, производитель, модель, габариты, стоимость, активность и признак модели по умолчанию. Общая абстракция `Equipment` не означает единую универсальную модель характеристик.

## TypeLb
- `type` (TYPE1, TYPE2, TYPE3, TYPE4)
- `deltaWidth` (добавочная ширина, мм)
- `shelfThick` (толщина полки, мм)
- `serviceZoneWidth` (ширина сервисной зоны, мм)
- Все числовые значения >= 0

## ALSCharacteristics

- `paymentMode`: NONE, CASH, CASHLESS, CASH_AND_CASHLESS.
- `monitorSize`: NONE, 10, 17, 19.
- `printerRequired`: boolean (YES/NO).
- `accessMethods`: множество значений RFID, BARCODE, QR_CODE, PIN.
- `controlModulePosition`: LEFT, CENTER, RIGHT.
- `colorBody`, `colorDoor`: общие цвета для всех модулей ALS.
- `height`, `depth`: общие габариты, синхронизируемые со всеми модулями.
- `width`: производный параметр — сумма ширин модулей ALS.

**Бизнес-инварианты характеристик:**

- Если выбран PIN, монитор обязателен (размер 10, 17 или 19).
- Если выбран PIN, должен быть выбран как минимум один из RFID, BARCODE или QR_CODE.
- PIN не может быть единственным способом доступа.
- Принтер обязателен, если выбрана любая оплата, кроме NONE, либо выбран BARCODE, QR_CODE или PIN.
- Если ни одно условие обязательности принтера не выполняется, пользователь может выбрать наличие или отсутствие принтера.
- Несколько способов доступа могут быть выбраны одновременно.

## Dimensions
- `width`, `height`, `depth` (все > 0)

## CellDimensions
- `width`, `height`, `depth` (height >= 85, width > 0, depth > 0)

## Credentials
- `email` (уникальный, валидный формат)
- `passwordHash` (инкапсулирует логику проверки пароля)

## Enums
- `PaymentMode`: NONE, CASH, CASHLESS, CASH_AND_CASHLESS
- `MonitorSize`: NONE, 10, 17, 19
- `AccessMethod`: RFID, QR_CODE, BARCODE, PIN
- `ControlModulePosition`: LEFT, CENTER, RIGHT
- `DirectionDoorOpening`: LEFT, RIGHT
- `Colors`: значения из справочника цветов
- `ModuleType`: LB, LC, LBC
- `EquipmentCatalogType`: PRINTER, MONITOR, PAYMENT, RFID_READER, QR_READER, BAR_READER
- `Role`: MANAGER, ADMIN

---

---

# 4. DOMAIN SERVICES (Доменные сервисы)

Логика, которая не принадлежит одному агрегату, но является частью предметной области.

## 4.1. EquipmentRequirementResolver

**Ответственность:** Преобразует характеристики ALS в требования к типам оборудования и критериям его подбора.

**Правила:**

- `monitorSize != NONE` → требование MONITOR с соответствующим размером.
- `paymentMode != NONE` → требование к каталогу PAYMENT с учётом выбранного варианта оплаты.
- `printerRequired == true` → требование PRINTER.
- Для каждого выбранного способа доступа формируется требование к соответствующему каталогу RFID_READER, BAR_READER или QR_READER.
- PIN является способом доступа и учитывается в правилах обязательности монитора и принтера; отдельный каталог PIN_PAD в текущем упрощённом перечне каталогов не вводится.
- Оборудование для хранения и замков определяется структурой модулей и ранее установленными правилами расчёта.

Конкретная модель оборудования не задаётся характеристиками ALS: она подбирается из соответствующего каталога.

## 4.2. ModuleAllocationResolver

**Ответственность:** Размещает выбранное оборудование в управляющем модуле.

**Правила размещения:**

- Оборудование каталогов PRINTER, MONITOR, PAYMENT, RFID_READER, QR_READER и BAR_READER относится к управляющей части и размещается в LC/LBC.
- Оборудование хранения и замков размещается согласно действующим правилам для LB/LBC.
- LBC остаётся одним физическим модулем, совмещающим функции хранения и управления.

Подбор выполняется отдельно для каждого типа каталога. По умолчанию выбирается подходящая активная позиция, отмеченная как модель по умолчанию; если такой позиции нет, применяется установленное правило выбора первого подходящего кандидата. При редактировании LC/LBC пользователь может заменить позицию на другую подходящую модель из того же каталога. Ручной выбор не должен нарушать требования ALS.

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
- Модули выстраиваются в единую физическую линию с уникальным порядком слева направо.
- Положение управляющего модуля задаётся как LEFT, CENTER или RIGHT и должно соответствовать порядку модулей.
- Для позиции CENTER требуются модули хранения по обе стороны управляющего модуля.
- Высота, глубина и цвета корпуса/дверей являются общими характеристиками всех модулей ALS.
- Ширина ALS рассчитывается как сумма ширин модулей.

**Требует отдельного уточнения:** для ALS, состоящей только из одного LBC, понятие «слева / посередине / справа» не имеет физического смысла. До определения отдельного правила позиция не должна искусственно влиять на идентичность такой конфигурации.

## 6.2. Правила расчёта размеров

- `cellWidth = moduleWidth - typeLb.deltaWidth - typeLb.serviceZoneWidth`
- `cellDepth = moduleDepth - doorThickness`
- `cellHeight = (moduleHeight - upperFrame - bottomFrame - ((countCells - 1) * shelfThick)) / countCells`
- `cellHeight >= 85` мм (минимальная высота ячейки)
- `cellWidth > 0`, `cellDepth > 0`

## 6.3. Правила размещения оборудования

- Принтеры, мониторы, оборудование оплаты и считыватели RFID/QR/штрихкодов размещаются в управляющей части LC/LBC.
- Конкретная модель выбирается из отдельного логического каталога своего типа.
- Каталоги имеют разные наборы характеристик и правила валидации; универсальный набор технических параметров для всех типов не предполагается.
- Для каждого каталога действует автоматический выбор подходящей модели по умолчанию и возможность ручной замены при редактировании LC/LBC.
- Выбранная модель обязана удовлетворять требованиям ALS и ограничениям управляющего модуля.

## 6.4. Правила идентичности

- Модуль идентичен, если совпадают: габариты, цвета, рамы, тип (для storage), направление двери, набор оборудования.
- ALS идентична, если совпадают все бизнес-характеристики (оплата, монитор, принтер, множество способов доступа, цвета, общие высота и глубина), положение управляющего модуля и упорядоченный список модулей с их конфигурациями. Для одно-модульной ALS с LBC положение не различает физические конфигурации до определения отдельного правила.
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

# 7. GUEST MODE AND PROJECT LIFECYCLE
## 7.1. Guest user

A guest is an unauthenticated visitor who can create and configure projects and ALS configurations without creating an account. Guest work is temporary and is stored in the browser; it is not persisted as a Project aggregate in the server database.

Guest capabilities include:
- creating and editing projects in the browser;
- adding and configuring ALS and its modules;
- selecting equipment from server-side catalogs;
- using the same domain calculations and business validation as an authenticated user, through permitted public application endpoints.

## 7.2. Guest data storage

- IndexedDB is the primary browser storage for guest projects and their configuration data.
- LocalStorage is reserved for lightweight UI preferences and must not be used as the primary project store.
- Equipment catalogs and shared reference data remain server-side and are stored in MySQL. Public read access is provided only through explicitly permitted endpoints.
- No guest Project records or temporary guest-project tables are created in MySQL. No separate server-side guest account or guest session is introduced solely to store project drafts.

## 7.3. Authenticated project storage

Projects saved by authenticated users are persisted in MySQL and associated with the authenticated Employee/account. Access to saved projects follows the normal authorization and ownership rules.

## 7.4. Transfer of guest projects to an account

After registration or authentication, the user may transfer selected guest projects from IndexedDB to the authenticated account.

The transfer follows these rules:
1. The browser sends the selected project data to the server through the permitted REST API.
2. The server treats all received data as untrusted input and reconstructs/validates it using the same domain rules used for ordinary project operations.
3. The server assigns ownership from the authenticated principal; the client cannot choose or override the owner.
4. The server persists the project and its valid ALS configurations.
5. The browser deletes the corresponding local guest copy only after the server confirms successful persistence.
6. If the transfer fails, the local copy remains available for retry.

The exact user experience for duplicate projects, conflict resolution and retry presentation is a separate functional-design decision; it must not weaken server-side validation or cause premature deletion of local data.

## 7.5. Shared domain model

Guest and authenticated workflows use the same ALS, Module, Equipment and Project business rules and calculation logic. Guest mode changes the persistence location and access context, not the domain model or calculation algorithms.
