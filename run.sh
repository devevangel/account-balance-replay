#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mvn -q -DskipTests package
java -jar target/account-balance-replay.jar "$1"
