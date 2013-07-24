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

sudo mn --controller=remote,ip=127.0.0.1 --custom ~/mininet/custom/four-switch.py --topo=foursw --mac

#Usage: mn [options]
#(type mn -h for details)
#
#The mn utility creates Mininet network from the command line. It can create
#parametrized topologies, invoke the Mininet CLI, and run tests.
#
#Options:
#  -h, --help            show this help message and exit
#  --switch=SWITCH       ovsk|ovsl|user[,param=value...]
#  --host=HOST           cfs|proc|rt[,param=value...]
#  --controller=CONTROLLER
#                        none|nox|ovsc|ref|remote[,param=value...]
#  --link=LINK           default|tc[,param=value...]
#  --topo=TOPO           linear|minimal|reversed|single|tree[,param=value...]
#  -c, --clean           clean and exit
#  --custom=CUSTOM       read custom topo and node params from .pyfile
#  --test=TEST           cli|build|pingall|pingpair|iperf|all|iperfudp|none
#  -x, --xterms          spawn xterms for each node
#  -i IPBASE, --ipbase=IPBASE
#                        base IP address for hosts
#  --mac                 automatically set host MACs
#  --arp                 set all-pairs ARP entries
#  -v VERBOSITY, --verbosity=VERBOSITY
#                        info|warning|critical|error|debug|output
#  --innamespace         sw and ctrl in namespace?
#  --listenport=LISTENPORT
#                        base port for passive switch listening
#  --nolistenport        don't use passive listening port
#  --pre=PRE             CLI script to run before tests
#  --post=POST           CLI script to run after tests
#  --pin                 pin hosts to CPU cores (requires --host cfs or --host
#                        rt)
#  --version             
