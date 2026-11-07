#!/bin/bash
# Starts the TCPProxy as a recording HTTP proxy on port 8001 and writes the
# recorded script to grinder.py in the current directory.
. "$(dirname "$0")/setGrinderEnv.sh"
exec java $GRINDER_JVM_OPTS -cp "$CLASSPATH" net.grinder.TCPProxy -console -http "$@" > grinder.py
