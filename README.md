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