#Start with a base image containing Java runtime

#Base image: Starts with an official OpenJDK 21 image from the Eclipse Temurin project (formerly AdoptOpenJDK).
#This gives you a Linux environment with Java 21 pre-installed.
#Think of it as the foundation—your container will have everything this image has.

FROM eclipse-temurin:21-jdk
LABEL authors="nagar"

#
#Copy JAR into container: Takes your compiled JAR from your local target/ directory and copies it
#into the container's filesystem at /Account-0.0.1-SNAPSHOT.jar (root directory).
COPY target/Account-0.0.1-SNAPSHOT.jar Account-0.0.1-SNAPSHOT.jar

#Startup command: Defines what runs when the container starts.
#This is essentially the same command you'd run manually: java -jar Account-0.0.1-SNAPSHOT.jar.
ENTRYPOINT ["java", "-jar", "Account-0.0.1-SNAPSHOT.jar"]



