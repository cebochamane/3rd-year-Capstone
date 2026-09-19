#!/bin/sh
# Place this script beside pom.xml. Supports macOS and Linux.
set -eu

# Resolve paths from the script location, even when launched elsewhere.
PROJECT_DIR=$(CDPATH= cd -P "$(dirname "$0")" && pwd)
cd "$PROJECT_DIR"

for tool in python3 java mvn ar; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        printf 'Required tool not found: %s\n' "$tool" >&2
        exit 1
    fi
done

if [ ! -f pom.xml ] || [ ! -f native/build_dds.py ]; then
    printf '%s\n' 'Place this script in the BridgeTut project root, beside pom.xml and native/.' >&2
    exit 1
fi

printf '%s\n' 'Building the double-dummy solver…'
python3 native/build_dds.py

# Always use the executable just built for this project.
export BRIDGETUT_DDS_EXECUTABLE="$PROJECT_DIR/native/bin/bridgetut-dds"

printf '%s\n' 'Compiling and starting BridgeTut…'
exec mvn "-Dbridgetut.dds=$BRIDGETUT_DDS_EXECUTABLE" compile exec:java
