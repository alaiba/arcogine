#!/bin/bash
# Repository-owned Claude Cloud provisioning script for Arcogine.
#
# Keeps the Claude environment "setup script" field minimal:
#   exec /home/user/arcogine/infra/dev/claude-cloud.sh
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
# Append $$ so two invocations within the same second (the timestamp's
# resolution) still get distinct log files.
LOG_FILE="${LOG_DIR}/arcogine-cloud-setup-${TIMESTAMP}-$$.log"

# Redirect all subsequent stdout/stderr through tee so output is both
# visible live (Claude's provisioning UI) and captured to the log file.
# Plain (non-append) tee is fine: the timestamped+pid filename is unique per run.
exec > >(tee "$LOG_FILE") 2>&1

echo "===================================================================="
echo "Arcogine Claude Cloud setup"
echo "Start time:            $(date -Is)"
echo "Invoked from:          $INVOKED_FROM"
echo "OS/kernel:              $(uname -srvo 2>/dev/null || uname -a)"
echo "Machine architecture:  $(uname -m)"
echo "CPU:                   $(nproc) vCPU(s) - $(grep -m1 'model name' /proc/cpuinfo | cut -d: -f2 | sed 's/^ //')"
echo "Memory:                $(free -h | awk '/^Mem:/ {print $2 " total, " $7 " available"}')"
echo "Disk (/):               $(df -h / | awk 'NR==2 {print $2 " total, " $4 " available (" $5 " used)"}')"
echo "Log file:              $LOG_FILE"
echo "===================================================================="

if [ ! -d "$REPO_DIR" ]; then
  echo "FATAL: expected Arcogine repository at '$REPO_DIR' but it does not exist." >&2
  exit 1
fi

echo "==> Changing to repository directory: $REPO_DIR"
cd "$REPO_DIR"

# ---------------------------------------------------------------------------
# 0. Clean up stale pre-reorg directories.
# Repository structure was reorganized (java/ -> product/, ui/ ->
# product/interfaces/web/) in PR #142. Environment snapshots captured
# before that reorg ran can still carry a top-level java/ and/or ui/
# directory left behind by an earlier provisioning run (npm ci /
# gradlew executed against those old paths populated ui/node_modules and
# java/.gradle). Those directories are untracked and no longer meaningful
# under the current layout, so remove them before provisioning proceeds
# to avoid confusing leftover build/dependency caches.
# ---------------------------------------------------------------------------

for stale_dir in java ui; do
  if [ -d "$REPO_DIR/$stale_dir" ]; then
    echo "==> Removing stale pre-reorg directory: $REPO_DIR/$stale_dir"
    rm -rf "${REPO_DIR:?}/$stale_dir"
  fi
done

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
  # Don't assume the version string is on the first line: a JDK that
  # picks up JAVA_TOOL_OPTIONS (or similar env-driven JVM flags) prints a
  # "Picked up JAVA_TOOL_OPTIONS: ..." notice to stderr before the actual
  # `openjdk version "..."` line, which `head -1` would otherwise grab.
  ver="$(java -version 2>&1 | grep -m1 'version "' | sed -E 's/.*version "([^"]+)".*/\1/')"
  java_major_from_version_string "$ver"
}

# ---------------------------------------------------------------------------
# 1. Ensure Java 25 (Temurin preferred) is installed and selected.
# ---------------------------------------------------------------------------

echo "==> Checking Java toolchain (need major version ${REQUIRED_JAVA_MAJOR})..."

if [ "$(current_java_major)" != "$REQUIRED_JAVA_MAJOR" ]; then
  echo "    Java ${REQUIRED_JAVA_MAJOR} not currently selected; installing Temurin ${REQUIRED_JAVA_MAJOR}."

  if command -v apt-get >/dev/null 2>&1; then
    # Always (re-)write the keyring and source list rather than skipping
    # when the keyring file merely exists: a prior interrupted run (e.g.
    # curl failing mid-pipeline) can leave a 0-byte/invalid keyring file
    # behind, which would otherwise cause this block to be silently
    # skipped without ever adding a valid Adoptium source. Both writes are
    # cheap and idempotent, so there's no downside to always doing them.
    echo "    Adding Eclipse Adoptium apt repository..."
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public \
      | sudo tee /etc/apt/keyrings/adoptium.asc >/dev/null
    if [ ! -s /etc/apt/keyrings/adoptium.asc ]; then
      echo "FATAL: failed to download Adoptium GPG key (empty file written)." >&2
      exit 1
    fi
    # No architecture is hardcoded here: apt itself resolves the correct
    # binary-arch package for the host at install time.
    echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(. /etc/os-release && echo "$VERSION_CODENAME") main" \
      | sudo tee /etc/apt/sources.list.d/adoptium.list >/dev/null
    # Refresh only the Adoptium list we just added, not every apt source
    # configured on the base image. A plain `apt-get update` is all-or-
    # nothing: an unrelated, unreachable third-party PPA already present
    # on the image would abort the whole refresh, even though the
    # Adoptium source itself is fine.
    sudo -E apt-get update -y \
      -o Dir::Etc::sourcelist="sources.list.d/adoptium.list" \
      -o Dir::Etc::sourceparts="-" \
      -o APT::Get::List-Cleanup="0"
    sudo -E apt-get install -y "temurin-${REQUIRED_JAVA_MAJOR}-jdk"
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

  # update-alternatives only governs /usr/bin/java; it has no effect on
  # some other Java earlier on PATH (a version-manager shim, a JDK under
  # /usr/local/bin, etc). Prepend the JDK we just selected so it actually
  # wins PATH resolution, mirroring what the Node section already does
  # for the same class of problem.
  export JAVA_HOME="$JAVA_HOME_CANDIDATE"
  export PATH="${JAVA_HOME}/bin:${PATH}"
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
    # We add the NodeSource repo ourselves (mirroring what their official
    # setup_<major>.x script does) rather than piping that script into
    # `sudo bash -`: it internally runs unscoped `apt update -y` calls,
    # which would abort on any unrelated, unreachable apt source already
    # configured on the base image. Doing it ourselves lets us scope the
    # refresh to just the NodeSource list, same as we do for Adoptium.
    sudo mkdir -p /usr/share/keyrings
    curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
      | sudo gpg --dearmor --yes -o /usr/share/keyrings/nodesource.gpg
    if [ ! -s /usr/share/keyrings/nodesource.gpg ]; then
      echo "FATAL: failed to download NodeSource GPG key (empty file written)." >&2
      exit 1
    fi
    sudo chmod 644 /usr/share/keyrings/nodesource.gpg

    # dpkg reports the actual host architecture; nothing is hardcoded.
    NODE_ARCH="$(dpkg --print-architecture)"
    cat <<EOF | sudo tee /etc/apt/sources.list.d/nodesource.sources >/dev/null
Types: deb
URIs: https://deb.nodesource.com/node_${REQUIRED_NODE_MAJOR}.x
Suites: nodistro
Components: main
Architectures: ${NODE_ARCH}
Signed-By: /usr/share/keyrings/nodesource.gpg
EOF

    sudo -E apt-get update -y \
      -o Dir::Etc::sourcelist="sources.list.d/nodesource.sources" \
      -o Dir::Etc::sourceparts="-" \
      -o APT::Get::List-Cleanup="0"
    sudo -E apt-get install -y nodejs

    # Base images can carry other Node installs earlier on PATH (extra
    # runtime managers, stale symlinks, etc.) that would otherwise shadow
    # the one apt just installed. Discover where dpkg actually put it and
    # make sure that directory wins, rather than assuming a fixed path.
    NODE_BIN="$(dpkg -L nodejs 2>/dev/null | grep -E '/bin/node$' | head -1)"
    if [ -n "$NODE_BIN" ] && [ -x "$NODE_BIN" ]; then
      export PATH="$(dirname "$NODE_BIN"):${PATH}"
    fi
  else
    echo "FATAL: apt-get not available; cannot install Node.js ${REQUIRED_NODE_MAJOR}." >&2
    exit 1
  fi
else
  echo "    Node ${REQUIRED_NODE_MAJOR} already selected."
fi
echo "    node resolves to: $(command -v node)"

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
# 5. Prepare the repository. Actual dependency setup (frontend deps, Gradle
# warming, Playwright, etc.) is owned by ./arcogine setup so it isn't
# duplicated between this script and the devcontainer.
# ---------------------------------------------------------------------------

echo "==> Preparing repository..."

if [ ! -f .env ]; then
  if [ ! -f .env.example ]; then
    echo "FATAL: .env.example not found in $REPO_DIR." >&2
    exit 1
  fi
  echo "    Copying .env.example -> .env"
  cp .env.example .env
else
  echo "    .env already exists; leaving as-is"
fi

echo "    Running ./arcogine setup..."
./arcogine setup

# ---------------------------------------------------------------------------
# 6. Done.
# ---------------------------------------------------------------------------

echo "===================================================================="
echo "Arcogine Claude Cloud setup complete."
echo "Completion time: $(date -Is)"
echo "Log file:        $LOG_FILE"
echo "===================================================================="
