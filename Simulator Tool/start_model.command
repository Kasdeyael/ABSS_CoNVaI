#!/bin/bash

# Repast Simphony Model run script for macOS and Linux

PWD="${0%/*}"
cd "$PWD"

# Note the Repast Simphony Directories.
REPAST_SIMPHONY_ROOT=$PWD/repast.simphony/repast.simphony.runtime_2.9.1

# Define the Core Repast Simphony Directories and JARs
CP=$CP:$REPAST_SIMPHONY_ROOT/bin
CP=$CP:$REPAST_SIMPHONY_ROOT/lib/*

# Groovy jar
CP=$CP:$PWD/groovylib/groovy-3.0.9-indy.jar

# User model lib jars
CP=$CP:lib/*

# Change to the project directory
cd "CoNVaI_ABSS"

# Start the Model
java -XX:+IgnoreUnrecognizedVMOptions --add-modules=ALL-SYSTEM --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED -cp "$CP" repast.simphony.runtime.RepastMain  "./CoNVaI_ABSS.rs"
