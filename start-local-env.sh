#!/usr/bin/env bash
set -o errexit -o nounset -o pipefail -o xtrace
readonly repo="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

function stop() {
  cd "$repo"
  docker compose down
}
trap stop EXIT

function main {
  cd "$repo"
  local -r session="organisaatio"
  tmux kill-session -t "$session" || true
  tmux start-server
  tmux new-session -d -s "$session"

  tmux select-pane -t 0
  tmux send-keys "docker compose down --volumes; docker compose up --force-recreate --renew-anon-volumes" C-m

  tmux splitw -v
  tmux select-pane -t 1
  tmux send-keys "$repo/scripts/run-mocks.sh" C-m

  tmux splitw -h
  tmux select-pane -t 2
  tmux send-keys "$repo/scripts/run-organisaatio-ui.sh" C-m

  tmux select-pane -t 0
  tmux splitw -h
  tmux send-keys "$repo/scripts/run-organisaatio-service.sh" C-m

  open "http://localhost:3003/organisaatiot"

  tmux attach-session -t "$session"
}

main "$@"
