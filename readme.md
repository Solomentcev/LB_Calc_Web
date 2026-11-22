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

## Версионирование и CI/CD

Для CI используется GitHub Actions, для развёртывания DEV и TEST — Jenkins.

- GitHub Actions выполняет проверки PR и сборку/тестирование, но не развёртывает приложения.
- Jenkins DEV собирает выбранную рабочую ветку и развёртывает DEV.
- Jenkins TEST собирает `master` после merge PR, проверяет version label, развёртывает TEST и после smoke-проверки публикует release tag `vX.Y.Z`.
- Для PR в `master` используется ровно одна метка: `version:patch`, `version:minor` или `version:major`.

Подробная конфигурация серверов, Docker Compose, Jenkins, Credentials, SSH и порядок запуска стендов описаны в [руководстве по инфраструктуре](infrastructure/README.md).

## GitHub Actions

Workflow: `.github/workflows/java-ci.yml`.

- Запускается для pull request в `master` и при push в `master`.
- Для PR проверяет наличие ровно одной метки версии.
- Выполняет `mvn clean verify` на Java 17.
- Не развёртывает приложения и не управляет TEST/DEV стендами.

