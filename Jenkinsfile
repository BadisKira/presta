pipeline {
    agent any
    environment {
        DOCKER_HOST = 'tcp://localhost:2375'
        BACKEND_DIR = 'jenkinsTest'
        IMAGE_NAME  = 'backend-app'
        CONTAINER_NAME = 'backend-app'
    }
    stages {
        stage("verify tooling") {
            steps {
                bat '''
                    docker version
                    docker info
                    docker-compose version
                    curl --version
                    jq --version
                '''
            }
        }

        stage('Build Backend') {
            steps {
                echo 'Building...'
                dir("${env.BACKEND_DIR}") {
                    bat '''
                        dir
                        mvnw.cmd clean package -DskipTests
                    '''
                }
            }
        }

        stage('Unit Tests with Testcontainers') {
            steps {
                echo 'Testing...'
                dir("${env.BACKEND_DIR}") {
                    bat '''
                        mvnw.cmd test
                    '''
                }
            }
        }

        stage("Build Docker Image") {
            steps {
                script {
                    echo "🔨 Building Docker image..."
                    def appImage = docker.build("${IMAGE_NAME}:${env.BUILD_NUMBER}", "${BACKEND_DIR}")
                    bat "docker tag ${IMAGE_NAME}:${env.BUILD_NUMBER} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage("Run Container") {
            steps {
                script {
                    echo "🚀 Starting container..."
                    bat "docker run -d -p 8081:8081 --name ${CONTAINER_NAME} ${IMAGE_NAME}:latest"
                }
            }
        }

        stage("Test API") {
            steps {
                script {
                    echo "🧪 Running API test..."
                    try {
                        bat 'curl -f http://localhost:8081/employees | jq'
                    } catch (Exception e) {
                        error("❌ API endpoint returned a non-200 status code.")
                    }
                }
            }
        }
    }
    post {
        always {
            script {
                echo "🧹 Cleaning up only Jenkins-related Docker resources..."
                bat "docker stop ${CONTAINER_NAME} || exit 0"
                bat "docker rm ${CONTAINER_NAME} || exit 0"
                bat "docker rmi ${IMAGE_NAME}:${env.BUILD_NUMBER} || exit 0"
                bat "docker rmi ${IMAGE_NAME}:latest || exit 0"
            }
        }
    }
}
