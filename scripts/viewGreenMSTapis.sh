#!/bin/bash

########################################################
#
# Copyright (C) 2013 Luca Prete, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# author Luca Prete <luca.prete@garr.it>
# author Andrea Biancini <andrea.biancini@garr.it>
# author Fabio Farina <fabio.farina@garr.it>
#
########################################################

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
