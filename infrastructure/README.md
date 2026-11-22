# Инфраструктура и руководство по эксплуатации

Документ описывает фактическую конфигурацию DEV/TEST, Docker Compose и Jenkins для LB_Calc_Web. Он дополняет конфигурационные файлы: при расхождении сначала проверяется реализация в Compose/Jenkinsfile, затем актуализируется документация.

> Секреты, приватные ключи, пароли и содержимое `.env` в репозиторий не добавляются. В документации указаны только расположение файлов и идентификаторы Jenkins Credentials.

## 1. Схема стендов

Используются два физических хоста в локальной сети. Jenkins запущен в Docker на DEV-хосте и управляет DEV-развёртыванием локально, а TEST — по SSH.

| Назначение | Хост | Адрес | ОС / пользователь | Рабочий каталог |
|---|---|---|---|---|
| DEV + Jenkins | `Denis-Asus-Ubuntu` | `192.168.88.210` | Ubuntu; пользователь `denis` | `~/JAVA_PROJECTS/LB_Calc_Web` |
| TEST | `DENIS-ACER-UBUNTU` | `192.168.88.183` | Ubuntu 24.04.5; пользователь `denis` | `~/lb-calc-test` |
| Jenkins | Docker-контейнер `jenkins` на DEV | `http://localhost:8080` на DEV | контейнер `jenkins-jenkins` | `/var/jenkins_home` |

Jenkins доступен с DEV-хоста по адресу `http://localhost:8080`; при доступе из локальной сети — `http://192.168.88.210:8080`. Порт `50000` опубликован для inbound agents.

### Состав окружений

Каждый стенд имеет собственные контейнеры приложений, MySQL и Kafka:

- `lb-calc-web` — основное web-приложение;
- `notification-service` — обработчик событий;
- `mysql` — база данных;
- `kafka` — брокер сообщений.

Внутри Docker Compose приложения используют адреса сервисов `mysql:3306` и `kafka:9092`. Внешние порты задаются переменными окружения `WEB_PORT`, `NOTIFICATION_PORT`, `MYSQL_PORT` и `KAFKA_EXTERNAL_PORT`.

DEV и TEST изолированы друг от друга: отдельные Compose-файлы, имена контейнеров, Docker volumes, базы данных, Kafka и секретные env-файлы. Не переносите volumes между стендами и не удаляйте их при обычном перезапуске.

| Ресурс | DEV | TEST |
|---|---|---|
| Compose | `environments/dev/docker-compose.yml` | `environments/test/docker-compose.yml` |
| Env-шаблон | `environments/dev/.env.example` | `environments/test/.env.example` |
| MySQL container | `lb-calc-dev-mysql` | `lb-calc-test-mysql` |
| Kafka container | `lb-calc-dev-kafka` | `lb-calc-test-kafka` |
| Web container | `lb-calc-dev-web` | `lb-calc-test-web` |
| Notification container | `lb-calc-dev-notification` | `lb-calc-test-notification` |
| MySQL volume | `dev_mysql_data` | `test_mysql_data` |
| Kafka volume | `dev_kafka_data` | `test_kafka_data` |
| База по шаблону | `lb_calc_web_dev` | `lb_calc_web_test` |

Значения портов в `.env.example` являются примерами. Фактические значения стендов находятся в соответствующих секретных env-файлах Jenkins и не должны копироваться в Git.

## 2. Структура инфраструктурных файлов

| Путь | Назначение |
|---|---|
| `infrastructure/jenkins/jenkins/docker-compose.yml` | Запуск Jenkins-контейнера на DEV-хосте |
| `infrastructure/jenkins/jenkins/Dockerfile` | Сборка собственного Jenkins image с инструментами |
| `infrastructure/jenkins/jenkins/plugins.txt` | Список устанавливаемых Jenkins plugins |
| `infrastructure/Jenkinsfile.dev` | Pipeline сборки и развёртывания DEV |
| `infrastructure/Jenkinsfile.test` | Pipeline сборки, проверки и развёртывания TEST |
| `environments/dev/docker-compose.yml` | Состав DEV-окружения |
| `environments/test/docker-compose.yml` | Состав TEST-окружения |
| `environments/dev/.env.example` | Шаблон переменных DEV без секретов |
| `environments/test/.env.example` | Шаблон переменных TEST без секретов |
| `infrastructure/docker/` | Dockerfile обоих Java-приложений |

## 3. Jenkins: установка и запуск

### 3.1. Состав контейнера

Jenkins собирается из `jenkins/jenkins:lts` по файлу `infrastructure/jenkins/jenkins/Dockerfile`. В образ дополнительно устанавливаются:

- Git, Maven, Python 3;
- Docker CLI и Docker Compose V2;
- Google Chrome и ChromeDriver;
- Allure CLI;
- плагины из `infrastructure/jenkins/jenkins/plugins.txt`.

Compose-конфигурация Jenkins:

- имя контейнера: `jenkins`;
- имя локального образа после сборки: `jenkins-jenkins`;
- порт UI: `8080:8080`;
- порт inbound agents: `50000:50000`;
- постоянный named volume: `jenkins_home:/var/jenkins_home`;
- Docker socket хоста: `/var/run/docker.sock:/var/run/docker.sock`;
- каталог env-файлов хоста: `/home/denis/JAVA_PROJECTS/LB_Calc_Web/environments/.secrets:/var/jenkins_env:ro`.

В Compose задан пользователь контейнера `root`; Docker socket даёт Jenkins широкие полномочия на Docker-хосте. Доступ к Jenkins и его Credentials необходимо ограничивать.

### 3.2. Запуск и обслуживание

Команды выполняются на DEV-хосте из корня репозитория `~/JAVA_PROJECTS/LB_Calc_Web`.

Собрать образ и запустить Jenkins:

```bash
docker compose -f infrastructure/jenkins/jenkins/docker-compose.yml up -d --build
```

Проверить состояние:

```bash
docker compose -f infrastructure/jenkins/jenkins/docker-compose.yml ps
docker logs --tail=100 jenkins
```

Следить за логом запуска:

```bash
docker logs -f jenkins
```

Перезапустить контейнер:

```bash
docker restart jenkins
```

Остановить Jenkins без удаления данных:

```bash
docker compose -f infrastructure/jenkins/jenkins/docker-compose.yml down
```

Jenkins сохраняет конфигурацию, jobs, Credentials и историю сборок в named volume `jenkins_home`. Не использовать `docker compose down -v` при обычном обслуживании: удаление volume уничтожит данные Jenkins.

Открыть shell контейнера:

```bash
docker exec -u jenkins -it jenkins bash
```

Для административных операций с правами root:

```bash
docker exec -u root -it jenkins bash
```

Внутри контейнера домашний каталог Jenkins — `/var/jenkins_home`. Maven settings, используемый pipeline, расположен по адресу `/var/jenkins_home/.m2/settings.xml`.

## 4. Настройка Jenkins

### 4.1. Jobs

Обе jobs имеют тип **Pipeline script from SCM** и используют репозиторий `git@github.com:Solomentcev/LB_Calc_Web.git`.

| Настройка | `LB-CALC-DEV` | `LB-CALC-TEST` |
|---|---|---|
| SCM credentials | `github-ssh` | `github-ssh` |
| Branch для загрузки Jenkinsfile | `*/refactor-main` | `*/master` |
| Script Path | `infrastructure/Jenkinsfile.dev` | `infrastructure/Jenkinsfile.test` |
| Параметры | `SOURCE_BRANCH` | нет |
| Триггер | ручной запуск с выбранной веткой | Poll SCM: `H/2 * * * *` |

Для DEV параметр `SOURCE_BRANCH` принимает `refactor-main` либо существующую ветку `feature/*`, `refactor/*` или `bugfix/*`.

TEST pipeline запускается по изменениям `master` и дополнительно проверяет, что commit получен из merge ровно одного PR в `master`.

### 4.2. Credentials

В Jenkins используются следующие Credentials. Они создаются в **Manage Jenkins → Credentials**; идентификаторы должны совпадать с Jenkinsfile.

| ID | Тип Jenkins Credential | Назначение |
|---|---|---|
| `github-ssh` | SSH Username with private key | Checkout/fetch репозитория и публикация release tags |
| `github-api-token` | Secret text | GitHub REST API: чтение PR и меток |
| `test-host-ssh` | SSH Username with private key | SSH/SCP с DEV/Jenkins на TEST-хост |
| `dev-env-file` | Secret file | Переменные окружения DEV |
| `test-env-file` | Secret file | Переменные окружения TEST |

Для `github-api-token` используется fine-grained PAT с доступом только к репозиторию `Solomentcev/LB_Calc_Web` и разрешением `Pull requests: Read-only`. SSH-ключ `github-ssh` не заменяет API token.

Файлы `dev-env-file` и `test-env-file` формируются на основе соответствующих `.env.example`, но содержат реальные пароли и JWT-секреты. Не добавлять их в Git и не выводить содержимое в Jenkins console log.

В Compose Jenkins также смонтирован каталог хоста `environments/.secrets` в `/var/jenkins_env` только для чтения. Текущие Jenkinsfile получают env-файлы через Credentials типа Secret file; не путать этот mount с источником env-файлов в текущих pipeline.

### 4.3. Глобальные настройки Jenkins

В **Manage Jenkins → System → Global properties → Environment variables** задано:

| Переменная | Значение / назначение |
|---|---|
| `TEST_HOST` | `192.168.88.183` — адрес TEST-хоста |

В Jenkinsfile также задан путь к Maven settings:

```text
/var/jenkins_home/.m2/settings.xml
```

Внешние секреты не задаются обычными глобальными переменными — для них используются Jenkins Credentials.

## 5. SSH-доступ Jenkins к TEST

### 5.1. Подключение

Jenkins использует Credential `test-host-ssh` (SSH Username with private key). В TEST pipeline имя пользователя берётся из этого Credential, адрес — из `TEST_HOST`.

На TEST-хосте публичная часть соответствующего ключа должна быть добавлена в `~/.ssh/authorized_keys` пользователя Credential.

### 5.2. Проверка host key

Host key TEST-сервера проверен по ED25519 fingerprint непосредственно на TEST-хосте:

```text
SHA256:EaNYTXsBdsEwgVmIz5MvdIOR40lSjdyUZ/rTmueFTak
```

На DEV-хосте ключ сервера добавлен в файл Jenkins:

```text
/var/jenkins_home/.ssh/known_hosts
```

Проверка fingerprint на TEST-хосте:

```bash
sudo ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub
```

Получение ключа из Jenkins-контейнера выполняется после сверки fingerprint:

```bash
docker exec -u root -it jenkins bash
mkdir -p /var/jenkins_home/.ssh
chmod 700 /var/jenkins_home/.ssh
ssh-keyscan -t ed25519 192.168.88.183 >> /var/jenkins_home/.ssh/known_hosts
chown -R jenkins:jenkins /var/jenkins_home/.ssh
chmod 600 /var/jenkins_home/.ssh/known_hosts
ssh-keygen -lf /var/jenkins_home/.ssh/known_hosts
```

Не добавлять ключ сервера без предварительной проверки fingerprint по доверенному каналу.

В `infrastructure/Jenkinsfile.test` для SSH и SCP явно заданы:

```text
-o UserKnownHostsFile=/var/jenkins_home/.ssh/known_hosts
-o StrictHostKeyChecking=yes
```

Таким образом, pipeline использует конкретный файл ключей и не отключает проверку подлинности сервера.

## 6. DEV: запуск и развёртывание

### 6.1. Локальный запуск через Docker Compose

На DEV-хосте в корне репозитория подготовить локальный файл `environments/dev/.env` на основе `environments/dev/.env.example`. Файл должен содержать актуальный LAN IP Docker-хоста, параметры MySQL/Kafka и уникальные JWT-секреты.

Проверить Compose-конфигурацию:

```bash
docker compose --env-file environments/dev/.env \
  -f environments/dev/docker-compose.yml config -q
```

Запустить весь DEV:

```bash
docker compose --env-file environments/dev/.env \
  -f environments/dev/docker-compose.yml up -d --build
```

Проверить контейнеры и логи:

```bash
docker compose --env-file environments/dev/.env \
  -f environments/dev/docker-compose.yml ps

docker compose --env-file environments/dev/.env \
  -f environments/dev/docker-compose.yml logs -f
```

Остановить контейнеры, сохранив данные:

```bash
docker compose --env-file environments/dev/.env \
  -f environments/dev/docker-compose.yml down
```

Не добавлять `-v` к `down`, если нет отдельной задачи на удаление данных. MySQL и Kafka хранят данные в `dev_mysql_data` и `dev_kafka_data`.

### 6.2. Автоматический DEV pipeline

Job `LB-CALC-DEV`:

1. Получает выбранную ветку через `SOURCE_BRANCH`.
2. Проверяет допустимый формат имени ветки.
3. Получает последний release tag и формирует Maven-версию с суффиксом `-SNAPSHOT`.
4. Формирует Docker tag из версии, нормализованного имени ветки и номера Jenkins build.
5. Запускает MySQL и Kafka DEV и ожидает их healthcheck.
6. Выполняет `mvn clean verify` с параметрами подключения к DEV.
7. Собирает Docker-образы двух приложений.
8. Пересоздаёт только приложения, не затрагивая MySQL/Kafka и их volumes.
9. Проверяет доступность web login endpoint и состояние notification-service.

DEV pipeline не создаёт release tags.

## 7. TEST: запуск и развёртывание

### 7.1. Подготовка TEST-хоста

TEST-хост: `DENIS-ACER-UBUNTU`, IP `192.168.88.183`, пользователь `denis`.

Jenkins не клонирует репозиторий на TEST-хост. Он передаёт по SSH/SCP только необходимые файлы. Рабочий каталог создаётся автоматически:

```text
/home/denis/lb-calc-test
```

Внутри каталога pipeline размещает:

- `environments/test/.env` — секретный файл из Credential `test-env-file`;
- `environments/test/docker-compose.yml`;
- Dockerfile основного приложения и notification-service;
- собранные JAR-файлы в каталогах соответствующих модулей.

На TEST-хосте должны быть установлены Docker Engine и Docker Compose V2. GitHub credentials на TEST не нужны.

### 7.2. Ручная проверка TEST

Команды выполняются на TEST-хосте:

```bash
cd ~/lb-calc-test

docker compose --env-file environments/test/.env \
  -f environments/test/docker-compose.yml ps

docker compose --env-file environments/test/.env \
  -f environments/test/docker-compose.yml logs -f
```

Остановить TEST, сохранив данные:

```bash
docker compose --env-file environments/test/.env \
  -f environments/test/docker-compose.yml down
```

Не использовать `down -v` при обычном обслуживании: данные хранятся в `test_mysql_data` и `test_kafka_data`.

### 7.3. Автоматический TEST pipeline

Job `LB-CALC-TEST` работает только с `master`.

1. Получает commit из `master` и Git tags.
2. Через GitHub REST API находит merged PR для commit.
3. Проверяет, что найден ровно один merged PR в `master`.
4. Проверяет наличие ровно одной метки: `version:patch`, `version:minor` или `version:major`.
5. Рассчитывает SemVer-версию от последнего release tag.
6. По SSH создаёт каталог TEST, передаёт env-файл и Compose-конфигурацию.
7. Запускает MySQL и Kafka TEST и ожидает healthcheck.
8. Выполняет `mvn clean verify` с параметрами TEST.
9. Передаёт Dockerfile и собранные JAR-файлы на TEST.
10. Пересоздаёт приложения без пересоздания MySQL/Kafka.
11. Выполняет smoke-проверку web endpoint и notification-service.
12. Только после успешной проверки создаёт и отправляет в GitHub tag `vX.Y.Z`.

Docker tag TEST содержит релизную версию и номер Jenkins build. Ошибка на любом этапе до smoke-проверки не должна приводить к публикации нового release tag.

## 8. GitHub Actions и Jenkins: разделение ответственности

Workflow `.github/workflows/java-ci.yml` выполняет CI-проверки на GitHub Actions: проверку PR и Maven build/test. Он не развёртывает приложения.

Jenkins отвечает за развёртывание:
- DEV — выбранная рабочая ветка;
- TEST — только `master` после merge PR и проверки version label.

Прямые push в `master` не используются. Изменения проходят через PR. Для каждого PR в `master`, который должен попасть в TEST, задаётся ровно одна метка версии.

## 9. Диагностика

### Jenkins

```bash
docker ps --filter name=jenkins
docker logs --tail=200 jenkins
docker exec -u jenkins jenkins sh -lc 'echo "$HOME"; ls -la "$HOME/.ssh"; ssh-keygen -F 192.168.88.183 -f "$HOME/.ssh/known_hosts"'
```

### DEV

```bash
docker ps --filter name=lb-calc-dev
docker volume ls | grep -E 'dev_mysql_data|dev_kafka_data'
```

### TEST

```bash
ssh denis@192.168.88.183
docker ps --filter name=lb-calc-test
docker volume ls | grep -E 'test_mysql_data|test_kafka_data'
```

Если pipeline завершается ошибкой, сначала смотреть полный console log Jenkins и определять конкретный stage/команду. Не отключать `StrictHostKeyChecking`, не удалять volumes и не пересоздавать Credentials наугад.

## 10. Состояние и актуализация документа

Документ фиксирует конфигурацию, подтверждённую репозиторием и настройками, известными на **22.11.2026**.

После изменения любого из следующих элементов обновлять этот документ в том же PR:
- IP/hostname стендов и пути размещения;
- Jenkins Compose/Dockerfile/plugins;
- Jenkins jobs, Script Path, SCM branch и triggers;
- Credentials IDs и глобальные переменные Jenkins;
- SSH host key / known_hosts;
- порядок запуска, проверки и развёртывания;
- схема версионирования и release tags.

Фактические значения секретных env-файлов и содержимое Jenkins Credentials в документацию не копировать.
