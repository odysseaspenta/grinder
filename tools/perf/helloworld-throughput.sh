#!/usr/bin/env bash
# Runs one worker against a script from the examples directory with the
# console disabled and prints wall-clock seconds and the mean test time.
# The mean comes from the per-run "Test time" column of the worker data log,
# because the worker's main log (which holds the summary table) is empty on
# every release since at least 4.0.0.
#
# usage: helloworld-throughput.sh <distribution root> [script] [runs] [threads]
set -euo pipefail

dist=$(cd "$1" && pwd)
script=${2:-helloworld.py}
runs=${3:-100000}
threads=${4:-4}
java_home=${JAVA_HOME:-/usr}

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
grinder.jvm = $java_home/bin/java
PROPS

start=$(date +%s.%N)
(cd "$work" && "$java_home/bin/java" -cp "$dist/lib/grinder.jar" net.grinder.Grinder perf.properties > agent.out 2>&1)
end=$(date +%s.%N)

data=$(ls "$work"/log/*-0-data.log | head -1)
read -r tests errors mean <<< "$(awk -F', *' 'NR > 1 { n++; sum += $5; err += $6 } END { printf "%d %d %.4f", n, err, (n ? sum / n : 0) }' "$data")"
printf 'script=%s runs=%s threads=%s tests=%s errors=%s wall_seconds=%.2f mean_test_time_ms=%s\n' \
  "$script" "$runs" "$threads" "$tests" "$errors" "$(echo "$end - $start" | bc)" "$mean"
