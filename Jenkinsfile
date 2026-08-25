pipeline {

    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Deploy DEV') {
            steps {
                sh '''
                    docker compose \
                      --env-file environments/dev/.env \
                      -f environments/dev/docker-compose.yml \
                      up -d --build
                '''
            }
        }

        stage('Check DEV') {
            steps {
                sh '''
                    docker compose \
                      --env-file environments/dev/.env \
                      -f environments/dev/docker-compose.yml \
                      ps
                '''
            }
        }
    }
}