#!/bin/bash

sudo mn --controller=remote,ip=127.0.0.1 --custom four-switch.py --topo=foursw --mac

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
