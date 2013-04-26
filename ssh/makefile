JSCH_DIR=lib
TEST_SRC_DIR=src/test/sync/ssh
TESTMAINCLASS=TestFileSyncer

MAIN_SRC_DIR=src/main/sync/ssh
MAINCLASS=FileSyncer

.PHONY: test
test:
	javac -classpath $(JSCH_DIR)/*:classes -sourcepath $(TEST_SRC_DIR) -Xlint:unchecked -d classes $(MAIN_SRC_DIR)/*.java $(TEST_SRC_DIR)/*.java
	jar cvmf $(TESTMAINCLASS).mf $(TESTMAINCLASS).jar -C classes .

.PHONY: runtest
runtest:
	java -jar $(TESTMAINCLASS).jar

.PHONY: main
main:
	javac -classpath $(JSCH_DIR)/*:classes -sourcepath $(MAIN_SRC_DIR) -Xlint:unchecked -d classes $(MAIN_SRC_DIR)/*.java
	jar cvmf $(MAINCLASS).mf $(MAINCLASS).jar -C classes .

.PHONY: runmain
runmain:
	java -jar $(MAINCLASS).jar . minglin@datanode

.PHONY: clean
clean:
	@rm classes/* -rf
	@rm $(MAINCLASS).jar $(TESTMAINCLASS).jar
