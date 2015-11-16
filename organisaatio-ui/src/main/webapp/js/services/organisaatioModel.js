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

app.factory('OrganisaatioModel', function($filter, $log, $timeout, $location,
                                          $q, $cookieStore, $injector,
                                          Organisaatio, Organisaatiot,
                                          OrganisaatioHistoria,
                                          KoodistoSearchKoodis, KoodistoKoodi,
                                          KoodistoOrganisaatiotyypit,
                                          KoodistoOppilaitostyypit,
                                          KoodistoPaikkakunnat, KoodistoMaat,
                                          KoodistoPosti, KoodistoPostiCached,
                                          KoodistoPostiVersio, KoodistoVuosiluokat,
                                          UusiOrganisaatio, YTJYritysTiedot,
                                          Alert, KoodistoOpetuskielet,
                                          KoodistoPaikkakunta, HenkiloVirkailijat,
                                          Henkilo, HenkiloKayttooikeus,
                                          KoodistoKieli, Yhteystietojentyyppi,
                                          Paivittaja, NimiHistoriaModel,
                                          LocalisationService, SomeKoodisto) {

    $log = $log.getInstance("OrganisaatioModel");
    var loadingService = $injector.get('LoadingService');

    var model = new function() {
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
            yhteystietoTyypit: {}
        };

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
        this.some      = SomeKoodisto.some;
        this.someurls  = SomeKoodisto.someurls;

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

        this.savestatus = $filter('i18n')("Organisaationmuokkaus.tietojaeitallennettu");

        this.nameFormat = false;

        // Invalidit yhteystiedot kielen mukaan
        this.ytinvalid = [];

        // TODO: Add also parent needed possibly for moving organisaatio

        // Palauta lokalisoitu arvo.
        // Jos lokalisoitua arvoa ei löydy,
        //     palautetaan ensimmäinen löydetty arvo jos create==false
        //     tai luodaan uusi tyhjä arvo ja palautetaan se jos create==true
        // fi-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "kielivalikoima_fi" : "Suomenkielinen nimi"}, "kielivalikoima_") => "Suomenkielinen nimi"
        //   func({ "kieli_fi#1" : "Suomenkielinen nimi"}, "kielivalikoima_", "#1") => "Suomenkielinen nimi"
        // sv-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "fi" : "Suomenkielinen nimi" , "sv" : "Samma på svenska"}, "") => "Samma på svenska"
        var getLocalizedValue = function(res, prefix, suffix, create, language) {
            var lang = (language ? language : KoodistoKoodi.getLanguage().toLowerCase());
            var ret = "";
            if (res) {
                ret = res[prefix + lang + suffix];
                if (!ret) {
                    if (create) {
                        res[prefix + lang + suffix] = "";
                        return res[prefix + lang + suffix];
                    } else {
                        // Palauta ensimmäinen arvo
                        for (var i in res) {
                            return res[i];
                        }
                    }
                }
            }
            return ret;
        };

        var getDecodedLocalizedValue = function(res, prefix, suffix, create, language) {
            var ret = getLocalizedValue(res, prefix, suffix, create, language);
            if (ret) {
                return ret.replace(/&amp;/g, '&');
            }
        };

        this.getDecodedLocalizedValue= function(res, prefix, suffix, create, language) {
            return getDecodedLocalizedValue(res, prefix, suffix, create, language);
        };

        this.getLocalizedValueWithProperty = function (property) {
            return function(entry) {
                return getLocalizedValue(entry[property]);
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

        var initMk = function(mkSection) {
            var mkLangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
            for (var key in mkSection.types) {
                for (var lang in model.organisaatio.metadata.data[mkSection.types[key]]) {
                    if (model.organisaatio.metadata.data[mkSection.types[key]].hasOwnProperty(lang)) {
                        mkLangs[lang] = true;
                    }

                }
            }
            for (var mkLang in mkLangs) {
                mkSection.tabs.push({lang: mkLang, active: false});
            }
            if (mkSection.tabs.length > 0) {
                $timeout(function() {
                    mkSection.tabs[0].active = true;
                }, 0);
            }
            for (var field in mkSection.types) {
                if (!model.organisaatio.metadata.data[mkSection.types[field]]) {
                    model.organisaatio.metadata.data[mkSection.types[field]] = {};
                }
            }
        };

        var refreshMetadata = function(result) {
            model.mkSections.kt.tabs.length = 0;
            model.mkSections.hp.tabs.length = 0;
            model.mkSections.oe.tabs.length = 0;
            model.mkSections.sm.tabs.length = 0;
            model.mkSections.ects.tabs.length = 0;

            // for loop indeksi
            var i;

            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getDecodedLocalizedValue(result.metadata.hakutoimistonNimi, "kieli_", "#1", false);
                // Alusta nimikentät jos ei asetettu
                if (!result.metadata.hakutoimistonNimi) {
                    result.metadata.hakutoimistonNimi = {};
                }
                var nlangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
                for (var lang in nlangs) {
                    if (!result.metadata.hakutoimistonNimi[lang]) {
                        result.metadata.hakutoimistonNimi[lang] = null;
                    }
                }
                if (result.metadata.data) {
                    initMk(model.mkSections.kt);
                    initMk(model.mkSections.oe);
                    initMk(model.mkSections.sm);
                    initMk(model.mkSections.ects);
                }
                if (result.metadata.yhteystiedot) {
                    var hplangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
                    for (i = 0; i < model.organisaatio.metadata.yhteystiedot.length; i++) {
                        hplangs[model.organisaatio.metadata.yhteystiedot[i].kieli] = true;
                    }
                    for (lang in hplangs) {
                        model.mkSections.hp.tabs.push({lang: lang, active: false});
                    }
                    if (model.mkSections.hp.tabs.length > 0) {
                        $timeout(function() {
                            model.mkSections.hp.tabs[0].active = true;
                        }, 0);
                    }
                }

                if (result.metadata.ectstiedot) {
                    var ectslangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
                    for (i = 0; i < model.organisaatio.metadata.ectstiedot.lenght; i++) {
                        ectslangs[model.organisaatio.metadata.ectstiedot[i].kieli] = true;
                    }
                    for (var ectsLang in ectslangs) {
                        model.mkSections.ects.tabs.push({lang: ectsLang, active: false});
                    }
                    if (model.mkSections.ects.tabs.length > 0) {
                        $timeout(function() {
                            model.mkSections.ects.tabs[0].active = true;
                        }, 0);
                    }
                }
            }
        };

        // Alusta objektit joita ei vielä ole asetettu, luo mäppäys modelYhteystiedoista
        // organisaatioYhteystietoihin yhteystiedon tyypin perusteella
        var initYhteystiedot = function(organisaatioYhteystiedot, modelYhteystiedot, muoto) {
            //modelYhteystiedot.muu = [];
            modelYhteystiedot['kieli_fi#1'] = {};
            modelYhteystiedot['kieli_sv#1'] = {};
            modelYhteystiedot['kieli_en#1'] = {};
            for (var ytindex in organisaatioYhteystiedot) {
                var yt = organisaatioYhteystiedot[ytindex];
                var kieli = (yt.kieli === null ? 'kieli_fi#1' : yt.kieli);
                if (!(kieli in modelYhteystiedot)) {
                    modelYhteystiedot[kieli] = {};
                }
                if (yt.osoite) {
                    var osoiteTyyppi = yt.osoiteTyyppi;
                    if (osoiteTyyppi === 'muu') {
                        // Muita osoitteita voi olla useita, lisää listaan
                        if (!modelYhteystiedot[kieli][osoiteTyyppi]) {
                            modelYhteystiedot[kieli][osoiteTyyppi] = [];
                        }
                        modelYhteystiedot[kieli][osoiteTyyppi].push(yt);
                    } else {
                        if (muoto) {
                            if (osoiteTyyppi.indexOf('ulkomainen_') !== -1) {
                                muoto[kieli] = 'kansainvalinen';
                            } else {
                                muoto[kieli] = 'suomalainen';
                            }
                        }
                        modelYhteystiedot[yt.kieli][osoiteTyyppi] = yt;
                    }
                } else if (yt.numero) {
                    modelYhteystiedot[kieli][yt.tyyppi] = yt;
                } else if (yt.email) {
                    modelYhteystiedot[kieli].email = yt;
                } else if (yt.www) {
                    modelYhteystiedot[kieli].www = yt;
                }
            }
            // Luodaan puuttuville yhteystiedoille placeholderit
            model.initYhteystiedotPlaceholder(organisaatioYhteystiedot, modelYhteystiedot,
                    ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']);
        };

        this.initYhteystiedotPlaceholder = function(organisaatioYhteystiedot, modelYhteystiedot, kielet) {
            var uusiYt;
            var osoiteTyypit = ['kaynti', 'posti', 'ulkomainen_kaynti', 'ulkomainen_posti'];
            for (var kieli in kielet) {
                var phkieli = kielet[kieli];
                for (var i = 0; i < osoiteTyypit.length; i++) {
                    if (!modelYhteystiedot[phkieli][osoiteTyypit[i]]) {
                        uusiYt = {osoiteTyyppi: osoiteTyypit[i], kieli: phkieli};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][osoiteTyypit[i]] = uusiYt;
                    }
                }
                var etyypit = ['email', 'www'];
                for (i = 0; i < etyypit.length; ++i) {
                    if (!modelYhteystiedot[phkieli][etyypit[i]]) {
                        uusiYt = {};
                        uusiYt[etyypit[i]] = null;
                        uusiYt['kieli'] = phkieli;
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][etyypit[i]] = uusiYt;
                    }
                }
                var ptyypit = ['puhelin', 'faksi'];
                for (i = 0; i < ptyypit.length; ++i) {
                    if (!modelYhteystiedot[phkieli][ptyypit[i]]) {
                        uusiYt = {tyyppi: ptyypit[i], kieli: phkieli};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][ptyypit[i]] = uusiYt;
                    }
                }
            }
        };

        var finishModel = function() {
            if (model.organisaatio.yhteystiedot) {
                initYhteystiedot(model.organisaatio.yhteystiedot, model.yhteystiedot, model.osoitemuoto.yt);
            }
            if (!model.organisaatio.metadata) {
                model.organisaatio.metadata = {};
            }
            if (!model.organisaatio.metadata.yhteystiedot) {
                model.organisaatio.metadata.yhteystiedot = [];
            }
            if (!model.organisaatio.metadata.data) {
                model.organisaatio.metadata.data = {};
            }
            if (!model.organisaatio.metadata.hakutoimistoEctsEmail) {
                model.organisaatio.metadata.hakutoimistoEctsEmail = {};
            }
            if (!model.organisaatio.metadata.hakutoimistoEctsPuhelin) {
                model.organisaatio.metadata.hakutoimistoEctsPuhelin = {};
            }
            if (!model.organisaatio.metadata.hakutoimistoEctsTehtavanimike) {
                model.organisaatio.metadata.hakutoimistoEctsTehtavanimike = {};
            }
            if (!model.organisaatio.metadata.hakutoimistoEctsNimi) {
                model.organisaatio.metadata.hakutoimistoEctsNimi = {};
            }

            initYhteystiedot(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot, model.osoitemuoto.hp);
        };

        // Näyttää käyttäjälle virheen Alert-servicen avulla ja loggaa responsen statuksen
        var showAndLogError = function(msg, response) {
            loadingService.onErrorHandled();
            $log.error(msg + " (status: " + response.status + ")");
            model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey || msg), false);
        };

        var refreshLisayhteystietoArvos = function() {
            var st = {};
            for (var i in model.organisaatio.yhteystietoArvos) {
                var ya = model.organisaatio.yhteystietoArvos[i];
                st[ya['YhteystietojenTyyppi.oid']] = model.getLocalizedLisatietoNimi(ya);
            }
            var res = [];
            for (var k in st) {
                res.push({oid: k, nimi: st[k]});
            }
            model.lisayhteystietoarvos = res;
        };

        var refreshParent = function(parentResult) {
            model.uriLocalizedNames["parentnimi"] = getDecodedLocalizedValue(parentResult.nimi, "", "", false);
            model.parenttype = parentResult.tyypit[0];
            model.parent = parentResult;
            model.parentPattern = {};
            model.parentPattern["fi"] = (parentResult.nimi.fi ? "^" + parentResult.nimi.fi + ".*" : ".*");
            model.parentPattern["sv"] = (parentResult.nimi.sv ? "^" + parentResult.nimi.sv + ".*" : ".*");
            model.parentPattern["en"] = (parentResult.nimi.en ? "^" + parentResult.nimi.en + ".*" : ".*");
            model.nameFormat = {};
            model.nameFormat['fi'] = (model.organisaatio.nimi.fi ? model.organisaatio.nimi.fi.match(model.parentPattern["fi"]) : null);
            model.nameFormat['sv'] = (model.organisaatio.nimi.sv ? model.organisaatio.nimi.sv.match(model.parentPattern["sv"]) : null);
            model.nameFormat['en'] = (model.organisaatio.nimi.en ? model.organisaatio.nimi.en.match(model.parentPattern["en"]) : null);
        };

        var refresh = function(result) {
            $log.info("refresh()");

            $log.info("refresh: mode=" + model.mode);
            // tyhjennetään mahdolliset vanhat ytj tiedot
            model.ytjTiedot = {};
            modelYhteystiedot = {};
            model.organisaatio = result;
            model.uriLocalizedNames["nimi"] = getDecodedLocalizedValue(result.nimi, "", "", false);
            model.uriLangNames = {};
            model.uriLangNames["FI"] = {};
            model.uriLangNames["SV"] = {};
            model.organisaationtila = "";
            model.organisaationtila = model.getOrganisaationTila(model.organisaatio.status);
            model.organisaatio.historia = result.historia;

            // Otetaan talteen organisaation nimihistoria ennen muutoksia.
            model.originalNimet = model.organisaatio.nimet;

            // Päivitetään nimihistoria
            var nimiHistoriaModel = NimiHistoriaModel;
            nimiHistoriaModel.setNimihistoria(model.organisaatio.nimet);

            // Haetaan nimihistorian uusin nimi, joka tulevaisuudessa ja laitetaan se tulevaksi
            var tulevaNimi = nimiHistoriaModel.getAjastettuNimi();
            var nimi = nimiHistoriaModel.getNimi();

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

            Organisaatio.get({oid: result.parentOid}, function(parentResult) {
                // For loop index
                var i;

                refreshParent(parentResult);

                if (model.mode === 'edit') {
                    refreshKoodisto();
                    refreshHenkilo();
                }
                finishModel();
                refreshMetadata(result);
                refreshLisayhteystietoArvos();
                // Hae kaikki koodi-urit kerralla
                var koodiUris = {};
                for (i in model.yttabs) {
                    koodiUris[model.yttabs[i]] = true;
                }
                if (result["kotipaikkaUri"]) {
                    koodiUris[result["kotipaikkaUri"]] = true;
                }
                if (result["maaUri"]) {
                    koodiUris[result["maaUri"]] = true;
                }
                for (i = 0; i < result["kieletUris"].length; i++) {
                    var kieletUri = result["kieletUris"][i];
                    if (kieletUri) {
                        koodiUris[kieletUri] = true;
                    }
                }
                for (i = 0; i < result["vuosiluokat"].length; i++) {
                    var vuosiluokatUri = result["vuosiluokat"][i];
                    if (vuosiluokatUri) {
                        koodiUris[vuosiluokatUri] = true;
                    }
                }

                for (var yht in result.yhteystiedot) {
                    if (result.yhteystiedot[yht].postinumeroUri) {
                        koodiUris[result.yhteystiedot[yht].postinumeroUri] = true;
                    }
                }

                if (result.metadata && result.metadata.yhteystiedot) {
                    for (i = 0; i < result.metadata.yhteystiedot.length; i++) {
                        var osoite = result.metadata.yhteystiedot[i];
                        if (osoite.postinumeroUri) {
                            koodiUris[osoite.postinumeroUri] = true;
                        }
                    }
                }
                if (result.metadata && result.metadata.data) {
                    for (var key in result.metadata.data) {
                        if (result.metadata.data.hasOwnProperty(key)) {
                            for (var lang in result.metadata.data[key]) {
                                if (result.metadata.data[key].hasOwnProperty(lang)) {
                                    koodiUris[lang] = (lang.indexOf("kieli_") === 0);
                                }
                            }
                        }
                    }
                }
                if (result.oppilaitosTyyppiUri) {
                    koodiUris[result.oppilaitosTyyppiUri] = true;
                }

                // Poistetaan versiotieto vuosiluokat-listasta
                var vuosiluokat = model.organisaatio.vuosiluokat.slice(0);
                model.organisaatio.vuosiluokat.length = 0;
                if (vuosiluokat) {
                    for (var vl in vuosiluokat) {
                        model.organisaatio.vuosiluokat.push(vuosiluokat[vl].split("#")[0]);
                    }
                }

                var searchParams = "";
                for (var koodiUri in koodiUris) {
                    searchParams += "&koodiUris=" + koodiUri.split("#")[0];
                }
                searchParams = searchParams.substring(1, searchParams.length);
                KoodistoSearchKoodis.get({uris: searchParams}, function(koodiResult) {
                    for (var i = 0; i < koodiResult.length; i++) {
                        // Lisää kaikki koodit myös #<versio> -päätteisenä, koska result.koodiUri:ssa #<versio>
                        // -päätettä ei ole vaikka olisi annettu hakuparametrina
                        model.uriLocalizedNames[koodiResult[i]["koodiUri"]] = KoodistoKoodi.getLocalizedName(koodiResult[i]);
                        model.uriLocalizedNames[koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = KoodistoKoodi.getLocalizedName(koodiResult[i]);
                        model.uriKoodit[koodiResult[i]["koodiUri"]] = koodiResult[i];
                        model.uriKoodit[koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = koodiResult[i];
                        model.uriLangNames["FI"][koodiResult[i]["koodiUri"]] = KoodistoKoodi.getLangName(koodiResult[i], "FI");
                        model.uriLangNames["FI"][koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = KoodistoKoodi.getLangName(koodiResult[i], "FI");
                        model.uriLangNames["SV"][koodiResult[i]["koodiUri"]] = KoodistoKoodi.getLangName(koodiResult[i], "SV");
                        model.uriLangNames["SV"][koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = KoodistoKoodi.getLangName(koodiResult[i], "SV");
                    }
                },
                // Error case
                function(response) {
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                model.koodisto.localizedKoulutustoimija = "Koulutustoimija";
                model.koodisto.localizedOppilaitos = "Oppilaitos";
                model.koodisto.localizedToimipiste = "Toimipiste";
            },
            // Error case
            function(response) {
                // parenttia ei löytynyt
                showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
            });
            var hakuParametrit = {};
            hakuParametrit.aktiiviset   = true;
            hakuParametrit.suunnitellut = true;
            hakuParametrit.lakkautetut  = true;
            hakuParametrit.oidRestrictionList = [result.oid];

            model.muutettaviaAliorganisaatioita = 0;
            model.hasAliorganisaatios = false;
            model.aliorganisaatioHaunTulos = {};

            Organisaatiot.get(hakuParametrit, function(childResult) {
                model.aliorganisaatiot.length = 0;
                if (childResult && childResult.organisaatiot) {
                    for (var i = 0; i < childResult.organisaatiot.length; i++) {
                        if (!childResult.organisaatiot[i].lakkautusPvm) {
                            addAliorganisaatio(childResult.organisaatiot[i].children, 0);
                        }

                        // Voimassaolon muokkausta varten
                        if (childResult.organisaatiot[i].children.length) {
                            model.hasAliorganisaatios = true;
                        }
                    }

                    // Tallennetaan vielä koko hakutulos voimassaolonmuokkausta varten
                    model.aliorganisaatioHaunTulos = childResult.organisaatiot;
                    model.aliorganisaatioTiedotHaettu = true;
                }
            },
            // Error case
            function(response) {
                // aliorganisaatiohaku ei onnistunut
                model.showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
                model.aliorganisaatioTiedotHaettu = true;
            });
            model.paivitys = {};
            Paivittaja.get({oid: result.oid}, function(paivitys) {
                if (paivitys.paivitysPvm) {
                    var pvm = moment(new Date(paivitys.paivitysPvm));
                    model.paivitys.pvm = pvm.format('D.M.YYYY H:mm:ss');
                    Henkilo.get({hlooid: paivitys.paivittaja}, function(paivittaja_hlo) {
                        model.paivitys.paivittaja = paivittaja_hlo.etunimet + ' ' + paivittaja_hlo.sukunimi;
                    },
                    // Error case
                    function(response) {
                        $log.warn("Failed to get Henkilo!", response);
                        $log.debug("disable system error dialog.");
                        loadingService.onErrorHandled();
                        model.paivitys.paivittaja = paivitys.paivittaja;
                    });
                }
            },
            // Error case
            function(response) {
                // Päivittäjän haku ei onnistunut
                showAndLogError("Organisaationtarkastelu.paivittajahakuvirhe", response);
            });

            model.historia = {};
            OrganisaatioHistoria.get({oid: result.oid}, function(historia) {
                model.historia = historia;
            },
            // Error case
            function(response) {
                // Historian haku ei onnistunut
                showAndLogError("Organisaationtarkastelu.historiahakuvirhe", response);
            });
        };

        var addAliorganisaatio = function(aliOrgList, level) {
            if (aliOrgList) {
                for (var j = 0; j < aliOrgList.length; j++) {
                    if (!aliOrgList[j].lakkautusPvm) {
                        model.aliorganisaatiot.push({nimi: getDecodedLocalizedValue(aliOrgList[j].nimi, "", ""), oid: aliOrgList[j].oid, level: level});
                        addAliorganisaatio(aliOrgList[j].children, level + 1);
                    }
                }
            }
        };

        this.refresh = function(organisaatio) {
            $log.info("refresh(): " + organisaatio.oid);
            refresh(organisaatio);
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
                    refresh(result);
                },
                // Error case
                function(response) {
                    // Organisaatiohaku ei onnistunut
                    showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
                });
            }
        };

        var updateLisayhteystietoArvos = function(lisatieto) {
            model.uriLocalizedNames[lisatieto.oid] = $filter('i18n')("lisaakieli");
            lisatieto.nimi.teksti.forEach(function(teksti) {
                if (teksti.kieliKoodi === KoodistoKoodi.getLanguage().toLowerCase()) {
                    model.uriLocalizedNames[lisatieto.oid] =
                            teksti.value;
                }
            });
            if (!model.organisaatio.yhteystietoArvos) {
                model.organisaatio.yhteystietoArvos = [];
            }


            var ytlangs = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1'];
            lisatieto.allLisatietokenttas.forEach(function(yt) {

                model.uriLocalizedNames[yt.oid] =
                        (KoodistoKoodi.getLanguage() === "SV" ? yt.nimiSv : (
                                KoodistoKoodi.getLanguage() === "EN" ? yt.nimiEn : yt.nimi));

                for (var i in ytlangs) {
                    var ytlang = ytlangs[i];
                    // Lisätään jos arvoa ei ole
                    var arvo = null;
                    for (var a in model.organisaatio.yhteystietoArvos) {

                        if ((lisatieto.oid === model.organisaatio.yhteystietoArvos[a]['YhteystietojenTyyppi.oid']) &&
                                (yt.oid === model.organisaatio.yhteystietoArvos[a]['YhteystietoElementti.oid']) &&
                                (ytlang === model.organisaatio.yhteystietoArvos[a]['YhteystietoArvo.kieli'])) {
                                 if (yt.kaytossa === true )
                                 {
                                    arvo = model.organisaatio.yhteystietoArvos[a];
                                 }
                                 else if (model.organisaatio.yhteystietoArvos[a]['YhteystietoArvo.arvoText'] !== null )
                                 {
                                    arvo = model.organisaatio.yhteystietoArvos[a];
                                 }

                        }
                    }

                    // Jos arvoa ei vielä ole, lisätään muokkaus/uudenluontinäkymään bindausta varten
                    if (arvo === null) {
                        var uusiyt = {};
                        uusiyt["YhteystietoArvo.arvoText"] = null;
                        uusiyt["YhteystietoArvo.kieli"] = ytlang;
                        uusiyt["YhteystietojenTyyppi.oid"] = lisatieto.oid;
                        uusiyt["YhteystietoElementti.oid"] = yt.oid;
                        uusiyt["YhteystietoElementti.pakollinen"] = yt.pakollinen;
                        uusiyt["YhteystietoElementti.kaytossa"] = yt.kaytossa;
                        arvo = uusiyt;
                        if (yt.kaytossa === true)
                        {
                            model.organisaatio.yhteystietoArvos.push(arvo);
                        }
                    } else {
                        // jatketaan => mäpätään olemassaolevaan arvoon
                    }
                    // Mäpätään oidista nimeen. Mäppäys on oikeasti 1-1 vaikka nimi toistuu joka tietueessa.
                    if (!model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]]) {
                        model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]] = {};
                    }
                    // Luodaan elementti kielelle, jos sitä ei ole
                    if (!model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang]) {
                        model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang] = [];
                    }

                    // Laitetaan yhteystietoarvo editoitavaksi jos se on käytössä tai
                    // arvo on asetettu. Näin voidaan editoida vielä käytöstä poistettua
                    // arvoa.
                    // HUOM! Rajapinnan yli tulee "YhteystietoElementti.kaytossa" string muodossa!
                    if ((arvo["YhteystietoElementti.kaytossa"] === true) ||
                            (arvo["YhteystietoElementti.kaytossa"] === "true") ||
                            (arvo["YhteystietoArvo.arvoText"] !== null))
                    {
                        model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang].push(arvo);
                    }
                }
            });
        };

        var updateLisayhteystiedot = function() {
            model.lisayhteystiedot = {};
            var kaikkiTyypit = model.organisaatio.tyypit;
            if (model.organisaatio.oppilaitosTyyppiUri) {
                kaikkiTyypit = kaikkiTyypit.concat(model.organisaatio.oppilaitosTyyppiUri);
            }
            for (var tyyppi in kaikkiTyypit) {
                if (model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()]) {
                        model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()].forEach(function(t) {
                        updateLisayhteystietoArvos(t);
                    });
                }
            }
        };

        var refreshKoodisto = function(oid) {
            if (oid === null || (oid !== model.koodisto.oid)) {
                model.koodisto.localizedOppilaitos = "";
                model.koodisto.localizedKoulutustoimija = "";
                model.koodisto.localizedToimipiste = "";
                model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
                KoodistoOrganisaatiotyypit.get({}, function(result) {
                    model.koodisto.organisaatiotyypit.length = 0;
                    model.koodisto.ophOrganisaatiot.length = 0;
                    /* Organisaatiohierarkiasäännöt:
                     (oltava samanlainen logiikka kuin luokassa OrganisationHierarchyValidator)
                     Jos organisaatio on OPPILAITOS, sillä on oltava yläorganisaatio tyypiltään KOULUTUSTOIMIJA.
                     Jos organisaatio on MUU ORGANISAATIO ja sille on määritelty yläorganisaatio,
                     on yläorganisaation oltava joko OPH tai MUU ORGANISAATIO.
                     Jos organisaatio on KOULUTUSTOIMIJA ja sille on määritelty yläorganisaatio,
                     on yläorganisaation oltava joko OPH tai KOULUTUSTOIMIJA
                     Jos organisaatio on TYÖELÄMÄJÄRJESTÖ ja sille on määritelty yläorganisaatio,
                     on yläorganisaation oltava joko OPH tai TYÖELÄMÄJÄRJESTÖ.
                     Jos organisaatio on TOIMIPISTE, sillä on oltava yläorganisaatio joka on tyypiltään joko
                     TOIMIPISTE, OPPILAITOS, MUU ORGANISAATIO tai TYÖELÄMÄJÄRJESTÖ.
                     Jos organisaatio on OPPISOPIMUSTOIMIPISTE, sillä on oltava yläorganisaatio
                     joka on tyypiltään KOULUTUSTOIMIJA.
                     Siis: OPH [1] -> MUU ORGANISAATIO [0..n] -> KOULUTUSTOIMIJA [1] -> OPPILAITOS [0..1] -> TOIMIPISTE [0..n]
                     Koodiston tyypit: 01:Koulutustoimija, 02:Oppilaitos, 03:Toimipiste, 04:Oppisopimustoimipiste,
                     05:Muu organisaatio, 06:Työelämäjärjestö
                     OPH-organisaation tyyppi on 'Muu organisaatio'
                     Lisäys 30.6.2014: Kaikille organisaatiotyypeille saa lisätä Oppisopimustoimipisteen (OH-280)
                     */
                    var sallitutAlaOrganisaatiot = {
                        'Muu organisaatio': ["05", "03"],
                        'Koulutustoimija': ["02", "04"],
                        'Oppilaitos': ["03"],
                        'Toimipiste': ["03"],
                        'Oppisopimustoimipiste': [],
                        'Tyoelamajarjesto': ["06","03", "05"]}; // TODO "05" is temp
                    result.forEach(function(orgTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(orgTyyppiKoodi)) {
                            var localizedOrgType = KoodistoKoodi.getLangName(orgTyyppiKoodi, 'FI');
                            // Parentin sallitut aliorganisaatiot
                            if (model.organisaatio.parentOid !== model.OPHOid && sallitutAlaOrganisaatiot[model.parenttype].indexOf(orgTyyppiKoodi.koodiArvo) !== -1) {
                                model.koodisto.organisaatiotyypit.push(localizedOrgType);
                            } // Sallitut ylimmän tason organisaatiot
                            else if (model.organisaatio.parentOid === model.OPHOid &&
                                (orgTyyppiKoodi.koodiArvo === "01" || orgTyyppiKoodi.koodiArvo === "06"
                                || orgTyyppiKoodi.koodiArvo === "05")) {
                                model.koodisto.organisaatiotyypit.push(localizedOrgType);
                            }

                            if (orgTyyppiKoodi.koodiArvo === "01") {
                                model.koodisto.localizedKoulutustoimija = localizedOrgType;
                            } else if (orgTyyppiKoodi.koodiArvo === "02") {
                                model.koodisto.localizedOppilaitos = localizedOrgType;
                            } else if (orgTyyppiKoodi.koodiArvo === "03") {
                                model.koodisto.localizedToimipiste = localizedOrgType;
                            }
                            if (orgTyyppiKoodi.koodiArvo !== "03" && orgTyyppiKoodi.koodiArvo !== "04") {
                                model.koodisto.ophOrganisaatiot.push(localizedOrgType);
                            }
                        }
                    });
                },
                // Error case
                function(response) {
                    // organisaatiotyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                Yhteystietojentyyppi.get({}, function(result) {
                    model.yhteystietojentyyppi = {};
                    for (var ytt in result) {
                        if (result[ytt]['sovellettavatOrganisaatios']) {
                            var tyypit = result[ytt]['sovellettavatOrganisaatios'].concat(result[ytt].sovellettavatOppilaitostyyppis);
                            for (var tyyppi in tyypit) {
                                if (!model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()]) {
                                    model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()] = [];
                                }
                                model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()].push(result[ytt]);
                            }
                        }
                    }
                    updateLisayhteystiedot();
                },
                // Error case
                function(response) {
                    // oppilaitostyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.yhteystietojentyyppihakuvirhe", response);
                });
                KoodistoOppilaitostyypit.get({}, function(result) {
                    model.koodisto.oppilaitostyypit.length = 0;
                    result.forEach(function(olTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(olTyyppiKoodi)) {
                            model.koodisto.oppilaitostyypit.push({uri: olTyyppiKoodi.koodiUri + "#" + olTyyppiKoodi.versio, nimi: KoodistoKoodi.getLocalizedName(olTyyppiKoodi)});
                        }
                    });
                },
                // Error case
                function(response) {
                    // oppilaitostyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoPaikkakunnat.get({}, function(result) {
                    model.koodisto.kotipaikat.length = 0;
                    var kotipaikkaVoimassa = false;
                    result.forEach(function(kpKoodi) {
                        model.koodisto.kotipaikat.push({uri: kpKoodi.koodiUri, arvo: kpKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kpKoodi)});
                        if (model.organisaatio.kotipaikkaUri && (model.organisaatio.kotipaikkaUri === kpKoodi.koodiUri)) {
                            kotipaikkaVoimassa = true;
                        }
                    });
                    if (model.mode === 'edit' && !kotipaikkaVoimassa) {
                        // hae myös lakkautettu kotikunta
                        KoodistoPaikkakunta.get({uri: model.organisaatio.kotipaikkaUri}, function(result) {
                            model.koodisto.kotipaikat.push({uri: result.koodiUri, arvo: result.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(result)});
                        }, function(response) {
                            // paikkakuntaa ei löytynyt
                            showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                        });
                    }
                    // jos ytj:stä saatu organisaatioon liityvää tietoa --> päivitetään kotipaikka
                    model.addYtjKotipaikka();
                },
                // Error case
                function(response) {
                    // paikkakuntia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoMaat.get({}, function(result) {
                    model.koodisto.maat.length = 0;
                    result.forEach(function(maaKoodi) {
                        model.koodisto.maat.push({uri: maaKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(maaKoodi)});
                    });
                },
                // Error case
                function(response) {
                    // maita ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoKieli.get({}, function(result) {
                    model.koodisto.isokielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        // TODO: filter ?
                        var uri = kieliKoodi.koodiUri + "#" + kieliKoodi.versio;
                        model.koodisto.isokielet.push({uri: uri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                        model.uriLocalizedNames[uri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                    });
                },
                // Error case
                function(response) {
                    // kieliä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoOpetuskielet.get({}, function(result) {
                    model.koodisto.opetuskielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        var uri = kieliKoodi.koodiUri + "#" + kieliKoodi.versio;
                        model.koodisto.opetuskielet.push({uri: uri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                        model.uriLocalizedNames[uri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                    });
                    // jos ytj:stä saatu organisaatioon liittyvää tietoa --> päivitetään kieli
                    model.addYtjLang();
                },
                // Error case
                function(response) {
                    // kieliä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoVuosiluokat.get({}, function(result) {
                    model.koodisto.vuosiluokat.length = 0;
                    result.forEach(function(vuosiluokka) {
                        if (KoodistoKoodi.isValid(vuosiluokka)) {
                            model.koodisto.vuosiluokat.push({uri: vuosiluokka.koodiUri, nimi: KoodistoKoodi.getLocalizedName(vuosiluokka)});
                            model.uriLocalizedNames[vuosiluokka.koodiUri] = KoodistoKoodi.getLocalizedName(vuosiluokka);
                        }
                    });
                },
                // Error case
                function(response) {
                    // vuosiluokkia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });

                var getKoodistoPostiVersio = function() {
                    var deferred = $q.defer();
                    KoodistoPostiVersio.get({}, function(result) {
                        deferred.resolve(result.versio);
                    },
                    // Error case
                    function(response) {
                        deferred.reject();
                    });
                    return deferred.promise;
                };

                var getKoodistoPosti = function(cached) {
                    var deferred = $q.defer();
                    if (cached===true) {
                        KoodistoPostiCached.get({}, function(result) {
                            deferred.resolve(result);
                        }, function(response) {
                            deferred.reject();
                        });
                    } else {
                        KoodistoPosti.get({}, function(result) {
                            deferred.resolve(result);
                        }, function(response) {
                            deferred.reject();
                        });
                    }
                    return deferred.promise;
                };

                var updateKoodistoPosti = function() {
                    var deferred = $q.defer();
                    // Sallitaan postinumerokoodiston haku selaimen cachesta jos versionumero ei ole muuttunut.
                    //
                    // Kun koodistoa päivitetään, koodistoversion pitäisi muuttua, mutta vain jos koodisto ei
                    // ole luonnostilassa eli koodisto pitää käydä hyväksymässä muutoksen jälkeen.
                    // Oletettavasti koodistoja ei kuitenkaan jätetä luonnostilaan roikkumaan pitemmäksi aikaa.
                    //
                    // Testauksessa pitää huomioida että testiympäristössä koodistoversiota ei välttämättä päivitetä
                    // yllä kuvatulla tavalla.
                    getKoodistoPostiVersio().then(function(versio) {
                        var kversio = $cookieStore.get('KoodistoPNVersio');
                        if (typeof kversio === 'undefined' || kversio !== versio) {
                            // versio on vaihtunut, estä haku selaimen cachesta
                            $cookieStore.put('KoodistoPNVersio', versio);
                            getKoodistoPosti(false).then(function(result) {
                                deferred.resolve(result);
                            }, function(response) {
                                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                                deferred.reject();
                            });
                        } else {
                            // voidaan hakea selaimen cachesta
                            getKoodistoPosti(true).then(function(result) {
                                deferred.resolve(result);
                            }, function(response) {
                                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                                deferred.reject();
                            });
                        }
                    }, function(response) {
                        showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                        deferred.reject();
                    });
                    return deferred.promise;
                };

                updateKoodistoPosti().then(function(result) {
                    model.koodisto.postinumerot.length = 0;
                    model.koodisto.nimetFI = {};
                    model.koodisto.nimetSV = {};
                    var arvoByUri = {};
                    result.forEach(function(postiKoodi) {
                        if (KoodistoKoodi.isValid(postiKoodi)) {
                            model.koodisto.postinumerot.push(postiKoodi.koodiArvo);

                            // Mäppäys postinumerosta uriin ja postitoimipaikan käännökseen
                            model.koodisto.nimetFI[postiKoodi.koodiArvo] = {
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "FI")};
                            model.koodisto.nimetSV[postiKoodi.koodiArvo] = {
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "SV")};

                            arvoByUri[postiKoodi.koodiUri] = postiKoodi.koodiArvo;

                        }
                    });
                    model.koodisto.postinumerot.sort();

                    model.yhteystiedot.postinumerot = {
                        'kieli_fi#1': {},
                        'kieli_sv#1': {},
                        'kieli_en#1': {}
                    };
                    model.yhteystiedot.postinumerot.muu = [];
                    for (var ytindex in model.organisaatio.yhteystiedot) {
                        var yt = model.organisaatio.yhteystiedot[ytindex];
                        if (yt.osoite) {
                            var lang = (yt.kieli === null ? "kieli_fi#1" : yt.kieli);
                            if (yt.osoiteTyyppi === 'muu') {
                                // Muita osoitteita voi olla useita, lisää listaan
                                if (!model.yhteystiedot.postinumerot[lang][yt.osoiteTyyppi]) {
                                    model.yhteystiedot.postinumerot[lang][yt.osoiteTyyppi] = [];
                                }
                                model.yhteystiedot.postinumerot[lang][yt.osoiteTyyppi].push(arvoByUri[yt.postinumeroUri]);
                            } else {
                                model.yhteystiedot.postinumerot[lang][yt.osoiteTyyppi] = arvoByUri[yt.postinumeroUri];
                            }
                        }
                    }
                    model.mdyhteystiedot.postinumerot = {
                        'kieli_fi#1': {},
                        'kieli_sv#1': {},
                        'kieli_en#1': {}
                    };
                    model.mdyhteystiedot.postinumerot.muu = [];
                    if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                        for (var mytindex in model.organisaatio.metadata.yhteystiedot) {
                            var myt = model.organisaatio.metadata.yhteystiedot[mytindex];
                            if (myt.osoite) {
                                var mytlang = (myt.kieli === null ? "kieli_fi#1" : myt.kieli);
                                if (!(mytlang in model.mdyhteystiedot.postinumerot)) {
                                    model.mdyhteystiedot.postinumerot[mytlang] = {};
                                }
                                if (myt.osoiteTyyppi === 'muu') {
                                    // Muita osoitteita voi olla useita, lisää listaan
                                    model.mdyhteystiedot.postinumerot[mytlang][myt.osoiteTyyppi].push(arvoByUri[myt.postinumeroUri]);
                                } else {
                                    model.mdyhteystiedot.postinumerot[mytlang][myt.osoiteTyyppi] = arvoByUri[myt.postinumeroUri];
                                }
                            }
                        }
                    }
                    // jos ytj:stä saatu organisaatioon osoite tietoa --> päivitetään osoitteet
                    model.addYtjOsoite();
                }, function(response) {
                    // postinumeroita ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
            }
        };

        var refreshHenkilo = function() {
            model.henkilot.virkailijatTooltip = "";
            HenkiloVirkailijat.get({oid: model.organisaatio.oid}, function(result) {
                if (result.results) {
                    for (var i = 0; i < result.results.length; i++) {
                        model.henkilot.virkailijat.push({
                            nimi: result.results[i].etunimet + " " + result.results[i].sukunimi,
                            tiedot: result.results[i]});
                        model.henkilot.virkailijatTooltip += result.results[0].etunimet + " " + result.results[0].sukunimi + "<br>";
                    }
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                showAndLogError("Organisaationtarkastelu.henkilohakuvirhe", response);
            });
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
                model.uriLocalizedNames["parentnimi"] = getDecodedLocalizedValue(result.nimi, "", "", false);
                refreshParent(result);

                refreshKoodisto(null);
                refreshHenkilo();
                model.organisaatio.parentOid = parentoid;
                finishModel();
                refreshMetadata(model.organisaatio);

                // Jos yritystiedot on mukana --> täytetään tiedot
                if (typeof yritystiedot !== "undefined") {
                    model.fillYritysTiedot(yritystiedot);
                }

            }, function(response) {
                // postinumeroita ei löytynyt
                showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
            });
        };

        this.createOrganisaatioYTunnuksella = function(parentoid, ytunnus) {
            YTJYritysTiedot.get({'ytunnus': ytunnus}, function(result) {
                model.createOrganisaatio(parentoid, result);
            }, function(response) {
                // yritystietoa ei löytynyt
                showAndLogError("Organisaationtarkastelu.ytunnushakuvirhe", response);
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
                showAndLogError("Organisaationtarkastelu.ytunnushakuvirhe", response);
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
            if (yritystiedot.faksi) {
                model.yhteystiedot['kieli_fi#1'].faksi.numero = yritystiedot.faksi;
            }
            // kotipaikka / kotipaikkaKoodi, sitten kun koodiston kotipaikat on saatu
            if (yritystiedot.aloitusPvm) {
                model.organisaatio.alkuPvm = moment(yritystiedot.aloitusPvm, 'DD.MM.YYYY');
            }

            // YTunnuksella luotu organisaatio on oletusarvoisesti koulutustoimija
            // Ei kuitenkaan poisteta "Koulutustoimija" tyyppiä, jos se on jo asetettu
            var organisaatiotyyppi = "Koulutustoimija";
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

        // Konvertoi päivämäärät rajapinnan hyväksymään muotoon yyyy-mm-dd
        var formatDates = function() {
            if (model.organisaatio.alkuPvm) {
                model.organisaatio.alkuPvm = model.formatDate(model.organisaatio.alkuPvm);
            }
            if (model.organisaatio.lakkautusPvm) {
                model.organisaatio.lakkautusPvm = model.formatDate(model.organisaatio.lakkautusPvm);
            }
        };

        var clearAddress = function(address) {
            if (address) {
                for (var f in address) {
                    // Tyhjennä, vain tyyppi ja kieli jää placeholderiin
                    if (address.hasOwnProperty(f) && (f !== 'osoiteTyyppi') && (f !== 'kieli')) {
                        address[f] = null;
                    }
                }
            }
        };

        // Poistaa osoitetiedoista muut kuin valitun tyyppiset
        // Parametri:
        //      md - true: käsittele hakijapalveluiden yhteystietoja (metadata),
        //           false: käsittele organisaation yhteystietoja
        var selectAddressType = function(md) {
            var ytt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            var langs = (md ? model.mkSections.hp.tabs : [{lang: 'kieli_fi#1'}, {lang: 'kieli_sv#1'}, {lang: 'kieli_en#1'}]);
            for (var tab in langs) {
                var kv_lang = (md ? langs[tab].lang : langs[tab].lang);
                var yt = ytt[kv_lang];
                var osoiteMuoto = (md ? model.osoitemuoto.hp : model.osoitemuoto.yt);
                if (osoiteMuoto[langs[tab].lang] === 'suomalainen') {
                    clearAddress(yt.ulkomainen_kaynti);
                    clearAddress(yt.ulkomainen_posti);
                } else {
                    clearAddress(yt.kaynti);
                    clearAddress(yt.posti);
                }
            }
        };

        var checkLisayhteystiedot = function() {
            for (var i = model.organisaatio.yhteystietoArvos.length - 1; i >= 0; i--) {
                if ((model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.arvoText'] === null) ||
                        (model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.kieli'] === null)) {
                    model.organisaatio.yhteystietoArvos.splice(i, 1);
                }
            }
        };

        var getYhteystietoKielet = function(kieletUris) {
            var ret = {};
            if(angular.isDefined(kieletUris)) {
                for (var i = 0; i < kieletUris.length; i++) {
                    switch(kieletUris[i]) {
                        case 'oppilaitoksenopetuskieli_1#1':
                        case 'oppilaitoksenopetuskieli_5#1':
                            ret['kieli_fi#1'] = true;
                            break;
                        case 'oppilaitoksenopetuskieli_2#1':
                            ret['kieli_sv#1'] = true;
                            break;
                        case 'oppilaitoksenopetuskieli_3#1':
                            ret['kieli_fi#1'] = true;
                            ret['kieli_sv#1'] = true;
                            break;
                        case 'oppilaitoksenopetuskieli_4#1':
                        case 'oppilaitoksenopetuskieli_9#1':
                            ret['kieli_en#1'] = true;
                            break;
                    };
                }
            }
            else {
                $log.warn('getYhteystietoKielet :: kieletUris not defined.');
            }
            return ret;
        };

        this.setNimi = function(nimi) {
            model.organisaatio.nimi = nimi;
        };

        this.persistOrganisaatio = function(orgForm) {
            var deferred = $q.defer();
            formatDates();
            selectAddressType(false);
            selectAddressType(true);
            checkLisayhteystiedot();
            if (model.mode==="edit") {
                Organisaatio.post(model.organisaatio, function(result) {
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
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                    deferred.reject();
                });
            } else {
                // Asetetaan organisaation nimen alkupäiväksi organisaation alkupäivä
                model.organisaatio.nimet[0].alkuPvm = model.organisaatio.alkuPvm;

                UusiOrganisaatio.put(model.organisaatio, function(result) {
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
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                    deferred.reject();
                });
            }
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
            updateLisayhteystiedot();
        };

        this.selectOppilaitosTyyppi = function() {
            model.organisaatio.yhteystietoArvos = [];
            model.lisayhteystiedot = {};
            updateLisayhteystiedot();
        };

        this.toggleCheckVuosiluokka = function(vuosiluokka) {
            if (model.organisaatio.vuosiluokat.indexOf(vuosiluokka) === -1) {
                model.organisaatio.vuosiluokat.push(vuosiluokka);
            } else {
                model.organisaatio.vuosiluokat.splice(model.organisaatio.vuosiluokat.indexOf(vuosiluokka), 1);
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
            }
            else {
                $log.warn('removeLang :: model.organisaatio.kieletUris not defined.');
            }
            model.updateYhteystiedotValidity(ytform);
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
                    model.initYhteystiedotPlaceholder(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot,
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

        var isEmptyObject = function(obj) {
            for (var name in obj) {
                return false;
            }
            return true;
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
                        isEmptyObject(model.organisaatio.metadata.data[model.someplaceholder])) {
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
                return model.organisaatio.tyypit.indexOf(model.koodisto.localizedOppilaitos) !== -1;
            }
            return false;
        };

        this.isKoulutustoimija = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf(model.koodisto.localizedKoulutustoimija) !== -1;
            }
            return false;
        };

        this.isToimipiste = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf(model.koodisto.localizedToimipiste) !== -1;
            }
            return false;
        };

        this.hasVuosiluokat = function() {
            if (model.organisaatio.tyypit) {
                if (model.organisaatio.tyypit.indexOf(model.koodisto.localizedOppilaitos) !== -1) {
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
         *  md - true: käytä yhteystiedot-rakennetta
         *       false: käytä metadatan yhteystietoja
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
                } else {                     // kopioi kansainvälinen osoitemuoto
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
                    for (var i = 0; i < result.yhteystiedotRyhma[0].yhteystiedot.length; i++) {
                        if (result.yhteystiedotRyhma[0].yhteystiedot[i].yhteystietoTyyppi === 'YHTEYSTIETO_PUHELINNUMERO') {

                            model.organisaatio.metadata.hakutoimistoEctsPuhelin = result.yhteystiedotRyhma[0].yhteystiedot[i].yhteystietoArvo;
                        }
                        if (result.yhteystiedotRyhma[0].yhteystiedot[i].yhteystietoTyyppi === 'YHTEYSTIETO_SAHKOPOSTI') {

                            model.organisaatio.metadata.hakutoimistoEctsEmail = result.yhteystiedotRyhma[0].yhteystiedot[i].yhteystietoArvo;
                        }
                    }
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                $log.error($filter('i18n')("Organisaationtarkastelu.henkilohakuvirhe") + " (status: " + response.status + ")");
            });
            HenkiloKayttooikeus.get({hlooid: henkilo.tiedot.oidHenkilo, orgoid: model.organisaatio.oid}, function(result2) {
                if (result2.length > 0) {
                    model.organisaatio.metadata.hakutoimistoEctsTehtavanimike = result2[0].tehtavanimike;
                    // TODO: tarjoa käyttäjälle valintalista nimikkeistä (result[i].tehtavanimike) ?
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                $log.error($filter('i18n')("Organisaationtarkastelu.henkilohakuvirhe") + " (status: " + response.status + ")");
            });
        };

        this.getLocalizedLisatietoNimi = function(yta) {
            return getLocalizedValue(yta, 'YhteystietojenTyyppi.nimi.', '');
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
            var kielet = getYhteystietoKielet(model.organisaatio.kieletUris);
            for (var kieli in kielet) {
                $log.debug(kieli);
                if (kielet.hasOwnProperty(kieli)) {
                    if ((!model.yhteystiedot[kieli].posti.osoite || model.yhteystiedot[kieli].posti.osoite==='') &&
                            (!model.yhteystiedot[kieli].ulkomainen_posti || !model.yhteystiedot[kieli].ulkomainen_posti.osoite
                            || model.yhteystiedot[kieli].ulkomainen_posti.osoite==='')) {
                        model.ytinvalid.push(kieli);
                    }
                }
            }
        };

    };

    return model;
});



