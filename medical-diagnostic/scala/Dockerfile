FROM sbtscala/sbt:17-alpine

WORKDIR /app
COPY . /app

# Pre-compilamos el proyecto aquí para ahorrar memoria RAM en el servidor
RUN sbt compile

EXPOSE 8080
CMD ["sbt", "run"]