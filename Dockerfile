FROM eclipse-temurin:21.0.8_9-jdk-noble as deps

WORKDIR /build

COPY --chmod=0755 gradlew gradlew
COPY gradle/ gradle/


# Create a stage for building the application based on the stage with downloaded dependencies.
# This Dockerfile is optimized for Java applications that output an uber jar, which includes
# all the dependencies needed to run your app inside a JVM. If your app doesn't output an uber
# jar and instead relies on an application server like Apache Tomcat, you'll need to update this
# stage with the correct filename of your package and update the base image of the "final" stage
# use the relevant app server, e.g., using tomcat (https://hub.docker.com/_/tomcat/) as a base image.
FROM deps as package

WORKDIR /build

COPY . src/


################################################################################

# Create a new stage for running the application that contains the minimal
# runtime dependencies for the application. This often uses a different base
# image from the install or build stage where the necessary files are copied
# from the install stage.
#
# The example below uses eclipse-turmin's JRE image as the foundation for running the app.
# By specifying the "21-jre-jammy" tag, it will also use whatever happens to be the
# most recent version of that tag when you build your Dockerfile.
# If reproducibility is important, consider using a specific digest SHA, like
# eclipse-temurin@sha256:99cede493dfd88720b610eb8077c8688d3cca50003d76d1d539b0efc8cca72b4.
FROM eclipse-temurin:21.0.8_9-jre-noble AS final

# Create a non-privileged user that the app will run under.
# See https://docs.docker.com/go/dockerfile-user-best-practices/
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

COPY --from=package build/target/app.jar app.jar

EXPOSE 8000

ENTRYPOINT [ "java", "-jar", "app.jar" ]
