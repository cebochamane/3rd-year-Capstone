#!/bin/sh
# Run the compiled demo snapshot on macOS or Linux.
set -eu
DEMO_DIR=$(CDPATH= cd -P "$(dirname "$0")" && pwd)
cd "$DEMO_DIR"
for tool in java python3 ar; do
    if ! command -v "$tool" >/dev/null 2>&1; then
        printf 'Required tool not found: %s\n' "$tool" >&2
        exit 1
    fi
done
if [ ! -f BridgeTut.jar ] || [ ! -d tutorials ] || [ ! -f native/build_dds.py ]; then
    printf '%s\n' 'Keep BridgeTut.jar, tutorials/ and native/ beside this launcher.' >&2
    exit 1
fi
printf '%s\n' 'Building DDS for this computer...'
python3 native/build_dds.py
export BRIDGETUT_DDS_EXECUTABLE="$DEMO_DIR/native/bin/bridgetut-dds"
exec java "-Dbridgetut.dds=$BRIDGETUT_DDS_EXECUTABLE" -jar "$DEMO_DIR/BridgeTut.jar" "$@"
