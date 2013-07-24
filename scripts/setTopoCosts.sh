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
if [ "$1" != "" ]; then
  CONTROLLER="$1"
fi

NEWCOSTS="[{\"1,2\":10,\"1,3\":40,\"1,4\":20,\"2,3\":30,\"2,4\":10,\"3,4\":40}]"

curl -d $NEWCOSTS http://$CONTROLLER:8080/wm/greenmst/topocosts/json 2>/dev/null | python -m json.tool
