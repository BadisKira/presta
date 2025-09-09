pipeline {
    agent any

    environment {
        IMAGE_NAME      = 'backend-app-image'
        CONTAINER_NAME  = 'backend-app-container'
    }

    stages {
        stage("Checkout") {
            steps {
                sh '''
                    echo "🔧 Checking installed tools..."
                    docker version
                    docker info
                    docker compose version
                    curl --version
                    jq --version
                '''
            }
        }

        // Compilation du projet Spring Boot
        stage('Build Backend') {
            steps {
                echo 'Building the backend application...'
                sh './mvnw clean package -DskipTests'
            }
        }

        // Tests unitaires et tests d'intégration en parallèle
        stage('Run Tests') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        echo '🧪 Running unit tests...'
                        sh './mvnw test -Dtest=*Test*'
                    }
                }

                stage('Integration Tests') {
                    steps {
                        echo '🧪 Running integration tests...'
                        sh './mvnw verify -Dtest=*IT*'
                    }
                }
            }
        }

        // Construction de l'image Docker
        stage("Build Docker Image") {
            steps {
                echo "🐳 Building Docker image..."
                sh """
                    docker build -t ${IMAGE_NAME}:latest .
                """
            }
        }


        // Lancement du container Docker pour tests API
        stage("Run Container") {
            steps {
                echo "🚀 Starting Docker container..."
                sh "docker run -d -p 9001:8080 --name ${CONTAINER_NAME} ${IMAGE_NAME}:latest"
            }
        }

               // Test des endpoints API
               stage("Test API") {
                   steps {
                       echo "🔍 Testing API endpoints..."
                       script {
                           try {
                               // Attente que l'API réponde avant de tester
                               sh '''
                                   echo "⏳ Waiting for API to be ready..."
                                   for i in {1..15}; do
                                       if curl -s http://localhost:9001/actuator/health | grep -q '"status":"UP"'; then
                                           echo "✅ API is UP"
                                           break
                                       fi
                                       echo "⏳ API not ready yet... retrying in 5s"
                                       sleep 5
                                   done
                               '''

                               // Test des endpoints principaux
                               sh '''
                                   echo "🔎 Testing /api/products..."
                                   curl -f http://localhost:9001/api/products | jq

                                   echo "🔎 Testing /api/otherEndpoint..."
                                   curl -f http://localhost:9001/api/otherEndpoint | jq
                               '''
                           } catch (Exception e) {
                               error("❌ API endpoints are not responding correctly.")
                           }
                       }
                   }
               }



        stage("Push Docker Image") {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'badiskuikops', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker tag ${IMAGE_NAME}:latest ${DOCKER_USER}/${IMAGE_NAME}:latest
                            docker push ${DOCKER_USER}/${IMAGE_NAME}:latest
                            docker logout
                        """
                    }
                }
            }
        }
    }
    // Nettoyage des ressources Docker utilisées par Jenkins
    post {
        always {
            echo "🧹 Cleaning up Docker resources..."
            sh '''
                docker stop ${CONTAINER_NAME} || true
                docker rm ${CONTAINER_NAME} || true
                docker rmi ${IMAGE_NAME}:latest || true
            '''
        }
    }
}
