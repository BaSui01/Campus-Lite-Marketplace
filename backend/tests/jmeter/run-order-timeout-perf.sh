#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR=$(cd -- "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
JMETER_DIR="$ROOT_DIR/tests/jmeter"
RESULTS_DIR="$JMETER_DIR/results"
PLAN_FILE="${PLAN_FILE:-$JMETER_DIR/order_timeout_performance.jmx}"

mkdir -p "$RESULTS_DIR"

JMETER_IMAGE="${JMETER_IMAGE:-justb4/jmeter:5.6.2}"
JMETER_NETWORK="${JMETER_NETWORK:-campus-lite-marketplace_campus-perf}"

HOST="${HOST:-backend}"
PORT="${PORT:-8080}"
THREADS="${THREADS:-20}"
RAMP_UP="${RAMP_UP:-60}"
DURATION="${DURATION:-1200}"
THINK_TIME="${THINK_TIME:-1000}"
SEED_COUNT="${SEED_COUNT:-400}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-admin123}"
RESULT_FILE="${RESULT_FILE:-results/order_timeout_$(date +%Y%m%d_%H%M%S).jtl}"

docker run --rm \
  --network "$JMETER_NETWORK" \
  -v "$JMETER_DIR:/plans" \
  -e JVM_ARGS="${JVM_ARGS:-}" \
  "$JMETER_IMAGE" \
    -n \
    -t "/plans/$(basename "$PLAN_FILE")" \
    -Jhost="$HOST" \
    -Jport="$PORT" \
    -Jthreads="$THREADS" \
    -JrampUp="$RAMP_UP" \
    -Jduration="$DURATION" \
    -JthinkTime="$THINK_TIME" \
    -JseedCount="$SEED_COUNT" \
    -Jusername="$USERNAME" \
    -Jpassword="$PASSWORD" \
    -JresultsFile="/plans/$RESULT_FILE"

echo "JMeter run completed. Results saved to $JMETER_DIR/$RESULT_FILE"
