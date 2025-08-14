#!/bin/bash
#
# Packages the web application (only) as an archive

OUTPUT_DIR_DEFAULT="$(dirname "$0")"
OUTPUT_ARCHIVE="${1:-${OUTPUT_DIR_DEFAULT}/mgwa_pairing-web-app.zip}"
OUTPUT_ARCHIVE_NAME="$(basename "${OUTPUT_ARCHIVE}")"
cd web-app-src
zip "${OUTPUT_ARCHIVE_NAME}" *
cd - > /dev/null
mv "web-app-src/${OUTPUT_ARCHIVE_NAME}" "$(dirname "${OUTPUT_ARCHIVE}")/"
