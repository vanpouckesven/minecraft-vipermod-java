# Generiek build-image voor Minecraft-mods (Gradle-wrapper per mod).
# Bevat JDK 17 en 21: Gradle-toolchains kiezen zelf de juiste versie
# (bv. Forge 1.20.1 compileert met 17, ModDevGradle-tooling vraagt 21).
FROM eclipse-temurin:17-jdk AS jdk17

FROM eclipse-temurin:21-jdk

ARG UID=1000
ARG GID=1000

COPY --from=jdk17 /opt/java/openjdk /opt/java/jdk17

RUN apt-get update \
    && apt-get install -y --no-install-recommends git \
    && rm -rf /var/lib/apt/lists/*

# Bestaande user/group met dezelfde UID/GID (bv. 'ubuntu') vervangen door 'builder',
# zodat build-output in WSL van jou is en niet van root.
RUN existing_user="$(getent passwd "${UID}" | cut -d: -f1)"; \
    if [ -n "$existing_user" ]; then userdel -r "$existing_user"; fi; \
    existing_group="$(getent group "${GID}" | cut -d: -f1)"; \
    if [ -n "$existing_group" ]; then groupdel "$existing_group"; fi; \
    groupadd -g "${GID}" builder \
    && useradd -m -u "${UID}" -g "${GID}" -s /bin/bash builder \
    && mkdir -p /home/builder/.gradle /workspace \
    && chown -R builder:builder /home/builder /workspace

USER builder
WORKDIR /workspace

# Daemon heeft geen zin in een kortlevende container; toolchains expliciet aanwijzen.
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.java.installations.paths=/opt/java/jdk17,/opt/java/openjdk"
