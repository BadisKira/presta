# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copier les fichiers nécessaires pour télécharger les dépendances en cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler et packager l’application
RUN mvn clean package -DskipTests

# Étape 2 : Image finale avec JRE
FROM azul/zulu-openjdk-alpine:21-jre

WORKDIR /app

# Copier le jar généré depuis le builder
COPY --from=builder /app/target/*.jar app.jar

# Lancer l'application
CMD ["java", "-jar", "app.jar"]
