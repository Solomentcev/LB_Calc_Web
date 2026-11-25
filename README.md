# LB_CALC_WEB

## Описание

Приложение для расчёта размеров и визуализации автоматических камер хранения и постаматов.

Проект состоит из двух Spring Boot-приложений и инфраструктуры:
- `lb-calc-web` — основное web-приложение;
- `notification-service` — обработка Kafka-событий с возможностью HTTP-контроллеров;
- MySQL 8.4 — хранение данных;
- Kafka 4.0 — асинхронная коммуникация.

MySQL и Kafka запускаются только в Docker. Оба приложения можно запускать в Docker Compose или независимо из IntelliJ IDEA.

## Технологии

- Java 17, Spring Boot 3.5.3
- Spring Data JPA / Hibernate, Spring Security / JWT
- Spring Kafka, Thymeleaf
- MySQL 8.4, Apache Kafka 4.0
- Docker / Docker Compose, Flyway, Maven

## Два стенда

Используются два независимых стенда: DEV и TEST. Каждый размещается на отдельном ноутбуке и имеет собственные MySQL, Kafka и Docker volumes. Структура и имена параметров одинаковы, значения отличаются.

```text
environments/
├── dev/
│   ├── .env
│   ├── .env.example
│   └── docker-compose.yml
└── test/
    ├── .env
    ├── .env.example
    └── docker-compose.yml
```

Создать локальные файлы:
```bash
cp environments/dev/.env.example environments/dev/.env
cp environments/test/.env.example environments/test/.env
```

В каждом `.env` указывается LAN IP соответствующего ноутбука в `DOCKER_HOST_ADDRESS` (адрес закрепить через DHCP reservation), а также собственные пароли и JWT-секреты. На разных ноутбуках можно использовать одинаковые номера портов.

## Единая адресация приложений

Оба приложения используют одинаковые адреса MySQL и Kafka независимо от запуска из IntelliJ IDEA или Docker:
- MySQL: `DOCKER_HOST_ADDRESS:MYSQL_PORT`;
- Kafka: `DOCKER_HOST_ADDRESS:KAFKA_EXTERNAL_PORT`.

Имя topic задаётся через `KAFKA_TOPIC_USER_EVENTS`, количество partitions — через `KAFKA_TOPIC_USER_EVENTS_PARTITIONS`. В одноброкерной конфигурации replication factor равен 1.

Compose публикует порты MySQL и Kafka на Docker-хосте. Kafka рекламирует клиентам тот же LAN-адрес. Контейнеры приложений также подключаются к опубликованным портам хоста, а не к Docker DNS-именам `mysql` и `kafka`. Поэтому `application.properties` не меняются при переключении способа запуска.

## Запуск DEV из Docker

Для локальной Docker-сборки сначала собрать JAR-файлы из корня проекта:
```bash
mvn -B clean package -DskipTests
```

Затем запустить инфраструктуру и приложения:
```bash
cd environments/dev
docker compose up -d --build
```

При ручном запуске Compose использует значения по умолчанию `APP_VERSION=0.1.0-SNAPSHOT` и `IMAGE_TAG=local`. Эти параметры не хранятся в `.env`; Jenkins передаёт собственные значения при deployment. Штатное развёртывание TEST выполняет Jenkins на втором ноутбуке и передаёт туда собранные JAR-файлы.

Остановка без удаления данных:
```bash
docker compose down
```

## Независимый запуск приложений из IntelliJ IDEA

MySQL и Kafka остаются в Docker. Чтобы запустить приложение из IDEA, остановить только его контейнер:
```bash
cd environments/dev
docker compose stop lb-calc-web
# или
docker compose stop notification-service
```

Запустить приложение из IDEA, передав переменные из соответствующего `.env` (например, через EnvFile plugin или Environment variables в Run Configuration). Второе приложение и инфраструктура продолжают работать. Для возврата приложения в Docker:
```bash
docker compose up -d lb-calc-web
# или
docker compose up -d notification-service
```

Для подключения контейнеров к LAN IP Docker-хоста опубликованные порты должны быть доступны с Docker bridge-сети и разрешены локальным firewall.

## Конфигурация

- `lb-calc-web/src/main/resources/application.properties` — настройки основного приложения.
- `notification-service/src/main/resources/application.properties` — настройки notification-service.
- `lb-calc-web/src/main/resources/size-bounds.properties` — доменные геометрические ограничения, не настройки стенда.
- Один полный `.env` на стенд содержит параметры обоих приложений и инфраструктуры. Реальные файлы не коммитятся.

Flyway автоматически применяет миграции при запуске основного приложения. Hibernate использует `ddl-auto=validate`.

## CI/CD и версионирование

GitHub Actions выполняет CI-проверки: проверяет метку версии PR в `master`, собирает проект и запускает тесты. Развёртыванием занимаются две Jenkins Pipeline Job:

- `LB-CALC-DEV` — сборка выбранной рабочей ветки (`refactor-main`, `feature/*`, `refactor/*`, `bugfix/*`) и развёртывание DEV;
- `LB-CALC-TEST` — сборка `master` после merge PR, проверка единственной метки `version:patch|minor|major`, расчёт SemVer и развёртывание TEST.

DEV использует версию последнего релиза с суффиксом `-SNAPSHOT`. TEST создаёт release tag только после успешной сборки, развёртывания и smoke-проверки. Подробные инструкции по конфигурации Jenkins, Credentials, SSH, запуску и обслуживанию стендов, а также диагностике находятся в [руководстве по инфраструктуре](infrastructure/README.md).

## Сброс данных

`docker compose down -v` удаляет volumes и все данные текущего стенда. Выполнять только при осознанном полном сбросе.

## Документация

Документация проекта находится в `docs/` и в отдельном [руководстве по инфраструктуре и эксплуатации](infrastructure/README.md).

| Документ | Назначение |
|---|---|
| [Техническое задание](docs/01-Техническое_задание.md) | Назначение системы, бизнес-правила и пользовательские сценарии |
| [Функциональные требования](docs/02-Функциональные_требования.md) | Проверяемые требования и критерии приёмки |
| [Доменная модель](docs/03-Доменная_модель.md) | Предметные сущности, связи и инварианты |
| [Бизнес-логика и состояние реализации](docs/04-Бизнес-логика_и_состояние_реализации.md) | Фактическое поведение и выявленные расхождения |
| [Архитектура и техническое проектирование](docs/05-Архитектура_и_техническое_проектирование.md) | Архитектура, API, безопасность, хранение данных и инфраструктура |
| [Инфраструктура и руководство по эксплуатации](infrastructure/README.md) | DEV/TEST, Docker Compose, Jenkins, Credentials, SSH, CI/CD и диагностика |
