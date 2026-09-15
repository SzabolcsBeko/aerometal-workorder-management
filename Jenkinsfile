pipeline {
agent any

stages {

    stage('Build') {
        steps {
            dir('backend') {
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }
    }

    stage('Test') {
        steps {
            dir('backend') {
                bat 'mvnw.cmd test'
            }
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
