#!/bin/bash
cd "$(dirname "$0")"
pip3 install -r requirements.txt
SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
cat > $SCRIPTPATH/notifier-cons-cli <<EOF
cd $SCRIPTPATH/..
python3 notifier-cons-cli/run.py \$*
EOF
chmod +x $SCRIPTPATH/notifier-cons-cli
ln -s $SCRIPTPATH/notifier-cons-cli /usr/local/bin