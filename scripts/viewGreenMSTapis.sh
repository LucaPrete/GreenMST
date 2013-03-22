#!/bin/bash

CONTROLLER="127.0.0.1"
ACTION="redundantedges"

while getopts ":hc:a:" optname
do
  case "$optname" in
    "h")
      echo "Run command with:"
      echo "  $0 [-c controller_ip] [-a action_name]"
      echo "  where action_name can be one in: {topocosts, topoedges, redundantedges, mstedges}"
      exit 1
      ;;
    "c")
      CONTROLLER=$OPTARG
      ;;
    "a")
      ACTION=$OPTARG
      ;;
    *)
    # Should not occur
      echo "Unknown error while processing options"
      ;;
  esac
done

#echo curl http://$CONTROLLER:8080/wm/greenmst/$ACTION/json
curl http://$CONTROLLER:8080/wm/greenmst/$ACTION/json 2>/dev/null | python -m json.tool
