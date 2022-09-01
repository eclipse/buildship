pipeline {
    agent any
    tools {
        jdk 'adoptopenjdk-hotspot-jdk11-latest'
    }
    stages {
        stage('build') {
            steps {
                configFileProvider([configFile(fileId: '16a3af82-68ea-4c6f-8985-1d20d86e3e06', variable: 'GE_ACCESS_KEY')]) {
                    sh """
                        cat ${env.GE_ACCESS_KEY} >> settings.gradle
                        ./gradlew clean build -Pbuild.invoker=ci -Peclipse.test.java.version=11
                    """
                }
            }
        }
    }
}