# fetch basic image
FROM maven:3.8.3-jdk-11

RUN java --version

RUN mvn --version

# application placed into /opt/app
RUN mkdir -p /app
WORKDIR /app

# selectively add the POM file and
# install dependencies
COPY pom.xml /app/
# RUN mvn install

# rest of the project
# COPY ./ /app/
COPY evaluator/ /app/evaluator/
COPY evaluator.activitydefinition/ /app/evaluator.activitydefinition/
COPY evaluator.builder/ /app/evaluator.builder/
COPY evaluator.content-test/ /app/evaluator.content-test/
COPY evaluator.cli/ /app/evaluator.cli/
COPY evaluator.cql2elm/ /app/evaluator.cql2elm/
COPY evaluator.engine/ /app/evaluator.engine/
COPY  evaluator.expression/ /app/evaluator.expression/
COPY  evaluator.dagger/ /app/evaluator.dagger/
COPY  evaluator.fhir/ /app/evaluator.fhir/
COPY  evaluator.library/ /app/evaluator.library/
COPY  evaluator.measure/ /app/evaluator.measure/
COPY  evaluator.measure-hapi/ /app/evaluator.measure-hapi/
COPY  evaluator.spring/ /app/evaluator.spring/
# RUN cd /app && mvn clean && mvn package -P release
# mvn dependency:resolve-plugins

# local application port
# EXPOSE 8080

# execute it
# CMD ["mvn", "exec:java"]
# CMD ["java", "-jar", "target/cqlTranslationServer-1.5.4-jar-with-dependencies.jar", "-d"]
USER root