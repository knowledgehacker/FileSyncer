LIB_DIR=lib

CLIENT_COMMON_SRC_DIR=src/main/sync/client
CLIENT_HTTP_SRC_DIR=src/main/sync/client/http
CLIENTCLASS=HttpSyncClient

.PHONY: client
client:
	javac -classpath $(LIB_DIR)/*:classes -sourcepath $(CLIENT_COMMON_SRC_DIR) -Xlint:unchecked -d classes ${CLIENT_COMMON_SRC_DIR}/*.java
	javac -classpath $(LIB_DIR)/*:classes -sourcepath $(CLIENT_HTTP_SRC_DIR) -Xlint:unchecked -d classes ${CLIENT_HTTP_SRC_DIR}/*.java
	jar cvmf $(CLIENTCLASS).mf $(CLIENTCLASS).jar -C classes .

.PHONY: runclient
runclient:
	java -jar $(CLIENTCLASS).jar . namenode:8099

.PHONY: clean
clean:
	@rm classes/* -rf
	@rm $(CLIENTCLASS).jar 
