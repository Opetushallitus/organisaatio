app.factory('OrganisaatioModel', function(Organisaatio, Aliorganisaatiot, KoodistoSearchKoodis, KoodistoKoodi,
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoPaikkakunnat, KoodistoMaat, KoodistoKielet,
        KoodistoPosti, $filter) {
    var model = new function() {
        this.organisaatio = {};

        // Koodistodata organisaation muokkausta varten
        this.koodisto = {
            oid: 0,
            organisaatiotyypit: [],
            oppilaitostyypit: [],
            kotipaikat: [],
            maat: [],
            kielet: [],
            kieliplaceholder: $filter('i18n')("lisaakieli"),
            postinumerot: [],
            postinumerot2: [],
            nimetFI: {},
            nimetSV: {}
        };

        // Koodin lokalisoitu nimi, avaimena uri
        this.uriLocalizedNames = {};

        // Koko koodi, avaimena uri
        this.uriKoodit = {};

        // Aliorganisaatioiden nimet listana
        this.aliorganisaatiot = [];

        // Metadatan yhteystiedot mäpättynä tyypin perusteella
        this.yhteystiedot = {};

        // Sosiaaline media
        this.sometext = {};
        this.some = [];
        sometypes = ['FACEBOOK', 'GOOGLE_PLUS', 'LINKED_IN', 'TWITTER', 'MUU'];
        for (var st in sometypes) {
            this.some.push({'type': sometypes[st], 'nimi': $filter('i18n')('Organisaationtarkastelu.' + sometypes[st])});
        }

        // TODO: Add also parent needed possibly for moving organisaatio

        // Palauta lokalisoitu arvo tai ensimmäinen jos lokaalia ei löydy
        // fi-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "kielivalikoima_fi" : "Suomenkielinen nimi"}, "kielivalikoima_") => "Suomenkielinen nimi"
        // sv-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "fi" : "Suomenkielinen nimi" , "sv" : "Samma på svenska"}, "") => "Samma på svenska"
        getLocalizedValue = function(res, prefix) {
            var lang = KoodistoKoodi.getLanguage().toLowerCase();
            var ret = "";
            if (res) {
                ret = res[prefix + lang];
                if (!ret) {
                    for (i in res) {
                        return res[i];
                    }
                }
            }
            return ret;
        };

        refreshKoulutustarjoajatiedot = function(result) {
            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getLocalizedValue(result.metadata.hakutoimistonNimi, "kielivalikoima_");

                if (result.metadata.data.YLEISKUVAUS) {
                    model.uriLocalizedNames["YLEISKUVAUS"] =
                            getLocalizedValue(result.metadata.data.YLEISKUVAUS, "kielivalikoima_");
                }
                if (result.metadata.data.ESTEETOMYYS) {
                    model.uriLocalizedNames["ESTEETOMYYS"] =
                            getLocalizedValue(result.metadata.data.ESTEETOMYYS, "kielivalikoima_");
                }
                if (result.metadata.data.OPPIMISYMPARISTO) {
                    model.uriLocalizedNames["OPPIMISYMPARISTO"] =
                            getLocalizedValue(result.metadata.data.OPPIMISYMPARISTO, "kielivalikoima_");
                }
                if (result.metadata.data.VASTUUHENKILOT) {
                    model.uriLocalizedNames["VASTUUHENKILOT"] =
                            getLocalizedValue(result.metadata.data.VASTUUHENKILOT, "kielivalikoima_");
                }
                if (result.metadata.data.AIEMMIN_HANKITTU_OSAAMINEN) {
                    model.uriLocalizedNames["AIEMMIN_HANKITTU_OSAAMINEN"] =
                            getLocalizedValue(result.metadata.data.AIEMMIN_HANKITTU_OSAAMINEN, "kielivalikoima_");
                }
                if (result.metadata.data.VALINTAMENETTELY) {
                    model.uriLocalizedNames["VALINTAMENETTELY"] =
                            getLocalizedValue(result.metadata.data.VALINTAMENETTELY, "kielivalikoima_");
                }
                if (result.metadata.data.VUOSIKELLO) {
                    model.uriLocalizedNames["VUOSIKELLO"] =
                            getLocalizedValue(result.metadata.data.VUOSIKELLO, "kielivalikoima_");
                }
                if (result.metadata.data.KIELIOPINNOT) {
                    model.uriLocalizedNames["KIELIOPINNOT"] =
                            getLocalizedValue(result.metadata.data.KIELIOPINNOT, "kielivalikoima_");
                }
                if (result.metadata.data.OPISKELIJALIIKKUVUUS) {
                    model.uriLocalizedNames["OPISKELIJALIIKKUVUUS"] =
                            getLocalizedValue(result.metadata.data.OPISKELIJALIIKKUVUUS, "kielivalikoima_");
                }
                if (result.metadata.data.KANSAINVALISET_KOULUTUSOHJELMAT) {
                    model.uriLocalizedNames["KANSAINVALISET_KOULUTUSOHJELMAT"] =
                            getLocalizedValue(result.metadata.data.KANSAINVALISET_KOULUTUSOHJELMAT, "kielivalikoima_");
                }
                if (result.metadata.data.TYOHARJOITTELU) {
                    model.uriLocalizedNames["TYOHARJOITTELU"] =
                            getLocalizedValue(result.metadata.data.TYOHARJOITTELU, "kielivalikoima_");
                }
            }
        };

        refreshOpiskelijanedut = function(result) {
            model.uriLocalizedNames.opiskelijanedut = {};

            if (result.metadata) {
                if (result.metadata.data.KUSTANNUKSET) {
                    model.uriLocalizedNames.opiskelijanedut.KUSTANNUKSET =
                            getLocalizedValue(result.metadata.data.KUSTANNUKSET, "kielivalikoima_");
                }
                if (result.metadata.data.TIETOA_ASUMISESTA) {
                    model.uriLocalizedNames.opiskelijanedut.TIETOA_ASUMISESTA =
                            getLocalizedValue(result.metadata.data.TIETOA_ASUMISESTA, "kielivalikoima_");
                }
                if (result.metadata.data.RAHOITUS) {
                    model.uriLocalizedNames.opiskelijanedut.RAHOITUS =
                            getLocalizedValue(result.metadata.data.RAHOITUS, "kielivalikoima_");
                }
                if (result.metadata.data.OPISKELIJARUOKAILU) {
                    model.uriLocalizedNames.opiskelijanedut.OPISKELIJARUOKAILU =
                            getLocalizedValue(result.metadata.data.OPISKELIJARUOKAILU, "kielivalikoima_");
                }
                if (result.metadata.data.TERVEYDENHUOLTOPALVELUT) {
                    model.uriLocalizedNames.opiskelijanedut.TERVEYDENHUOLTOPALVELUT =
                            getLocalizedValue(result.metadata.data.TERVEYDENHUOLTOPALVELUT, "kielivalikoima_");
                }
                if (result.metadata.data.VAKUUTUKSET) {
                    model.uriLocalizedNames.opiskelijanedut.VAKUUTUKSET =
                            getLocalizedValue(result.metadata.data.VAKUUTUKSET, "kielivalikoima_");
                }
                if (result.metadata.data.OPISKELIJALIIKUNTA) {
                    model.uriLocalizedNames.opiskelijanedut.OPISKELIJALIIKUNTA =
                            getLocalizedValue(result.metadata.data.OPISKELIJALIIKUNTA, "kielivalikoima_");
                }
                if (result.metadata.data.VAPAA_AIKA) {
                    model.uriLocalizedNames.opiskelijanedut.VAPAA_AIKA =
                            getLocalizedValue(result.metadata.data.VAPAA_AIKA, "kielivalikoima_");
                }
                if (result.metadata.data.OPISKELIJA_JARJESTOT) {
                    model.uriLocalizedNames.opiskelijanedut.OPISKELIJA_JARJESTOT =
                            getLocalizedValue(result.metadata.data.OPISKELIJA_JARJESTOT, "kielivalikoima_");
                }
            }
        };

//        refreshSome = function(result) {
//            for (s in ['FACEBOOK','GOOGLE_PLUS','LINKED_IN','TWITTER','MUU']) {
//                model.sometext.push({s: });
//            }
//        };
//        
//        refreshSome2 = function(result) {
//            var somes = ['FACEBOOK','GOOGLE_PLUS','LINKED_IN','TWITTER','MUU'];
//            var somesLeft = ['FACEBOOK','GOOGLE_PLUS','LINKED_IN','TWITTER','MUU'];
//            model.some.length = 0;
//            if (result.metadata) {
//                for (s in result.metadata.data) {
//                    if (somes.indexOf(s) >- 1) {
//                        console.log("SOME: " + s);
//                        for (v in result.metadata.data[s]) {
//                            model.some.push({'type':s,'link':result.metadata.data[s][v],'nimi': $filter('i18n')('Organisaationtarkastelu.' + s)});
//                            console.log("SOMEV: " + result.metadata.data[s][v]);
//                            console.log("SOMEN: " + $filter('i18n')('Organisaationtarkastelu.' + s));    
//                        }
//                    }
//                    somesLeft.splice(somesLeft.indexOf(s));
//                }
//                for (sl in somesLeft) {
//                    model.some.push({'type':sl,'link':'','nimi': $filter('i18n')('Organisaationtarkastelu.' + sl)});
//                }
//            }
//        };

        finishModel = function() {
            if (!model.organisaatio.ruotsiKayntiOsoite) {
                model.organisaatio.ruotsiKayntiOsoite = {};
            }
            if (!model.organisaatio.ruotsiPostiOsoite) {
                model.organisaatio.ruotsiPostiOsoite = {};
            }
            if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                for (var yt in model.organisaatio.metadata.yhteystiedot) {
                    if (yt.osoite) {
                        model.yhteystiedot[yt.osoitetyyppi] = yt;                        
                    } else if (yt.numero) {
                        model.yhteystiedot[yt.tyyppi] = yt;
                    } else if (yt.email) {
                        model.yhteystiedot.email = yt;
                    } else if (yt.www) {
                        model.yhteystiedot.www = yt;
                    }
                }
            }
        };

        refresh = function(oid) {
            Organisaatio.get({oid: oid}, function(result) {
                model.organisaatio = result;
                model.uriLocalizedNames["nimi"] = getLocalizedValue(result.nimi, "");

                if (model.mode === 'edit') {
                    finishModel();
                }
                refreshKoulutustarjoajatiedot(result);
                refreshOpiskelijanedut(result);
                //refreshSome(result);

                // Hae kaikki koodi-urit kerralla
                var koodiUris = {};
                if (result["kotipaikkaUri"]) {
                    koodiUris[result["kotipaikkaUri"]] = true;
                }
                if (result["maaUri"]) {
                    koodiUris[result["maaUri"]] = true;
                }
                for (var i = 0; i < result["kieletUris"].length; i++) {
                    var param = result["kieletUris"][i];
                    if (param) {
                        koodiUris[param] = true;
                    }
                }
                if (result["kayntiosoite"]) {
                    if (result["kayntiosoite"]["postinumeroUri"]) {
                        koodiUris[result["kayntiosoite"]["postinumeroUri"]] = true;
                    }
                }
                if (result["postiosoite"]) {
                    if (result["postiosoite"]["postinumeroUri"]) {
                        koodiUris[result["postiosoite"]["postinumeroUri"]] = true;
                    }
                }
                if (result.metadata && result.metadata.yhteystiedot) {
                    for (var i = 0; i < result.metadata.yhteystiedot.length; i++) {
                        var osoite = result.metadata.yhteystiedot[i];
                        if (osoite.postinumeroUri) {
                            koodiUris[osoite.postinumeroUri] = true;
                        }
                    }
                }
                if (result["oppilaitosTyyppiUri"]) {
                    koodiUris[result["oppilaitosTyyppiUri"]] = true;
                    // Poistetaan lopusta #x jotta editointi toimii
                    var uri = model.organisaatio.oppilaitosTyyppiUri;
                    uri = uri.substring(0, uri.lastIndexOf("#"));
                    model.organisaatio.oppilaitosTyyppiUri = uri;
                }

                var searchParams = "";
                for (koodiUri in koodiUris) {
                    searchParams += "&koodiUris=" + koodiUri.split("#")[0];
                }
                searchParams = searchParams.substring(1, searchParams.length);
                KoodistoSearchKoodis.get({uris: searchParams}, function(result) {
                    for (var i = 0; i < result.length; i++) {
                        // Lisää kaikki koodit myös #<versio> -päätteisenä, koska result.koodiUri:ssa #<versio>
                        // -päätettä ei ole vaikka olisi annettu hakuparametrina
                        model.uriLocalizedNames[result[i]["koodiUri"]] = KoodistoKoodi.getLocalizedName(result[i]);
                        model.uriLocalizedNames[result[i]["koodiUri"] + "#" + result[i]["versio"]] = KoodistoKoodi.getLocalizedName(result[i]);
                        model.uriKoodit[result[i]["koodiUri"]] = result[i];
                        model.uriKoodit[result[i]["koodiUri"] + "#" + result[i]["versio"]] = result[i];
                    }
                });
            });
            Aliorganisaatiot.get({oid: oid}, function(result) {
                model.aliorganisaatiot.length = 0;
                if (result) {
                    for (var i = 0; i < result.length; i++) {
                        if (!result[i].lakkautusPvm) {
                            model.aliorganisaatiot.push(result[i].nimi.fi);
                        }
                    }
                }
            });
        };

        this.refreshIfNeeded = function(oid) {
            if (oid !== model.organisaatio.oid) {
                refresh(oid);
            }
        };

        this.refreshKoodisto = function(oid) {
            if (oid !== model.koodisto.oid) {
                model.koodisto.localizedOppilaitos = "";
                model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
                KoodistoOrganisaatiotyypit.get({oid: oid}, function(result) {
                    model.koodisto.organisaatiotyypit.length = 0;
                    result.forEach(function(orgTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(orgTyyppiKoodi)) {
                            if (model.mode === "edit" && (orgTyyppiKoodi.koodiArvo !== "01" && orgTyyppiKoodi.koodiArvo !== "05")) {
                                model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                            } else if (model.mode === "new") {
                                model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                            }
                            if (orgTyyppiKoodi.koodiArvo === "02") {
                                model.koodisto.localizedOppilaitos = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            }
                        }
                    });
                });
                KoodistoOppilaitostyypit.get({oid: oid}, function(result) {
                    model.koodisto.oppilaitostyypit.length = 0;
                    result.forEach(function(olTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(olTyyppiKoodi)) {
                            model.koodisto.oppilaitostyypit.push({uri: olTyyppiKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(olTyyppiKoodi)});
                        }
                    });
                });
                KoodistoPaikkakunnat.get({oid: oid}, function(result) {
                    model.koodisto.kotipaikat.length = 0;
                    result.forEach(function(kpKoodi) {
                        model.koodisto.kotipaikat.push({uri: kpKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(kpKoodi)});
                    });
                });
                KoodistoMaat.get({oid: oid}, function(result) {
                    model.koodisto.maat.length = 0;
                    result.forEach(function(maaKoodi) {
                        if (KoodistoKoodi.isValid(maaKoodi)) {
                            model.koodisto.maat.push({uri: maaKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(maaKoodi)});
                        }
                    });
                });
                KoodistoKielet.get({oid: oid}, function(result) {
                    model.koodisto.kielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        if (KoodistoKoodi.isValid(kieliKoodi)) {
                            model.koodisto.kielet.push({uri: kieliKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                            model.uriLocalizedNames[kieliKoodi.koodiUri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                        }
                    });
                });
                KoodistoPosti.get({oid: oid}, function(result) {
                    model.koodisto.postinumerot.length = 0;
                    model.koodisto.postinumerot2.length = 0;
                    model.koodisto.nimetFI = {};
                    model.koodisto.nimetSV = {};
                    result.forEach(function(postiKoodi) {
                        if (KoodistoKoodi.isValid(postiKoodi)) {
                            model.koodisto.postinumerot.push(postiKoodi.koodiArvo);
                            model.koodisto.postinumerot2.push({uri: postiKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(postiKoodi),
                                arvo: postiKoodi.koodiArvo + " " + KoodistoKoodi.getLangName(postiKoodi, "FI"),
                                arvosv: postiKoodi.koodiArvo + " " + KoodistoKoodi.getLangName(postiKoodi, "SV")});
                            //model.koodisto.nimetFI[postiKoodi.koodiUri] = KoodistoKoodi.getLangName(postiKoodi, "FI");
                            model.koodisto.nimetFI[postiKoodi.koodiArvo] = { 
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "FI")
                            };
                            model.koodisto.nimetSV[postiKoodi.koodiArvo] = { 
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "SV")
                            };
                            model.koodisto.nimetSV[postiKoodi.koodiUri] = KoodistoKoodi.getLangName(postiKoodi, "SV");
                            if (postiKoodi.koodiUri===model.organisaatio.kayntiosoite.postinumeroUri) {
                                model.koodisto.nimetFI.kayntiosoite = postiKoodi.koodiArvo;
                            }
                            if (postiKoodi.koodiUri===model.organisaatio.postiosoite.postinumeroUri) {
                                model.koodisto.nimetFI.postiosoite = postiKoodi.koodiArvo;
                            }
                            if (postiKoodi.koodiUri===model.organisaatio.ruotsiKayntiOsoite.postinumeroUri) {
                                model.koodisto.nimetSV.kayntiosoite = postiKoodi.koodiArvo;
                            }
                            if (postiKoodi.koodiUri===model.organisaatio.ruotsiPostiOsoite.postinumeroUri) {
                                model.koodisto.nimetSV.postiosoite = postiKoodi.koodiArvo;
                            }
                            if (model.organisaatio.muutOsoitteet) {
                                for (var i = 0; i < model.organisaatio.muutOsoitteet.length; i++) {
                                    if (model.organisaatio.muutOsoitteet[i].suomiOsoite) {
                                        model.koodisto.nimetFI['muuosoite'+i] = postiKoodi.koodiArvo;
                                    }
                                    if (model.organisaatio.muutOsoitteet[i].ruotsiOsoite) {
                                        model.koodisto.nimetSV['muuosoite'+i] = postiKoodi.koodiArvo;
                                    }
                                }
                            }
                        } 
                    });
                    model.koodisto.postinumerot.sort();
                });
            }
        };

        this.persistOrganisaatio = function() {
            //Organisaatio.post(model.organisaatio, function(result) {
            //});
            model.organisaatio.$post();
        };

        this.toggleCheck = function(organisaatiotyyppi) {
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                model.organisaatio.tyypit.push(organisaatiotyyppi);
            } else {
                model.organisaatio.tyypit.splice(model.organisaatio.tyypit.indexOf(organisaatiotyyppi), 1);
            }
        };

        this.addLang = function() {
            if (model.organisaatio.kieletUris.indexOf(model.organisaatio.kieliplaceholder) === -1) {
                if (model.organisaatio.kieliplaceholder && (model.organisaatio.kieliplaceholder !== $filter('i18n')("lisaakieli"))) {
                    model.organisaatio.kieletUris.push(model.organisaatio.kieliplaceholder);
                }
            }
            model.organisaatio.kieliplaceholder = $filter('i18n')("lisaakieli");
        };

        this.removeLang = function(lang) {
            model.organisaatio.kieletUris.pop(lang);
        };

        this.addSome = function() {
            if (model.organisaatio.metadata) {
                console.log("addSOME:" + JSON.stringify(model.organisaatio.someplaceholder));
                if (!model.organisaatio.metadata.data[model.organisaatio.someplaceholder]) {
                    console.log("addSOME:" + JSON.stringify(model.sometext[model.organisaatio.someplaceholder]));
                    model.organisaatio.metadata.data[model.organisaatio.someplaceholder] = {'0': model.sometext[model.organisaatio.someplaceholder]};
                }
            }
            model.organisaatio.someplaceholder = $filter('i18n')("lisaasosiaalinenmedia");
        };

        this.removeSome = function(some) {
            if (model.organisaatio.metadata) {
                console.log("removeSOME:" + JSON.stringify(some));
                console.log("removeSOME:" + JSON.stringify(model.organisaatio.metadata.data[some]));
                if (model.organisaatio.metadata.data[some]) {
                    delete model.organisaatio.metadata.data[some];
                }
            }
        };

        this.isOppilaitos = function() {
            if (model.organisaatio.tyypit) {
                return model.organisaatio.tyypit.indexOf(model.koodisto.localizedOppilaitos) !== -1;
            }
        };

        this.setPostinumero = function(addressmodel, postcode) {
            if (addressmodel && postcode) {
                addressmodel.postinumeroUri = model.koodisto.nimetFI[postcode].uri;
                addressmodel.postitoimipaikka = model.koodisto.nimetFI[postcode].paikka;
            }
        };

        this.setRuotsiPostinumero = function(addressmodel, postcode) {
            if (addressmodel && postcode) {
                addressmodel.postinumeroUri = model.koodisto.nimetSV[postcode].uri;
                addressmodel.postitoimipaikka = model.koodisto.nimetSV[postcode].paikka;
            }
        };

        this.copyAddress = function() {
            if (model.samaosoite) {
                for (kentta in model.organisaatio.kayntiosoite) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.organisaatio.postiosoite[kentta] = model.organisaatio.kayntiosoite[kentta];
                    }
                }
                for (kentta in model.organisaatio.ruotsiKayntiOsoite) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.organisaatio.ruotsiPostiOsoite[kentta] = model.organisaatio.ruotsiKayntiOsoite[kentta];
                    }
                }
                for (kentta in model.organisaatio.kvKayntiOsoite) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.organisaatio.kvPostiOsoite[kentta] = model.organisaatio.kvKayntiOsoite[kentta];
                    }
                }
            }
        };

        this.addAddress = function() {
            if (!model.organisaatio.muutOsoitteet) {
                model.organisaatio.muutOsoitteet = [];
            }            
            model.organisaatio.muutOsoitteet.push({suomiOsoite: {osoiteTyyppi: 'muu'}, ruotsiOsoite: {osoiteTyyppi: 'muu'}, kvOsoite: {osoiteTyyppi: 'muu'}});            
        };

        this.removeAddress = function(index) {
            if (model.organisaatio.muutOsoitteet) {
                model.organisaatio.muutOsoitteet.splice(index, 1);
            }
        };
        
        this.addKtAddress = function() {
            // FIXME: metadataan
            if (!model.organisaatio.ktMuutOsoitteet) {
                model.organisaatio.ktMuutOsoitteet = [];
            }
            model.organisaatio.ktMuutOsoitteet.push({suomiOsoite: {osoiteTyyppi: 'muu'}, ruotsiOsoite: {osoiteTyyppi: 'muu'}, kvOsoite: {osoiteTyyppi: 'muu'}});
        };

        this.removeKtAddress = function(index) {
            if (model.organisaatio.ktMuutOsoitteet) {
                model.organisaatio.ktMuutOsoitteet.splice(index, 1);
            }
        };
        
        this.removeImage = function() {
            if (model.organisaatio.metadata) {
                model.organisaatio.metadata.kuvaEncoded = undefined;
            }
        };
    };

    return model;
});



