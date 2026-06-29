#!/usr/bin/env bash
#
# Analyze a captured .pftrace offline and print an emulator-robust SDK report.
# Uses Perfetto's trace_processor (cached under ~/.cache/walletkit-profiling).
#
# Usage: ./analyze-trace.sh [trace.pftrace]   # omit -> newest in captures/
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

TRACE="${1:-}"
if [[ -z "$TRACE" ]]; then
  TRACE="$(ls -t "$SCRIPT_DIR"/captures/*.pftrace 2>/dev/null | head -1 || true)"
fi
if [[ -z "$TRACE" || ! -f "$TRACE" ]]; then
  echo "No trace found. Pass a .pftrace path, or run capture-perfetto.sh first." >&2
  exit 1
fi

CACHE="$HOME/.cache/walletkit-profiling"
TP="$CACHE/trace_processor"
mkdir -p "$CACHE"
if [[ ! -x "$TP" ]]; then
  echo "Fetching Perfetto trace_processor (one-time) from get.perfetto.dev ..."
  curl -LsS https://get.perfetto.dev/trace_processor -o "$TP"
  chmod +x "$TP"
fi

# On an emulator the async WalletKit.rpc:* wall-clock is dominated by the software
# RenderThread + JIT; trust the encode/reverse slices and per-thread CPU instead.
run_q() {
  local title="$1" sql="$2" tmp
  tmp="$(mktemp)"
  printf '%s\n' "$sql" > "$tmp"
  echo
  echo "── $title ──"
  "$TP" "$TRACE" -q "$tmp" 2>/dev/null \
    | grep -vE '^#|%$|Loading trace|common_flags|query\.cc|^column [0-9]' | sed '/^$/d'
  rm -f "$tmp"
}

echo "Trace: $TRACE"

run_q "[RELIABLE] Kotlin serialization cost (encode)" "
SELECT name AS slice, COUNT(*) AS calls, CAST(SUM(dur)/1e6 AS REAL) AS total_ms, CAST(MAX(dur)/1e6 AS REAL) AS max_ms
FROM slice WHERE name LIKE 'WalletKit.encode:%' GROUP BY name ORDER BY total_ms DESC;"

run_q "[RELIABLE] CPU per thread — ignore RenderThread / Jit (noise)" "
SELECT t.name AS thread, CAST(SUM(ts.dur)/1e6 AS REAL) AS running_ms
FROM thread_state ts JOIN thread t USING (utid) JOIN process p USING (upid)
WHERE p.name LIKE '%walletkit%' AND ts.state = 'Running'
GROUP BY t.name ORDER BY running_ms DESC LIMIT 20;"

run_q "[RELIABLE] Native reverse RPC / signing (reverse + adapterCall)" "
SELECT name AS slice, COUNT(*) AS calls, CAST(SUM(dur)/1e6 AS REAL) AS total_ms, CAST(MAX(dur)/1e6 AS REAL) AS max_ms
FROM slice WHERE name LIKE 'WalletKit.reverse:%' OR name LIKE 'WalletKit.adapterCall:%' GROUP BY name ORDER BY total_ms DESC;"

run_q "[POLLUTED — relative only] rpc wall-clock latency" "
SELECT name AS slice, COUNT(*) AS calls, CAST(SUM(dur)/1e6 AS REAL) AS total_ms, CAST(MAX(dur)/1e6 AS REAL) AS max_ms
FROM slice WHERE name LIKE 'WalletKit.rpc:%' GROUP BY name ORDER BY total_ms DESC;"

run_q "[UI] Frame jank (needs frametimeline in the capture)" "
SELECT a.jank_type AS jank, COUNT(*) AS frames, CAST(MAX(a.dur)/1e6 AS REAL) AS worst_frame_ms
FROM actual_frame_timeline_slice a JOIN process p USING (upid)
WHERE p.name LIKE '%walletkit%' GROUP BY a.jank_type ORDER BY frames DESC;"

run_q "[UI] Longest main-thread slices (frame blockers)" "
SELECT s.name AS slice, COUNT(*) AS n, CAST(SUM(s.dur)/1e6 AS REAL) AS total_ms, CAST(MAX(s.dur)/1e6 AS REAL) AS max_ms
FROM slice s JOIN thread_track tt ON s.track_id = tt.id JOIN thread t ON tt.utid = t.utid JOIN process p ON t.upid = p.upid
WHERE p.name LIKE '%walletkit%' AND t.is_main_thread = 1 GROUP BY s.name ORDER BY max_ms DESC LIMIT 15;"
