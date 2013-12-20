#!/bin/bash -e
export MAVEN_OPTS="-Dfile.encoding=UTF8 -XX:MaxPermSize=512m -Xms1024m -Xmx1024m -Xss16M"
cd ../organisaatio-ui
mvn install -Dmaven.test.skip=true
cd ../servers
mvn install jetty:run

