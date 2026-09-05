pipeline {
    agent any

    stages {

        stage('Build') {
            steps {
                bat 'mvnw.cmd clean package'
            }
        }

        stage('Test') {
            steps {
                bat 'mvnw.cmd test'
            }
        }
    }

    post {
        success {
            echo 'Build and tests completed successfully.'
        }

        failure {
            echo 'Build failed.'
        }
    }
}