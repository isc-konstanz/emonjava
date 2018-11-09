#!/bin/bash
#Description: Setup script to install emonjava
SERVER="https://github.com/isc-konstanz"
PROJECT="emonjava"
ID="emoncms"

VERSION="1.1.5"

# Set the targeted location of the OpenMUC framework if not already set.
if [ -z ${ROOT_DIR+x} ]; then
  ROOT_DIR="/opt/emonmuc"
fi
TMP_DIR="/var/tmp/emonmuc"

# Verify, if the specific version does exists already
if [ ! -f "$ROOT_DIR/bundles/openmuc-datalogger-$ID-$VERSION.jar" ]; then
  # Create temporary directory and remove old versions
  mkdir -p "$TMP_DIR"
  rm -f "$ROOT_DIR/bundles/openmuc-datalogger-$ID*"
  rm -rf "$ROOT_DIR/lib/device/$ID*"

  if [ ! -f  "$TMP_DIR/$PROJECT-$VERSION.tar.gz" ]; then
  	rm -rf "$TMP_DIR/$PROJECT"*
    wget --quiet \
         --show-progress \
         --directory-prefix="$TMP_DIR" \
         "$SERVER/$PROJECT/releases/download/v$VERSION/$PROJECT-$VERSION.tar.gz"

    tar -xzf "$TMP_DIR/$PROJECT-$VERSION.tar.gz" -C "$TMP_DIR/"
  fi
  mv -f "$TMP_DIR/$PROJECT/lib/openmuc-datalogger-$ID-$VERSION.jar" "$ROOT_DIR/bundles/"
  mv -f "$TMP_DIR/$PROJECT/conf/emoncms.conf" "$ROOT_DIR/conf/emoncms.conf"

  rm -rf "$TMP_DIR/$PROJECT"*
fi
exit 0
