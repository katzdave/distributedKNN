compile: */*.java
	mkdir -p out
	javac -d out $^
runConsumer: 
	java -cp out/ consumer.Consumer
runClient:
	java -classpath out/ client.Client $(sp) $(mip) $(mp)
runMaster:
	java -classpath out/ masterserver.MasterServer $(sp) $(lip) $(lp) $(max) $(k) $(file)
runAcc:
	java -classpath out/ accumulator.Accumulator $(sp) $(lip) $(lp) $(mip) $(mp)
clean:
	rm -rf out