FROM openjdk:8-jre-slim as extractor
WORKDIR /workspace/app
COPY target/*.jar .
RUN mkdir -p dependency && (cd dependency; java -Djarmode=layertools -jar ../*.jar extract --destination .)
RUN ls dependency

FROM openjdk:8-jdk-alpine as final
RUN mkdir /opt/app
WORKDIR /opt/app
ARG DEPENDENCY=/workspace/app/dependency
COPY --from=extractor ${DEPENDENCY}/dependencies/ ./
COPY --from=extractor ${DEPENDENCY}/spring-boot-loader/ ./
COPY --from=extractor ${DEPENDENCY}/application/ ./
ENV PROFILE=dev
ENTRYPOINT ["sh", "-c","java org.springframework.boot.loader.JarLauncher --spring.profiles.active=${PROFILE}"]

EXPOSE 8081