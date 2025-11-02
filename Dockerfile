# Estágio de Build
FROM openjdk:21-jdk-slim-bullseye as builder
WORKDIR /app

# -------------------------------------------------------------
# 🌟 CORREÇÃO FINAL PARA JAVA_HOME EM SLIM IMAGES
# Este é o caminho mais provável para o JDK na imagem base.
ENV JAVA_HOME /usr/lib/jvm/java-21-openjdk-amd64
# Garante que o diretório do Maven está no PATH para ser encontrado
ENV PATH $PATH:/opt/maven/apache-maven-3.9.9/bin
# -------------------------------------------------------------

# Instala o Maven manualmente
# Assegure-se que os pacotes necessários para download e descompactação estejam disponíveis
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip -P /tmp && \
    unzip -d /opt/maven /tmp/apache-maven-3.9.9-bin.zip && \
    rm /tmp/apache-maven-3.9.9-bin.zip && \
    # Remove a linha "ln -s..." que foi substituída por "ENV PATH"
    # ln -s /opt/maven/apache-maven-3.9.9/bin/mvn /usr/local/bin/mvn
    rm -rf /var/lib/apt/lists/*

# Agora, o Maven está disponível, continue com o build
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o restante do código
COPY src ./src

# Empacota a aplicação Spring Boot em um JAR executável
RUN mvn package -DskipTests

# Estágio de Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Exponha a porta que sua aplicação Spring Boot escuta (8081, conforme seu application.properties)
EXPOSE 8081

# Copia o JAR do estágio de build para o estágio de runtime
COPY --from=builder /app/target/seusanimes-backend-0.0.1-SNAPSHOT.jar ./app.jar

# Define o comando de inicialização da aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]