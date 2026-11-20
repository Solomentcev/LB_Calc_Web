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

## Запуск стенда

На соответствующем ноутбуке:
```bash
cd environments/dev   # либо environments/test
docker compose up -d --build
```

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

## Jenkins и версионирование

На DEV-ноутбуке работают две независимые Pipeline Job. Отдельной Release Job нет.

| Job | Ветка Jenkinsfile | Jenkinsfile | Назначение |
|---|---|---|---|
| LB-CALC-DEV | refactor-main | infrastructure/Jenkinsfile.dev | Выбор ветки разработки и развёртывание на DEV |
| LB-CALC-TEST | master | infrastructure/Jenkinsfile.test | Автоматическая сборка версии и развёртывание на TEST после merge PR |

### DEV

В Job LB-CALC-DEV перед запуском указывается SOURCE_BRANCH. Разрешены feature/*, refactor/*, bugfix/*; refactor-main временно оставлена как текущая рабочая ветка.

DEV получает последнюю релизную версию с суффиксом -SNAPSHOT. Пока Git-тегов нет, базовая версия берётся из корневого POM. Docker tag уникален для ветки и номера сборки: <APP_VERSION>-<branch>-build-<BUILD_NUMBER>.

### TEST и SemVer

Каждый PR в master обязан иметь ровно одну GitHub-метку:
- version:patch — исправление или обратно совместимое изменение;
- version:minor — новая обратно совместимая функциональность;
- version:major — несовместимое изменение.

GitHub Actions проверяет наличие ровно одной метки до merge. После merge LB-CALC-TEST находит PR по merge commit, берёт его метку и увеличивает последнюю версию:
- PATCH: X.Y.Z → X.Y.(Z+1);
- MINOR: X.Y.Z → X.(Y+1).0;
- MAJOR: X.Y.Z → (X+1).0.0.

Пока Git-тегов нет, исходная версия берётся из revision корневого POM (0.1.0-SNAPSHOT) без суффикса. Поэтому первый merge с version:patch создаст v0.1.1, с version:minor — v0.2.0, с version:major — v1.0.0.

После успешной Maven-сборки Jenkins создаёт и отправляет в GitHub аннотированный тег vX.Y.Z, затем развёртывает оба приложения на TEST. Если pipeline повторно запускается для того же commit, уже существующий тег этого commit используется повторно. GitHub Releases не создаются.

Оба приложения получают одну Maven-версию через -Drevision=.... Docker tag TEST имеет вид <APP_VERSION>-build-<BUILD_NUMBER>. APP_VERSION и IMAGE_TAG передаются Jenkins в Compose как переменные окружения и не хранятся в .env.

### Настройки GitHub

Создать в репозитории метки version:patch, version:minor, version:major. Для master включить правила:
- изменения только через Pull Request;
- обязательные проверки Validate version label и Maven verification;
- запрет прямого push в master.

Сейчас ветка master не защищена; правила необходимо включить в GitHub Settings → Rules → Rulesets (или Branches).

### Настройки Jenkins

Обе Job настраиваются как Pipeline script from SCM с Git URL git@github.com:Solomentcev/LB_Calc_Web.git и credential github-ssh. Для DEV Pipeline script branch остаётся refactor-main, для TEST — master. Путь к Jenkinsfile указан в таблице. DEV запускается вручную с параметром SOURCE_BRANCH. Для TEST включить Poll SCM с расписанием H/2 * * * * — Jenkins проверяет master каждые две минуты и запускает Pipeline при обнаружении нового commit.

Credential github-ssh должен иметь право отправлять Git-теги в репозиторий. Для TEST дополнительно нужны:
- SSH credential test-host-ssh;
- глобальные переменные TEST_HOST и TEST_PROJECT_DIR;
- DEV .env доступен Jenkins по /var/jenkins_env/dev/.env;
- TEST .env хранится на TEST в environments/test/.env;
- host key TEST-ноутбука находится в /var/jenkins_home/.ssh/known_hosts Jenkins-контейнера.

Обе Pipeline собирают приложения на Jenkins/DEV. DEV развёртывается локально. TEST получает JAR по SCP и собирает контейнеры на Docker Engine TEST. Java и Maven на TEST не требуются.

## Сброс данных

`docker compose down -v` удаляет volumes и все данные текущего стенда. Выполнять только при осознанном полном сбросе.

## Документация

Актуальная документация находится в `docs/`.

| Документ | Назначение |
|---|---|
| [Техническое задание](docs/01-Техническое_задание.md) | Назначение системы, бизнес-правила и пользовательские сценарии |
| [Функциональные требования](docs/02-Функциональные_требования.md) | Проверяемые требования и критерии приёмки |
| [Доменная модель](docs/03-Доменная_модель.md) | Предметные сущности, связи и инварианты |
| [Бизнес-логика и состояние реализации](docs/04-Бизнес-логика_и_состояние_реализации.md) | Фактическое поведение и выявленные расхождения |
| [Архитектура и техническое проектирование](docs/05-Архитектура_и_техническое_проектирование.md) | Архитектура, API, безопасность, хранение данных и инфраструктура |
