#!/bin/bash
# Repository-owned Claude Cloud provisioning script for Arcogine.
#
# Keeps the Claude environment "setup script" field minimal:
#   exec /home/user/arcogine/infra/dev/claude-cloud.sh
# Do NOT `cd` before invoking this script: it captures its own invocation
# directory (see INVOKED_FROM below) and does its own `cd` into the repo,
# so a preceding `cd` would defeat that diagnostic.
#
# Provisioning is intentionally lightweight: inspect and validate the base
# image, but do not install project dependencies. Run `./arcogine setup`
# explicitly later when a task actually needs the development dependencies.
set -euo pipefail

REPO_DIR="/home/user/arcogine"
MIN_JAVA_MAJOR="21"
SUPPORTED_NODE_RANGE="^22.22.2 || ^24.15.0 || >=26.0.0"

# Capture where we were invoked from before doing anything else, including
# before setting up logging (log path itself must not depend on the cwd).
INVOKED_FROM="$(pwd -P)"

LOG_DIR="${HOME}/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
# Append $$ so two invocations within the same second (the timestamp's
# resolution) still get distinct log files.
LOG_FILE="${LOG_DIR}/arcogine-cloud-setup-${TIMESTAMP}-$$.log"

# Redirect all subsequent stdout/stderr through tee so output is both
# visible live (Claude's provisioning UI) and captured to the log file.
# Plain (non-append) tee is fine: the timestamped+pid filename is unique per run.
exec > >(tee "$LOG_FILE") 2>&1

echo "===================================================================="
echo "Arcogine Claude Cloud provisioning"
echo "Start time:            $(date -Is)"
echo "Invoked from:          $INVOKED_FROM"
echo "OS release:            $(. /etc/os-release 2>/dev/null && echo "${PRETTY_NAME:-unknown}" || echo unknown)"
echo "OS/kernel:             $(uname -srvo 2>/dev/null || uname -a)"
echo "Machine architecture:  $(uname -m)"
echo "CPU:                   $(nproc) vCPU(s) - $(grep -m1 'model name' /proc/cpuinfo | cut -d: -f2 | sed 's/^ //')"
echo "Memory:                $(free -h | awk '/^Mem:/ {print $2 " total, " $7 " available"}')"
echo "Disk (/):              $(df -h / | awk 'NR==2 {print $2 " total, " $4 " available (" $5 " used)"}')"
echo "Java compatibility:    ${MIN_JAVA_MAJOR}+"
echo "Supported Node.js:     ${SUPPORTED_NODE_RANGE}"
echo "Log file:              $LOG_FILE"
echo "===================================================================="

if [ ! -d "$REPO_DIR" ]; then
  echo "FATAL: expected Arcogine repository at '$REPO_DIR' but it does not exist." >&2
  exit 1
fi

echo "==> Changing to repository directory: $REPO_DIR"
cd "$REPO_DIR"

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

# Print the major version number from a `java -version`/`javac -version`
# style version string, handling both modern "21" and legacy "1.8" formats.
java_major_from_version_string() {
  local ver="$1"
  if [[ "$ver" =~ ^1\.([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
  elif [[ "$ver" =~ ^([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
  else
    echo ""
  fi
}

current_java_major() {
  if ! command -v java >/dev/null 2>&1; then
    echo ""
    return
  fi
  local ver
  # Don't assume the version string is on the first line: a JDK that
  # picks up JAVA_TOOL_OPTIONS (or similar env-driven JVM flags) prints a
  # notice to stderr before the actual `openjdk version "..."` line.
  ver="$(java -version 2>&1 | grep -m1 'version "' | sed -E 's/.*version "([^"]+)".*/\1/')"
  java_major_from_version_string "$ver"
}

current_node_version() {
  if ! command -v node >/dev/null 2>&1; then
    echo ""
    return
  fi
  node --version | sed -E 's/^v//'
}

# Keep this deliberately small and explicit rather than installing a semver
# utility during provisioning. It implements exactly the range declared in
# product/interfaces/web/package.json:
#   ^22.22.2 || ^24.15.0 || >=26.0.0
node_version_supported() {
  local version="$1"
  local major minor patch

  if [[ ! "$version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
    return 1
  fi

  major="${BASH_REMATCH[1]}"
  minor="${BASH_REMATCH[2]}"
  patch="${BASH_REMATCH[3]}"

  if (( major == 22 )); then
    (( minor > 22 || (minor == 22 && patch >= 2) ))
  elif (( major == 24 )); then
    (( minor >= 15 ))
  else
    (( major >= 26 ))
  fi
}

# Log a version command without making optional tooling a provisioning
# prerequisite. The executable is the first command argument after the label.
log_optional_version() {
  local label="$1"
  shift
  local executable="$1"

  echo "--- ${label} ---"
  if command -v "$executable" >/dev/null 2>&1; then
    if ! "$@" 2>&1; then
      echo "(version command failed; continuing because this inventory entry is informational)"
    fi
  else
    echo "not installed"
  fi
}

# ---------------------------------------------------------------------------
# 1. Record the host/base-image toolchain. Nothing below installs or upgrades
#    packages, so this is the environment Claude supplied to the container.
# ---------------------------------------------------------------------------

echo "==> Base-image toolchain inventory:"
log_optional_version "bash --version" bash bash --version
log_optional_version "git --version" git git --version
log_optional_version "curl --version" curl curl --version
log_optional_version "java -version" java java -version
log_optional_version "javac -version" javac javac -version
log_optional_version "node --version" node node --version
log_optional_version "npm --version" npm npm --version
log_optional_version "npx --version" npx npx --version
log_optional_version "docker --version" docker docker --version
log_optional_version "docker compose version" docker docker compose version
log_optional_version "trivy --version" trivy trivy --version
log_optional_version "gitleaks version" gitleaks gitleaks version

echo "--- resolved executable paths ---"
for tool in java javac node npm npx git docker trivy gitleaks; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf '%-10s %s\n' "$tool" "$(command -v "$tool")"
  else
    printf '%-10s %s\n' "$tool" "not installed"
  fi
done

# ---------------------------------------------------------------------------
# 2. Verify the platform-provided Java/Node versions satisfy Arcogine's
#    supported development contract. Do not upgrade them here: base-image
#    migrations are observed in the log above and adopted deliberately in the
#    repository when the supported contract changes.
# ---------------------------------------------------------------------------

echo "==> Verifying platform toolchain compatibility..."

ACTUAL_JAVA_MAJOR="$(current_java_major)"
if [ -z "$ACTUAL_JAVA_MAJOR" ]; then
  echo "FATAL: Java is not available on PATH; Arcogine requires JDK ${MIN_JAVA_MAJOR} or newer." >&2
  exit 1
fi
if [ "$ACTUAL_JAVA_MAJOR" -lt "$MIN_JAVA_MAJOR" ]; then
  echo "FATAL: Arcogine requires Java ${MIN_JAVA_MAJOR} or newer, but 'java -version' reports major version ${ACTUAL_JAVA_MAJOR}." >&2
  exit 1
fi
if ! command -v javac >/dev/null 2>&1; then
  echo "FATAL: javac is not available; Arcogine requires a JDK, not only a JRE." >&2
  exit 1
fi
echo "    Java major version OK: ${ACTUAL_JAVA_MAJOR} (compatibility floor ${MIN_JAVA_MAJOR})"

ACTUAL_NODE_VERSION="$(current_node_version)"
if [ -z "$ACTUAL_NODE_VERSION" ]; then
  echo "FATAL: Node.js is not available on PATH; Arcogine supports ${SUPPORTED_NODE_RANGE}." >&2
  exit 1
fi
if ! command -v npm >/dev/null 2>&1; then
  echo "FATAL: npm is not available on PATH; Arcogine's frontend setup requires npm." >&2
  exit 1
fi

# The package manifest is the frontend's public support contract. Fail clearly
# if this lightweight provisioning guard ever drifts from that source of truth.
PACKAGE_NODE_RANGE="$(node -p "require('./product/interfaces/web/package.json').engines.node || ''" 2>/dev/null || true)"
if [ "$PACKAGE_NODE_RANGE" != "$SUPPORTED_NODE_RANGE" ]; then
  echo "FATAL: Node support contract drift: claude-cloud.sh expects '${SUPPORTED_NODE_RANGE}', but package.json declares '${PACKAGE_NODE_RANGE:-none}'." >&2
  exit 1
fi

if ! node_version_supported "$ACTUAL_NODE_VERSION"; then
  echo "FATAL: Node.js ${ACTUAL_NODE_VERSION} is outside Arcogine's supported range: ${SUPPORTED_NODE_RANGE}." >&2
  exit 1
fi
echo "    Node.js version OK: ${ACTUAL_NODE_VERSION} (supported ${SUPPORTED_NODE_RANGE})"

echo "    No project dependencies were installed during provisioning."
echo "    Run './arcogine setup' explicitly when a task requires them."

# ---------------------------------------------------------------------------
# 3. Done.
# ---------------------------------------------------------------------------

echo "===================================================================="
echo "Arcogine Claude Cloud provisioning complete."
echo "Completion time: $(date -Is)"
echo "Log file:        $LOG_FILE"
echo "===================================================================="
