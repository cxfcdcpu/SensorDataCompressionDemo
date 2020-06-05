GEN=OscilloscopeMsg.java Constants.java CompressedMsg.java baseControlMsg.java superResetMsg.java

all: oscilloscope.jar

oscilloscope.jar: Oscilloscope.class
	jar cf $@ *.class

OscilloscopeMsg.java: ./Oscilloscope.h
	mig -target=null -java-classname=OscilloscopeMsg java ./Oscilloscope.h oscilloscope -o $@

baseControlMsg.java: ./Oscilloscope.h
	mig  -target=null -java-classname=baseControlMsg java ./Oscilloscope.h baseControl -o $@

superResetMsg.java: ./Oscilloscope.h
	mig  -target=null -java-classname=superResetMsg java ./Oscilloscope.h superReset -o $@

CompressedMsg.java: ./Oscilloscope.h
	mig  -target=null -java-classname=CompressedMsg java ./Oscilloscope.h compressed -o $@

Constants.java: ./Oscilloscope.h
	ncg  -target=null -java-classname=Constants java ./Oscilloscope.h ITEM PACKET_SIZE NREADINGS DEFAULT_INTERVAL  AM_OSCILLOSCOPE -o $@

Oscilloscope.class: $(wildcard *.java) $(GEN)
	javac *.java

clean:
	rm -f *.class $(GEN)

veryclean: clean
	rm oscilloscope.jar
