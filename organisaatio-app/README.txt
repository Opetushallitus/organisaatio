======================================================================
			Organisaatio APP (ui)
======================================================================

PROPERTIES
##################################################

Projektin propertyt ja esimerkkiarvoja:

Spring conffis hakee property tiedostoista:

  ~/oph-configuration/common.properties
  ~/oph-configuration/organisaatio-app.properties


Spring importtaa myös:

  ~/oph-configuration/cache-context.xml


Esimerkkisisältöä: (wanhasta konffiksesta)
----------------------------------------------------------------------
#
# Koodisto URIs (käyttö: application-context.xml)
#
organisaatio-app.koodisto-uris.yritys-uri=http://yritys
organisaatio-app.koodisto-uris.kieli-uri=KIELI
organisaatio-app.koodisto-uris.maa-uri=maat kaksimerkkisell\u00e4 arvolla
organisaatio-app.koodisto-uris.postinumero-uri=posti
organisaatio-app.koodisto-uris.kotipaikka-uri=KUNTA
organisaatio-app.koodisto-uris.oppilaitostyyppi-uri=Oppilaitostyyppi

# root.organisaatio.oid=1.2.246.562.10.00000000001
# organisaatio.webservice.url=http://NO_ORGANISAATIO_WEBSERVICE_URL_CONFIGURED
# oid.webservice.url=http://NO_OID_WEBSERVICE_URL_CONFIGURED
# koodi.webservice.url=http://NO_KOODI_WEBSERVICE_URL_CONFIGURED
# koodisto.webservice.url=http://NO_KOODISTO_WEBSERVICE_URL_CONFIGURED

#
# YTJ
#
#rajapinnat.ytj.asiakastunnus:YTJ_ASIAKASTUNNUS_UNKNOWN
#rajapinnat.ytj.avain:YTJ_SALAINEN_AVAIN_UNKNOWN

#
# Organisaatio APP
#
# debug.ui.active=true
# debug.role.r=true
# debug.role.ru=true
# debug.role.crud=true
----------------------------------------------------------------------
