#!/bin/bash
set -e
# Where to put the file that contains our environment variables
ENV_SCRIPT="${SCRIPT_DIR}/env-vars"

echo "PR_HOST=${PR_HOST:-prometheus.prometheus:9090}
OP_SERVER_IP=${OP_SERVER_IP:-somedbname}
DC_NAME=${DC_NAME:-digitalclientname}
DC_TOKEN=${DC_TOKEN:-digitalclienttoken}

LOG_FILE=${LOG_FILE:-\"/var/log/pycli/pycli.log\"}
" > ${ENV_SCRIPT}

# Activate the environment variables (just for printing, basically)
source ${ENV_SCRIPT}

# Write out our crontab to a file
echo "
${CRON_PY} SCRIPT_DIR=${SCRIPT_DIR} ${SCRIPT_DIR}/py.sh >> ${LOG_FILE} 2>&1
" > ${SCRIPT_DIR}/crontab
# Install the crontab for root
crontab -u root ${SCRIPT_DIR}/crontab

# For debugging purposes, to make sure the crontab's been set up properly and
# all of our parameters have been configured correctly (NOTE: I haven't echo'd
# out any passwords here, for good reason: we don't know who has access to our
# logs, and our logs are going to be e-mailed around).
echo "Crontab for root:"
crontab -l
echo ""
echo "Parameters:"
echo "  - Prometheus Host      : prometheus://$PR_HOST"
echo "  - Onesait Platform Server : $OP_SERVER"
echo ""

# Execute the container's CMD commands
exec "$@"