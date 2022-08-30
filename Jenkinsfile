pipeline {
    agent any
    tools {
        jdk 'temurin-jdk17-latest'
    }
    stages {
        stage('build') {
            steps {
                sh './gradlew clean'
            }
        }
    }
}