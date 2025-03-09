#!/bin/bash

#annoying thing about docker:
#hard to tell when service inside it is actually up and running..
#have to just try to connect.
JAR_FILE="app/build/libs/app.jar"

echo "Starting deployment tests..."

if ! ./gradlew build; then
    echo "Gradle build failed!"
    exit 1
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found!"
    exit 1
fi
