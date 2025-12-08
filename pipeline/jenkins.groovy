#!/usr/bin/env groovy

/*
 * Jenkins declarative pipeline for the devops-k8s repository.
 *
 * How to use in Jenkins:
 *  - Create a new "Pipeline" job.
 *  - Enable "This project is parameterized" and just save (parameters are defined here).
 *  - In the "Pipeline" section choose "Pipeline script from SCM".
 *  - Point SCM to your Git repository and set Script Path to: pipeline/jenkins.groovy
 *  - Run the job via "Build with Parameters" and choose values or use defaults.
 */

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['dev', 'stage', 'prod'],
            description: 'Target environment for this build/deploy'
        )

        choice(
            name: 'ACTION',
            choices: [
                'build',
                'test',
                'build_and_test',
                'build_test_push'
            ],
            description: 'What this pipeline should do'
        )

        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: false,
            description: 'Skip unit tests stage even if ACTION includes tests'
        )

        string(
            name: 'IMAGE_TAG',
            defaultValue: 'latest',
            description: 'Optional Docker image tag override (for informational/demo purposes)'
        )
    }

    environment {
        APP_DIR     = 'tmp-go-demo-app'
        DOCKER_IMAGE = 'denvasyliev/k8sdiy'
    }

    stages {
        stage('Show parameters') {
            steps {
                script {
                    echo "ENVIRONMENT : ${params.ENVIRONMENT}"
                    echo "ACTION      : ${params.ACTION}"
                    echo "SKIP_TESTS  : ${params.SKIP_TESTS}"
                    echo "IMAGE_TAG   : ${params.IMAGE_TAG}"
                }
            }
        }

        stage('Checkout') {
            steps {
                // Repository is already checked out when using "Pipeline script from SCM",
                // but this keeps the script self-contained if you ever run it differently.
                checkout scm
            }
        }

        stage('Build') {
            when {
                anyOf {
                    expression { params.ACTION == 'build' }
                    expression { params.ACTION == 'build_and_test' }
                    expression { params.ACTION == 'build_test_push' }
                }
            }
            steps {
                dir(env.APP_DIR) {
                    sh 'make build'
                }
            }
        }

        stage('Unit tests') {
            when {
                allOf {
                    expression { !params.SKIP_TESTS }
                    anyOf {
                        expression { params.ACTION == 'test' }
                        expression { params.ACTION == 'build_and_test' }
                        expression { params.ACTION == 'build_test_push' }
                    }
                }
            }
            steps {
                dir(env.APP_DIR) {
                    sh 'make test'
                }
            }
        }

        stage('Docker push') {
            when {
                expression { params.ACTION == 'build_test_push' }
            }
            steps {
                dir(env.APP_DIR) {
                    sh 'make push'
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline finished successfully for ENVIRONMENT=${params.ENVIRONMENT}, ACTION=${params.ACTION}"
        }
        failure {
            echo "Pipeline failed. Check the logs above for details."
        }
        always {
            echo "Build completed on branch: ${env.BRANCH_NAME ?: 'N/A'}"
        }
    }
}


