# Estágio de Build (SIMPLIFICADO E ROBUSTO)
# Usaremos uma imagem que JÁ tem Maven 3.9.9 e JDK 21 configurados.
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Não precisa mais do ENV JAVA_HOME nem da instalação manual do Maven!

# Agora, o Maven está disponível, continue com o build
COPY pom.xml .
# O comando de dependências agora deve funcionar:
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Empacota a aplicação Spring Boot em um JAR executável
RUN mvn package -DskipTests

# Estágio de Runtime (Mantenha o mesmo, pois já está ótimo)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
