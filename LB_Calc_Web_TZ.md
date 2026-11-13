# Техническое задание: LB_Calc_Web

**Система расчёта конфигураций автоматических камер хранения**

Автор: Denis Solomentsev  
Дата: 16 октября 2026  
Версия: 1.1  
Статус: Утверждено

---

## Содержание

1. Описание проекта
2. Бизнес-логика
3. DDD-спецификация
4. Модель данных

---

# 1. ОПИСАНИЕ ПРОЕКТА

## 1.1. Что это за продукт

**LB_Calc_Web** — веб-приложение для расчёта конфигураций автоматических камер хранения (АКХ) и формирования коммерческих предложений.

**Основные возможности:**

- проектирование конфигураций локерных систем (ALS) из модулей хранения и управления
- автоматический подбор оборудования на основе заданных характеристик
- расчёт физических параметров (размеры, количество ячеек, вес, стоимость)
- формирование коммерческих предложений с техническими спецификациями
- ведение учёта проектов для разных клиентов

## 1.2. Целевая аудитория

**Менеджеры по продажам (роль ROLE_MANAGER):**

- работают с клиентами (ТЦ, аэропорты, офисы, ЖД вокзалы)
- получают запрос от клиента и подбирают оптимальную конфигурацию ALS
- формируют коммерческое предложение с расчётом стоимости

**Администраторы системы (роль ROLE_ADMIN):**

- ведут справочник оборудования (конкретные модели дисплеев, сканеров, замков)
- управляют каталогом типовых конфигураций модулей
- имеют доступ ко всем проектам всех менеджеров
- могут архивировать устаревшие конфигурации и удалять неиспользуемые

## 1.3. Какую проблему решает продукт

**До внедрения системы:**

- расчёт занимает 30-60 минут
- ошибки в расчётах из-за человеческого фактора
- несоответствие физическим ограничениям
- нет единой базы конфигураций

**После внедрения системы:**

- время расчёта: 5-10 минут
- нет ошибок благодаря автоматической валидации
- все конфигурации валидны физически
- единая база типовых решений

## 1.4. Ключевые особенности

1. Модульная архитектура ALS — гибкая компоновка из модулей LB, LC, LBC
2. Автоматический подбор оборудования из каталога
3. Неизменяемость конфигураций — принцип Find-or-Create
4. Визуализация — генерация схемы ALS с модулями
5. Экспорт в Excel/PDF — автоматическое формирование КП

---

# 2. БИЗНЕС-ЛОГИКА

## Часть 1. Основные понятия и термины

**ALS (Автоматическая локерная система)** — законченная конфигурация из модулей, выстроенных в физическую линию.

**Модуль (Module)** — базовая физическая единица конструкции. Имеет габариты, цвета и набор оборудования.

**LB (Storage Module)** — модуль хранения. Только ячейки с электронными замками.

**LC (Control Module)** — управляющий модуль. Дисплей, сканеры, принтер, терминал оплаты, контроллеры замков.

**LBC (Combined Module)** — комбинированный модуль. Объединяет функции хранения и управления.

**Оборудование (Equipment)** — конкретная физическая модель устройства. Имеет тип, производителя, модель и тех. параметры.

**Характеристика ALS** — бизнес-требование пользователя. Задаётся на уровне ALS.

**Требование к оборудованию** — техническая спецификация, выводимая из характеристик ALS.

**Проект (Project)** — контейнер для ALS-конфигураций клиента. Хранит ссылку на ALS и количество.

**Find-or-Create** — принцип: перед созданием новой сущности система ищет в БД идентичную. Если находит — переиспользует, если нет — создаёт.

**TypeLb** — конструктивный тип модуля. Задаёт deltaWidth, serviceZoneWidth, shelfThick.

## Часть 2. Структурные правила ALS

### 2.1. Состав и комбинации модулей

**Инварианты ALS:**

1. Ровно один управляющий модуль: LC или LBC
2. Хотя бы один модуль хранения: LB или LBC
3. Модули выстраиваются в единую физическую линию

**Допустимые комбинации:**

- LB + LC — минимальная стандартная
- LC + LB — управляющий модуль слева
- LB + LC + LB — классическая схема (LC в центре)
- LBC — минимальная компактная
- LB + LBC
- LBC + LB
- LB + LBC + LB

**Недопустимые комбинации:**

- Только LB — нет управляющего модуля
- Только LC — нет модуля хранения
- LC + LBC — два управляющих модуля
- LBC + LBC — два управляющих модуля

### 2.2. Физический порядок модулей

- Модули имеют строгий физический порядок слева направо (module_order: 1, 2, 3...)
- Порядок — неотъемлемая часть конфигурации ALS
- Изменение порядка = создание новой конфигурации ALS

### 2.3. Принцип неизменяемости (Immutability)

После сохранения в БД конфигурации Equipment, Module (LB/LC/LBC) и ALS не подлежат изменению (UPDATE бизнес-полей запрещён).

**При любом изменении характеристик:**

1. Формируются новые требования к оборудованию
2. Выполняется Find-or-Create новых модулей
3. Выполняется Find-or-Create новой ALS
4. В Project обновляется ссылка на новую ALS

**Исключение:** изменение количества ALS в проекте (project_als.quantity) не создаёт новую ALS.

### 2.4. Влияние TypeLb на габариты

Каждый TypeLb жёстко задаёт:

- shelfThick — толщина полки между ячейками, мм
- deltaWidth — добавочная ширина конструкции, мм
- serviceZoneWidth — ширина сервисной зоны, мм

**Формулы расчёта габаритов модуля:**

Высота: Height = upperFrame + bottomFrame + (countCells × cellHeight) + ((countCells − 1) × shelfThick)

Ширина: Width = cellWidth + deltaWidth + serviceZoneWidth

Глубина: Depth = cellDepth

### 2.5. Ограничения конфигурации (Size Bounds)

Все размеры в миллиметрах. Ограничения загружаются из size-bounds.properties и применяются классом SizeValidator.

**Количество ячеек:**

countCells — минимум 1, максимум 20, по умолчанию 3

**Размеры модулей и ячеек:**

- Высота модуля (height) — минимум 125, максимум 2300, по умолчанию 1940
- Высота ячейки (cellHeight) — минимум 85
- Высота LC — минимум 600, максимум 2300
- Глубина модуля (depth) — минимум 170, максимум 900, по умолчанию 500
- Глубина ячейки (cellDepth) — минимум 100, максимум 880
- Ширина модуля (width) — минимум 100, максимум 1200, по умолчанию 500
- Ширина ячейки (cellWidth) — минимум 100

**Конструктивные параметры:**

- Верхняя рама (upperFrame) — минимум 20, максимум 300, по умолчанию 50
- Нижняя рама (bottomFrame) — минимум 20, максимум 300, по умолчанию 50
- Толщина двери (doorThickness) — минимум 5, максимум 50, по умолчанию 20

**Параметры TypeLb:**

- deltaWidth — минимум 0, максимум 500 (TYPE1: 100, TYPE3: 160)
- shelfThick — минимум 0, максимум 100 (TYPE1: 5, TYPE3: 20)
- serviceZoneWidth — минимум 0, максимум 300 (TYPE1: 40, TYPE3: 40)

## Часть 3. Характеристики ALS

### 3.1. Что такое характеристики ALS

Характеристики ALS — это бизнес-требования пользователя, которые:

1. Задаются на уровне ALS (не модуля)
2. Трансформируются в требования к оборудованию
3. Реализуются через физическое оборудование в модулях LC/LBC
4. Определяют уникальность модулей (для Find-or-Create)

### 3.2. Список характеристик ALS

**Базовые характеристики:**

- Режим обслуживания (paymentMode) — выпадающий список: FREE, PAID (обязательна)
- Режим подключения (connectionMode) — выпадающий список: AUTONOMOUS, NETWORK (обязательна)
- Цвет корпуса (colorBody) — выпадающий список: RAL 7035, RAL 9003, Синий (обязателен)
- Цвет дверей (colorDoor) — выпадающий список: RAL 9003, RAL 7035, Прозрачный (обязателен)
- Печать (printOption) — выпадающий список: NONE, REQUIRED (обязательна)

**Дисплей (группа):**

- Включить дисплей — чекбокс (опциональна)
- Размер — выпадающий список: 15.6, 21.5, 27.0 (если включён)
- Сенсорный — чекбокс (опционален)

**Методы доступа (группа чекбоксов):**

- RFID — опциональна
- QR-код — опциональна
- Штрих-код — опциональна
- PIN-код — опциональна

**Электропитание (группа):**

- Подключение к сети — чекбокс (опциональна)
- Требуется ИБП — чекбокс (опциональна)
- Время резерва (мин) — числовое поле: 5, 15, 30, 60 (если ИБП включён)
- Мощность (Вт) — числовое поле: 300, 500, 1000 (опционально)

**Прочие:**

- Освещение — чекбокс (опциональна)
- Розетки: включить — чекбокс (опциональна)
- Розетки: количество — числовое поле: 1, 2, 4, 6 (если включены)

### 3.3. Правила валидации характеристик

**Структурная валидация:**

- paymentMode, connectionMode, printOption, colorBody, colorDoor не могут быть null

**Условная валидация:**

- если display_enabled = true, то display_size обязателен
- если ups_required = true, то backup_duration >= 5
- если outlet_enabled = true, то outlet_quantity >= 1

**Бизнесовая валидация:**

- должен быть выбран хотя бы один метод доступа

## Часть 4. Формирование требований к оборудованию

### 4.1. Оборудование для управляющего модуля (LC/LBC)

- display.enabled = true → DISPLAY (size, touch) — количество 1
- accessMethods содержит RFID → RFID_READER — количество 1
- accessMethods содержит QR_CODE → QR_READER — количество 1
- accessMethods содержит BARCODE → BARCODE_READER — количество 1
- accessMethods содержит PIN → PIN_PAD — количество 1
- printOption = REQUIRED → PRINTER — количество 1
- upsRequired = true → UPS (backupDuration) — количество 1
- outlet.enabled = true → POWER_OUTLET — количество outlet_quantity
- Всегда → LOCK_CONTROLLER — количество ceil(totalLocks / maxLocks)

### 4.2. Оборудование для модулей хранения (LB/LBC)

- Всегда для каждой ячейки → ELECTRONIC_LOCK — количество countCells модуля
- lighting.enabled = true → LIGHTING — количество countCells модуля

### 4.3. Алгоритм выбора оборудования из каталога

Для каждого EquipmentRequirement система:

1. Ищет все активные (isActive = true) записи нужного типа
2. Фильтрует по параметрам (если указаны в требовании)
3. Если не найдено ни одного — блокирует сохранение с ошибкой
4. Если найдено несколько — берёт модель с isDefaultForType = true или первую из списка

## Часть 5. Размещение оборудования по модулям

### 5.1. Правило размещения оборудования

**Для модулей хранения (LB / LBC):**

- ELECTRONIC_LOCK — в каждую ячейку, количество countCells модуля
- LIGHTING — в каждую ячейку, количество countCells модуля

**Для управляющего модуля (LC / LBC):**

- DISPLAY — в корпус управления, количество 1
- RFID_READER, QR_READER, BARCODE_READER, PIN_PAD — в корпус управления, по 1 на каждый выбранный метод
- PRINTER — в корпус управления, количество 1 (если printOption = REQUIRED)
- UPS — в корпус управления, количество 1 (если upsRequired = true)
- POWER_OUTLET — в корпус управления, количество outlet_quantity
- LOCK_CONTROLLER — в корпус управления, количество по формуле

### 5.2. Расчёт количества LOCK_CONTROLLER

Ёмкость контроллера (maxLocks) берётся из конкретной модели в каталоге Equipment.

**Формула:**

totalLocks = сумма(countCells) всех модулей хранения в ALS

lockControllersNeeded = ceil(totalLocks / maxLocks_контроллера)

**Пример:** В ALS 18 ячеек, выбрана модель контроллера на 5 замков. ceil(18 / 5) = 4 контроллера. Все они размещаются в управляющем модуле.

## Часть 6. Расчёт физических параметров

### 6.1. Формулы расчёта размеров ячеек

Расчёт выполняется методом recalculateStorageCells() внутри доменной модели StorageModule:

**Ширина ячейки:** cellWidth = moduleWidth - typeLb.deltaWidth - typeLb.serviceZoneWidth

**Глубина ячейки:** cellDepth = moduleDepth - doorThickness

**Высота ячейки:**

usableHeight = moduleHeight - upperFrame - bottomFrame - ((countCells - 1) * typeLb.shelfThick)

cellHeight = (int) (usableHeight / countCells)

### 6.2. Инварианты валидации расчётов

После расчёта Domain-модель обязана проверить физическую реализуемость:

- cellWidth > 0
- cellDepth > 0
- cellHeight >= 85
- countCells >= 1

### 6.3. Расчёт габаритов ALS

- Общая ширина ALS: ALS.width = сумма ширин всех модулей в порядке module_order
- Высота и глубина ALS: синхронизируются со всеми модулями

### 6.4. Синхронизация цветовых параметров

При сборке ALS метод synchronizeModules() принудительно устанавливает значения colorBody и colorDoor из корневой сущности ALS во все вложенные модули (LB, LC, LBC). Это гарантирует монолитный внешний вид всей линии локеров.

## Часть 7. Идентичность конфигурации

### 7.1. Главный принцип

Идентичность определяется независимыми входными полями. Производные поля (рассчитанные размеры ячеек, сгенерированное имя, номера ячеек) не участвуют в ключе сравнения.

### 7.2. Идентичность Equipment

Два объекта Equipment считаются одинаковыми, если совпадают:

- equipmentType
- manufacturer
- model
- технические параметры (parameters)
- габариты (width, height, depth)
- потребляемая мощность (powerConsumption)

Не участвуют в идентичности: id, unitCost, isActive, isDefaultForType.

### 7.3. Идентичность модуля

**Для LB:**

- Габариты: height, width, depth
- Рамы: upperFrame, bottomFrame
- Цвета: colorBody, colorDoor
- Конструкция: typeLb, countCells, doorThickness, directionDoorOpening
- Набор оборудования: точный список пар (equipmentId, quantity)

**Для LC:**

- Габариты: height, width, depth
- Рамы: upperFrame, bottomFrame
- Цвета: colorBody, colorDoor
- Набор оборудования: точный список пар (equipmentId, quantity)

**Для LBC:**

- Все параметры хранения (как у LB) + directionDoorOpening
- Все параметры управления (как у LC)
- Набор оборудования: объединённый список (и для ячеек, и для управления)

### 7.4. Идентичность ALS

ALS считается идентичной, если совпадают:

1. Все характеристики ALS (включая цвета, режимы, дисплей, методы доступа)
2. Упорядоченный список модулей (совпадение конфигураций модулей и их строгого порядка module_order)

### 7.5. Имя ALS — производное от характеристик

Поля name и description ALS генерируются системой автоматически. Пользователь их не задаёт.

**Формат имени:** ALS {totalCells} яч. | {paymentMode} | {accessMethods} | {colorBody}/{colorDoor}

**Примеры:**

- ALS 32 яч. | PAID | RFID, QR | RAL7035/RAL9003
- ALS 16 яч. | FREE | RFID | Синий/Белый

## Часть 8. Find-or-Create

### 8.1. Алгоритм поиска или создания

Применяется для Equipment, Module (LB/LC/LBC) и ALS.

1. Получить входные параметры будущей конфигурации
2. Выполнить расчёты (ячейки, габариты, направления) и валидацию
3. Сузить выборку кандидатов в БД по индексированным полям
4. Для каждого кандидата загрузить его полный набор оборудования
5. Сравнить полное множество значимых полей и наборов оборудования
6. Если найден полный эквивалент — вернуть его id
7. Если не найден — выполнить INSERT новой записи

### 8.2. Сравнение набора оборудования

Два набора оборудования считаются идентичными, если:

- Количество уникальных позиций (equipmentId) совпадает
- Для каждой позиции в первом наборе во втором наборе существует запись с тем же equipmentId и тем же quantity
- Порядок следования записей в БД не имеет значения

## Часть 9. Жизненный цикл

### 9.1. Состояния конфигурации

DRAFT (в сессии) → SAVED/IMMUTABLE (в БД) → USED (в проектах) → ORPHAN (сирота)

### 9.2. Разрешённые операции

- Создать ALS — Find-or-create модулей → find-or-create ALS → создать ProjectALS
- Изменить характеристику ALS — Find-or-create новых модулей и ALS → обновить ссылку в ProjectALS
- Изменить количество — UPDATE project_als.quantity (единственный разрешённый UPDATE)
- Переименовать проект — UPDATE project
- Удалить ALS из проекта — DELETE project_als
- Удалить проект — DELETE project + каскадно project_als
- Архивировать ALS (админ) — установка флага archived

**Запрещено:** UPDATE als, UPDATE lb, UPDATE lc для изменения бизнес-конфигурации.

### 9.3. Каскады

- Project → ProjectALS — принадлежность — каскад + orphanRemoval
- ProjectALS → ALS — ссылка — никакого каскада
- ALS → ALSModule — принадлежность — каскад + orphanRemoval
- ALSModule → Module — ссылка — никакого каскада
- Module → ModuleEquipment — принадлежность — каскад
- ModuleEquipment → Equipment — ссылка — никакого каскада

### 9.4. Сироты

Сироты (ALS, модули, оборудование без ссылок из проектов) хранятся в БД. Администратор может удалить сироту или пометить архивной. Админ может создавать новые модули и ALS вне проектов для подготовки каталога.

## Часть 10. Роли и права доступа

### 10.1. Матрица прав

**Проекты:**

- Создать проект — MANAGER: да, ADMIN: да
- Видеть все проекты — MANAGER: да, ADMIN: да
- Редактировать свой проект — MANAGER: да, ADMIN: да
- Редактировать чужой проект — MANAGER: нет, ADMIN: да

**ALS и модули:**

- Запускать конфигуратор ALS — MANAGER: да, ADMIN: да
- Выбирать ALS из библиотеки — MANAGER: да, ADMIN: да

**Справочники (только ADMIN):**

- Создавать/редактировать Equipment — MANAGER: нет, ADMIN: да
- Создавать новые ALS/модули (вне проектов) — MANAGER: нет, ADMIN: да
- Удалять сироты — MANAGER: нет, ADMIN: да

**Управление сотрудниками:**

- Создавать/редактировать сотрудников — MANAGER: нет, ADMIN: да

### 10.2. Защита владения проектом

На уровне сервиса проверяется: менеджер может редактировать проект, только если project.createdBy.email == currentUserEmail. Администратор может редактировать любые проекты.

## Часть 11. Главный сценарий

### 11.1. Создание новой ALS

1. Получить характеристики из формы (Controller → DTO)
2. Структурная и бизнес-валидация (Validator)
3. Создать доменную ALS с модулями (Domain)
4. Сформировать требования к оборудованию (EquipmentRequirementResolver)
5. Подобрать конкретные модели из каталога (EquipmentService)
6. Разместить оборудование по модулям (ModuleAllocationResolver)
7. Определить физический порядок и вычислить направления дверей (Domain ALS)
8. Перенумеровать ячейки и пересчитать размеры (Domain)
9. Проверить границы (SizeValidator) и совместимость
10. Find-or-create каждого модуля (LBService / LCService / LBCService)
11. Find-or-create ALS (ALSService)
12. Создать ProjectALS с количеством (ProjectService)
13. Очистить draft, вернуть ID (Controller → redirect)

### 11.2. Изменение характеристики существующей ALS

1. Загрузить существующую ALS в draft как копию (не редактировать саму сущность)
2. Менеджер меняет параметр (например, дисплей 15.6" → 21.5")
3. Система формирует новые требования → находит/создаёт новый LC → находит/создаёт новую ALS
4. Связь в проекте обновляется: ProjectALS теперь ссылается на новую ALS. Старая ALS остаётся в БД нетронутой.

---

# 3. DDD-СПЕЦИФИКАЦИЯ

## 3.1. Bounded Context

Единый контекст: ALS Configuration.

## 3.2. Aggregates

### Aggregate: Equipment

**Корень:** Equipment

**Поля:**

- id — идентификатор
- type — тип оборудования (VO)
- manufacturer — производитель
- model — модель
- parameters — технические параметры (Map/JSON)
- width, height, depth — габариты, мм
- powerConsumption — потребление, Вт
- maxLocks — для LOCK_CONTROLLER: ёмкость
- unitCost — цена (можно менять)
- isActive — доступно для выбора (можно менять)
- isDefaultForType — дефолтная модель (можно менять)

**Инварианты:**

- type, manufacturer, model, parameters, габариты — неизменяемы после создания
- manufacturer и model не могут быть пустыми
- габариты должны быть > 0

### Aggregate: Module

**Корень:** Module (абстрактный)

**Наследники:**

- LB (наследует StorageModule)
- LC (наследует ControlModule)
- LBC (наследует StorageModule и реализует ControlModule)

**StorageModule:**

- typeLb — конструктивный тип (VO)
- countCells — количество ячеек
- doorThickness — толщина дверцы, мм
- direction — направление открытия двери (VO)
- cells — список ячеек (без номеров)
- equipment — оборудование в ячейках
- cellHeight, cellWidth, cellDepth — производные (рассчитываются)

**LC:**

- реализует ControlModule
- содержит equipment — оборудование управления
- Инвариант: width >= max(equipment.width)

### Aggregate: ALS

**Корень:** ALS

**Поля:**

- id — идентификатор
- paymentMode, connectionMode — режимы
- colorBody, colorDoor — цвета
- displayConfig — конфигурация дисплея (VO)
- accessMethods — множество методов доступа
- printOption — опция печати
- lightingConfig, powerSupplyConfig, powerOutletConfig — конфигурации (VO)
- modules — упорядоченный список ALSModule
- name, description — производные (генерируются автоматически)
- totalWidth, totalCells — производные (рассчитываются)

**Инварианты:**

- Ровно один ControlModule (LC или LBC)
- Хотя бы один StorageModule (LB или LBC)
- LC и LBC не могут сосуществовать

### Aggregate: Project

**Корень:** Project

**Поля:**

- id — идентификатор
- name, description, company
- createdBy, updatedBy — сотрудники
- createdAt, updatedAt — даты
- alsEntries — список ProjectALS

## 3.3. Value Objects

- EquipmentType: code, name, category
- DisplayConfiguration: enabled, size, touch
- PowerSupplyConfiguration: mainsRequired, upsRequired, backupDuration, requiredPower
- PowerOutletConfiguration: enabled, quantity
- LightingConfiguration: enabled
- TypeLb: type, deltaWidth, shelfThick, serviceZoneWidth
- Colors: Enum (Blue, White, Gray, Black, RAL7035, RAL9003, ...)

## 3.4. Domain Services

- EquipmentRequirementResolver — преобразует характеристики ALS в список EquipmentRequirement
- ModuleAllocationResolver — размещает оборудование по модулям согласно правилам
- FindOrCreateService — универсальный сервис поиска или создания сущности по её идентичности

## 3.5. Repositories

Интерфейсы:

- EquipmentRepository
- ModuleRepository
- ALSRepository
- ProjectRepository

**Ключевые методы:**

- findById
- findByConfiguration — для Find-or-Create
- save

---

# 4. МОДЕЛЬ ДАННЫХ

## 4.1. Таблицы

### equipment_type

- id — BIGINT — PK
- code — VARCHAR(50) — UNIQUE, NOT NULL
- name — VARCHAR(100) — NOT NULL
- category — ENUM — NOT NULL (CONTROL, STORAGE, POWER, NETWORK)

### equipment

- id — BIGINT — PK
- equipment_type_id — BIGINT — FK → equipment_type.id, NOT NULL
- manufacturer — VARCHAR(100) — NOT NULL
- model — VARCHAR(100) — NOT NULL
- parameters — JSONB — NOT NULL, DEFAULT '{}'
- width, height, depth — INTEGER — NOT NULL
- power_consumption — INTEGER
- max_locks — INTEGER — для LOCK_CONTROLLER
- unit_cost — DECIMAL(12,2)
- is_active — BOOLEAN — DEFAULT true
- is_default_for_type — BOOLEAN — DEFAULT false

### module (базовая, наследование JOINED)

- id — BIGINT — PK
- height, width, depth — INTEGER — NOT NULL
- upper_frame, bottom_frame — INTEGER — NOT NULL
- color_body, color_door — ENUM — NOT NULL
- module_type — ENUM — NOT NULL (LB, LC, LBC)

### lb

- id — BIGINT — PK, FK → module.id
- type — VARCHAR(50) — NOT NULL
- delta_width, shelf_thick, service_zone_width — INTEGER — NOT NULL
- door_thickness — INTEGER — NOT NULL
- count_cells — INTEGER — NOT NULL
- cell_width, cell_height, cell_depth — INTEGER — NOT NULL
- direction_door_opening — ENUM — NOT NULL (LEFT, RIGHT)

### lc

- id — BIGINT — PK, FK → module.id

### lbc

- id — BIGINT — PK, FK → module.id
- все поля как в lb

### module_equipment

- id — BIGINT — PK
- module_id — BIGINT — FK → module.id, NOT NULL
- equipment_id — BIGINT — FK → equipment.id, NOT NULL
- quantity — INTEGER — NOT NULL, CHECK > 0
- UNIQUE(module_id, equipment_id)

### als

- id — BIGINT — PK
- height, width, depth — INTEGER — NOT NULL
- upper_frame, bottom_frame — INTEGER — NOT NULL
- color_body, color_door — ENUM — NOT NULL
- payment_mode, connection_mode — ENUM — NOT NULL
- display_enabled — BOOLEAN — NOT NULL
- display_size — DECIMAL(4,1)
- display_touch — BOOLEAN
- print_option — ENUM — NOT NULL
- lighting_enabled — BOOLEAN — NOT NULL
- mains_required, ups_required — BOOLEAN — NOT NULL
- backup_duration, required_power — INTEGER
- outlet_enabled — BOOLEAN — NOT NULL
- outlet_quantity — INTEGER

### als_access_method (таблица для множества методов доступа ALS)

- als_id — BIGINT — FK → als.id, NOT NULL
- access_method — ENUM — NOT NULL (RFID, QR_CODE, BARCODE, PIN)

### als_module

- id — BIGINT — PK
- als_id — BIGINT — FK → als.id, NOT NULL
- module_id — BIGINT — FK → module.id, NOT NULL
- module_order — INTEGER — NOT NULL
- UNIQUE(als_id, module_order)

### project

- id — BIGINT — PK
- name, description, company — VARCHAR — NOT NULL
- created_at, updated_at — DATE — NOT NULL
- created_by_id, updated_by_id — BIGINT — FK → employee. id, NOT NULL

### project_als

- id — BIGINT — PK
- project_id — BIGINT — FK → project.id, NOT NULL
- als_id — BIGINT — FK → als.id, NOT NULL
- quantity — INTEGER — NOT NULL, CHECK > 0
- UNIQUE(project_id, als_id)

---

Документ утверждён и готов к передаче в разработку.