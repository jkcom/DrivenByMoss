#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BITWIG_EXTENSION_DIR="${BITWIG_EXTENSION_DIR:-$HOME/Documents/Bitwig Studio/Extensions}"

if [ -z "${JAVA_HOME:-}" ] && [ -x "/usr/libexec/java_home" ]; then
    JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || true)"
    export JAVA_HOME
fi

mkdir -p "$BITWIG_EXTENSION_DIR"

echo "Building DrivenByMoss..."
echo "Extension target: $BITWIG_EXTENSION_DIR"

mvn -f "$SCRIPT_DIR/pom.xml" clean install -Dbitwig.extension.directory="$BITWIG_EXTENSION_DIR"

echo "Installed: $BITWIG_EXTENSION_DIR/DrivenByMoss.bwextension"
