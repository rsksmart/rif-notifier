#!/bin/bash
cd "$(dirname "$0")"
pip3 install -r requirements.txt
SCRIPTPATH="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
cat > $SCRIPTPATH/notifier-prov-cli <<EOF
cd $SCRIPTPATH/..
python3 notifier-prov-cli/run.py \$*
EOF
chmod +x $SCRIPTPATH/notifier-prov-cli
ln -s $SCRIPTPATH/notifier-prov-cli /usr/local/bin