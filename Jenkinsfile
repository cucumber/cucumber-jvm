 pipeline {
     
    agent any

    stages {
        stage('Build') {
            steps {
                //run your build
                sh 'mvn test'
            }
            post {
                always {
                    //generate cucumber reports
                    cucumber '**/*.json'
                }
            }
        }
    }
}
