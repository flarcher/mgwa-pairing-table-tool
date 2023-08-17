#!/bin/bash
#
# Updates the web-app resources for local testing

DEST_DIR="${WEBAPP_TMP_FOLDER:-$HOME/.cache/mgwa/pairing}"
SRC_DIR="$(dirname "$0")/web-app/src/main/webapp"

cp -r "$SRC_DIR"/* "${DEST_DIR}/web-app/"
