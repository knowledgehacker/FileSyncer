#!/bin/sh
rm -rf webapps/repos/ml/*
rmdir webapps/repos/ml

#java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhostname=$1 -Dport=$2 -Dwebroot=webapps -Dpush.src.root.dir=${PWD}/webapps/repos/ml -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.spdy.LEVEL=INFO -jar target/filesyncer-server-spdy-0.1-jar-with-dependencies.jar

#SERVER_IP=192.168.221.68
SERVER_IP=192.168.0.100
SERVER_PORT=8443
java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhostname=${SERVER_IP} -Dport=${SERVER_PORT} -Dwebroot=webapps -Dpush.src.root.dir=${PWD}/webapps/repos/ml -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.spdy.LEVEL=INFO -jar target/filesyncer-server-spdy-0.1-jar-with-dependencies.jar
