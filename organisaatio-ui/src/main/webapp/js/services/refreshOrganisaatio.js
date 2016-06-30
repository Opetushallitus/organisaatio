/*
 Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus

 refreshFunctions program is free software:  Licensed under the EUPL, Version 1.1 or - as
 soon as they will be approved by the European Commission - subsequent versions
 of the EUPL (the "Licence");

 You may not use refreshFunctions work except in compliance with the Licence.
 You may obtain a copy of the Licence at: http://www.osor.eu/eupl/

 refreshFunctions program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 European Union Public Licence for more details.
 */

var app = angular.module('organisaatio');

app.factory('RefreshOrganisaatio', function ($filter, $log, $timeout, $injector,
                                             Organisaatio, Organisaatiot,
                                             OrganisaatioHistoria,
                                             KoodistoKoodi, KoodistoClient,
                                             Alert, HenkiloVirkailijat,
                                             Henkilo, Paivittaja, NimiHistoriaModel,
                                             LocalisationService, RefreshKoodisto) {
    $log = $log.getInstance("OrganisaatioModel");
    var loadingService = $injector.get('LoadingService');
    var refreshFunctions = {
        getDecodedLocalizedValue: function(res, prefix, suffix, create, language) {
            var ret = refreshFunctions.getLocalizedValue(res, prefix, suffix, create, language);
            if (ret) {
                return ret.replace(/&amp;/g, '&');
            }
        },
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
        getLocalizedValue: function(res, prefix, suffix, create, language) {
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
        },

        initMk: function(mkSection, model) {
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
        },

        refreshMetadata: function(result, model) {
            model.mkSections.kt.tabs.length = 0;
            model.mkSections.hp.tabs.length = 0;
            model.mkSections.oe.tabs.length = 0;
            model.mkSections.sm.tabs.length = 0;
            model.mkSections.ects.tabs.length = 0;

            // for loop indeksi
            var i;

            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                    refreshFunctions.getDecodedLocalizedValue(result.metadata.hakutoimistonNimi, "kieli_", "#1", false);
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
                    refreshFunctions.initMk(model.mkSections.kt, model);
                    refreshFunctions.initMk(model.mkSections.oe, model);
                    refreshFunctions.initMk(model.mkSections.sm, model);
                    refreshFunctions.initMk(model.mkSections.ects, model);
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
        },

        // Alusta objektit joita ei vielä ole asetettu, luo mäppäys modelYhteystiedoista
        // organisaatioYhteystietoihin yhteystiedon tyypin perusteella
        initYhteystiedot: function(organisaatioYhteystiedot, modelYhteystiedot, muoto) {
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
            refreshFunctions.initYhteystiedotPlaceholder(organisaatioYhteystiedot, modelYhteystiedot,
                ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1']);
        },

        initYhteystiedotPlaceholder: function(organisaatioYhteystiedot, modelYhteystiedot, kielet) {
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
        },

        finishModel: function(model) {
            if (model.organisaatio.yhteystiedot) {
                refreshFunctions.initYhteystiedot(model.organisaatio.yhteystiedot, model.yhteystiedot, model.osoitemuoto.yt);
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

            refreshFunctions.initYhteystiedot(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot, model.osoitemuoto.hp);
        },

        // Näyttää käyttäjälle virheen Alert-servicen avulla ja loggaa responsen statuksen
        showAndLogError: function(msg, response, model, loadingService) {
            loadingService.onErrorHandled(response);
            $log.error(msg + " (status: " + response.status + ")");
            model.alert = Alert.add("error", $filter('i18n')(response.data ? response.data.errorKey : msg), false);
        },

        refreshLisayhteystietoArvos: function(model) {
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
        },

        refreshParent: function(parentResult, model) {
            model.uriLocalizedNames["parentnimi"] = refreshFunctions.getDecodedLocalizedValue(parentResult.nimi, "", "", false);
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
        },

        refresh: function(result, model) {
            $log.info("refresh()");

            $log.info("refresh: mode=" + model.mode);
            // tyhjennetään mahdolliset vanhat ytj tiedot
            model.ytjTiedot = {};
            modelYhteystiedot = {};
            model.organisaatio = result;
            model.uriLocalizedNames["nimi"] = refreshFunctions.getDecodedLocalizedValue(result.nimi, "", "", false);
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

                    refreshFunctions.refreshParent(parentResult, model);

                    if (model.mode === 'edit') {
                        RefreshKoodisto(result.parentOid, model);
                        refreshFunctions.refreshHenkilo(model);
                    }
                    refreshFunctions.finishModel(model);
                    refreshFunctions.refreshMetadata(result, model);
                    refreshFunctions.refreshLisayhteystietoArvos(model);
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
                    KoodistoClient.koodistoSearchKoodis.get({uris: searchParams}, function(koodiResult) {
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
                            refreshFunctions.showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response, model, loadingService);
                        });
                    model.koodisto.localizedKoulutustoimija = "Koulutustoimija";
                    model.koodisto.localizedOppilaitos = "Oppilaitos";
                    model.koodisto.localizedToimipiste = "Toimipiste";
                },
                // Error case
                function(response) {
                    // parenttia ei löytynyt
                    refreshFunctions.showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response, model, loadingService);
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
                                refreshFunctions.addAliorganisaatio(childResult.organisaatiot[i].children, 0, model);
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
                    refreshFunctions.showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response, model, loadingService);
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
                                loadingService.onErrorHandled(response);
                                model.paivitys.paivittaja = paivitys.paivittaja;
                            });
                    }
                },
                // Error case
                function(response) {
                    // Päivittäjän haku ei onnistunut
                    refreshFunctions.showAndLogError("Organisaationtarkastelu.paivittajahakuvirhe", response, model, loadingService);
                });

            model.historia = {};
            OrganisaatioHistoria.get({oid: result.oid}, function(historia) {
                    model.historia = historia;
                },
                // Error case
                function(response) {
                    // Historian haku ei onnistunut
                    refreshFunctions.showAndLogError("Organisaationtarkastelu.historiahakuvirhe", response, model, loadingService);
                });
        },

        addAliorganisaatio: function(aliOrgList, level, model) {
            if (aliOrgList) {
                for (var j = 0; j < aliOrgList.length; j++) {
                    if (!aliOrgList[j].lakkautusPvm) {
                        model.aliorganisaatiot.push({nimi: refreshFunctions.getDecodedLocalizedValue(aliOrgList[j].nimi, "", ""), oid: aliOrgList[j].oid, level: level});
                        refreshFunctions.addAliorganisaatio(aliOrgList[j].children, level + 1, model);
                    }
                }
            }
        },

        refreshHenkilo: function(model) {
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
                refreshFunctions.showAndLogError("Organisaationtarkastelu.henkilohakuvirhe", response, model, loadingService);
            });
        },

        // Konvertoi päivämäärät rajapinnan hyväksymään muotoon yyyy-mm-dd
        formatDates: function(model) {
            if (model.organisaatio.alkuPvm) {
                model.organisaatio.alkuPvm = model.formatDate(model.organisaatio.alkuPvm);
            }
            if (model.organisaatio.lakkautusPvm) {
                model.organisaatio.lakkautusPvm = model.formatDate(model.organisaatio.lakkautusPvm);
            }
        },

        clearAddress: function(address) {
            if (address) {
                for (var f in address) {
                    // Tyhjennä, vain tyyppi ja kieli jää placeholderiin
                    if (address.hasOwnProperty(f) && (f !== 'osoiteTyyppi') && (f !== 'kieli')) {
                        address[f] = null;
                    }
                }
            }
        },

        // Poistaa osoitetiedoista muut kuin valitun tyyppiset
        // Parametri:
        //      md - true: käsittele hakijapalveluiden yhteystietoja (metadata),
        //           false: käsittele organisaation yhteystietoja
        selectAddressType: function(md, model) {
            var ytt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            var langs = (md ? model.mkSections.hp.tabs : [{lang: 'kieli_fi#1'}, {lang: 'kieli_sv#1'}, {lang: 'kieli_en#1'}]);
            for (var tab in langs) {
                var kv_lang = (md ? langs[tab].lang : langs[tab].lang);
                var yt = ytt[kv_lang];
                var osoiteMuoto = (md ? model.osoitemuoto.hp : model.osoitemuoto.yt);
                if (osoiteMuoto[langs[tab].lang] === 'suomalainen') {
                    refreshFunctions.clearAddress(yt.ulkomainen_kaynti);
                    refreshFunctions.clearAddress(yt.ulkomainen_posti);
                } else {
                    refreshFunctions.clearAddress(yt.kaynti);
                    refreshFunctions.clearAddress(yt.posti);
                }
            }
        },

        checkLisayhteystiedot: function(model) {
            for (var i = model.organisaatio.yhteystietoArvos.length - 1; i >= 0; i--) {
                if ((model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.arvoText'] === null) ||
                    (model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.kieli'] === null)) {
                    model.organisaatio.yhteystietoArvos.splice(i, 1);
                }
            }
        },

        getYhteystietoKielet: function(kieletUris) {
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
                    }
                }
            }
            else {
                $log.warn('getYhteystietoKielet :: kieletUris not defined.');
            }
            return ret;
        },

        isEmptyObject: function(obj) {
            for (var name in obj) {
                return false;
            }
            return true;
        }
    };
    return refreshFunctions;
});

app.factory('LisaYhteystiedot', function () {
    var lisaYhteystiedot = {
        updateLisayhteystietoArvos: function(lisatieto, model) {
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
        },

        updateLisayhteystiedot: function(model) {
            model.lisayhteystiedot = {};
            var kaikkiTyypit = model.organisaatio.tyypit;
            if (model.organisaatio.oppilaitosTyyppiUri) {
                kaikkiTyypit = kaikkiTyypit.concat(model.organisaatio.oppilaitosTyyppiUri);
            }
            for (var tyyppi in kaikkiTyypit) {
                if (model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()]) {
                    model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()].forEach(function(t) {
                        lisaYhteystiedot.updateLisayhteystietoArvos(t, model);
                    });
                }
            }
        }
    };
    
    return lisaYhteystiedot;
});
