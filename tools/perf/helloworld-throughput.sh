#!/usr/bin/env bash
# Runs one worker against a script from the examples directory with the
# console disabled and prints wall-clock seconds and the mean test time.
#
# usage: helloworld-throughput.sh <distribution root> [script] [runs] [threads]
set -euo pipefail

dist=$(cd "$1" && pwd)
script=${2:-helloworld.py}
runs=${3:-100000}
threads=${4:-4}

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
cp -r "$dist/examples/." "$work/"
cat > "$work/perf.properties" <<PROPS
grinder.script = $script
grinder.processes = 1
grinder.threads = $threads
grinder.runs = $runs
grinder.useConsole = false
grinder.logDirectory = log
PROPS

start=$(date +%s.%N)
(cd "$work" && "${JAVA_HOME:-/usr}/bin/java" -cp "$dist/lib/grinder.jar" net.grinder.Grinder perf.properties > agent.out 2>&1)
end=$(date +%s.%N)

log=$(ls "$work"/log/*-0.log | head -1)
totals=$(grep -E '^Totals' "$log" | tail -1)
mean=$(echo "$totals" | awk '{print $4}')
printf 'script=%s runs=%s threads=%s wall_seconds=%.2f mean_test_time_ms=%s\n' \
  "$script" "$runs" "$threads" "$(echo "$end - $start" | bc)" "$mean"
