#!/bin/bash
# Starts the console: web UI on port 6373, agents on port 6372.
# Pass -classic for the Swing console (set GRINDER_UI_SCALE=2 on a HiDPI
# display), or -headless for no UI.
. "$(dirname "$0")/setGrinderEnv.sh"
exec java $GRINDER_JVM_OPTS ${GRINDER_UI_SCALE:+-Dsun.java2d.uiScale=$GRINDER_UI_SCALE} \
  -cp "$CLASSPATH" net.grinder.Console "$@"
