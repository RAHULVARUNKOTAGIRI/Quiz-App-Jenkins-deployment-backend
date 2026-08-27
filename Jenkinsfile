pipeline {

    agent any

    environment {

        // Java
        JAVA_HOME = 'C:/Program Files/Java/jdk-17.0.2'

        // Backend
        BACKEND_PORT = '8093'
        APP_JAR = 'quizapp/target/quizapp-0.0.1-SNAPSHOT.jar'
        BACKEND_URL = 'http://localhost:8093'

        // Appzillon
        APPZ_WAR = 'C:/Users/Kotagiri.varun/Desktop/Quiz App/Quiz App/bin/Web/Quiz App.war'

        // Tomcat
        TOMCAT_HOME = 'C:/Users/Kotagiri.varun/Downloads/apache-tomcat-9.0.53 2/apache-tomcat-9.0.53'
        TOMCAT_PORT = '8595'
        APPZILLON_URL = 'http://localhost:8595/QuizApp/'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '=========================================='
                echo 'CHECKING OUT PROJECT'
                echo '=========================================='

                checkout scm

                echo 'CHECKOUT SUCCESSFUL'
            }
        }

        stage('Environment') {
            steps {
                bat '''
                    echo ==========================================
                    echo JAVA
                    echo ==========================================
                    java -version

                    echo.
                    echo ==========================================
                    echo MAVEN
                    echo ==========================================
                    mvn -version

                    echo.
                    echo ==========================================
                    echo TOMCAT
                    echo ==========================================
                    if exist "%TOMCAT_HOME%\\bin\\catalina.bat" (
                        echo Tomcat found
                    ) else (
                        echo ERROR: Tomcat not found
                        exit /b 1
                    )
                '''
            }
        }

        stage('Build Backend') {
            steps {

                echo '=========================================='
                echo 'BUILDING BACKEND'
                echo '=========================================='

                bat '''
                    mvn -f quizapp\\pom.xml clean package -DskipTests
                '''

                echo '=========================================='
                echo 'CHECKING JAR'
                echo '=========================================='

                bat '''
                    if not exist "quizapp\\target\\quizapp-0.0.1-SNAPSHOT.jar" (
                        echo ERROR: JAR NOT FOUND
                        exit /b 1
                    )

                    dir "quizapp\\target\\*.jar"
                '''
            }
        }

        stage('Deploy Backend') {
            steps {

                echo '=========================================='
                echo 'DEPLOYING BACKEND'
                echo '=========================================='

                bat '''
                    @echo off

                    echo Checking port 8093...

                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8093 ^| findstr LISTENING') do (
                        echo Stopping process %%a on port 8093
                        taskkill /F /PID %%a >nul 2>&1
                    )

                    ping 127.0.0.1 -n 3 >nul

                    echo Starting QuizApp backend...

                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    set "JENKINS_NODE_COOKIE=dontKillMe"

                    start "QuizApp-Backend" /B cmd /c ^
                    "set JENKINS_NODE_COOKIE=dontKillMe && java -jar quizapp\\target\\quizapp-0.0.1-SNAPSHOT.jar > backend.log 2>&1"

                    echo Backend start command executed.

                    echo Waiting for backend...
                    ping 127.0.0.1 -n 8 >nul

                    if exist backend.log (
                        echo.
                        echo BACKEND LOG:
                        powershell -Command "Get-Content backend.log -Tail 20"
                    )
                '''
            }
        }

        stage('Backend Health Check') {
            steps {

                bat '''
                    @echo off

                    echo ==========================================
                    echo BACKEND HEALTH CHECK
                    echo ==========================================

                    set RETRIES=20

                    :CHECK_BACKEND

                    echo Checking backend on port 8093...
                    curl -s -o nul -w "%%{http_code}" "http://localhost:8093" 

                    if not errorlevel 1 (
                        echo.
                        echo BACKEND IS RUNNING
                        exit /b 0
                    )

                    set /a RETRIES-=1

                    if %RETRIES% LEQ 0 (
                        echo.
                        echo BACKEND FAILED TO START

                        if exist backend.log (
                            echo.
                            echo BACKEND LOG:
                            type backend.log
                        )

                        exit /b 1
                    )

                    echo Waiting 3 seconds...
                    ping 127.0.0.1 -n 4 >nul

                    goto CHECK_BACKEND
                '''
            }
        }

        stage('Deploy Appzillon') {
            steps {

                echo '=========================================='
                echo 'DEPLOYING APPZILLON'
                echo '=========================================='

                bat '''
                    @echo off

                    echo Checking Appzillon WAR...

                    if not exist "%APPZ_WAR%" (
                        echo ERROR: WAR NOT FOUND
                        echo %APPZ_WAR%
                        exit /b 1
                    )

                    echo Quiz App.war found.

                    echo.
                    echo Checking Tomcat...

                    if not exist "%TOMCAT_HOME%\\bin\\catalina.bat" (
                        echo ERROR: catalina.bat not found
                        exit /b 1
                    )

                    echo Tomcat found.

                    echo.
                    echo Stopping Tomcat on port 8595...

                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8595 ^| findstr LISTENING') do (
                        echo Killing PID %%a
                        taskkill /F /PID %%a >nul 2>&1
                    )

                    ping 127.0.0.1 -n 4 >nul

                    echo.
                    echo Removing old QuizApp deployment...

                    rmdir /S /Q "%TOMCAT_HOME%\\webapps\\QuizApp" >nul 2>&1
                    del /F /Q "%TOMCAT_HOME%\\webapps\\QuizApp.war" >nul 2>&1

                    echo.
echo Copying Quiz App.war...

copy /Y "C:/Users/Kotagiri.varun/Desktop/Quiz App/Quiz App/bin/Web/Quiz App.war" "C:/Users/Kotagiri.varun/Downloads/apache-tomcat-9.0.53 2/apache-tomcat-9.0.53/webapps/QuizApp.war"

if errorlevel 1 (
    echo ERROR: WAR COPY FAILED
    exit /b 1
)

echo WAR copied successfully.

                    echo.
                    echo Starting Tomcat...

                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    set "CATALINA_HOME=%TOMCAT_HOME%"
                    set "JENKINS_NODE_COOKIE=dontKillMe"

                    "%TOMCAT_HOME%\\bin\\catalina.bat" start

                    echo Tomcat start command executed.

                    echo Waiting for Tomcat...
                    ping 127.0.0.1 -n 16 >nul

                    echo.
                    echo Checking port 8595...

                    netstat -ano | findstr :8595
                '''
            }
        }

        stage('Appzillon Health Check') {
            steps {

                bat '''
                    @echo off

                    echo ==========================================
                    echo APPZILLON HEALTH CHECK
                    echo ==========================================

                    set RETRIES=30

                    :CHECK_APPZILLON

                    echo Checking Appzillon...

                    curl -s -o nul -w "%%{http_code}" "http://localhost:8595/QuizApp/" | findstr "200 302 404"

                    if not errorlevel 1 (
                        echo.
                        echo APPZILLON IS RUNNING
                        echo URL: http://localhost:8595/QuizApp/
                        exit /b 0
                    )

                    set /a RETRIES-=1

                    if %RETRIES% LEQ 0 (
                        echo.
                        echo APPZILLON FAILED TO START

                        echo.
                        echo PORT 8595 STATUS:
                        netstat -ano | findstr :8595

                        echo.
                        echo TOMCAT LOGS:

                        if exist "%TOMCAT_HOME%\\logs\\catalina.out" (
                            powershell -Command "Get-Content '%TOMCAT_HOME%\\logs\\catalina.out' -Tail 30"
                        ) else (
                            dir "%TOMCAT_HOME%\\logs\\"
                        )

                        exit /b 1
                    )

                    echo Waiting 3 seconds...
                    ping 127.0.0.1 -n 4 >nul

                    goto CHECK_APPZILLON
                '''
            }
        }
    }

    post {

        success {
            echo '=========================================='
            echo 'QUIZAPP DEPLOYMENT SUCCESSFUL'
            echo '=========================================='
            echo 'Backend:'
            echo 'http://localhost:8093'
            echo 'Appzillon:'
            echo 'http://localhost:8595/QuizApp/'
            echo '=========================================='
        }

        failure {
            echo '=========================================='
            echo 'QUIZAPP DEPLOYMENT FAILED'
            echo '=========================================='
            echo 'Check the stage that failed.'
            echo '=========================================='
        }
    }
}