#!/bin/bash
set -e
# Set our working directory
cd $SCRIPT_DIR
# Set our environment variables (configured by entrypoint.sh)
source env-vars

echo "Executing prometheus extractor..."
./prometheus.py 
echo ""

echo "Done."