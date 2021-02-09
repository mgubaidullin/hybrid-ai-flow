FROM gcr.io/distroless/java:11
COPY target/lib/* /app/lib/
COPY target/*-runner.jar /app/app.jar
COPY models /app/models
COPY dataset /app/dataset
WORKDIR /app
CMD ["app.jar"]