#!/bin/bash
# Get the folder where this script is located
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Run the Fat JAR
java -jar "$DIR/target/database_manager-1.0-SNAPSHOT-jar-with-dependencies.jar --cli"