# LB_CALC_WEB

## Описание

Тренировочное web-приложение для расчёта размеров и визуализации автоматических камер хранения и постаматов.

Приложение позволяет:

* создавать и редактировать проекты;
* хранить данные в MySQL;
* рассчитывать размеры конструкций;
* формировать коммерческие предложения с техническими характеристиками и эскизами;
* регистрировать и авторизовывать сотрудников с разграничением доступа по ролям;
* отправлять пользовательские события и email-уведомления.

## Цели

Практическое освоение:

* Java и Spring Boot;
* Spring Data JPA и MySQL;
* Spring Security и JWT;
* REST API и Thymeleaf;
* Apache Kafka;
* Docker и Docker Compose;
* JUnit и Mockito;
* Apache POI и Java IO;
* Git;
* ООП, SOLID и MVC.

## Архитектура

Проект состоит из:

* `lb-calc-web` — основное web-приложение;
* `notification-service` — сервис обработки событий и отправки email;
* `MySQL` — база данных;
* `Kafka` — обмен событиями между сервисами.

```text
LB Calc Web → Kafka → Notification Service → Email
      ↓
    MySQL
```

## Технологии

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* Spring Security / JWT
* Spring Kafka
* Thymeleaf
* MySQL 8.4
* Docker / Docker Compose
* JUnit / Mockito
* Apache POI
* Maven
* Git

## Запуск

Сборка проекта:

```bash
./mvnw clean package
```

Запуск Docker-окружения:

```bash
cd environments/dev
docker compose --env-file dev.env up -d --build
```

Основное приложение:

`http://localhost:8081`

Notification Service:

`http://localhost:8082`

При запуске приложений из IntelliJ IDEA Kafka доступна по адресу:

`localhost:29092`

