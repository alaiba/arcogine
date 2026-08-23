#!/bin/bash
# Repository-owned Claude Cloud provisioning script for Arcogine.
#
# Keeps the Claude environment "setup script" field minimal:
#   exec /home/user/arcogine/scripts/cloud-setup.sh
# Do NOT `cd` before invoking this script: it captures its own invocation
# directory (see INVOKED_FROM below) and does its own `cd` into the repo,
# so a preceding `cd` would defeat that diagnostic.
# All Arcogine-specific provisioning logic lives here, version-controlled.
set -euo pipefail

REPO_DIR="/home/user/arcogine"
REQUIRED_JAVA_MAJOR="25"
REQUIRED_NODE_MAJOR="24"

# Capture where we were invoked from before doing anything else, including
# before setting up logging (log path itself must not depend on the cwd).
INVOKED_FROM="$(pwd -P)"

LOG_DIR="${HOME}/logs"
mkdir -p "$LOG_DIR"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
LOG_FILE="${LOG_DIR}/arcogine-cloud-setup-${TIMESTAMP}.log"

# Redirect all subsequent stdout/stderr through tee so output is both
# visible live (Claude's provisioning UI) and captured to the log file.
exec > >(tee -a "$LOG_FILE") 2>&1

echo "===================================================================="
echo "Arcogine Claude Cloud setup"
echo "Start time:            $(date -Is)"
echo "Invoked from:          $INVOKED_FROM"
echo "OS/kernel:              $(uname -srvo 2>/dev/null || uname -a)"
echo "Machine architecture:  $(uname -m)"
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
# style version string, handling both "25" and legacy "1.8" formats.
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
  ver="$(java -version 2>&1 | head -1 | sed -E 's/.*"([^"]+)".*/\1/')"
  java_major_from_version_string "$ver"
}

# ---------------------------------------------------------------------------
# 1. Ensure Java 25 (Temurin preferred) is installed and selected.
# ---------------------------------------------------------------------------

echo "==> Checking Java toolchain (need major version ${REQUIRED_JAVA_MAJOR})..."

if [ "$(current_java_major)" != "$REQUIRED_JAVA_MAJOR" ]; then
  echo "    Java ${REQUIRED_JAVA_MAJOR} not currently selected; installing Temurin ${REQUIRED_JAVA_MAJOR}."

  if command -v apt-get >/dev/null 2>&1; then
    if [ ! -f /etc/apt/keyrings/adoptium.asc ]; then
      echo "    Adding Eclipse Adoptium apt repository..."
      sudo install -m 0755 -d /etc/apt/keyrings
      curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
        | sudo tee /etc/apt/keyrings/adoptium.asc >/dev/null
      # No architecture is hardcoded here: apt itself resolves the correct
      # binary-arch package for the host at install time.
      echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo "$VERSION_CODENAME") main" \
        | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null
    fi
    sudo apt-get update -y
    sudo apt-get install -y "temurin-${REQUIRED_JAVA_MAJOR}-jdk"
  else
    echo "FATAL: apt-get not available; cannot install Temurin ${REQUIRED_JAVA_MAJOR} JDK." >&2
    exit 1
  fi

  # Discover the installed JDK directory dynamically rather than assuming a
  # path (Temurin apt packages lay out under /usr/lib/jvm/temurin-<major>-jdk-*,
  # where the trailing segment is architecture-specific). Discovery and
  # validation are kept separate so a missing/empty find result fails with
  # a clear diagnostic instead of collapsing into a bogus path.
  JDK_DIR="$(find /usr/lib/jvm -maxdepth 1 -type d \
    -iname "temurin-${REQUIRED_JAVA_MAJOR}-jdk*" -print -quit)"

  if [ -z "$JDK_DIR" ] || [ ! -x "$JDK_DIR/bin/java" ]; then
    echo "FATAL: could not locate installed Temurin ${REQUIRED_JAVA_MAJOR} JDK under /usr/lib/jvm." >&2
    exit 1
  fi

  JAVA_HOME_CANDIDATE="$(readlink -f "$JDK_DIR")"

  echo "    Selecting $JAVA_HOME_CANDIDATE via update-alternatives..."
  sudo update-alternatives --install /usr/bin/java java "${JAVA_HOME_CANDIDATE}/bin/java" 2100
  sudo update-alternatives --install /usr/bin/javac javac "${JAVA_HOME_CANDIDATE}/bin/javac" 2100
  sudo update-alternatives --set java "${JAVA_HOME_CANDIDATE}/bin/java"
  sudo update-alternatives --set javac "${JAVA_HOME_CANDIDATE}/bin/javac"
else
  echo "    Java ${REQUIRED_JAVA_MAJOR} already selected."
fi

# Always (re-)derive JAVA_HOME from the actually-selected `java` binary,
# not just when we just installed one: an inherited JAVA_HOME could still
# point at an old JDK even though `java` on PATH now resolves to the
# right one. Some JVM tooling (Gradle included) prefers JAVA_HOME over PATH.
JAVA_BIN="$(readlink -f "$(command -v java)")"
export JAVA_HOME="$(dirname "$(dirname "$JAVA_BIN")")"
export PATH="${JAVA_HOME}/bin:${PATH}"
echo "    JAVA_HOME set to: $JAVA_HOME"

# ---------------------------------------------------------------------------
# 2. Ensure Node.js 24 is installed and selected.
# ---------------------------------------------------------------------------

echo "==> Checking Node.js toolchain (need major version ${REQUIRED_NODE_MAJOR})..."

current_node_major() {
  if ! command -v node >/dev/null 2>&1; then
    echo ""
    return
  fi
  node --version | sed -E 's/^v([0-9]+).*/\1/'
}

if [ "$(current_node_major)" != "$REQUIRED_NODE_MAJOR" ]; then
  echo "    Node ${REQUIRED_NODE_MAJOR} not currently selected; installing via NodeSource."

  if command -v apt-get >/dev/null 2>&1; then
    # The NodeSource setup script detects host architecture itself; no
    # architecture-specific path is hardcoded here.
    curl -fsSL "https://deb.nodesource.com/setup_${REQUIRED_NODE_MAJOR}.x" | sudo -E bash -
    sudo apt-get install -y nodejs
  else
    echo "FATAL: apt-get not available; cannot install Node.js ${REQUIRED_NODE_MAJOR}." >&2
    exit 1
  fi
else
  echo "    Node ${REQUIRED_NODE_MAJOR} already selected."
fi

# ---------------------------------------------------------------------------
# 3. Verify and log toolchain versions.
# ---------------------------------------------------------------------------

echo "==> Toolchain versions:"
echo "--- java -version ---"
java -version
echo "--- javac -version ---"
javac -version
echo "--- node --version ---"
node --version
echo "--- npm --version ---"
npm --version

# ---------------------------------------------------------------------------
# 4. Explicitly verify Java/Node major versions before touching the repo.
# Failing here, before npm ci / gradlew, gives a clear diagnostic instead
# of a confusing downstream failure under the wrong toolchain.
# ---------------------------------------------------------------------------

echo "==> Verifying selected toolchain versions..."

ACTUAL_JAVA_MAJOR="$(current_java_major)"
if [ "$ACTUAL_JAVA_MAJOR" != "$REQUIRED_JAVA_MAJOR" ]; then
  echo "FATAL: expected Java major version ${REQUIRED_JAVA_MAJOR}, but 'java -version' reports major version '${ACTUAL_JAVA_MAJOR:-unknown}'." >&2
  exit 1
fi
echo "    Java major version OK: ${ACTUAL_JAVA_MAJOR}"

ACTUAL_NODE_MAJOR="$(current_node_major)"
if [ "$ACTUAL_NODE_MAJOR" != "$REQUIRED_NODE_MAJOR" ]; then
  echo "FATAL: expected Node major version ${REQUIRED_NODE_MAJOR}, but 'node --version' reports major version '${ACTUAL_NODE_MAJOR:-unknown}'." >&2
  exit 1
fi
echo "    Node major version OK: ${ACTUAL_NODE_MAJOR}"

# ---------------------------------------------------------------------------
# 5. Prepare the repository.
# ---------------------------------------------------------------------------

echo "==> Preparing repository..."

if [ ! -f .env ]; then
  echo "    Copying .env.example -> .env"
  cp .env.example .env
else
  echo "    .env already exists; leaving as-is"
fi

echo "    Running npm ci in ui/..."
(cd ui && npm ci)

echo "    Warming Gradle wrapper (./gradlew --no-daemon help) in java/..."
(cd java && ./gradlew --no-daemon help)

# ---------------------------------------------------------------------------
# 6. Done.
# ---------------------------------------------------------------------------

echo "===================================================================="
echo "Arcogine Claude Cloud setup complete."
echo "Completion time: $(date -Is)"
echo "Log file:        $LOG_FILE"
echo "===================================================================="
