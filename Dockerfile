FROM ubuntu:20.04

MAINTAINER Drew Hilton "adhilton@ee.duke.edu"

USER root

ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get -yq dist-upgrade \
  && apt-get install -yq --no-install-recommends \
     curl \
     wget \
     bzip2 \
     sudo \
     locales \
     ca-certificates \
     git \
     unzip \
     openjdk-21-jdk-headless \
     emacs-nox \
     libharfbuzz0b \
     fontconfig \
     fonts-dejavu 

RUN echo "en_US.UTF-8 UTF-8" > /etc/locale.gen && \
    locale-gen

ARG LOCAL_USER_ID=1001
ENV USER_ID ${LOCAL_USER_ID}
RUN adduser --uid ${USER_ID} juser
WORKDIR /home/juser

# Setup a minimal emacs with dcoverage
USER juser
WORKDIR /home/juser
COPY --chown=juser scripts/emacs-bare.sh ./
RUN mkdir -p /home/juser/.emacs.d/dcoverage
COPY --chown=juser scripts/dcoverage.el /home/juser/.emacs.d/dcoverage/
RUN chmod u+x emacs-bare.sh && ./emacs-bare.sh


# we are going to do a bit of gradle first, just to speed
# up future builds
COPY --chown=juser app/build.gradle app/
COPY --chown=juser gradlew settings.gradle  ./
COPY --chown=juser gradle/wrapper gradle/wrapper
COPY --chown=juser gradle/libs.versions.toml gradle/libs.versions.toml

# this will fetch gradle 7.3, and the packages we depend on
WORKDIR /home/juser/app
RUN ../gradlew dependencies --project-dir .

# Now we copy all our source files in.  Note that
# if we change src, etc, but not our gradle setup,
# Docker can resume from this point
WORKDIR /home/juser
COPY --chown=juser ./ ./
RUN chmod +x scripts/*.sh

# compile the code
RUN ./gradlew  assemble
