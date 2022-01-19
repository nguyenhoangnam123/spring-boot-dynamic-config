FROM openjdk:8-jdk-alpine as extractor
WORKDIR /workspace/app
COPY target/*.jar .
COPY docker-entrypoint.sh .
RUN mkdir -p dependency && (cd dependency; java -Djarmode=layertools -jar ../*.jar extract --destination .; mv ../docker-entrypoint.sh .)

FROM openjdk:8-jdk-alpine as final
RUN mkdir /opt/app
RUN apk add -y curl vim
WORKDIR /opt/app
ARG DEPENDENCY=/workspace/app/dependency
COPY --from=extractor ${DEPENDENCY}/dependencies/ ./
COPY --from=extractor ${DEPENDENCY}/spring-boot-loader/ ./
COPY --from=extractor ${DEPENDENCY}/application/ ./

COPY --from=extractor ${DEPENDENCY}/docker-entrypoint.sh ./

RUN chmod a+x docker-entrypoint.sh
ENTRYPOINT ["/bin/sh", "-c", "source docker-entrypoint.sh"]
EXPOSE 8081