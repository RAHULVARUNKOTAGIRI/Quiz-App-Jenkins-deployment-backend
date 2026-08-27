pipeline {
 
    agent any
 
    stages {
 
        stage('Checkout') {

            steps {

                checkout scm

            }

        }
 
        stage('Environment') {

            steps {

                bat '''

                    echo ==============================

                    echo JAVA

                    echo ==============================

                    java -version
 
                    echo.

                    echo ==============================

                    echo MAVEN

                    echo ==============================

                    mvn -version

                '''

            }

        }
 
        stage('Build') {

            steps {

                bat 'mvn clean package'

            }

        }
 
        stage('Verify JAR') {

            steps {

                bat 'dir target\\*.jar'

            }

        }
 
        stage('Archive JAR') {

            steps {

                archiveArtifacts(

                    artifacts: 'target/*.jar',

                    fingerprint: true

                )

            }

        }

    }
 
    post {
 
        success {

            echo '======================================'

            echo 'BUILD SUCCESSFUL'

            echo '======================================'

        }
 
        failure {

            echo '======================================'

            echo 'BUILD FAILED'

            echo '======================================'

        }

    }

}
 