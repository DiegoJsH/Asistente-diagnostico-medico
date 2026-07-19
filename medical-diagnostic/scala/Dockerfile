FROM eclipse-temurin:17-alpine

# 1. Instalar curl y bash
RUN apk add --no-cache curl bash

# 2. Descargar e instalar sbt
RUN curl -sL "https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz" | tar -xz -C /usr/local
ENV PATH="/usr/local/sbt/bin:$PATH"

# 3. Restringir la memoria máxima de SBT a 256MB y optimizar el recolector de basura
ENV SBT_OPTS="-Xms128M -Xmx256M -Xss2M -XX:+UseSerialGC"

WORKDIR /app
COPY . /app

# 4. Pre-compilar el proyecto
RUN sbt compile

EXPOSE 8080

# 5. Ejecutar desactivando el 'fork' para que NO duplique el proceso en la RAM
CMD ["sbt", "set fork in run := false", "run"]