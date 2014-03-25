distributedKNN
==============

distributed system for the K-nearest-neighbors algorithm

Structure:

Master starts up and waits for a number of consumers to connect.
Client sends to master feature vectors
Master broadcasts to consumers
Consumers compute euclidean distance, send top k results to aggregator
Aggregator compiles the results and returns the top k
Aggregator finds most frequent and sends to master ID and category

To compile:
make

To run:
Master: make runMaster
Consumer: make runConsumer
Client: make runClient
Accumulator: make runAcc

To clean:
make clean
To edit the arguments open pom.xml
Go down to the profiles section where you'll see profiles for the master, consumer, client, and accumulator. In each profile, there's a list of <argument></argument> tags with the argument description next to it. Add your argument here.