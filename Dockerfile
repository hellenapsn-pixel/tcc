FROM maven:3.9.16-eclipse-temurin-8 AS build

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package -DskipTests


FROM tomcat:8.5.87-jdk8-temurin-focal

RUN rm -rf /usr/local/tomcat/webapps/ROOT

COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war
COPY gameboxd.db /app/data/gameboxd.db
EXPOSE 8080

CMD ["catalina.sh", "run"]