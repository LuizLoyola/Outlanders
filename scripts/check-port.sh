#!/usr/bin/bash

PORT=$1

while true; do
    if ss -tuln | grep -q ":$PORT "; then
        echo "Port $PORT is in use."
        exit 0
    else
        echo "Port $PORT is not in use. Waiting..."
        sleep 0.2
    fi
done