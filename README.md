distributedKNN
==============

distributed system for the K-nearest-neighbors algorithm

Structure:

Master starts up and waits for a number of consumers to connect.
Client requests Master for consumers.
Master assigns request an ID and gives client a list of consumers
Client sends to each consumers the ID and feature vector
Consumers compute euclidean distance, send top k results to aggregator
Aggregator compiles the results and returns the top k
Aggregator finds most frequent and sends to master ID and category


To run:
Master: mvn exec:java -Dmaster
Consumer: mvn exec:java -Dconsumer
Client: mvn exec:java -Dclient
Accumulator: mvn exec:java -Daccumulator

To edit the arguments open pom.xml
Go down to the profiles section where you'll see profiles for the master, consumer, client, and accumulator. In each profile, there's a list of <argument></argument> tags with the argument description next to it. Add your argument here.