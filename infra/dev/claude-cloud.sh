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
# image, but do not install project dependencies. Install only dependencies
# needed by the task; `./arcogine setup` remains an explicit, optional
# full-development convenience.
set -euo pipefail

# Resolve this script's directory using Bash parameter expansion rather than an
# external `dirname` dependency; the hermetic provisioning test deliberately
# exposes only the primitives the provisioning contract declares.
SCRIPT_SOURCE="${BASH_SOURCE[0]}"
case "$SCRIPT_SOURCE" in
  */*) SCRIPT_DIR="${SCRIPT_SOURCE%/*}" ;;
  *) SCRIPT_DIR="." ;;
esac
SCRIPT_DIR="$(cd "$SCRIPT_DIR" && pwd -P)"
REPO_DIR="${ARCOGINE_REPO_DIR:-/home/user/arcogine}"
MIN_JAVA_MAJOR="21"

# Capture where we were invoked from before doing anything else, including
# before setting up logging (log path itself must not depend on the cwd).
INVOKED_FROM="$(pwd -P)"

LOG_DIR="${ARCOGINE_LOG_DIR:-${HOME}/logs}"
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
echo "Log file:              $LOG_FILE"
echo "===================================================================="

if [ ! -d "$REPO_DIR" ]; then
  echo "FATAL: expected Arcogine repository at '$REPO_DIR' but it does not exist." >&2
  exit 1
fi

echo "==> Changing to repository directory: $REPO_DIR"
cd "$REPO_DIR"

echo "==> Checking Git commit identity..."
# The helper configures only explicit ARCOGINE_GIT_USER_* values and warns on
# missing or agent-owned identities without making provisioning fail.
source "$SCRIPT_DIR/git-identity.sh"
configure_arcogine_git_identity

echo "==> Installing Git pre-commit check..."
bash "$SCRIPT_DIR/install-git-hooks.sh"

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
log_optional_version "bash --version" bash --version
log_optional_version "git --version" git --version
log_optional_version "curl --version" curl --version
log_optional_version "java -version" java -version
log_optional_version "javac -version" javac -version
log_optional_version "node --version" node --version
log_optional_version "npm --version" npm --version
log_optional_version "npx --version" npx --version
log_optional_version "docker --version" docker --version
log_optional_version "docker compose version" docker compose version
log_optional_version "trivy --version" trivy --version
log_optional_version "gitleaks version" gitleaks version

echo "--- resolved executable paths ---"
for tool in java javac node npm npx git docker trivy gitleaks; do
  if command -v "$tool" >/dev/null 2>&1; then
    printf '%-10s %s\n' "$tool" "$(command -v "$tool")"
  else
    printf '%-10s %s\n' "$tool" "not installed"
  fi
done

# ---------------------------------------------------------------------------
# 2. Verify the platform-provided Java version satisfies Arcogine's
#    supported development contract. Do not upgrade it here: base-image
#    migrations are observed in the log above and adopted deliberately.
# ---------------------------------------------------------------------------

echo "==> Verifying platform toolchain compatibility..."

# These are inventory/compatibility checks, not provisioning prerequisites:
# a documentation-only or backend-only task must not fail provisioning over
# an unrelated or unsupported runtime. Warn, don't exit, and let the task's
# own validation step (e.g. `./arcogine check`) enforce what it actually needs.
ACTUAL_JAVA_MAJOR="$(current_java_major)"
if [ -z "$ACTUAL_JAVA_MAJOR" ]; then
  echo "WARNING: Java is not available on PATH; Arcogine requires JDK ${MIN_JAVA_MAJOR} or newer for Java work." >&2
elif [ "$ACTUAL_JAVA_MAJOR" -lt "$MIN_JAVA_MAJOR" ]; then
  echo "WARNING: Arcogine requires Java ${MIN_JAVA_MAJOR} or newer for Java work, but 'java -version' reports major version ${ACTUAL_JAVA_MAJOR}." >&2
elif ! command -v javac >/dev/null 2>&1; then
  echo "WARNING: javac is not available; Arcogine requires a JDK, not only a JRE, for Java work." >&2
else
  echo "    Java major version OK: ${ACTUAL_JAVA_MAJOR} (compatibility floor ${MIN_JAVA_MAJOR})"
fi

echo "    No project dependencies were installed during provisioning."
echo "    Install only task-required dependencies, or explicitly run './arcogine setup' for resolved Java dependencies."
echo "    Any WARNING above only matters for work that exercises that runtime; it does not block unrelated tasks."

# ---------------------------------------------------------------------------
# 3. Done.
# ---------------------------------------------------------------------------

echo "===================================================================="
echo "Arcogine Claude Cloud provisioning complete."
echo "Completion time: $(date -Is)"
echo "Log file:        $LOG_FILE"
echo "===================================================================="
