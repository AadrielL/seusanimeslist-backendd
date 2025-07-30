# Estágio de Build
FROM openjdk:17-jdk-slim as builder
WORKDIR /app

# Copia o arquivo pom.xml e faz o download das dependências do Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Empacota a aplicação Spring Boot em um JAR executável
RUN mvn package -DskipTests

# Estágio de Runtime
FROM openjdk:21-jre
WORKDIR /app

# Exponha a porta que sua aplicação Spring Boot escuta (8081, conforme seu application.properties)
EXPOSE 8081

# Copia o JAR do estágio de build para o estágio de runtime
COPY --from=builder /app/target/seusanimes-backend-0.0.1-SNAPSHOT.jar ./app.jar

# Define o comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]