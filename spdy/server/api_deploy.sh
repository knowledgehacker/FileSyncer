#!/bin/sh
CP=cp

# Note we must delete the spdy-api stuff in ${M2_DIR}, otherwise maven will fetch spdy-api stuff from there by default
M2_DIR=${HOME}/.m2/repository/lib
rm -rf ${M2_DIR}/spdy-api/*
rmdir ${M2_DIR}/spdy-api

rm -rf ./lib/spdy-api/*
rmdir ./lib/spdy-api

SPDY_API_DIR=${HOME}/workspace/git/spdy-api-extension/spdy-api
SPDY_API_VERSION=0.1
${CP} ${SPDY_API_DIR}/target/spdy-api-${SPDY_API_VERSION}.jar ./lib/

PROJECT_DIR=file:///Users/minglin/workspace/git/FileSyncer/spdy/server
#PROJECT_DIR=${PWD}
mvn deploy:deploy-file -Durl=${PROJECT_DIR} -Dfile=lib/spdy-api-${SPDY_API_VERSION}.jar -DgroupId=lib -DartifactId=spdy-api -Dpackaging=jar -Dversion=${SPDY_API_VERSION}
