#!/bin/bash

# Run sbt in continuous mode in background
sbt "~backend/runMain gitinsp.application.GitInspector" &

# Store the PID of the background process
SBT_PID=$!

# Function to cleanup on script exit
cleanup() {
    echo "Stopping sbt process..."
    kill $SBT_PID 2>/dev/null
    exit
}

# Set trap to cleanup on script termination
trap cleanup INT TERM

# Run npm dev server
npm run dev

# Cleanup when npm exits
cleanup