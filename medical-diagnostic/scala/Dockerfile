FROM eclipse-temurin:17-alpine

# 1. Instalar curl y bash
RUN apk add --no-cache curl bash

# 2. Descargar e instalar sbt
RUN curl -sL "https://github.com/sbt/sbt/releases/download/v1.9.9/sbt-1.9.9.tgz" | tar -xz -C /usr/local
ENV PATH="/usr/local/sbt/bin:$PATH"

# 3. Limitar la RAM del proceso base de SBT a solo 180MB
ENV SBT_OPTS="-Xms64M -Xmx180M -Xss2M -XX:+UseSerialGC"

WORKDIR /app
COPY . /app

# 4. Pre-compilar el proyecto
RUN sbt compile

EXPOSE 8080

# 5. Inyectar límites de RAM directamente al proceso hijo que levanta tu servidor HTTP
CMD ["sbt", "-Dsbt.java.options=-Xms64M -Xmx180M -XX:+UseSerialGC", "run"]