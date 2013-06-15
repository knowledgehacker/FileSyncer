#!/bin/sh
# Run the SPDY Client
java -Xbootclasspath/p:npn-boot-1.1.5.v20130313.jar -Dhost=$1 -Dport=$2 -Dorg.eclipse.jetty.util.log.class=org.eclipse.jetty.util.log.StdErrLog -Dorg.eclipse.jetty.LEVEL=INFO -jar target/filesyncer-client-spdy-0.1-jar-with-dependencies.jar
