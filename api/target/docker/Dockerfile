FROM azul/zulu-openjdk-alpine:17.0.8-17.44.15-jre
ADD /newrelic-agent.jar //
ADD /newrelic.yml //
ADD /price-service.jar //
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -javaagent:newrelic-agent.jar -Dnewrelic.environment=$SPRING_PROFILES_ACTIVE -Dnewrelic.config.file=newrelic.yml -jar /price-service.jar"]
