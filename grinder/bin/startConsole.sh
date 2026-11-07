#!/bin/bash
# Starts the console: web UI on port 6373, agents on port 6372.
# Pass -classic for the Swing console (add -Dsun.java2d.uiScale=2 to JAVA_OPTS
# on a HiDPI display), or -headless for no UI.
. "$(dirname "$0")/setGrinderEnv.sh"
exec java $JAVA_OPTS -cp "$CLASSPATH" net.grinder.Console "$@"
