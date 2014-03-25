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
rc:
	java -cp target/classes/ consumer.Consumer ice07.ee.cooper.edu 20000
clean:
	mvn clean
