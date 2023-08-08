#!/bin/sh
# Start script for Unix-based OS
#
# The first argument should be the path to the matrix input file

LOCAL_DIR=$(dirname $0)
java -jar "$LOCAL_DIR/${project.build.finalName}.jar" $1
