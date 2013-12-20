======================================================================
			 Organisaatio Service
======================================================================

Tietokannan alustaminen lokaaliin "organisaatio" kantaan:
----------------------------------------------------------------------
1. Luo kanta

  createdb organisaatio

2. Luo skeemat
  (Älä aja flyway:init - se näyttäisi varaavan migraation "000", joka on myös meillä käytössä)

  mvn -Dflyway.user=XXX -Dflyway.password=XXX -P flyway flyway:migrate

3. Solr

ks tarjonta-service/README.txt

Palvelun deployment
----------------------------------------------------------------------

1. tyhjä tomcat

2. alusta palvelu

./conf/Catalina/localhost/organisaatio-service.xml:

<Context displayName="organisaatio-service" 
         docBase="/Users/mlyly/work/OPH/svn/organisaatio/trunk/organisaatio-service/target/organisaatio-service-1.0-SNAPSHOT"
         reloadable="true"/>

3. muuta default portit ja luo datasourcet

./conf/context.xml:

    <Resource auth="Container" 
              name="jdbc/organisaatio" 
              type="javax.sql.DataSource" 
              username="oph" 
              password="oph" 
              driverClassName="org.postgresql.Driver" 
              url="jdbc:postgresql://localhost:5432/organisaatio" 
              maxActive="150"
              maxIdle="4"/>


./conf/server.xml:

Ports:

<Server port="8105" shutdown="SHUTDOWN">

    <Connector port="8181" protocol="HTTP/1.1"
               connectionTimeout="20000"
               redirectPort="8443" />

    <Connector port="8109" protocol="AJP/1.3" redirectPort="8443" />










Lataa propertyjä seuraavista paikoista:

  classpath:organisaatio-service.properties
  ~/oph-configuration/common.properties
  ~/oph-configuration/organisaatio-service.properties

Propertyt:

----------------------------------------------------------------------
organisaatio.solr.url=http://127.0.0.1:8181/solr/organisaatiot
#
# dao-context.xml
#
# jpa.schemaUpdate=true
# jpa.showSql=true

#
# service-context.xml
#
# root.organisaatio.oid=1.2.246.562.10.00000000001

#
# ws-context.xml
#
# activemq.brokerurl=xxx
# activeMq.targetDestination.organisaatio=xxx


# activeMq.targetDestination.learningopportunityprovider.public=xxx


----------------------------------------------------------------------
