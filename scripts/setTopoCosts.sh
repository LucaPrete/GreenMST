#!/bin/bash

CONTROLLER="127.0.0.1"
if [ "$1" != "" ]; then
  CONTROLLER="$1"
fi

NEWCOSTS="[{\"1,2\":10,\"1,3\":40,\"1,4\":20,\"2,3\":30,\"2,4\":10,\"3,4\":40}]"

curl -d $NEWCOSTS http://$CONTROLLER:8080/wm/greenmst/topocosts/json 2>/dev/null | python -m json.tool
