#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
COLLECTION="$ROOT_DIR/tests/e2e/campus-e2e.postman_collection.json"
ENV_FILE="$ROOT_DIR/tests/e2e/backend-e2e.postman_environment.json"

if ! command -v newman >/dev/null 2>&1; then
  echo "[ERROR] Newman 未安装，请先运行: npm install -g newman" >&2
  exit 1
fi

echo "[INFO] 使用环境文件: $ENV_FILE"
echo "[INFO] 运行 Postman 集合: $COLLECTION"

mkdir -p "$ROOT_DIR/tests/e2e/report"

newman run "$COLLECTION" \
  --environment "$ENV_FILE" \
  --reporters cli,junit \
  --reporter-junit-export "$ROOT_DIR/tests/e2e/report/e2e-results.xml"

echo "[INFO] 端到端流程执行完成，报告已生成在 tests/e2e/report/e2e-results.xml"
