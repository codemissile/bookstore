# Primera fase: Construcción del JAR con Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia los archivos del proyecto
COPY . .

# Compila el proyecto
RUN mvn clean package -DskipTests

# Segunda fase: Imagen más ligera para ejecutar la aplicación
FROM eclipse-temurin:21-jre AS runtime

# Directorio de trabajo
WORKDIR /app

# Copia el JAR compilado desde la imagen anterior 
COPY --from=builder /app/target/cart-service-1.0-SNAPSHOT.jar cart-service.jar

# Expone el puerto 8081
EXPOSE 8081

# Comando para ejecutar el servicio
CMD ["java", "-jar", "cart-service.jar"]
