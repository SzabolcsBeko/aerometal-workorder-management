stage('Build') {
    steps {
         dir('backend') {
            bat 'mvnw.cmd clean package'
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
