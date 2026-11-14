#!/bin/bash
# Reads the project version from pom.xml and invokes makensis with it, so installer.nsi needs no post-processing.
#
# Prerequisites (same layout CI is expected to provide):
#   - \assembly\jdk\            - JRE created via jlink (see HOWTO_JRE.md)
#   - NSIS\mplayer4anime.exe    - Windows launcher built by maven 'windows' profile

set -e

# This script lives in misc/windows/; pom.xml is two levels up
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
NSIS_DIR="$(cd "$(dirname "$0")/NSIS" && pwd)"

# Project version is a first <version> in pom.xml; expected format: two blocks of numbers
VERSION=$(grep '<version>' "$ROOT/pom.xml" | head -1 | sed -e 's/^.*<version>\([0-9][0-9.]*\).*/\1/')
MAJOR="${VERSION%%.*}"
MINOR="${VERSION#*.}"

if [ -z "$VERSION" ]; then
    echo "ERROR: could not extract project version from pom.xml" >&2
    exit 1
fi

echo "Version is: MAJOR=$MAJOR MINOR=$MINOR"

if [ ! -f "$NSIS_DIR/mplayer4anime.exe" ]; then
    echo "WARNING: $NSIS_DIR/mplayer4anime.exe not found; build it with: mvn package -Pwindows" >&2
fi

makensis -V4 -DVERSIONMAJOR="$MAJOR" -DVERSIONMINOR="$MINOR" "$NSIS_DIR/installer.nsi"
