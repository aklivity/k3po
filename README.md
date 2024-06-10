# K3PO

K3PO is a network driver and language agnostic testing tool.  It is designed to be able to create arbitrary network traffic and behavior, and to certify whether a network endpoint behaves correctly when subject to that behavior.  

The K3PO network driver can be directed to start emulating behavior defined in scripts via a [control protocol](Control Protocol).  Test frameworks in various programming languages can then utilize the control protocol to leverage K3PO for their own testing needs, including JUnit for Java integration tests.
