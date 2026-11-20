# LB_CALC_WEB

## Описание

Тренировочное web-приложение для расчёта размеров и визуализации автоматических камер хранения и постаматов.

Приложение позволяет:

- создавать и редактировать проекты;
- хранить данные в MySQL;
- рассчитывать размеры конструкций;
- формировать коммерческие предложения с техническими характеристиками и эскизами;
- регистрировать и авторизовывать сотрудников с разграничением доступа по ролям;
- отправлять пользовательские события и email-уведомления.

## Архитектура

Проект состоит из двух Java-приложений и инфраструктурных компонентов:

- `lb-calc-web` — основное web-приложение;
- `notification-service` — обработка событий и отправка email;
- MySQL — хранение данных;
- Apache Kafka — обмен событиями между приложениями.

```text
LB Calc Web → Kafka → Notification Service → Email
      ↓
    MySQL
```

## Технологии

- Java 17
- Spring Boot 3
- Spring MVC, Spring Security, JWT
- Spring Data JPA / Hibernate
- Spring Kafka
- Thymeleaf
- MySQL 8.4
- Docker / Docker Compose
- JUnit 5 / Mockito
- Apache POI
- Maven

## Локальная конфигурация DEV

1. Скопируйте `environments/dev/.env.example` в `environments/dev/.env`.
2. Укажите актуальный LAN-адрес Docker-хоста в `DOCKER_HOST_ADDRESS` и замените тестовые пароли и JWT-секреты.
3. Для приложений внутри Docker Compose используются внутренние адреса:
   - MySQL: `mysql:3306`;
   - Kafka: `kafka:9092`.
4. Запустите окружение из корня репозитория:

```bash
docker compose --env-file environments/dev/.env -f environments/dev/docker-compose.yml up -d --build
```

Внешние порты по умолчанию:

- web-приложение: `http://localhost:8081`;
- notification-service: `http://localhost:8082`;
- MySQL: `localhost:3307`;
- Kafka: `localhost:29092`.

Для запуска Java-приложений из IntelliJ IDEA используйте внешние адреса из DEV-файла окружения:

- MySQL: значение `DB_URL`;
- Kafka: значение `KAFKA_BOOTSTRAP_SERVERS`.

MySQL создаёт пользователя приложения только при первоначальной инициализации пустого volume. Для уже существующего `dev_mysql_data` изменение `DB_USERNAME` и `DB_PASSWORD` в env-файле не создаёт и не меняет пользователя автоматически. Существующий volume не удаляйте.

## Версионирование и Jenkins

Для DEV и TEST используются отдельные Jenkins pipeline.

### DEV

Pipeline: `infrastructure/Jenkinsfile`.

- В Jenkins передаётся имя ветки `feature/*`, `refactor/*` или `bugfix/*`.
- Maven-версия формируется от последнего релизного Git-тега с суффиксом `-SNAPSHOT`.
- Запускается `mvn clean verify`.
- Docker tag включает SNAPSHOT-версию, нормализованное имя ветки и номер сборки Jenkins.
- Развёртывание выполняется только в DEV.

### TEST

Pipeline: `infrastructure/Jenkinsfile.test`.

- Job должна собирать ветку `master`.
- Обрабатывается commit, полученный в результате merge PR в `master`.
- PR должен иметь ровно одну метку: `version:patch`, `version:minor` или `version:major`.
- Следующая SemVer-версия вычисляется автоматически относительно последнего тега `vX.Y.Z`.
- После успешных Maven-проверок и развёртывания TEST создаётся Git-тег `vX.Y.Z`.
- Docker tag TEST включает релизную версию и номер сборки Jenkins.

### Настройка Jenkins

Для обоих pipeline используется credential `github-ssh`.

DEV:

- Script Path: `infrastructure/Jenkinsfile`;
- параметр сборки: `SOURCE_BRANCH`;
- файл окружения агента: `/var/jenkins_env/dev.env`.

TEST:

- Script Path: `infrastructure/Jenkinsfile.test`;
- SCM branch: `master`;
- credentials: `test-host-ssh`, `test-env-file`, `github-ssh`;
- переменные Jenkins: `TEST_HOST`, `TEST_PROJECT_DIR`.

Для TEST credential `test-env-file` должен содержать конфигурацию второго ноутбука. Секреты не хранятся в репозитории.

