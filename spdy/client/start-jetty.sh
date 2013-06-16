#!/bin/sh
rm -rf ${HOME}/Sync/*
rm -rf ${HOME}/Sync/.log.txt

# Run the SPDY Client
#java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhostname=$1 -Dport=$2 -Dpush.dst.root.dir=${HOME}/Sync -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.LEVEL=INFO -jar target/filesyncer-client-spdy-0.1-jar-with-dependencies.jar

#SERVER_IP=192.168.221.68
SERVER_IP=192.168.0.100
SERVER_PORT=8443
java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhostname=${SERVER_IP} -Dport=${SERVER_PORT} -Dpush.dst.root.dir=${HOME}/Sync -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.LEVEL=INFO -jar target/filesyncer-client-spdy-0.1-jar-with-dependencies.jar
