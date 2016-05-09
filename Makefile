.PHONY: clean tar
JC = javac
JFLAGS = 
TAR = tar
TFLAGS = czf
SOURCE = Makefile Speedup.java GUI.java TumorAutomaton.java Time.java
	
all: Speedup.class GUI.class TumorAutomaton.class Time.class
	
%.class : %.java
	$(JC) $(JFLAGS) $<

clean:
	@rm -f *.class *.dat

tar:
	@$(TAR) $(TFLAGS) tar/vX.tar.gz $(SOURCE) *.png
