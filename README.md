ORFuzer
=======

Proof Of Concept for rating CDRs for a corporate fixed line voice service (fuzer/colt)

  * CDRs provided by COLT
  * account lookup based on subscriber number (accounts have many numbers)
  * setup and minute price depending on destination
  * each account has 2 free minutes packages: fix and mobile
  * cdr, configuration data and balance counters stored in files (no database)


Build instructions
==================

Prerequisites:

  * jdk 7
  * maven
  * git

Building OpenRate core:

$ git clone git@github.com:isparkes/OpenRate.git
$ (cd OpenRate; mvn install)

Building this project:

$ git clone git@github.com:isparkes/ORFuzer.git
$ (cd ORFuzer; mvn install)


Installation
============

All the below can be run on another host. Only prerequisite is a jre.

Unpack the zip file created by the build instructions above:

$ unzip ORFuzer/target/ORFuzer-0.0.0-bin.zip


Running
=======

All following commands should be run from the project dir:

$ cd ORFuzer-0.0.0

Starting
--------

This will start the openrate process in the background:

$ bin/start.sh

Feeding some data
-----------------

An example cdr file is in Data/, rename it to have an extension ".in" 
to let OpenRate process it:

$ cp Data/example.cdr Data/example.in


Viewing results
---------------

The result cdr file will contain additional columns with e.g. the price:

$ cat Data/example.out

CDRs that were not rated due to some errors contain additional columns 
with the error:

$ cat Data/example.err

More debugging information per CDR is in the "*.dump" file.


Stopping
--------

Do not simply kill the OpenRate application or caches may not have been 
persisted. Use this:

$ bin/stop.sh


Balance counters
----------------

Per account and month, 2 balance counters keep track of how many free minutes
a customer has consumed. There are stored in ConfigData/balances.dat

$ cat ConfigData/balances.dat

Note that this file is only written when OpenRate is stopped.


Troubleshooting
===============

Inspect the log files:

  * log/Framework.log 
  * log/Pipeline.log

