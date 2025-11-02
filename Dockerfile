# ----------------------------------------------------------------------
# ESTÁGIO 1: BUILD (Compila e Gera o JAR)
# Usa uma imagem que JÁ TEM o Maven 3.9.9 e o JDK 21 configurados.
# Isso garante que JAVA_HOME e mvn estejam no PATH.
# ----------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia o POM e baixa as dependências (para cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte e empacota a aplicação
COPY src ./src
RUN mvn package -DskipTests

# ----------------------------------------------------------------------
# ESTÁGIO 2: PACKAGE (Runtime - Leve e Seguro)
# Usa apenas o JRE para rodar a aplicação (imagem menor).
# ----------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Exponha a porta que sua aplicação Spring Boot escuta
EXPOSE 8081

# Copia o JAR do estágio de build
COPY --from=builder /app/target/seusanimes-backend-0.0.1-SNAPSHOT.jar ./app.jar

# Define o comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]