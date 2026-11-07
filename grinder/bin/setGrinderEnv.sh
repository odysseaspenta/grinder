#!/bin/bash
# Environment for The Grinder. Source this file from a start script; do not
# run it. GRINDERPATH is the directory this file lives in, so the install can
# move. Site settings belong in setGrinderEnv.local.sh next to this file;
# updateGrinder.sh replaces this file on every release and never touches the
# local one. The start scripts read:
#   JAVA_HOME          JDK to run with (Java 21 or later); else java on the PATH
#   GRINDERPROPERTIES  properties file for the agent; default grinder.properties here
#   JAVA_OPTS          extra JVM options, e.g. -Xmx512m
#   GRINDER_LOG_LEVEL  console/agent log level (debug, info, warn); default warn
#   GRINDER_UI_SCALE   Swing console scale on a HiDPI display, e.g. 2
GRINDERPATH=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

if [ -f "$GRINDERPATH/setGrinderEnv.local.sh" ]; then
  . "$GRINDERPATH/setGrinderEnv.local.sh"
fi

: "${GRINDERPROPERTIES:=$GRINDERPATH/grinder.properties}"

# lib/grinder.jar's manifest names every other jar in lib/, so it is the
# whole classpath.
CLASSPATH=$GRINDERPATH/lib/grinder.jar${CLASSPATH:+:$CLASSPATH}

if [ -n "$JAVA_HOME" ]; then
  PATH=$JAVA_HOME/bin:$PATH
fi

# JVM options every start script passes: JAVA_OPTS plus the log level when set.
GRINDER_JVM_OPTS="$JAVA_OPTS${GRINDER_LOG_LEVEL:+ -Dgrinder.logLevel=$GRINDER_LOG_LEVEL}"

export GRINDERPATH GRINDERPROPERTIES CLASSPATH JAVA_HOME PATH
