#!/usr/bin/env bash
EASTWOOD_CONF="{:test-paths [\"test/backend\" \"test/frontend\"]}"

lein eastwood "$EASTWOOD_CONF"
