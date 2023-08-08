#!/bin/sh
# Start script for Unix-based OS
#
# The first argument should be the path to the matrix input file

LOCAL_DIR=$(dirname $0)
DEBUG_PORT="${2:-5005}"
java -jar \
  "$LOCAL_DIR/${project.build.finalName}.jar" \
  -Xdebug \
  "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=0.0.0.0:${DEBUG_PORT}"
