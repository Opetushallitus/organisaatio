/*
 Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus

 This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 soon as they will be approved by the European Commission - subsequent versions
 of the EUPL (the "Licence");

 You may not use this work except in compliance with the Licence.
 You may obtain a copy of the Licence at: http://www.osor.eu/eupl/

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 European Union Public Licence for more details.
 */

var app = angular.module('organisaatio');

app.factory('OrganisaatioModel', function($filter, $log, $timeout, $location,
                                          $q, $cookieStore, $injector,
                                          Organisaatio, Organisaatiot,
                                          OrganisaatioHistoria,
                                          KoodistoKoodi, KoodistoClient,
                                          UusiOrganisaatio, YTJYritysTiedot,
                                          Alert, HenkiloVirkailijat,
                                          Henkilo, HenkiloKayttooikeus,
                                          Yhteystietojentyyppi,
                                          Paivittaja, NimiHistoriaModel,
                                          LocalisationService, SomeKoodisto,
                                          RefreshKoodisto, RefreshOrganisaatio,
                                          LisaYhteystiedot) {

    $log = $log.getInstance("OrganisaatioModel");
    var loadingService = $injector.get('LoadingService');
    
    function initOrganisaatioModelData() {
        this.organisaatio = {};

        // Koodistodata organisaation muokkausta varten
        this.koodisto = {
            oid: 0,
            organisaatiotyypit: [],
            ophOrganisaatiot: [],
            oppilaitostyypit: [],
            kotipaikat: [],
            maat: [],
            //kielet: [],
            isokielet: [],
            opetuskielet: [],
            vuosiluokat: [],
            kieliplaceholder: $filter('i18n')("lisaakieli"),
            postinumerot: [],
            nimetFI: {},
            nimetSV: {},
            yhteystietoTyypit: {},
            jarjestamismuoto: [],
            kasvatusopillinenJarjestelma: [],
            toiminnallinenPainotus: [],
            varhaiskasvatuksenToimintamuodot: [],
            varhaiskasvatuksenToimintamuodotPlaceholder: $filter('i18n')("Organisaationmuokkaus.lisaaVarhaiskasvatuksenToimintamuodot"),
            kieli: []
        };

        this.kaikkiOrganisaatiotyypit = [];

        this.henkilot = {
            virkailijat: []
        };

        // Muokattavat yhteystietotyypit indeksoituna organisaatio- ja oppilaitostyyppien mukaan
        this.yhteystietojentyyppi = {};

        // Koodin lokalisoitu nimi, avaimena uri
        this.uriLocalizedNames = {};

        // Koko koodi, avaimena uri
        this.uriKoodit = {};

        // Koodin nimi eri kielillä, avaimena kieli.uri
        this.uriLangNames = {};
        this.uriLangNames["FI"] = {};
        this.uriLangNames["SV"] = {};

        // Aliorganisaatioiden nimet listana
        this.aliorganisaatiot = [];

        //this.organisaatio.metadata.ectstiedot = {};

        // Aliorganisaatiohaun tulos voimassaolonmuokkaus dialogia varten.
        this.aliorganisaatioHaunTulos = {};
        this.hasAliorganisaatios = false;
        this.aliorganisaatioTiedotHaettu = false;
        this.muutettaviaAliorganisaatioita = 0;

        // Metadatan yhteystiedot mäpättynä tyypin perusteella
        this.mdyhteystiedot = {
            'kieli_fi#1': {},
            'kieli_sv#1': {},
            'kieli_en#1': {}
        };

        this.ectstiedot = {
            'kieli_fi#1': {},
            'kieli_sv#1': {},
            'kieli_en#1': {}
        };

        // Päätason yhteystiedot mäpättynä tyypin perusteella
        this.yhteystiedot = {
            'kieli_fi#1': {},
            'kieli_sv#1': {},
            'kieli_en#1': {}
        };

        // Lisäyhteystiedot mäpättynä: oid => tyypin nimi molemmilla kielillä
        this.lisayhteystiedot = {};

        // yhteystietojen ja hakijapalveluiden yhteystietojen osoitemuoto
        this.osoitemuoto = {
            yt: {
                'kieli_fi#1': 'suomalainen',
                'kieli_sv#1': 'suomalainen',
                'kieli_en#1': 'kansainvalinen'
            },
            hp: {
                'kieli_fi#1': 'suomalainen',
                'kieli_sv#1': 'suomalainen',
                'kieli_en#1': 'kansainvalinen'
            },
            ytsamaosoite: {},
            hpsamaosoite: {}
        };

        this.url = {
            autofill: 'http://',
            validator: '^(https?)(:\/\/)([-a-zA-Z0-9+&@#\/%ÅåÄäÖö?=~_|!:,.;]*[-a-zA-Z0-9+&@#\/%ÅåÄäÖö=~_|])'
        };

        // Sosiaalinen media
        this.sometypes = SomeKoodisto.sometyypit;
        this.some = SomeKoodisto.some;
        this.someurls = SomeKoodisto.someurls;

        this.kttypes = ['YLEISKUVAUS', 'ESTEETOMYYS', 'OPPIMISYMPARISTO',
            'VUOSIKELLO', 'VASTUUHENKILOT', 'VALINTAMENETTELY',
            'AIEMMIN_HANKITTU_OSAAMINEN', 'KIELIOPINNOT',
            'TYOHARJOITTELU', 'OPISKELIJALIIKKUVUUS',
            'KANSAINVALISET_KOULUTUSOHJELMAT'];
        this.oetypes = ['KUSTANNUKSET', 'TIETOA_ASUMISESTA', 'RAHOITUS',
            'OPISKELIJARUOKAILU', 'TERVEYDENHUOLTOPALVELUT',
            'VAKUUTUKSET', 'OPISKELIJALIIKUNTA', 'VAPAA_AIKA',
            'OPISKELIJA_JARJESTOT'];
        this.ectstypes = ['NIMI', 'TEHTAVANIMIKE', 'PUHELINNUMERO', 'SAHKOPOSTIOSOITE'];

        // Monikielisen tekstin valinta
        // kt: koulutustarjoajatiedot
        // hp: hakijapalvelut
        // oe: opiskelijan edut
        // sm: sosiaalinen media
        // ects: ECTS-koordinaattori
        this.mkSections = {
            kt: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.kttypes,
                fields: []
            },
            hp: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: [],
                fields: ['hakutoimistonNimi']
            },
            oe: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.oetypes,
                fields: []
            },
            sm: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.sometypes,
                fields: []
            },
            ects: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.ectstypes,
                fields: []
            }
        };

        // Yhteystietojen kielivälilehdet
        this.yttabs = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1'];

        // Lisäyhteystietojen kielivälilehdet
        this.lttabs = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1'];

        // YTJ rajapinnan kautta saadut yrityksen tiedot
        this.ytjTiedot = {};

        // Organisaation tuleva nimi (ajastettu nimenmuutos)
        this.organisaationTulevaNimi = {};
        this.organisaationTulevaNimi.nimi = {};

        // Organisaation tila
        this.organisaationTila = '';

        this.OPHOid = "1.2.246.562.10.00000000001";

        this.savestatus = LocalisationService.t("Organisaationmuokkaus.tietojaeitallennettu");

        this.nameFormat = false;

        // Invalidit yhteystiedot kielen mukaan
        this.ytinvalid = [];
    }

    var model = new function() {

        initOrganisaatioModelData.call(this);

        KoodistoClient.koodistoOrganisaatiotyypit.get({}, function (organisaatioTyypit) {
            model.kaikkiOrganisaatiotyypit = organisaatioTyypit;
        });

        // TODO: Add also parent needed possibly for moving organisaatio

        this.getDecodedLocalizedValue= function(res, prefix, suffix, create, language) {
            return RefreshOrganisaatio.getDecodedLocalizedValue(res, prefix, suffix, create, language);
        };

        this.getLocalizedValueWithProperty = function (property) {
            return function(entry) {
                return RefreshOrganisaatio.getLocalizedValue(entry[property]);
            };
        };

        this.getLocalizedValue = function (entry) {
            if (LocalisationService.getLocale() in entry &&
                    entry[LocalisationService.getLocale()]) {
                return entry[LocalisationService.getLocale()];
            }

            // Ei löytynyt nimeä käyttäjän kielellä, kokeillaan muut vaihtoehdot
            if ('fi' in entry && entry.fi) {
                return entry.fi;
            }
            if ('sv' in entry && entry.sv) {
                return entry.sv;
            }
            if ('en' in entry && entry.en) {
                return entry.en;
            }
            return "--";
        };

        this.localiseJarjestamismuoto = function (koodiUri) {
            return this.localiseKoodiUri(model.koodisto.jarjestamismuoto, koodiUri);
        };

        this.localiseKasvatusopillinenJarjestelma = function (koodiUri) {
            return this.localiseKoodiUri(model.koodisto.kasvatusopillinenJarjestelma, koodiUri);
        };

        this.localiseToiminnallinenPainotus = function (koodiUri) {
            return this.localiseKoodiUri(model.koodisto.toiminnallinenPainotus, koodiUri);
        };

        this.localiseVarhaiskasvatuksenToimintamuodot = function (koodiUri) {
            return this.localiseKoodiUri(model.koodisto.varhaiskasvatuksenToimintamuodot, koodiUri);
        };

        this.localiseKielipainotus = function (koodiUri) {
            return this.localiseKoodiUri(model.koodisto.kieli, koodiUri);
        };

        this.localiseKoodiUri = function (koodisto, koodiUri) {
            var matchingKoodi = koodisto.filter(function (kieliKoodi) {
                return kieliKoodi.uri === koodiUri;
            })[0];
            return matchingKoodi && matchingKoodi.nimi;
        };

        this.setNimet = function() {
            $log.log('setNimet()');
            var nimiHistoriaModel = NimiHistoriaModel;

            var nimi = nimiHistoriaModel.getNimi();
            var tulevaNimi = nimiHistoriaModel.getAjastettuNimi();
            model.organisaatio.nimi = nimi.nimi;

            // Jos tuleva nimi on sama kuin organisaation validi nimi
            // niin kyseessa on uusi tulevaisuuden organisaatio
            if (tulevaNimi !== null && angular.equals(nimi, tulevaNimi) === false) {
                model.organisaationTulevaNimi = tulevaNimi;
            }
            else {
                // Tyhjennetään tuleva nimi
                model.organisaationTulevaNimi = {};
                model.organisaationTulevaNimi.nimi = {};
            }

            model.organisaatio.nimet = nimiHistoriaModel.getNimihistoria();
        };

        this.refresh = function(organisaatio) {
            $log.info("refresh(): " + organisaatio.oid);
            RefreshOrganisaatio.refresh(organisaatio, model);
        };

        this.refreshIfNeeded = function(oid) {
            $log.info("refreshIfNeeded(): " + oid);
            if (oid) {
                if (model.keepsavestatus) {
                    model.keepsavestatus = false;
                } else {
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tietojaeitallennettu");
                }
                Organisaatio.get({oid: oid}, function(result) {
                    RefreshOrganisaatio.refresh(result, model);
                },
                // Error case
                function(response) {
                    // Organisaatiohaku ei onnistunut
                    RefreshOrganisaatio.showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response, model, loadingService);
                });
            }
        };

        this.createOrganisaatio = function(parentoid, yritystiedot) {
            // tyhjennetään mahdolliset vanhat ytj tiedot
            if (typeof yritystiedot !== "undefined") {
                model.ytjTiedot = yritystiedot;
            } else {
                model.ytjTiedot = {};
            }

            // tyhjennetään mahdolliset vanhat kentät
            model.organisaatio = {};
            model.organisaatio.tyypit = [];
            model.organisaatio.nimi = null;
            model.organisaatio.nimet = [];
            model.originalNimet = model.organisaatio.nimet;
            model.organisaatio.nimi = {};
            model.organisaatio.kieletUris = [];
            model.organisaatio.yhteystiedot = [];
            model.organisaatio.vuosiluokat = [];
            model.organisaatio.lisatiedot = [];
            model.yhteystiedot = {};
            model.mdyhteystiedot = {};
            model.organisaationTulevaNimi = {};
            model.organisaationTulevaNimi.nimi = {};
            model.muutettaviaAliorganisaatioita = 0;

            // tyhjennetään nimihistoriamalli
            var nimiHistoriaModel = NimiHistoriaModel;
            nimiHistoriaModel.clear();

            // oletusarvoisesti luodaan organisaatio Suomeen
            model.organisaatio.maaUri = "maatjavaltiot1_fin";

            Organisaatio.get({oid: parentoid}, function(result) {
                model.uriLocalizedNames["parentnimi"] = RefreshOrganisaatio.getDecodedLocalizedValue(result.nimi, "", "", false);
                RefreshOrganisaatio.refreshParent(result, model);

                RefreshKoodisto(null, model);
                RefreshOrganisaatio.refreshHenkilo(model);
                model.organisaatio.parentOid = parentoid;
                RefreshOrganisaatio.finishModel(model);
                RefreshOrganisaatio.refreshMetadata(model.organisaatio, model);

                // Jos yritystiedot on mukana --> täytetään tiedot
                if (typeof yritystiedot !== "undefined") {
                    model.fillYritysTiedot(yritystiedot);
                }

            }, function(response) {
                // postinumeroita ei löytynyt
                RefreshOrganisaatio.showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response, model, loadingService);
            });
        };

        this.createOrganisaatioYTunnuksella = function(parentoid, ytunnus) {
            YTJYritysTiedot.get({'ytunnus': ytunnus}, function(result) {
                model.createOrganisaatio(parentoid, result);
            }, function(response) {
                // yritystietoa ei löytynyt
                RefreshOrganisaatio.showAndLogError("Organisaationtarkastelu.ytunnushakuvirhe", response, model, loadingService);
                model.createOrganisaatio(parentoid);
            });
        };

        this.updateOrganisaatioYTunnuksella = function(ytunnus, orgForm) {
            YTJYritysTiedot.get({'ytunnus': ytunnus}, function(result) {
                model.ytjTiedot = result;

                // Täytetään yritystiedot, niiltä osin kun koodistosta saatuja tietoja ei tarvitse käyttää
                model.fillYritysTiedot(result);

                // Täytetään yritystiedot, koodiston tietoja käyttävältä osalta
                model.addYtjLang();
                model.addYtjOsoite();
                model.addYtjKotipaikka();
                orgForm.$setDirty();
            }, function(response) {
                // yritystietoa ei löytynyt
                RefreshOrganisaatio.showAndLogError("Organisaationtarkastelu.ytunnushakuvirhe", response, model, loadingService);
                model.createOrganisaatio(model.parentoid);
            });
        };

        this.fillYritysTiedot = function(yritystiedot) {
            $log.debug('fillYritysTiedot(): ', yritystiedot);
            var nimi = {
                "nimi" : {
                },
                "alkuPvm" : ""
            };

            // Tarkistetaan "kenttien" olemassaolo, sillä yritystiedot voidaan täyttää myöhemminkin
            if (yritystiedot.nimi) {
                nimi.nimi.fi = yritystiedot.nimi;
            }
            if (yritystiedot.svNimi) {
                nimi.nimi.sv = yritystiedot.svNimi;
            }
            if (yritystiedot.ytunnus) {
                model.organisaatio.ytunnus = yritystiedot.ytunnus;
            }
            if (yritystiedot.yritysmuoto) {
                model.organisaatio.yritysmuoto = yritystiedot.yritysmuoto;
            }
            // yrityksenKieli, sitten kun koodiston kielet on saatu
            // postiOsoite, sitten kun koodiston postinumerot on saatu
            // kayntiOsoite, sitten kun koodiston postinumerot on saatu
            if (yritystiedot.sahkoposti) {
                model.yhteystiedot['kieli_fi#1'].email.email = yritystiedot.sahkoposti;
            }
            if (yritystiedot.www) {
                model.yhteystiedot['kieli_fi#1'].www.www = yritystiedot.www;
            }
            if (yritystiedot.puhelin) {
                model.yhteystiedot['kieli_fi#1'].puhelin.numero = yritystiedot.puhelin;
            }
            // kotipaikka / kotipaikkaKoodi, sitten kun koodiston kotipaikat on saatu
            if (yritystiedot.aloitusPvm) {
                model.organisaatio.alkuPvm = moment(yritystiedot.aloitusPvm, 'DD.MM.YYYY');
            }

            // YTunnuksella luotu organisaatio on oletusarvoisesti koulutustoimija
            // Ei kuitenkaan poisteta "Koulutustoimija" tyyppiä, jos se on jo asetettu
            var organisaatiotyyppi = "organisaatiotyyppi_01"; // Koulutustoimija
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                this.toggleCheckOrganisaatio(organisaatiotyyppi);
            }

            // asetetaan päivitys timestamp
            model.organisaatio.ytjpaivitysPvm = model.formatDate(new Date());
            nimi.alkuPvm = model.organisaatio.ytjpaivitysPvm;

            // Lisätään nimi nimihistoriaan, jos se eroaa nykyisestä nimestä
            var nimiHistoriaModel = NimiHistoriaModel;

            // Uuden organisaation tapauksess ei ole nimihistoriaa
            if (nimiHistoriaModel.getNimi() === null) {
                nimiHistoriaModel.getNimihistoria().push(nimi);
                this.setNimet();
            }
            else if (angular.equals(nimiHistoriaModel.getNimi().nimi, nimi.nimi) === false) {
                if (nimiHistoriaModel.getNimi().alkuPvm === nimi.alkuPvm) {
                    nimiHistoriaModel.getNimi().nimi = nimi.nimi;
                }
                else {
                    nimiHistoriaModel.getNimihistoria().push(nimi);
                }
                this.setNimet();
            }

        };

        // Konvertoi päivämäärän rajapinnan hyväksymään muotoon yyyy-mm-dd
        this.formatDate = function(dateToFormat) {
            // TODO replace this with moment()
            if (dateToFormat) {
                var d = new Date(dateToFormat);
                var curr_date = 100 + d.getDate();
                var curr_month = 100 + d.getMonth() + 1;
                var curr_year = d.getFullYear();
                return curr_year + "-" + curr_month.toString().slice(1) + "-" + curr_date.toString().slice(1);
            }
            return;
        };

        this.setNimi = function(nimi) {
            model.organisaatio.nimi = nimi;
        };

        this.persistOrganisaatio = function(orgForm) {
            var deferred = $q.defer();
            RefreshOrganisaatio.formatDates(model);
            RefreshOrganisaatio.selectAddressType(false, model);
            RefreshOrganisaatio.selectAddressType(true, model);
            RefreshOrganisaatio.checkLisayhteystiedot(model);
            if (model.mode==="edit") {
                Organisaatio.update(model.organisaatio, function(result) {
                    //console.log(result);
                    if (orgForm) {
                        orgForm.$setPristine();
                    }
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                    Alert.closeAlert(model.alert);
                    if (result.status==="WARNING") {
                        model.alert = Alert.add("warn", $filter('i18n')(result.info), false);
                    }
                    deferred.resolve(result.organisaatio);
                }, function(response) {
                    RefreshOrganisaatio.showAndLogError("Organisaationmuokkaus.tallennusvirhe", response, model, loadingService);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                    deferred.reject();
                });
            } else {
                // Asetetaan organisaation nimen alkupäiväksi organisaation alkupäivä
                model.organisaatio.nimet[0].alkuPvm = model.organisaatio.alkuPvm;

                UusiOrganisaatio.create(model.organisaatio, function(result) {
                    //console.log(result);
                    if (orgForm) {
                        orgForm.$setPristine();
                    }
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                    model.keepsavestatus = true;
                    Alert.closeAlert(model.alert);
                    $location.path($location.path().split(model.organisaatio.parentOid)[0] + result.organisaatio.oid + "/edit");
                    if (result.status==="WARNING") {
                        model.alert = Alert.add("warn", $filter('i18n')(result.info), false);
                    }
                    deferred.resolve();
                }, function(response) {
                    RefreshOrganisaatio.showAndLogError("Organisaationmuokkaus.tallennusvirhe", response, model, loadingService);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                    deferred.reject();
                });
            }
            return deferred.promise;
        };

        this.checkOrganisaatio = function() {
            var organisaatio = JSON.parse(JSON.stringify(model.organisaatio));
            organisaatio.tarkastusPvm = new Date().getTime();

            var deferred = $q.defer();
            Organisaatio.update(organisaatio, function(result) {
                model.organisaatio = result.organisaatio;
                deferred.resolve(result.organisaatio);
            }, function(response) {
                RefreshOrganisaatio.showAndLogError("Organisaationmuokkaus.tallennusvirhe", response, model, loadingService);
                deferred.reject(response);
            });
            return deferred.promise;
        };

        this.toggleCheckOrganisaatio = function(organisaatiotyyppi) {
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                model.organisaatio.tyypit.push(organisaatiotyyppi);
            } else {
                model.organisaatio.tyypit.splice(model.organisaatio.tyypit.indexOf(organisaatiotyyppi), 1);
            }
            model.organisaatio.yhteystietoArvos = [];
            model.lisayhteystiedot = {};
            LisaYhteystiedot.updateLisayhteystiedot(model);
        };

        this.selectOppilaitosTyyppi = function() {
            model.organisaatio.yhteystietoArvos = [];
            model.lisayhteystiedot = {};
            LisaYhteystiedot.updateLisayhteystiedot(model);
        };

        this.toggleCheckVuosiluokka = function(vuosiluokka) {
            if (model.organisaatio.vuosiluokat.indexOf(vuosiluokka) === -1) {
                model.organisaatio.vuosiluokat.push(vuosiluokka);
            } else {
                model.organisaatio.vuosiluokat.splice(model.organisaatio.vuosiluokat.indexOf(vuosiluokka), 1);
            }
        };

        this.toggleLisatieto = function (lisatieto) {
            var index = model.organisaatio.lisatiedot.indexOf(lisatieto);
            if (index === -1) {
                model.organisaatio.lisatiedot.push(lisatieto);
            }
            else {
                model.organisaatio.lisatiedot.splice(index);
            }
        };

        this.addLang = function(ytform) {
            if(angular.isDefined(model.organisaatio.kieletUris)) {
                if (model.organisaatio.kieletUris.indexOf(model.koodisto.kieliplaceholder) === -1) {
                    if (model.koodisto.kieliplaceholder && (model.koodisto.kieliplaceholder !== $filter('i18n')("lisaakieli"))) {
                        model.organisaatio.kieletUris.push(model.koodisto.kieliplaceholder);

                        model.updateYhteystiedotValidity(ytform);
                    }
                }
            }
            else {
                $log.warn('addLang :: kieletUris not defined');
            }
            model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
        };

        this.addYtjLang = function() {
            // Tämä tehdään vasta kun koodiston kielet on saatu ja ytj tiedot on olemassa
            if ('yrityksenKieli' in model.ytjTiedot === false) {
                return;
            }
            var getKieliUri = function(kieli) {
                if (!kieli) {
                    $log.debug("fillYritysTiedot.getKieliUri(), tyhjä kieli");
                    return;
                }

                // yritystietojen mukana kieli tulee "suomeksi" --> muutetaan se kieliArvoksi
                // Koodisto "oppilaitoksenopetuskieli" arvot numeroita
                var kieliArvo = null;
                switch (kieli.trim().toLowerCase()) {
                    case "suomi":
                        kieliArvo = "1";
                        break;
                    case "ruotsi":
                        kieliArvo = "2";
                        break;
                    case "englanti":
                        kieliArvo = "4";
                        break;
                    default:
                        $log.warn("Failed to get kieli uri for language: " + kieli);
                        return;
                }

                // etsitään koodiston kielistä kieliArvoa ja palautetaan vastaava uri jos löytyy
                var found = $filter('filter')(model.koodisto.opetuskielet, {arvo: kieliArvo}, true);
                if (found.length) {
                    return found[0].uri;
                }
                else {
                    $log.warn("Failed to found uri for kieli: " + kieli + " arvo: " + kieliArvo);
                }
            };
            var kieliUri = getKieliUri(model.ytjTiedot.yrityksenKieli);
            if (kieliUri) {
                if(angular.isDefined(model.organisaatio.kieletUris)) {
                    // lisätään kieli, jos organisaatiolla ei vielä ole YTJ:stä tullutta kieltä
                    if (model.organisaatio.kieletUris.indexOf(kieliUri) === -1) {
                        model.organisaatio.kieletUris.push(kieliUri);
                    }
                }
                else{
                    $log.warn('addYtjLang :: model.organisaatio.kieletUris undefined.');
                }
            }

        };

        this.removeLang = function(lang, ytform) {
            if(angular.isDefined(model.organisaatio.kieletUris)) {
                var index = model.organisaatio.kieletUris.indexOf(lang);
                if (index !== -1) {
                    model.organisaatio.kieletUris.splice(index, 1);
                }
                model.updateYhteystiedotValidity(ytform);
            }
            else {
                $log.warn('removeLang :: model.organisaatio.kieletUris not defined.');
            }
        };

        this.addMkLang = function(section) {
            if (model.mkSections[section].placeholder !== $filter('i18n')("lisaakieli")) {
                //if (selectedLang !== $filter('i18n')("lisaakieli")) {
                var lang = model.mkSections[section].placeholder;
                //var lang = selectedLang;
                var tab = {lang: lang, active: true};
                model.mkSections[section].placeholder = null;
                if (lang) {
                    for (var i in model.mkSections[section].tabs) {
                        if (model.mkSections[section].tabs[i].lang === lang) {
                            // Siirry olemassaolevalle välilehdelle
                            $timeout(function() {
                                model.mkSections[section].tabs[i].active = true;
                            }, 0);
                            return;
                        }
                        model.mkSections[section].tabs[i].active = false;
                    }
                    model.mkSections[section].tabs.push(tab);
                }
                if (!(lang in model.mdyhteystiedot)) {
                    model.mdyhteystiedot[lang] = {};
                    RefreshOrganisaatio.initYhteystiedotPlaceholder(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot,
                            [lang]);
                }
                for (var type in model.mkSections[section].types) {
                    if (!model.organisaatio.metadata.data[model.mkSections[section].types[type]]) {
                        model.organisaatio.metadata.data[model.mkSections[section].types[type]] = {};
                    }
                    model.organisaatio.metadata.data[model.mkSections[section].types[type]][lang] = "";
                }
                for (var field in model.mkSections[section].fields) {
                    if (typeof model.organisaatio.metadata[model.mkSections[section].fields[field]] === 'undefined') {
                        model.organisaatio.metadata[model.mkSections[section].fields[field]] = null;
                    }
                }
                if (!model.osoitemuoto.hp[lang]) {
                    model.osoitemuoto.hp[lang] = 'kansainvalinen';
                }
                if (!model.mdyhteystiedot.postinumerot[lang]) {
                    model.mdyhteystiedot.postinumerot[lang] = {};
                }
                // Näytä juuri luotu uusi välilehti
                $timeout(function() {
                    tab.active = true;
                }, 0);
            }
        };

        this.removeMkLang = function(section, index) {
            for (var field in model.mkSections[section].types) {
                if (model.organisaatio.metadata.data[model.mkSections[section].types[field]]) {
                    if (model.organisaatio.metadata.data[model.mkSections[section].types[field]][model.mkSections[section].tabs[index].lang]) {
                        delete model.organisaatio.metadata.data[model.mkSections[section].types[field]][model.mkSections[section].tabs[index].lang];
                    }
                }
            }
            model.mkSections[section].tabs.splice(index, 1);
        };

        this.addYtjKotipaikka = function() {
            // Tämä tehdään vasta kun koodiston kotipaikat on saatu ja ytj tiedot on olemassa
            if ('kotiPaikkaKoodi' in model.ytjTiedot === false) {
                return;
            }

            // etsitään koodiston kotipaikoista kieliArvoa ja palautetaan vastaava uri jos löytyy
            var found = $filter('filter')(model.koodisto.kotipaikat, {arvo: model.ytjTiedot.kotiPaikkaKoodi}, true);
            if (found.length) {
                model.organisaatio.kotipaikkaUri = found[0].uri;
            }
            else {
                $log.warn("Failed to found uri for kotipaikka: " + model.ytjTiedot.kotiPaikkaKoodi);
            }
            return;
        };

        this.addYtjOsoite = function() {
            var mapOsoiteYhteystieto = function(ytjOsoite, yhteystieto, postinumeroField) {
                yhteystieto.osoite = ytjOsoite.katu;

                // asetetaan postinumero input kenttään
                model.yhteystiedot.postinumerot['kieli_fi#1'][postinumeroField] = ytjOsoite.postinumero;
                // asettaa postinumeroUrin ja toimipaikan
                model.setPostinumero(false, yhteystieto, ytjOsoite.postinumero);

                // Todo: Pitäisikö asettaa yhteystiedon maa
                // model.yhteystiedot.kaynti.maaUri --> yhteystieto.maaUri = getMaaUri(ytjOsoite.maa);

                // asetetaan päivitys timestamp
                yhteystieto.ytjPaivitysPvm = model.formatDate(new Date());
                return;
            };

            // Tämä tehdään vasta kun koodiston postinumerot on saatu ja ytj tiedot on olemassa
            if ('postiOsoite' in model.ytjTiedot) {
                if (model.ytjTiedot.postiOsoite.kieli === 1) {
                    mapOsoiteYhteystieto(model.ytjTiedot.postiOsoite,
                            model.yhteystiedot['kieli_fi#1'].posti,
                            "posti");
                }
                else if (model.ytjTiedot.postiOsoite.kieli === 2) {
                    mapOsoiteYhteystieto(model.ytjTiedot.postiOsoite,
                            model.yhteystiedot['kieli_sv#1'].posti,
                            "posti");
                }
                else {
                    $log.debug("Unknown language in ytj osoite: " + JSON.stringify(model.ytjTiedot.postiOsoite));
                }

            }
            if ('kayntiOsoite' in model.ytjTiedot) {
                if (model.ytjTiedot.kayntiOsoite.kieli === 1) {
                    mapOsoiteYhteystieto(model.ytjTiedot.kayntiOsoite,
                            model.yhteystiedot['kieli_fi#1'].kaynti,
                            "kaynti");
                }
                else if (model.ytjTiedot.kayntiOsoite.kieli === 2) {
                    mapOsoiteYhteystieto(model.ytjTiedot.kayntiOsoite,
                            model.yhteystiedot['kieli_sv#1'].kaynti,
                            "kaynti");
                }
                else {
                    $log.debug("Unknown language in ytj osoite: " + JSON.stringify(model.ytjTiedot.kayntiOsoite));
                }
            }
        };

        this.addSome = function() {
            if (model.organisaatio.metadata && model.someplaceholder && (model.someplaceholder !== $filter('i18n')("lisaasosiaalinenmedia"))) {
                if (!model.organisaatio.metadata.data[model.someplaceholder] ||
                        RefreshOrganisaatio.isEmptyObject(model.organisaatio.metadata.data[model.someplaceholder])) {
                    model.organisaatio.metadata.data[model.someplaceholder] = {};
                }
                if (!model.organisaatio.metadata.data[model.someplaceholder][model.smlang]) {
                    model.organisaatio.metadata.data[model.someplaceholder][model.smlang] = null;
                }
            }
            model.someplaceholder = $filter('i18n')("lisaasosiaalinenmedia");
        };

        this.removeSome = function(some) {
            if (model.organisaatio.metadata) {
                if (model.organisaatio.metadata.data[some]) {
                    delete model.organisaatio.metadata.data[some];
                }
            }
        };

        this.hasSome = function() {
            if (model.organisaatio.metadata && model.smlang) {
                for (var key in model.sometypes) {
                    if (model.organisaatio.metadata.data[model.sometypes[key]]) {
                        for (var key2 in model.organisaatio.metadata.data[model.sometypes[key]][model.smlang]) {
                            return true;
                        }
                    }
                }
            }
            return false;
        };

        this.isOPHParent = function() {
            if (model.organisaatio.parentOid === ROOT_ORGANISAATIO_OID) {
                return true;
            }
            return false;
        };

        this.isOppilaitos = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf("organisaatiotyyppi_02") !== -1;
            }
            return false;
        };

        this.isKoulutustoimija = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf("organisaatiotyyppi_01") !== -1;
            }
            return false;
        };

        this.isToimipiste = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf("organisaatiotyyppi_03") !== -1;
            }
            return false;
        };

        this.isVarhaiskasvatuksenToimipaikka = function() {
            return model.organisaatio.tyypit && model.organisaatio.tyypit.indexOf("organisaatiotyyppi_08") !== -1;
        };

        this.hasVuosiluokat = function() {
            if (model.organisaatio.tyypit) {
                if (model.organisaatio.tyypit.indexOf("organisaatiotyyppi_02") !== -1) {
                    var tyyppi = model.organisaatio.oppilaitosTyyppiUri;
                    if (tyyppi) {
                        return (tyyppi === "oppilaitostyyppi_11#1" ||
                                tyyppi === "oppilaitostyyppi_12#1" ||
                                tyyppi === "oppilaitostyyppi_19#1");
                    }
                }
            }
            return false;
        };

        this.setPostinumero = function(md, addressmodel, postcode) {
            var sama = (md ? model.osoitemuoto.hpsamaosoite[model.hplang] : model.osoitemuoto.ytsamaosoite[model.ytlang]);
            var yt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            var lang = (md ? model.hplang : model.ytlang);
            var koodistoPostiKoodi = (lang === 'kieli_sv#1' ? model.koodisto.nimetSV[postcode] : model.koodisto.nimetFI[postcode]);
            if (typeof koodistoPostiKoodi === 'undefined') {
                koodistoPostiKoodi = {
                    uri: null,
                    paikka: null
                };
            }
            if (sama === true) {
                if (yt.postinumerot[lang].posti) {
                    yt.postinumerot[lang].kaynti = yt.postinumerot[lang].posti;
                    yt[lang].kaynti.postinumeroUri = koodistoPostiKoodi.uri;
                    yt[lang].kaynti.postitoimipaikka = koodistoPostiKoodi.paikka;
                }
            }
            if (addressmodel) {
                addressmodel.postinumeroUri = koodistoPostiKoodi.uri;
                addressmodel.postitoimipaikka = koodistoPostiKoodi.paikka;
                model.uriKoodit[addressmodel.postinumeroUri] = koodistoPostiKoodi.paikka;
            }
        };

        /*
         * kopioi käyntisoitteen postiosoitteeksi
         * Parametrit:
         *  md - true: käytä metadatan yhteystietoja
         *       false: käytä yhteystiedot-rakennetta
         *  suomalainen - true: kopioi suomalainen muoto
         *                false: kopioi kansainvälinen muoto
         */
        this.copyAddress = function(md, suomalainen) {
            var sama = (md ? model.osoitemuoto.hpsamaosoite[model.hplang] : model.osoitemuoto.ytsamaosoite[model.ytlang]);
            var ytp = (md ? model.mdyhteystiedot : model.yhteystiedot);
            var lang = (md ? model.hplang : model.ytlang);
            if (sama === true) {
                if (suomalainen === true) {
                    // kopioi suomalainen osoitemuoto
                    if (!('kaynti' in ytp[lang])) {
                        ytp[lang].kaynti = {};
                    }
                    for (var kentta in ytp[lang].posti) {
                        if ((kentta !== 'osoiteTyyppi') && (kentta !== 'id') && (kentta !== 'yhteystietoOid')) {
                            ytp[lang].kaynti[kentta] = ytp[lang].posti[kentta];
                        }
                    }
                    if (ytp.postinumerot[lang].posti) {
                        ytp.postinumerot[lang].kaynti = ytp.postinumerot[lang].posti;
                    }
                } else {
                    // kopioi kansainvälinen osoitemuoto
                    if (!('ulkomainen_kaynti' in ytp[lang])) {
                        ytp[lang].ulkomainen_kaynti = {};
                    }
                    for (var ukentta in ytp[lang].ulkomainen_posti) {
                        if ((ukentta !== 'osoiteTyyppi') && (ukentta !== 'id') && (ukentta !== 'yhteystietoOid')) {
                            ytp[lang].ulkomainen_kaynti[ukentta] = ytp[lang].ulkomainen_posti[ukentta];
                        }
                    }
                }
            }
        };

        this.addAddress = function() {
            if (model.organisaatio.yhteystiedot) {
                var uusiYt = {
                    osoiteTyyppi: 'muu', postinumeroUri: null, postitoimipaikka: null, osoite: null
                };
                model.organisaatio.yhteystiedot.push(uusiYt);
                model.yhteystiedot.muu.push(uusiYt);
            }
        };

        this.removeAddress = function(index) {
            if (model.organisaatio.yhteystiedot) {
                model.yhteystiedot.muu.splice(index, 1);
            }
        };

        this.addKtAddress = function() {
            if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                var uusiYt = {
                    osoiteTyyppi: 'muu', postinumeroUri: null, postitoimipaikka: null, osoite: null
                };
                model.organisaatio.metadata.yhteystiedot.push(uusiYt);
                model.mdyhteystiedot.muu.push(uusiYt);
            }
        };

        this.removeKtAddress = function(index) {
            if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                model.mdyhteystiedot.muu.splice(index, 1);
            }
//            if (model.organisaatio.ktMuutOsoitteet) {
//                model.organisaatio.ktMuutOsoitteet.splice(index, 1);
//            }
        };

        this.removeImage = function() {
            model.imagefile = null;
            if (model.organisaatio.metadata) {
                model.organisaatio.metadata.kuvaEncoded = undefined;
            }
        };


        this.getLocalizedPaikkaByUri = function(uri) {
            if (uri in model.uriKoodit) {
                var koodi = model.uriKoodit[uri];
                if (typeof koodi !== 'undefined') {
                    return KoodistoKoodi.getLocalizedName(koodi);
                }
            }
        };

        this.getLocalizedPaikka = function(postikoodi, lang) {
            if ((typeof postikoodi !== 'undefined') && (postikoodi in model.koodisto.nimetFI)) {
                if (lang.substring(0, 8) === 'kieli_sv') {
                    return this.getLocalizedPaikkaSv(postikoodi);
                }
                return model.koodisto.nimetFI[postikoodi].paikka;
            }
        };

        this.getLocalizedPaikkaSv = function(postikoodi) {
            if ((typeof postikoodi !== 'undefined') && (postikoodi in model.koodisto.nimetSV)) {
                return model.koodisto.nimetSV[postikoodi].paikka;
            }
        };

        this.setEctsNimi = function(henkilo) {

            Henkilo.get({hlooid: henkilo.tiedot.oidHenkilo}, function(result) {
                if (result.yhteystiedotRyhma.length > 0) {
                    for (var i = 0; i < result.yhteystiedotRyhma[0].yhteystieto.length; i++) {
                        var yhteystieto = result.yhteystiedotRyhma[0].yhteystieto[i];
                        if (yhteystieto && yhteystieto.yhteystietoArvo && yhteystieto.yhteystietoTyyppi === 'YHTEYSTIETO_PUHELINNUMERO') {
                            model.organisaatio.metadata.hakutoimistoEctsPuhelin[model.ectslang] = yhteystieto.yhteystietoArvo;
                        }
                        if (yhteystieto && yhteystieto.yhteystietoArvo && yhteystieto.yhteystietoTyyppi === 'YHTEYSTIETO_SAHKOPOSTI') {
                            model.organisaatio.metadata.hakutoimistoEctsEmail[model.ectslang] = yhteystieto.yhteystietoArvo;
                        }
                    }
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                $log.error($filter('i18n')("Organisaationtarkastelu.henkilohakuvirhe") + " (status: " + response.status + ")");
            });
            HenkiloKayttooikeus.get({hlooid: henkilo.tiedot.oidHenkilo, orgoid: model.organisaatio.oid}, function(result2) {
                if (result2.length > 0) {
                    // Tehtavanimike is not required information so find first.
                    for (var i = 0; i < result2.length; i++) {
                        if (result2[i].tehtavanimike && result2[i].tehtavanimike !== '') {
                            model.organisaatio.metadata.hakutoimistoEctsTehtavanimike[model.ectslang] = result2[i].tehtavanimike;
                            break;
                        }
                    }
                    // TODO: tarjoa käyttäjälle valintalista nimikkeistä (result[i].tehtavanimike) ?
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                $log.error($filter('i18n')("Organisaationtarkastelu.henkilohakuvirhe") + " (status: " + response.status + ")");
            });
        };

        this.getLocalizedLisatietoNimi = function(yta) {
            return RefreshOrganisaatio.getLocalizedValue(yta, 'YhteystietojenTyyppi.nimi.', '');
        };

        this.getLocalizedLisatietoElementtiNimi = function(yta) {
            var firstLetterToUpperCase = function(s) {
                return s.slice(0, 1).toUpperCase() + s.slice(1).toLowerCase();
            };
            var stripMuuPrefix = function(s) {
                if (s && s.slice(0, 5) === 'Muu: ') {
                    return s.slice(4);
                } else {
                    return s;
                }
            };
            var lang = firstLetterToUpperCase(KoodistoKoodi.getLanguage());
            var ret = yta['YhteystietoElementti.nimi' + lang];
            if (ret) {
                return stripMuuPrefix(ret);
            }
            return stripMuuPrefix(yta['YhteystietoElementti.nimi']);
        };

        this.localize = function(name) {
            var ret = $filter('i18n')("Organisaationtarkastelu." + name);
            // Jos ei löydy käännöstä tarkastelun puolelta, katsotaan onko muokkauksen puolella.
            if (ret.indexOf("Missing translation ")===0) {
                ret = $filter('i18n')("Organisaationmuokkaus." + name);
            }
            return ret;
        };

        this.copyMkFromParent = function(section, orgForm) {
            if (section==='oe') {
                this.copyKTOEFromParent("oelang", model.mkSections.oe);
            } else {
                this.copyKTOEFromParent("ktlang", model.mkSections.kt);
            }
            orgForm.$setDirty();
        };

        this.copyKTOEFromParent = function(mklang, mksection) {
            var kieli = model[mklang];
            model[mklang] = "+";
            var alllangs = {};
            for (var type in mksection.types) {
                if (model.parent.metadata && model.parent.metadata.data) {
                    for (var l in model.parent.metadata.data[mksection.types[type]]) {
                        model.organisaatio.metadata.data[mksection.types[type]][l] =
                                model.parent.metadata.data[mksection.types[type]][l];
                        alllangs[l] = true;
                    }
                }
            }
            // Näytä tabit uusille kielille
            for (var lang in alllangs) {
                var langfound = false;
                for (var i in mksection.tabs) {
                    if (mksection.tabs[i].lang === lang) {
                        langfound = true;
                    }
                }
                if (langfound === false) {
                    var tab = {lang: lang, active: false};
                    mksection.tabs.push(tab);
                }
            }
            // Palauta välilehden kielivalinta => päivittää tekstikentät
            $timeout(function() {model[mklang] = kieli;}, 0);
        };

        this.getUserLang = function() {
            return KoodistoKoodi.getLanguage().toLowerCase();
        };

        this.getOrganisaationTila = function(status) {
            switch(status) {
                case 'PASSIIVINEN':
                    return ($filter('i18n')("Organisaatiot.passivoitu",""));
                    break;
                case 'SUUNNITELTU':
                    return ($filter('i18n')("Organisaatiot.suunniteltu",""));
                    break;
                case 'AKTIIVINEN':
                    return ($filter('i18n')("Organisaatiot.aktiivinen",""));
                    break;
                default:
                    return ($filter('i18n')("Organisaatiot.poistettu",""));
            }
        };


        /*
         * palauttaa true/false jos postiosoite (pätee myös sähköpostiin) on pakollinen annetulle kielelle
         */
        this.isPostiOsoiteRequired = function(lang) {
            //$log.info("lang:" + model.ytlang + ", uris:" + model.organisaatio.kieletUris);
            //switch (model.ytlang) {
            if(angular.isDefined(model.organisaatio.kieletUris)) {
                switch (lang) {
                    case 'kieli_fi#1':
                        return (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_1#1') >= 0) ||
                            (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_3#1') >= 0) ||
                            (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_5#1') >= 0);
                        break;
                    case 'kieli_sv#1':
                        return (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_2#1') >= 0) ||
                            (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_3#1') >= 0);
                        break;
                    case 'kieli_en#1':
                        return (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_4#1') >= 0) ||
                            (model.organisaatio.kieletUris.indexOf('oppilaitoksenopetuskieli_9#1') >= 0);
                        break;
                }
            }
            else {
                $log.warn('isPostiOsoiteRequired :: model.organisaatio.kieletUris is not defined.');
            }
            return false;
        };

        this.updateYhteystiedotValidity = function(ytform) {
            model.ytinvalid = [];
            var kielet = RefreshOrganisaatio.getYhteystietoKielet(model.organisaatio.kieletUris);
            for (var kieli in kielet) {
                if(angular.isDefined(model.yhteystiedot[kieli])) {
                    if (kielet.hasOwnProperty(kieli)) {
                        if ((!model.yhteystiedot[kieli].posti.osoite || model.yhteystiedot[kieli].posti.osoite==='') &&
                            (!model.yhteystiedot[kieli].ulkomainen_posti || !model.yhteystiedot[kieli].ulkomainen_posti.osoite
                            || model.yhteystiedot[kieli].ulkomainen_posti.osoite==='')) {
                            model.ytinvalid.push(kieli);
                        }
                    }
                }
                else {
                    // Most likely something asynchronously resets model.yhteystiedot
                    $log.warn('updateYhteystiedotValidity :: model.yhteystiedot[kieli] is not defined');
                }
            }
        };

    };

    return model;
});

