GreenMST
========


                GARR GreenMST Beacon OpenFlow controller

  What is it?
  -----------

  The GARR GreenMST Beacon OpenFlow controller (or simply GreenMST) is
  an OSGi module creating minimum spanning tree overlay with green features.
  Having a simple and flexible spanning tree is a pre-requisite for many OpenFlow
  usages. We offer a solution to this issue adding also a couple of nice features:
  GreenMST supports deactivation for interfaces causing loops (saving energy) and 
  introduces user-defined weights over links, letting users choose the preferred
  metric.
  GreenMST is an add-on for the Beacon OpenFlow controller framework. 
  
  For more information on GreenMST see the following paper:
  
  "Energy efficient minimum spanning tree in OpenFlow networks"
  Luca Prete, Fabio Farina, Andrea Biancini and Mauro Campanella
  Procs. of European Workshop on Software Defined Network 2012 (EWSDN-12)
  October 25th - 26th, 2012 | Darmstadt, Germany.
 

  The Latest Version
  ------------------

  Details of the latest version can be found at <TBD>.

  Documentation
  -------------

  For documentation on Beacon and how to modify or create modules see
  the official Beacon documentation at
  
  https://openflow.stanford.edu/display/Beacon/Guides
  
  Installation
  ------------

    o  Install Beacon and OpenFlowJ library using Maven. 
  	See https://openflow.stanford.edu/display/Beacon/Quick+Start#QuickStart-DevelopwithyourowneditorandbuildusingMaven
		
		Pay attention to pre-requistites on Eclipse version and Spring.

    o  Replace the file <workspace>/beacon/net.beaconcontroller.topology/src/main/java/net/beaconcontroller/topology/LinkTuple.java
		with the LinkTuple.java shipped with GreenMST.
		
    o  Import it.garr.beacon.GreenMST as a the Maven project into your Eclipse workspace.
  

  Licensing
  ---------

  Please see the file called LICENSE.
  
  In publications using GreenMST please add a reference to the paper reported 
  in "What is it" section.

  Contacts
  --------

  For bugs, support, suggestions or collaborations you can get in contact with
  the corrisponding authors of the paper:
  
       o Luca Prete <luca.prete@garr.it>
	     o Fabio Farina <fabio.farina@garr.it>
