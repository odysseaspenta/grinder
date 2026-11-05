#!/usr/bin/env bash
# Rewrites the Mockito 1.x API the tests use to its Mockito 5 names.
# Rerunnable: every substitution is a no-op once applied.
#
# usage: tools/mockito5-codemod.sh [root]   (default: the repository root)
set -euo pipefail
root=${1:-$(git -C "$(dirname "$0")" rev-parse --show-toplevel)}

files=$(grep -rlE 'org\.mockito\.(Matchers|runners|internal\.util\.reflection\.Whitebox)|MockitoAnnotations\.initMocks' \
          --include='*.java' "$root" | grep -v '/target/' || true)

for f in $files; do
  # Only files that import Mockito's Matchers class get the qualified-call
  # rewrite; a file that imports org.hamcrest.Matchers must keep its calls.
  if grep -q '^import org\.mockito\.Matchers;' "$f"; then
    sed -i -e 's/\bMatchers\./ArgumentMatchers./g' "$f"
  fi
  sed -i \
    -e 's/import static org\.mockito\.Matchers\./import static org.mockito.ArgumentMatchers./' \
    -e 's/import org\.mockito\.Matchers;/import org.mockito.ArgumentMatchers;/' \
    -e 's/MockitoAnnotations\.initMocks(/MockitoAnnotations.openMocks(/' \
    -e 's/import org\.mockito\.runners\.MockitoJUnitRunner;/import org.mockito.junit.MockitoJUnitRunner;/' \
    -e 's/import org\.mockito\.internal\.util\.reflection\.Whitebox;/import net.grinder.testutility.Reflection;/' \
    -e 's/\bWhitebox\.setInternalState(/Reflection.setField(/g' \
    "$f"
  echo "rewrote $f"
done
echo "files: $(echo "$files" | grep -c . || true)"
