# Jenkins CI/CD

## DEV pipeline

Jenkins job `LB-CALC-DEV`:
- Тип: Pipeline from SCM.
- Repository: `git@github.com:Solomentcev/LB_Calc_Web.git`.
- Credentials: `github-ssh`.
- Branch для загрузки Jenkinsfile: `*/refactor-main`.
- Script Path: `infrastructure/Jenkinsfile.dev`.
- Параметр `SOURCE_BRANCH`: ветка `feature/*`, `refactor/*` или `bugfix/*`.

Pipeline собирает выбранную ветку, выполняет `mvn clean verify`, собирает Docker-образы и развёртывает приложения в DEV. Версия приложения — последняя SemVer-версия с суффиксом `-SNAPSHOT`; Docker tag дополнительно содержит нормализованное имя ветки и номер сборки. DEV не создаёт release tags.

## TEST pipeline

Jenkins job `LB-CALC-TEST`:
- Тип: Pipeline from SCM.
- Repository: `git@github.com:Solomentcev/LB_Calc_Web.git`.
- Credentials для SCM: `github-ssh`.
- Branch: `*/master`.
- Script Path: `infrastructure/Jenkinsfile.test`.
- Триггер: проверка SCM каждые две минуты.

TEST pipeline проверяет, что собранный commit является результатом merge ровно одного PR в `master`. У PR должна быть ровно одна из меток `version:patch`, `version:minor` или `version:major`. Следующая SemVer-версия рассчитывается от последнего release tag. Release tag создаётся и отправляется в GitHub только после успешного развёртывания и smoke-проверки.

Jenkinsfile TEST должен присутствовать в ветке `master`, прежде чем job сможет загрузить его из SCM. В текущих рабочих ветках файл поддерживается синхронно; `master` обновляется отдельно.

### Развёртывание на TEST-хост

Jenkins собирает оба JAR-файла и передаёт на TEST-хост только необходимые для Docker build файлы:
- `environments/test/docker-compose.yml`;
- оба Dockerfile из `infrastructure/docker/`;
- собранные JAR-файлы;
- секретный `.env` из Jenkins credential.

Рабочий каталог на TEST-хосте автоматически создаётся как `$HOME/lb-calc-test`. TEST-хосту не нужны GitHub credentials, Git clone или Git fetch. Docker Compose использует корень этого каталога как build context. Именованные Docker volumes базы данных и Kafka сохраняются между развёртываниями.

## Jenkins credentials и настройки

| ID | Тип | Назначение |
|---|---|---|
| `github-ssh` | SSH Username with private key | Checkout/fetch Git и публикация release tags |
| `github-api-token` | Secret text | GitHub REST API для чтения PR и его меток |
| `test-host-ssh` | SSH Username with private key | Передача файлов и команды на TEST-хост |
| `dev-env-file` | Secret file | Переменные окружения DEV |
| `test-env-file` | Secret file | Переменные окружения TEST |

`github-api-token` — отдельный fine-grained Personal Access Token для репозитория `Solomentcev/LB_Calc_Web`. Достаточно разрешения `Pull requests: Read-only`; доступ к репозиторию должен быть ограничен этим репозиторием. SSH-ключ `github-ssh` не используется для REST API.

Глобальные настройки Jenkins:
- `TEST_HOST` — IP-адрес TEST-хоста.
- Maven settings: `/var/jenkins_home/.m2/settings.xml`.

Идентификаторы credentials должны совпадать с указанными в Jenkinsfile. Секреты не хранятся в репозитории.