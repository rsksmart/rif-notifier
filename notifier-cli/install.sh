#!/bin/bash
cd "$(dirname "$0")"
pip install -r requirements.txt
SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
cat > $SCRIPT_PATH/notifier-cli <<EOF
cd $SCRIPTPATH/..
python notifier-cli/run.py \$*
EOF
chmod +x $SCRIPTPATH/notifier-cli
ln -s $SCRIPTPATH/notifier-cli /usr/local/bin