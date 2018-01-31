

    #!/usr/bin/env groovy
     
    pipeline {
        agent any
        
        stages {
            stage('Preparation') {
                steps {
                    deleteDir()
                    git 'ssh://ci@91.121.149.68:29418/netbeans/netesta.git'
                }
            }
     
            stage('Build & Unit Tests') {
                tools { 
                    maven 'Maven3.3.9' 
                }
                steps {
                    sh "mvn clean install"
                }
                post {
                    success {
                        archive "**/lib/target/*.jar"
                    },
                    always {
                        junit '**/lib/target/surefire-reports/TEST-*.xml'
//                        step( [ $class: 'JacocoPublisher', execPattern: 'lib/target/jacoco.exec' ] )
                    }
                }
            }
        }
    }


