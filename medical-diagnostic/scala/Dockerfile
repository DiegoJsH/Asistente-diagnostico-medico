FROM eclipse-temurin:17-alpine

# 1. Instalar curl y bash (esenciales para bajar y ejecutar sbt)
RUN apk add --no-cache curl bash

# 2. Descargar e instalar sbt de forma directa e independiente
RUN curl -sL "https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz" | tar -xz -C /usr/local
ENV PATH="/usr/local/sbt/bin:$PATH"

WORKDIR /app
COPY . /app

# 3. Pre-compilar el proyecto
RUN sbt compile

EXPOSE 8080
CMD ["sbt", "run"]