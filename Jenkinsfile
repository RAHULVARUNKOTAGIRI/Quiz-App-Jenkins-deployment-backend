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

        // Playwright
        // The test reads this, so it points at the context path Jenkins
        // deploys to (QuizApp), not the exported name that has a space in it.
        PLAYWRIGHT_BASE_URL = 'http://localhost:8595/QuizApp/'
        PLAYWRIGHT_TEST = 'QuizFlowUiTest'
        CI = 'true'
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
        stage('Stop Existing Backend') {
    steps {

        echo '=========================================='
        echo 'STOPPING EXISTING BACKEND'
        echo '=========================================='

        bat '''
            @echo off

            echo Checking port 8093...

            for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8093 ^| findstr LISTENING') do (
                echo Stopping process %%a
                taskkill /F /PID %%a
            )

            echo.
            echo Waiting for process to stop...

            ping 127.0.0.1 -n 4 >nul

            echo Backend process stopped.
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

                    REM /subjects is a real endpoint, so a 200 proves the app and
                    REM its MySQL connection are up. Hitting the root would 404
                    REM while curl still exits 0, which passes without checking
                    REM anything - hence piping through findstr.
                    curl -s -o nul -w "%%{http_code}" "http://localhost:8093/subjects" | findstr "200"

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

                    REM The paths in the environment block use forward slashes,
                    REM which "if not exist" accepts but "copy" and "rmdir" do
                    REM not - they are cmd builtins and read / as a switch, so
                    REM they fail with "The system cannot find the file
                    REM specified". The substitutions below rewrite them to
                    REM backslashes. Note the doubled backslash: Groovy eats one
                    REM inside a triple-quoted string, so cmd receives a single.
                    set "WAR_SRC=%APPZ_WAR:/=\\%"
                    set "TOMCAT=%TOMCAT_HOME:/=\\%"

                    echo Checking Appzillon WAR...

                    if not exist "%WAR_SRC%" (
                        echo ERROR: WAR NOT FOUND
                        echo %APPZ_WAR%
                        exit /b 1
                    )

                    echo Quiz App.war found.

                    echo.
                    echo Checking Tomcat...

                    if not exist "%TOMCAT%\\bin\\catalina.bat" (
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

                    rmdir /S /Q "%TOMCAT%\\webapps\\QuizApp" >nul 2>&1
                    del /F /Q "%TOMCAT%\\webapps\\QuizApp.war" >nul 2>&1

                    REM Also clear the older deployment that kept the exported
                    REM name with a space, otherwise Tomcat serves two copies.
                    rmdir /S /Q "%TOMCAT%\\webapps\\Quiz App" >nul 2>&1
                    del /F /Q "%TOMCAT%\\webapps\\Quiz App.war" >nul 2>&1

                    echo.
                    echo Copying Quiz App.war...

                    REM Deployed as QuizApp.war so the context path has no space.
                    copy /Y "%WAR_SRC%" "%TOMCAT%\\webapps\\QuizApp.war"

                    if errorlevel 1 (
                        echo ERROR: WAR COPY FAILED
                        exit /b 1
                    )

                    echo WAR copied successfully.

                    echo.
                    echo Starting Tomcat...

                    set "JAVA_HOME=C:\\Program Files\\Java\\jdk-17.0.2"
                    set "PATH=%JAVA_HOME%\\bin;%PATH%"
                    set "CATALINA_HOME=%TOMCAT%"
                    set "JENKINS_NODE_COOKIE=dontKillMe"

                    call "%TOMCAT%\\bin\\catalina.bat" start

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

                    set "TOMCAT=%TOMCAT_HOME:/=\\%"

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

                        if exist "%TOMCAT%\\logs\\catalina.out" (
                            powershell -Command "Get-Content '%TOMCAT%\\logs\\catalina.out' -Tail 30"
                        ) else (
                            dir "%TOMCAT%\\logs\\"
                        )

                        exit /b 1
                    )

                    echo Waiting 3 seconds...
                    ping 127.0.0.1 -n 4 >nul

                    goto CHECK_APPZILLON
                '''
            }
        }

        stage('Playwright UI Tests') {
            steps {

                echo '=========================================='
                echo 'INSTALLING PLAYWRIGHT CHROMIUM'
                echo '=========================================='

                // Downloads the browser Playwright drives. It is cached under
                // %LOCALAPPDATA%\\ms-playwright, so after the first build this
                // is a no-op that finishes in seconds.
                bat 'mvn -f quizapp\\pom.xml "-DskipTests" exec:java "-Dexec.classpathScope=test" "-Dexec.mainClass=com.microsoft.playwright.CLI" "-Dexec.args=install chromium"'

                echo '=========================================='
                echo 'RUNNING PLAYWRIGHT TESTS'
                echo '=========================================='

                // Runs headless by default, which matters because a Jenkins
                // service has no desktop to open a browser window on.
                // PLAYWRIGHT_BASE_URL from the environment block tells the test
                // which URL to hit.
                bat 'mvn -f quizapp\\pom.xml "-Dtest=%PLAYWRIGHT_TEST%" test'
            }
        }
    }

    post {

        always {

            // Publishes the test results graph. allowEmptyResults keeps a
            // failure in an earlier stage from also failing the report step.
            junit allowEmptyResults: true,
                testResults: 'quizapp/target/surefire-reports/*.xml'

            // The screenshot the test takes of the result screen, plus the jar
            // and backend log - the three things worth having when a build
            // fails on a machine you cannot see.
            archiveArtifacts allowEmptyArchive: true,
                artifacts: 'quizapp/target/*.png, quizapp/target/*.jar, backend.log'
        }

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