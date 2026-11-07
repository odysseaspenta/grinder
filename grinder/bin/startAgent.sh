#!/bin/bash
# Starts an agent. It reads $GRINDERPROPERTIES when that file exists, else
# grinder.properties in the current directory. Extra arguments go to the agent.
. "$(dirname "$0")/setGrinderEnv.sh"
if [ -f "$GRINDERPROPERTIES" ]; then
  exec java $GRINDER_JVM_OPTS -cp "$CLASSPATH" net.grinder.Grinder "$GRINDERPROPERTIES" "$@"
else
  exec java $GRINDER_JVM_OPTS -cp "$CLASSPATH" net.grinder.Grinder "$@"
fi
