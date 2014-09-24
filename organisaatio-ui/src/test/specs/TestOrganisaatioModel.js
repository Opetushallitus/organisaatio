describe("Testing OrganisaatioModel", function() {
    var $scope = null;
    var $rootScope;
    var $httpBackend;
    var mockOrganisaatio;
    var oid = "1.2.246.562.10.99999999999";
    var organisaatioResult = {

  "yhteishaunKoulukoodi" : "",
  "version" : 35,
  "metadata" : {
    "data" : {
      "VALINTAMENETTELY" : {
        "kielivalikoima_fi" : "<p><strong>Valintamen:</strong></p><p><strong><br /></strong><em>ettelyistä.</em></p>"
      },
      "VAPAA_AIKA" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus vapaa-ajan</strong> viettoon liittyvistä palveluista</p>"
      },
      "LINKED_IN" : {
        "2" : "http://linkedin.com/testiorg-li"
      },
      "TWITTER" : {
        "1" : "twitter.com/testiorg-tw"
      },
      "FACEBOOK" : {
        "0" : "facebook.com/testiorg-fb"
      },
      "RAHOITUS" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus opintotuest</strong>a, stipendeistä ja muista mahdollisuuksista rahoittaa elämisen kustannuksia.</p>"
      },
      "OPISKELIJALIIKUNTA" : {
        "kielivalikoima_fi" : "<p><strong>Koulutus opiskelijoille suunnatuista liikuntamahdollisuuksista</strong> oppilaitoksen yhteydessä.</p>"
      },
      "AIEMMIN_HANKITTU_OSAAMINEN" : {
        "kielivalikoima_fi" : "<p><strong>AHOT</strong></p><p><em>Osaamisesta</em></p><p>Toisesta</p><p>Kolmannesta</p><p>Neljännestä</p>"
      },
      "MUU" : {
        "4" : "muu.com/testiorg-mu"
      },
      "OPISKELIJALIIKKUVUUS" : {
        "kielivalikoima_fi" : "<p><strong>Opiskelijaliikkuvuus</strong></p><ul><li>Opiskelijaliikkuvuudesta.</li><li>bb</li><li>cc</li></ul>"
      },
      "OPPIMISYMPARISTO" : {
        "kielivalikoima_fi" : "<p>aaab</p><p>cccdbbbayyyt</p><p><em><strong><em><strong>Tietoa</strong></em></strong></em></p><p><em><strong><br /></strong></em></p>"
      },
      "KANSAINVALISET_KOULUTUSOHJELMAT" : {
        "kielivalikoima_fi" : "<p><strong>Kansainvälinen</strong></p><ol><li><strong>a</strong></li><li><strong>b</strong></li><li><strong>c</strong></li></ol><p> </p>"
      },
      "OPISKELIJARUOKAILU" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus olennaisista tiedoista</strong> siitä, miten ruokailu oppilaitoksessa tai toimipisteessä toimii.</p>"
      },
      "GOOGLE_PLUS" : {
        "3" : "google.com/testiorg-gl"
      },
      "TIETOA_ASUMISESTA" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus opiskelija-asuntoja</strong> välittävistä säätiöistä ja keskeisistä vuokra-asuntotarjoajista.</p>"
      },
      "ESTEETOMYYS" : {
        "kielivalikoima_fi" : "<p><em><strong>Esteettömyys:</strong></em></p><p><em>Esteettömyydestä.</em></p>"
      },
      "TERVEYDENHUOLTOPALVELUT" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus opiskelijaterveydenhuollon</strong> olennaisista tiedoista.</p>"
      },
      "VASTUUHENKILOT" : {
        "kielivalikoima_fi" : "<p><strong>Vastuuhenkilöt:</strong></p><p>Vastuuhenkilöistä.</p>"
      },
      "VAKUUTUKSET" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus koulutuksen</strong> järjestämän organisaation tarjoamista vakuutuksista.</p>"
      },
      "KIELIOPINNOT" : {
        "kielivalikoima_fi" : "<p><strong>kieliopinnot</strong></p><p><em>Kieliopinnoista.</em></p>"
      },
      "VUOSIKELLO" : {
        "kielivalikoima_fi" : "<p><strong>Vuosikello:</strong></p><p><strong><br /></strong><em>Vuosikellosta.</em></p>"
      },
      "YLEISKUVAUS" : {
        "kielivalikoima_fi" : "<p><strong><em>Testi yleiskuvaus:</em></strong></p><p><em>Yleiskuvausta.</em></p>"
      },
      "KUSTANNUKSET" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus olennaisista</strong> tiedoista esimerkiksi oppikirjojen tai opiskelun kannalta tärkeiden välineiden hankkimisesta tai yhteiskäytöstä.</p>"
      },
      "TYOHARJOITTELU" : {
        "kielivalikoima_fi" : "<p><strong>työharjoittelu</strong></p><p><strong><br /></strong></p><p><em><span style=\"text-decoration: underline;\">Työharjoittelusta</span></em><strong><br /></strong></p>"
      },
      "OPISKELIJA_JARJESTOT" : {
        "kielivalikoima_fi" : "<p><strong>Kuvaus organisaation</strong> yhteydessä toimivista opiskelijajärjestöistä.</p>"
      }
    },
    "nimi" : {
    },
    "yhteystiedot" : [ {
      "kieli" : "kielivalikoima_fi",
      "osoiteTyyppi" : "posti",
      "yhteystietoOid" : "1.2.246.562.5.55555555555",
      "postinumeroUri" : "posti_00002",
      "osoite" : "PL 99999",
      "postitoimipaikka" : "HELSINKI",
      "lng" : null,
      "maaUri" : null,
      "id" : "125417",
      "ytjPaivitysPvm" : "null",
      "lap" : null,
      "coordinateType" : null,
      "osavaltio" : null,
      "extraRivi" : null
    }, {
      "kieli" : "kielivalikoima_fi",
      "osoiteTyyppi" : "kaynti",
      "yhteystietoOid" : "1.2.246.562.5.44444444444",
      "postinumeroUri" : "posti_00002",
      "osoite" : "HT Testiosoite 1",
      "postitoimipaikka" : "HELSINKI",
      "lng" : null,
      "maaUri" : null,
      "id" : "125418",
      "ytjPaivitysPvm" : "null",
      "lap" : null,
      "coordinateType" : null,
      "osavaltio" : null,
      "extraRivi" : null
    }, {
      "kieli" : "kielivalikoima_fi",
      "tyyppi" : "faksi",
      "id" : "125419",
      "numero" : "01-7654321"
    }, {
      "kieli" : "kielivalikoima_fi",
      "tyyppi" : "puhelin",
      "id" : "125420",
      "numero" : "01-1234567"
    }, {
      "kieli" : "kielivalikoima_fi",
      "id" : "125421",
      "email" : "HT@testi.fi"
    }, {
      "kieli" : "kielivalikoima_fi",
      "id" : "125422",
      "www" : "www.testi.fi"
    } ],
    "hakutoimistoEctsNimi" : "Testi Koordinaattori",
    "hakutoimistoEctsPuhelin" : "040-1234567",
    "hakutoimistoEctsEmail" : "pomo@test.fi",
    "hakutoimistoEctsTehtavanimike" : "Pomo",
    "luontiPvm" : 1385039780718,
    "muokkausPvm" : 1385039780718,
    "hakutoimistonNimi" : {
      "kielivalikoima_en" : "HT Testitoimisto englanniksi",
      "kielivalikoima_sv" : "HT Testitoimisto ruotsiksi",
      "kielivalikoima_fi" : "HT Testitoimisto"
    },
    "kuvaEncoded" : "/9Q=="
  },
  "oid" : "1.2.246.562.10.99999999999",
  "parentOid" : "1.2.246.562.10.11111111111",
  "tyypit" : [ "Toimipiste" ],
  "nimi" : {
    "fi" : "Testi-Koulutus"
  },
  "alkuPvm" : "1999-11-26",
  "parentOidPath" : "|1.2.246.562.10.00000000001|1.2.246.562.10.22222222222|1.2.246.562.10.33333333333|",
  "yhteystietoArvos" : [ ],
  "vuosiluokat" : [ ],
  "puhelinnumero" : "02  1234 200",
  "toimipistekoodi" : "3009999",
  "kayntiosoite" : {
    "osoiteTyyppi" : "kaynti",
    "yhteystietoOid" : "1.2.246.562.5.11111111111",
    "postinumeroUri" : "posti_00002",
    "osoite" : "Testitie 113",
    "postitoimipaikka" : "TESTIKUNTA",
    "ytjPaivitysPvm" : "null",
    "lng" : null,
    "lap" : null,
    "coordinateType" : null,
    "osavaltio" : null,
    "extraRivi" : null,
    "maaUri" : null
  },
  "kieletUris" : [ "kielivalikoima_fi" ],
  "kotipaikkaUri" : "kunta_886",
  "kuvaus2" : {
  },
  "maaUri" : "maatjavaltiot1_fin",
  "postiosoite" : {
    "osoiteTyyppi" : "posti",
    "yhteystietoOid" : "1.2.246.562.5.22222222222",
    "postinumeroUri" : "posti_00002",
    "osoite" : "Testitie 113",
    "postitoimipaikka" : "TESTIKUNTA",
    "ytjPaivitysPvm" : "null",
    "lng" : null,
    "lap" : null,
    "coordinateType" : null,
    "osavaltio" : null,
    "extraRivi" : null,
    "maaUri" : null
  },
  "emailOsoite" : "testi@testi.fi"
};

var parentResult = {
  "version" : 35,
  "oid" : "1.2.246.562.10.11111111111",
  "parentOid" : "1.2.246.562.10.00000000001",
  "tyypit" : [ "Koulutustoimija" ],
  "nimi" : {
    "fi" : "Testi-Koulutustoimija"
  },
};

    var koodistoResult =
        [ {
  "koodiUri" : "maatjavaltiot1_fin",
  "resourceUri" : "http://koodistopalvelu.opintopolku.fi/maatjavaltiot1/koodi/maatjavaltiot1_fin",
  "versio" : 1,
  "koodisto" : {
    "koodistoUri" : "maatjavaltiot1",
    "organisaatioOid" : "1.2.246.562.10.00000000001",
    "koodistoVersios" : [ 1 ]
  },
  "koodiArvo" : "FIN",
  "paivitysPvm" : 1379313840005,
  "voimassaAlkuPvm" : "1990-01-01",
  "voimassaLoppuPvm" : null,
  "tila" : "LUONNOS",
  "metadata" : [ {
    "nimi" : "Finland",
    "kuvaus" : "Finland",
    "lyhytNimi" : "FI",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "SV"
  }, {
    "nimi" : "Suomi",
    "kuvaus" : "Suomi",
    "lyhytNimi" : "FI",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "FI"
  }, {
    "nimi" : "Finland",
    "kuvaus" : "Finland",
    "lyhytNimi" : "FI",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "EN"
  } ]
}, {
  "koodiUri" : "oppilaitostyyppi_11",
  "resourceUri" : "http://koodistopalvelu.opintopolku.fi/oppilaitostyyppi/koodi/oppilaitostyyppi_11",
  "versio" : 1,
  "koodisto" : {
    "koodistoUri" : "oppilaitostyyppi",
    "organisaatioOid" : "1.2.246.562.10.00000000001",
    "koodistoVersios" : [ 1 ]
  },
  "koodiArvo" : "11",
  "paivitysPvm" : 1379315176784,
  "voimassaAlkuPvm" : "1990-01-01",
  "voimassaLoppuPvm" : null,
  "tila" : "LUONNOS",
  "metadata" : [ {
    "nimi" : "Grundskolor",
    "kuvaus" : "Grundskolor",
    "lyhytNimi" : "Grundskolor",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "SV"
  }, {
    "nimi" : "Peruskoulut",
    "kuvaus" : "Peruskoulut",
    "lyhytNimi" : "Peruskoulut",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "FI"
  } ]
}, {
  "koodiUri" : "kunta_886",
  "resourceUri" : "http://koodistopalvelu.opintopolku.fi/kunta/koodi/kunta_743",
  "versio" : 1,
  "koodisto" : {
    "koodistoUri" : "kunta",
    "organisaatioOid" : "1.2.246.562.10.00000000001",
    "koodistoVersios" : [ 1 ]
  },
  "koodiArvo" : "886",
  "paivitysPvm" : 1379313816415,
  "voimassaAlkuPvm" : "1990-01-01",
  "voimassaLoppuPvm" : null,
  "tila" : "LUONNOS",
  "metadata" : [ {
    "nimi" : "Seinäjoki",
    "kuvaus" : "Seinäjoki",
    "lyhytNimi" : "Seinäjoki",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "FI"
  }, {
    "nimi" : "Seinäjoki",
    "kuvaus" : "Seinäjoki",
    "lyhytNimi" : "Seinäjoki",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "SV"
  } ]
}, {
  "koodiUri" : "posti_00002",
  "resourceUri" : "http://koodistopalvelu.opintopolku.fi/posti/koodi/posti_00002",
  "versio" : 1,
  "koodisto" : {
    "koodistoUri" : "posti",
    "organisaatioOid" : "1.2.246.562.10.00000000001",
    "koodistoVersios" : [ 1 ]
  },
  "koodiArvo" : "00002",
  "paivitysPvm" : 1379313898765,
  "voimassaAlkuPvm" : "1990-01-01",
  "voimassaLoppuPvm" : null,
  "tila" : "LUONNOS",
  "metadata" : [ {
    "nimi" : "LOUKO",
    "kuvaus" : "LOUKO",
    "lyhytNimi" : "LOUKO",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "FI"
  }, {
    "nimi" : "LOUKO",
    "kuvaus" : "LOUKO",
    "lyhytNimi" : "LOUKO",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "SV"
  } ]
}, {
  "koodiUri" : "kielivalikoima_fi",
  "resourceUri" : "http://koodistopalvelu.opintopolku.fi/kielivalikoima/koodi/kielivalikoima_fi",
  "versio" : 1,
  "koodisto" : {
    "koodistoUri" : "kielivalikoima",
    "organisaatioOid" : "1.2.246.562.10.00000000001",
    "koodistoVersios" : [ 1 ]
  },
  "koodiArvo" : "FI",
  "paivitysPvm" : 1379314391870,
  "voimassaAlkuPvm" : "1990-01-01",
  "voimassaLoppuPvm" : null,
  "tila" : "LUONNOS",
  "metadata" : [ {
    "nimi" : "Finnish",
    "kuvaus" : "Finnish",
    "lyhytNimi" : "Finnish",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "EN"
  }, {
    "nimi" : "suomi",
    "kuvaus" : "suomi",
    "lyhytNimi" : "suomi",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "FI"
  }, {
    "nimi" : "finska",
    "kuvaus" : "finska",
    "lyhytNimi" : "finska",
    "kayttoohje" : null,
    "kasite" : null,
    "sisaltaaMerkityksen" : null,
    "eiSisallaMerkitysta" : null,
    "huomioitavaKoodi" : null,
    "sisaltaaKoodiston" : null,
    "kieli" : "SV"
  } ]
} ]

    beforeEach(function() { module('organisaatio'); });

    beforeEach(function () {
        angular.mock.inject(function ($injector) {
            $httpBackend = $injector.get('$httpBackend');
            $rootScope = $injector.get('$rootScope');
            $scope = $rootScope.$new();
            mockOrganisaatio = $injector.get('Organisaatio');
        })
    });


    beforeEach(inject(function($rootScope, OrganisaatioModel) {
        $scope.model = OrganisaatioModel;

        // Olisi kivempi ladata data erillisestä tiedostosta, mutta ao. ei toimi:
        //jasmine.getJSONFixtures().fixturesPath='data';

        $httpBackend.expectGET(/cas\/me\?noCache=.*/).respond('{"lang": "fi"}');

        $httpBackend.expectGET(/organisaatio\/auth\?noCache=.*/).respond("");

        $httpBackend.expectGET(/cas\/myroles\?noCache=.*/).respond("");

        $httpBackend.expectGET(/v1\/localisation\/authorize\?noCache=.*/).respond(200, {});

        $httpBackend.expectGET(/organisaatio\/1.2.246.562.10.99999999999\?noCache=.*/).respond(organisaatioResult);

        $httpBackend.expectGET(/organisaatio\/1.2.246.562.10.11111111111\?noCache=.*/).respond(parentResult);

        $httpBackend.expectGET(/organisaatio\/v2\/hierarkia\/hae\?noCache=.*&aktiiviset=true&lakkautetut=true&oidRestrictionList=1.2.246.562.10.99999999999&suunnitellut=true/).respond("{}");

        $httpBackend.expectGET(/organisaatio\/v2\/1.2.246.562.10.99999999999\/paivittaja\?noCache=.*/).respond("{}");

        $httpBackend.expectGET(/organisaatio\/v2\/1.2.246.562.10.99999999999\/nimet\?noCache=.*/).respond("[]");

        $httpBackend.expectGET(/json\/searchKoodis?.*/).respond(koodistoResult);

        spyOn($scope.model, "refreshIfNeeded").andCallThrough();
    }));

    it('should have a OrganisaatioModel', function() {
        expect($scope.model).toBeDefined();
    });

    it('should GET organisaatio by OID', function() {
        $scope.model.refreshIfNeeded(oid);
        expect($scope.model.refreshIfNeeded).toHaveBeenCalled();
        $httpBackend.flush();
        expect($scope.model.organisaatio.emailOsoite).toEqual("testi@testi.fi");
    });

    it('should GET koodisto and map it', function() {
        $scope.model.refreshIfNeeded(oid);
        expect($scope.model.refreshIfNeeded).toHaveBeenCalled();
        $httpBackend.flush();
        expect($scope.model.uriLocalizedNames).toBeDefined();
        jasmine.log(JSON.stringify($scope.model.uriLocalizedNames));
        jasmine.log($scope.model.organisaatio.kotipaikkaUri);
        expect($scope.model.uriLocalizedNames.nimi).toEqual("Testi-Koulutus");
        expect($scope.model.uriLocalizedNames[$scope.model.organisaatio.kotipaikkaUri]).toEqual("Seinäjoki");
    });

});