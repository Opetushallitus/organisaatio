#!/bin/bash -e
export MAVEN_OPTS="-Dfile.encoding=UTF8 -Xms1024m -Xmx2048m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
"
mvn -Dmaven.test.skip=true jetty:run



