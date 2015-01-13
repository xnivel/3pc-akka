Testing
-------

Set up a network between your nodes (in the example this is 10.0.0.0/24). Run
the following commands, each on a different node, with this repository's root
as the working directory:

    sbt "run-main CoordinatorMain 10.0.0.1 9000"
    sbt "run-main ServerMain server1 10.0.0.2 9001"
    sbt "run-main ServerMain server2 10.0.0.3 9002"
    sbt "run-main ClientMain 10.0.0.1 9000 10.0.0.2 9001 10.0.0.3 9002"
