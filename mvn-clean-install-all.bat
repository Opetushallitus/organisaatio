@echo off
echo ####### organisaatio-api #######
pushd organisaatio-api
call mvn -q clean install -DskipTests
popd
echo ####### organisaatio-service #######
pushd organisaatio-service
call mvn -q clean install -DskipTests
popd
echo ####### organisaatio-solr-client #######
pushd organisaatio-solr-client
call mvn -q clean install -DskipTests
popd
echo ####### organisaatio-ui #######
pushd organisaatio-ui
call mvn -q clean install -DskipTests
popd
