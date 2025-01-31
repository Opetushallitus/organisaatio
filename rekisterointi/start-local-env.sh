#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail

function require_command {
  if ! command -v "$1" > /dev/null; then
    echo "I require $1 but it's not installed. Aborting."
    exit 1
  fi
}

require_command tmux
require_command mvn
require_command npm
require_command aws

session="rekisterointi"

tmux kill-session -t $session || true
tmux start-server
tmux new-session -d -s $session

tmux splitw -h

echo "starting server"
tmux send-keys "./start-local-backend.sh" C-m

tmux select-pane -t 0
echo "starting frontend"
tmux send-keys "cd rekisterointi-ui && npm run build:watch" C-m

tmux splitw -v
echo "starting mock-api"
tmux send-keys "cd mock-api && npm install && npm start" C-m

open "http://localhost:3000/jotpa"

tmux select-pane -t 0
tmux attach-session -t $session
