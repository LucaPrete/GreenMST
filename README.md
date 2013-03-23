GreenMST floodlight module
==========================

Implementation of module for Floodligth which implements a MST over
a network.

This project depends on Floodlight, which can be found here:
 [Floodlight project on GitHub](https://github.com/floodlight/floodlight).

It has been tested with Mininet, which can be found here:
 [Mininet project on GitHub](https://github.com/mininet/mininet).


License
=======

This sofware is licensed under the Apache License, Version 2.0.

Information can be found here:
 [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).


Installation and configuration
==============================

This project has been developed and tested on Floodlight v0.90. After its installation the ``floodlight.jar``
must be copied within the root folder of this project.

At this point the project can be imported into Eclipse and run right away.

It is also possible to create a ``greenmst.jar`` with the compiled files from this project, and run it with the command:
```
java -cp floodlight.jar:greenmst.jar -Dlogback.configurationFile=logback.xml net.floodlightcontroller.core.Main -cf floodlight.properties
```

The parameters specified have the following meaning:
 * ``logback.xml`` is the XML file which permits to configure the log facility for the running instance of Floodlight
 * ``floodlight.properties`` is the file specifying the properties for the running instance of Floodlight,
   it is configred to start the GreenMST module provided with this project.


Utilities
=========

In the scripts folder this projects provides some utility file that could be helpful to perform typical operations:

 * ``four-switch.py`` is a mininet custom topology file which permits to create a four switches full meshed topology;
 * ``startTopo.sh`` is a bash script which permits to start mininet with the four-switch topology (it assumes mininet
   is installed in the home for the user, and that the ``four-switch.py`` file is copied in the ``custom`` folder inside mininet);
 * ``viewGreenMSTapis.sh`` is a bash script that permits to query and show the results for the REST APIs;
 * ``setTopoCosts.sh`` is a bash script that permits to upload new topology costs via the REST APIs.


REST APIs
=========

This project offers the following REST APIs:

 * ``http://controller-ip:8080/wm/greenmst/topoedges/json``: only supports GET and shows all edges in the topology
 * ``http://controller-ip:8080/wm/greenmst/mstedges/json``: only supports GET and shows all edges in the MST computed from the topology
 * ``http://controller-ip:8080/wm/greenmst/redundantedges/json``: only supports GET and shows all redundant edges
   (edges in the topoloty but not in the computed MST)
 * ``http://controller-ip:8080/wm/greenmst/topocotsts/json``: supports GET and POST and permits to view/modify the costs for all edges
   in the topology


Tests
=====

In this section a typical use case will be presented to show how this project could be used to achieve important results in 
network optimization and configuration.

Use case
--------

The following use case has been implemented:
 * realization of a four switches full mashed topology with mininet;
 * GreenMST needs to compute a minimum spanning tree and send commands to close un-necessary ports to switches so that
   loops are avoided;
 * visualization of the MST deployed on the network via the REST APIs developed by GreenMST;
 * modification of the costs for all links to prove that GreenMST is able to compute a new minimum spanning tree on
   the provided costs and so adapt the network to the updated situation.

With this use case it is possible to prove that the GreenMST is working by identifying the correct spanning tree which
optimizes all switch-to-switch connections. It is also possible to prove that by creating an agent which senses the network
conditions and triggers an update of link costs, GreenMST could be able to real-time adapt the network to changing
configurations.

Step by step tutorial
---------------------

To test this software a implementation the following steps have been executed:

 * We have defined a four switches fully mashed topology in mininet.
   The costs for all edges of the topology are as shown by command:
   ```
   $ ./viewGreenMSTapis.sh -a topocosts
   [
      {
        "1,2": 1, 
        "1,3": 4, 
        "1,4": 2, 
        "2,3": 3, 
        "2,4": 4, 
        "3,4": 1
      }
   ]
   ```

 * At this point the topology can be shown with the following command:
   ```
   $ ./viewGreenMSTapis.sh -a topoEdges
   [
      {
        "cost": 1, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:03", 
        "sourcePort": 3, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 2, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:01", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 1, 
        "destinationPort": 1, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:01"
      }, 
      {
        "cost": 4, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 2, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 3, 
        "destinationPort": 2, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 2, 
        "sourceSwitch": "00:00:00:00:00:00:00:03"
      }, 
      {
        "cost": 4, 
        "destinationPort": 2, 
        "destinationSwitch": "00:00:00:00:00:00:00:01", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:03"
      }
   ]
   ```

 * Running the GreenMST the MST computed is as follows:
   ```
   $ ./viewGreenMSTapis.sh -a mstedges
   [
      {
        "cost": 1, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:03", 
        "sourcePort": 3, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 2, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:01", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 1, 
        "destinationPort": 1, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:01"
      }
   ]
   ```
   Which represent the minimum spaning tree computed on the topology (it is possible to verify that only edges with
   low costs have been kept open).

 * This MST has also been deployed on the switches by closing the ports for the unused links.
   For example with mininet it is possible to verify that the port on switch 4 has been closed with the command:
   ```
   mininet> s4 dpctl show tcp:127.0.0.1:6637
   features_reply (xid=0x4a7cacc5): ver:0x1, dpid:4
   n_tables:255, n_buffers:256
   features: capabilities:0xc7, actions:0xfff
    1(s4-eth1): addr:a2:8f:c2:a8:6d:57, config: 0, state:0
        current:    10GB-FD COPPER 
    2(s4-eth2): addr:ba:d8:da:a1:26:5b, config: 0x1, state:0x1
        current:    10GB-FD COPPER 
    3(s4-eth3): addr:0e:1d:34:0a:18:b2, config: 0, state:0
        current:    10GB-FD COPPER 
    LOCAL(s4): addr:82:45:0b:bc:e8:44, config: 0x1, state:0x1
   get_config_reply (xid=0xe67cf3b9): miss_send_len=0
   ```
   It is possible to see that port 2, for instance, has a state of 1 which means the port is down.

 * Now it is possible to submit new costs for the links, for example with the followin commands:
   ```
   $ ./setTopoCosts.sh 
   {
      "status": "new topology costs set"
   }
   
   $ ./viewGreenMSTapis.sh -a topocosts
   [
      {
        "1,2": 10, 
        "1,3": 40, 
        "1,4": 20, 
        "2,3": 30, 
        "2,4": 10, 
        "3,4": 40
      }
   ]
   ```

 * GreenMST module has recomputed a new MST, it is possible to check this with the command:
   ```
   $ ./viewGreenMSTapis.sh -a mstedges
   [
      {
        "cost": 10, 
        "destinationPort": 1, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 1, 
        "sourceSwitch": "00:00:00:00:00:00:00:01"
      }, 
      {
        "cost": 10, 
        "destinationPort": 3, 
        "destinationSwitch": "00:00:00:00:00:00:00:02", 
        "sourcePort": 2, 
        "sourceSwitch": "00:00:00:00:00:00:00:04"
      }, 
      {
        "cost": 30, 
        "destinationPort": 2, 
        "destinationSwitch": "00:00:00:00:00:00:00:03", 
        "sourcePort": 2, 
        "sourceSwitch": "00:00:00:00:00:00:00:02"
      }
   ]
   ```
   This command shows a different MST than the previous one due to fact the all costs have changed.

 * The new MST has been deployed also on the switches, as show by mininet command:
   ```
   mininet> s4 dpctl show tcp:127.0.0.1:6637
   features_reply (xid=0x461fa8c): ver:0x1, dpid:4
   n_tables:255, n_buffers:256
   features: capabilities:0xc7, actions:0xfff
    1(s4-eth1): addr:a2:8f:c2:a8:6d:57, config: 0x1, state:0x1
        current:    10GB-FD COPPER 
    2(s4-eth2): addr:ba:d8:da:a1:26:5b, config: 0, state:0
        current:    10GB-FD COPPER 
    3(s4-eth3): addr:0e:1d:34:0a:18:b2, config: 0x1, state:0x1
        current:    10GB-FD COPPER 
    LOCAL(s4): addr:82:45:0b:bc:e8:44, config: 0x1, state:0x1
   get_config_reply (xid=0x59b0cd47): miss_send_len=0
   ```
   The port 2 is now active while port 1 and 3 are down.
