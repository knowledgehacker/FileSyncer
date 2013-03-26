LIB_DIR=webapps/WEB-INF/lib

SERVER_COMMON_SRC_DIR=src/main/sync/server
SERVER_HTTP_SRC_DIR=src/main/sync/server/http
SERVER_CLASSES_DIR=webapps/WEB-INF/classes
SERVERCLASS=HttpSyncServer

REPOS_DIR=webapps/repos/minglin

.PHONY: server
server:
	javac -classpath $(LIB_DIR)/*:$(SERVER_CLASSES_DIR) -Xlint:unchecked -d $(SERVER_CLASSES_DIR) ${SERVER_COMMON_SRC_DIR}/*.java
	javac -classpath $(LIB_DIR)/*:$(SERVER_CLASSES_DIR) -Xlint:unchecked -d $(SERVER_CLASSES_DIR) ${SERVER_HTTP_SRC_DIR}/*.java
	#jar cvmf $(SERVERCLASS).mf $(SERVERCLASS).jar -C $(SERVER_CLASSES_DIR) .

.PHONY: runserver
runserver:
	#java -jar $(SERVERCLASS).jar
	java -classpath $(LIB_DIR)/*:$(SERVER_CLASSES_DIR) main.sync.server.http.$(SERVERCLASS)

.PHONY: clean
clean:
	@rm $(SERVER_CLASSES_DIR)/* -rf
	#@rm $(SERVERCLASS).jar 
	@rm $(REPOS_DIR)/* -rf
