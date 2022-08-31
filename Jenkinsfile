pipeline {
    agent any
    environment {
        GRADLE_ENTERPRISE_ACCESS_KEY = credentials('gradle-enterprise-key')
    }
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