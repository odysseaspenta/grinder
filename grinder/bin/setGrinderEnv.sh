#!/bin/bash
# Environment for The Grinder. Source this file from a start script; do not
# run it. GRINDERPATH is the directory this file lives in, so the install can
# move. Site settings (JAVA_HOME, GRINDERPROPERTIES, JAVA_OPTS) belong in
# setGrinderEnv.local.sh next to this file; updateGrinder.sh replaces this
# file on every release and never touches the local one.
GRINDERPATH=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

if [ -f "$GRINDERPATH/setGrinderEnv.local.sh" ]; then
  . "$GRINDERPATH/setGrinderEnv.local.sh"
fi

: "${GRINDERPROPERTIES:=$GRINDERPATH/grinder.properties}"

# lib/grinder.jar's manifest names every other jar in lib/, so it is the
# whole classpath. The Grinder needs Java 21 or later.
CLASSPATH=$GRINDERPATH/lib/grinder.jar${CLASSPATH:+:$CLASSPATH}

if [ -n "$JAVA_HOME" ]; then
  PATH=$JAVA_HOME/bin:$PATH
fi

export GRINDERPATH GRINDERPROPERTIES CLASSPATH JAVA_HOME PATH
