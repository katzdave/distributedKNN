compile: 
	mvn compile
runConsumer: 
	mvn exec:java -Dconsumer
runClient:
	mvn exec:java -Dclient
runMaster:
	mvn exec:java -Dmaster
runAcc:
	mvn exec:java -Dacc
clean:
	mvn clean
