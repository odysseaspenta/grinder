#!/bin/bash
# Updates an unpacked Grinder installation to the release in a binary zip.
#
#   updateGrinder.sh grinder-<version>-binary.zip /path/to/installation
#
# Replaces lib/ (the previous one is kept as lib.previous for a rollback),
# the shipped start scripts and this script, and CHANGES, AUTHORS and README.
# Everything else in the installation (grinder.properties, test scripts,
# setGrinderEnv.local.sh, log/, etc/, examples/, contrib/, your own
# directories) is left exactly as it was.
set -euo pipefail

usage() {
  echo "usage: $0 grinder-<version>-binary.zip <installation directory>" >&2
  exit 2
}

fail() {
  echo "updateGrinder.sh: $*" >&2
  exit 1
}

grinder_version() {
  local core
  core=$(ls "$1"/lib/grinder-core-*.jar 2>/dev/null | grep -v -- '-tests.jar' | head -n 1)
  [ -n "$core" ] || { echo "unknown"; return; }
  core=${core##*/grinder-core-}
  echo "${core%.jar}"
}

tmp=

main() {
  [ $# -eq 2 ] || usage
  local zip=$1 install=$2
  [ -f "$zip" ] || fail "$zip is not a file"
  [ -d "$install" ] || fail "$install is not a directory"
  [ -f "$install/lib/grinder.jar" ] \
    || fail "$install/lib/grinder.jar not found; $install is not a Grinder installation"
  command -v unzip >/dev/null || fail "unzip is not installed"

  install=$(cd "$install" && pwd)
  tmp=$(mktemp -d)
  trap 'rm -rf "$tmp"' EXIT

  unzip -q "$zip" -d "$tmp"
  local release
  release=$(find "$tmp" -mindepth 1 -maxdepth 1 -type d)
  [ "$(printf '%s\n' "$release" | wc -l)" -eq 1 ] && [ -f "$release/lib/grinder.jar" ] \
    || fail "$zip does not hold one grinder-<version> directory with lib/grinder.jar"

  local old new
  old=$(grinder_version "$install")
  new=$(grinder_version "$release")
  echo "Updating $install from $old to $new"

  rm -rf "${install:?}/lib.previous"
  mv "$install/lib" "$install/lib.previous"
  cp -R "$release/lib" "$install/lib"
  echo "  lib/ replaced; previous jars kept in lib.previous/"

  local f
  for f in AUTHORS CHANGES README; do
    [ -f "$release/$f" ] && cp -p "$release/$f" "$install/$f.new" && mv "$install/$f.new" "$install/$f"
  done

  # The scripts are copied into place with a rename so that this script can
  # replace itself while it is running.
  for f in "$release"/*.sh; do
    [ -f "$f" ] || continue
    local name
    name=$(basename "$f")
    cp -p "$f" "$install/$name.new"
    chmod 755 "$install/$name.new"
    mv "$install/$name.new" "$install/$name"
  done
  echo "  scripts and release notes replaced"
  echo "Done. Remove $install/lib.previous once the new release works."
}

main "$@"
exit $?
