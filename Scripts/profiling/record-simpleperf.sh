#!/usr/bin/env bash
#
# Sampled CPU flame graph (HTML) of the SDK process via simpleperf. Needs the NDK
# scripts and a debuggable/profileable app (the demo's debug build works).
#
# Usage: ./record-simpleperf.sh [package] [duration_seconds]
#
set -euo pipefail

PACKAGE="${1:-io.ton.walletkit.demo}"
DURATION_S="${2:-20}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$SCRIPT_DIR/captures"
mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
PERF_DATA="$OUT_DIR/perf-$STAMP.data"
REPORT_HTML="$OUT_DIR/perf-$STAMP.html"

if ! adb get-state >/dev/null 2>&1; then
  echo "No device connected (adb get-state failed). Plug in a physical device and enable USB debugging." >&2
  exit 1
fi

# Locate the NDK's host-side simpleperf scripts (app_profiler.py, report_html.py).
SIMPLEPERF_DIR="${SIMPLEPERF_DIR:-}"
if [[ -z "$SIMPLEPERF_DIR" ]]; then
  for base in \
    "${ANDROID_NDK_HOME:-}" \
    "${ANDROID_NDK_ROOT:-}" \
    "$HOME/Library/Android/sdk/ndk"/* \
    "${ANDROID_HOME:-$HOME/Library/Android/sdk}/ndk"/* ; do
    if [[ -n "$base" && -f "$base/simpleperf/app_profiler.py" ]]; then
      SIMPLEPERF_DIR="$base/simpleperf"
      break
    fi
  done
fi

if [[ -z "$SIMPLEPERF_DIR" || ! -f "$SIMPLEPERF_DIR/app_profiler.py" ]]; then
  echo "Could not find the NDK simpleperf scripts. Install an NDK via Android Studio," >&2
  echo "or set SIMPLEPERF_DIR=/path/to/ndk/<version>/simpleperf" >&2
  exit 1
fi

echo "simpleperf: $SIMPLEPERF_DIR"
echo "Package:    $PACKAGE"
echo "Duration:   ${DURATION_S}s"
echo
echo "Recording (cpu-clock, 1kHz, call graphs)... exercise the SDK now."

# cpu-clock is a software event, so this works without root; -g captures stacks.
python3 "$SIMPLEPERF_DIR/app_profiler.py" \
  -p "$PACKAGE" \
  -o "$PERF_DATA" \
  -r "-e cpu-clock -f 1000 -g --duration $DURATION_S"

echo
echo "Generating HTML flame graph..."
python3 "$SIMPLEPERF_DIR/report_html.py" -i "$PERF_DATA" -o "$REPORT_HTML"

echo
echo "Done: $REPORT_HTML"
echo "Open it in a browser. Search the flame graph for 'io.ton.walletkit' frames."
