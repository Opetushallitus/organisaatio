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

var koodisto = angular.module('Koodisto');

koodisto.factory('RefreshKoodisto', function($filter, $q, $cookieStore, $injector, $log, KoodistoClient,
                                             Yhteystietojentyyppi, KoodistoKoodi) {
    var loadingService = $injector.get('LoadingService');

    // Näyttää käyttäjälle virheen Alert-servicen avulla ja loggaa responsen statuksen
    var showAndLogError = function(msg, response) {
        loadingService.onErrorHandled(response);
        $log.error(msg + " (status: " + response.status + ")");
        model.alert = Alert.add("error", $filter('i18n')(response.data ? response.data.errorKey : msg), false);
    };

    return function(oid, model) {
        if (oid === null || (oid !== model.koodisto.oid)) {
            model.koodisto.localizedOppilaitos = "";
            model.koodisto.localizedKoulutustoimija = "";
            model.koodisto.localizedToimipiste = "";
            model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
            KoodistoClient.koodistoOrganisaatiotyypit.get({}, function(result) {
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
                        'Tyoelamajarjesto': ["06","03"]};
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
                },
                // Error case
                function(response) {
                    // oppilaitostyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.yhteystietojentyyppihakuvirhe", response);
                });
            KoodistoClient.koodistoOppilaitostyypit.get({}, function(result) {
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
            KoodistoClient.koodistoPaikkakunnat.get({}, function(result) {
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
                        KoodistoClient.koodistoPaikkakunta.get({uri: model.organisaatio.kotipaikkaUri}, function(result) {
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
            KoodistoClient.koodistoMaat.get({}, function(result) {
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
            KoodistoClient.koodistoKieli.get({}, function(result) {
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
            KoodistoClient.koodistoOpetuskielet.get({}, function(result) {
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
            KoodistoClient.koodistoVuosiluokat.get({}, function(result) {
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
                KoodistoClient.koodistoPostiVersio.get({}, function(result) {
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
                    KoodistoClient.koodistoPostiCached.get({}, function(result) {
                        deferred.resolve(result);
                    }, function(response) {
                        deferred.reject();
                    });
                } else {
                    KoodistoClient.koodistoPosti.get({}, function(result) {
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
});
