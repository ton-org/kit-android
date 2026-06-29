#!/usr/bin/env bash
#
# Capture a Perfetto system trace on a connected device (analyze with analyze-trace.sh
# or ui.perfetto.dev). The atrace_apps line surfaces the SDK's WalletKit.* trace slices.
#
# Usage: ./capture-perfetto.sh [package] [duration_seconds]
#
set -euo pipefail

PACKAGE="${1:-io.ton.walletkit.demo}"
DURATION_S="${2:-20}"
DURATION_MS=$((DURATION_S * 1000))

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$SCRIPT_DIR/captures"
mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
LOCAL_OUT="$OUT_DIR/walletkit-$STAMP.pftrace"
REMOTE_OUT="/data/misc/perfetto-traces/walletkit-$STAMP.pftrace"

if ! adb get-state >/dev/null 2>&1; then
  echo "No device connected (adb get-state failed). Plug in a physical device and enable USB debugging." >&2
  exit 1
fi

echo "Device:   $(adb shell getprop ro.product.model | tr -d '\r') (API $(adb shell getprop ro.build.version.sdk | tr -d '\r'))"
echo "Package:  $PACKAGE"
echo "Duration: ${DURATION_S}s"
echo

# Android 9 needs traced explicitly enabled; harmless on newer versions.
adb shell setprop persist.traced.enable 1 >/dev/null 2>&1 || true

echo "Recording... exercise the SDK now (connect / sign / send a transaction)."

adb shell perfetto --txt -c - -o "$REMOTE_OUT" <<EOF
buffers {
  size_kb: 131072
  fill_policy: RING_BUFFER
}
data_sources {
  config {
    name: "linux.ftrace"
    ftrace_config {
      ftrace_events: "sched/sched_switch"
      ftrace_events: "sched/sched_waking"
      ftrace_events: "sched/sched_wakeup_new"
      ftrace_events: "power/cpu_frequency"
      ftrace_events: "power/cpu_idle"
      ftrace_events: "power/suspend_resume"
      ftrace_events: "task/task_newtask"
      ftrace_events: "task/task_rename"
      atrace_categories: "am"
      atrace_categories: "wm"
      atrace_categories: "view"
      atrace_categories: "binder_driver"
      atrace_categories: "binder_lock"
      atrace_categories: "gfx"
      atrace_categories: "dalvik"
      atrace_categories: "sched"
      atrace_categories: "freq"
      atrace_categories: "idle"
      atrace_categories: "webview"
      atrace_apps: "$PACKAGE"
    }
  }
}
data_sources {
  config {
    name: "linux.process_stats"
    process_stats_config {
      scan_all_processes_on_start: true
      proc_stats_poll_ms: 1000
    }
  }
}
data_sources {
  config {
    name: "android.surfaceflinger.frametimeline"
  }
}
duration_ms: $DURATION_MS
EOF

echo
echo "Pulling trace..."
adb pull "$REMOTE_OUT" "$LOCAL_OUT"
adb shell rm -f "$REMOTE_OUT" >/dev/null 2>&1 || true

echo
echo "Done: $LOCAL_OUT"
echo "Open https://ui.perfetto.dev and drag the file in (or use the SQL query box)."
echo "Filter the timeline for 'WalletKit.' to isolate the SDK's own slices."
