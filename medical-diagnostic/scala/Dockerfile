FROM eclipse-temurin:17-alpine

# Instalar bash y sbt usando el gestor de paquetes de Alpine
RUN apk add --no-cache bash sbt

WORKDIR /app
COPY . /app

# Pre-compilar el proyecto para dejarlo listo
RUN sbt compile

EXPOSE 8080
CMD ["sbt", "run"]