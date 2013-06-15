#!/bin/sh
java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhost=$1 -Dport=$2 -Dwebroot=webapps -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.spdy.LEVEL=INFO -jar target/filesyncer-server-spdy-0.1-jar-with-dependencies.jar
