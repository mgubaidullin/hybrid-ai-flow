FROM gcr.io/distroless/java:11
COPY target/lib/* /app/lib/
COPY target/*-runner.jar /app/app.jar
WORKDIR /app
CMD ["app.jar"]