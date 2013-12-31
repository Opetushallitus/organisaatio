app.factory('OrganisaatioModel', function(Organisaatio, Aliorganisaatiot, KoodistoSearchKoodis, KoodistoKoodi,
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoPaikkakunnat, KoodistoMaat, KoodistoKielet,
        KoodistoPosti, Alert, $filter) {
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
            //postinumerot2: [],
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
        this.mdyhteystiedot = {};

        // Päätason yhteystiedot mäpättynä tyypin perusteella
        this.yhteystiedot = {};
        
        // Sosiaalinen media
        this.sometext = {};
        this.some = [];
        this.sometypes = ['FACEBOOK', 'GOOGLE_PLUS', 'LINKED_IN', 'TWITTER', 'MUU'];
        for (var st in this.sometypes) {
            this.some.push({'type': this.sometypes[st], 'nimi': $filter('i18n')('Organisaationtarkastelu.' + this.sometypes[st])});
        }

        // koulutustarjoajatietojen monikielinen teksti
        this.lop = {};

        // TODO: Add also parent needed possibly for moving organisaatio

        // Palauta lokalisoitu arvo.
        // Jos lokalisoitua arvoa ei löydy, 
        //     palautetaan ensimmäinen löydetty arvo jos create==false
        //     tai luodaan uusi tyhjä arvo ja palautetaan se jos create==true
        // fi-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "kielivalikoima_fi" : "Suomenkielinen nimi"}, "kielivalikoima_") => "Suomenkielinen nimi"
        // sv-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "fi" : "Suomenkielinen nimi" , "sv" : "Samma på svenska"}, "") => "Samma på svenska"
        getLocalizedValue = function(res, prefix, create, language) {
            var lang = (language ? language : KoodistoKoodi.getLanguage().toLowerCase());
            var ret = "";
            if (res) {
                ret = res[prefix + lang];
                if (!ret) {
                    if (create) {
                        res[prefix + lang] = "";
                        return res[prefix + lang];
                    } else {
                        // Palauta ensimmäinen arvo
                        for (i in res) {
                            return res[i];
                        }
                    }
                }
            }
            return ret;
        };

        isMonikielinenTeksti = function(key) {
            return model.sometypes.indexOf(key) === -1;
        };

        refreshKoulutustarjoajatiedot = function(result) {
            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getLocalizedValue(result.metadata.hakutoimistonNimi, "kielivalikoima_", false);
                
                model.lop["fi"] = {};
                
                for (var k in result.metadata.data) {
                    if (isMonikielinenTeksti(k)) {
                        // TODO: eri kielet omiin indekseihin
                        model.uriLocalizedNames[k] = getLocalizedValue(result.metadata.data[k], "kielivalikoima_", true, "fi");
                        model.lop["fi"] = result.metadata.data[k];
                    }
                }
                result.metadata.data.YLEISKUVAUS.kielivalikoima_fi = "footer";
            }
        };

        refreshOpiskelijanedut = function(result) {
            model.uriLocalizedNames.opiskelijanedut = {};

            if (result.metadata) {
                // mäppäys tehdään jo refreshKoulutustarjoajatiedot -funktiossa
                
                /*
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
                */
            }
        };


        // Alusta objektit joita ei vielä ole asetettu, luo mäppäys modelYhteystiedoista
        // organisaatioYhteystietoihin yhteystiedon tyypin perusteella
        initYhteystiedot = function(organisaatioYhteystiedot, modelYhteystiedot) {
            if (organisaatioYhteystiedot) {
                modelYhteystiedot.muu = [];
                for (var ytindex in organisaatioYhteystiedot) {
                    var yt = organisaatioYhteystiedot[ytindex];
                    if (yt.osoite) {
                        if (yt.osoiteTyyppi === 'muu') {
                            // Muita osoitteita voi olla useita, lisää listaan
                            modelYhteystiedot[yt.osoiteTyyppi].push(yt);
                        } else {
                            modelYhteystiedot[yt.osoiteTyyppi] = yt;
                        }
                    } else if (yt.numero) {
                        modelYhteystiedot[yt.tyyppi] = yt;
                    } else if (yt.email) {
                        modelYhteystiedot.email = yt;
                    } else if (yt.www) {
                        modelYhteystiedot.www = yt;
                    }
                }
                tyypit = ['kaynti', 'posti', 'ruotsi_kaynti', 'ruotsi_posti'];
                for (var i = 0; i < tyypit.length; ++i) {
                    if (!modelYhteystiedot[tyypit[i]]) {
                        var uusiYt = {osoiteTyyppi: tyypit[i]};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[tyypit[i]] = uusiYt;
                    }
                }
                tyypit = ['email', 'www'];
                for (var i = 0; i < tyypit.length; ++i) {
                    if (!modelYhteystiedot[tyypit[i]]) {
                        var uusiYt = {};
                        uusiYt[tyypit[i]] = null;
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[tyypit[i]] = uusiYt;
                    }
                }
                tyypit = ['puhelin', 'faksi'];
                for (var i = 0; i < tyypit.length; ++i) {
                    if (!modelYhteystiedot[tyypit[i]]) {
                        var uusiYt = {tyyppi: tyypit[i]};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[tyypit[i]] = uusiYt;
                    }
                }
            }
        };

        finishModel = function() {
            if (model.organisaatio.yhteystiedot) {
                initYhteystiedot(model.organisaatio.yhteystiedot, model.yhteystiedot);
            }
            if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                initYhteystiedot(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot);
            }
        }

        refresh = function(oid) {
            Organisaatio.get({oid: oid}, function(result) {
                model.organisaatio = result;
                model.uriLocalizedNames["nimi"] = getLocalizedValue(result.nimi, "", false);

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
                    //model.koodisto.postinumerot2.length = 0;
                    model.koodisto.nimetFI = {};
                    model.koodisto.nimetSV = {};
                    var arvoByUri = {};
                    result.forEach(function(postiKoodi) {
                        if (KoodistoKoodi.isValid(postiKoodi)) {
                            model.koodisto.postinumerot.push(postiKoodi.koodiArvo);

                            // Mäppäys postinumerosta uriin ja postitoimipaikan käännökseen 
                            model.koodisto.nimetFI[postiKoodi.koodiArvo] = {
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "FI")
                            };
                            model.koodisto.nimetSV[postiKoodi.koodiArvo] = {
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "SV")
                            };

                            arvoByUri[postiKoodi.koodiUri] = postiKoodi.koodiArvo;

                        }
                    });
                    model.koodisto.postinumerot.sort();

                    // 
                    model.yhteystiedot.postinumerot = {};
                    model.yhteystiedot.postinumerot.muu = [];
                    for (var ytindex in model.organisaatio.yhteystiedot) {
                        var yt = model.organisaatio.yhteystiedot[ytindex];
                        if (yt.osoite) {
                            if (yt.osoiteTyyppi === 'muu') {
                                // Muita osoitteita voi olla useita, lisää listaan
                                model.yhteystiedot.postinumerot[yt.osoiteTyyppi].push(arvoByUri[yt.postinumeroUri]);
                            } else {
                                model.yhteystiedot.postinumerot[yt.osoiteTyyppi] = arvoByUri[yt.postinumeroUri];
                            }
                        }
                    }
                    model.mdyhteystiedot.postinumerot = {};
                    model.mdyhteystiedot.postinumerot.muu = [];
                    if (model.organisaatio.metadata && model.organisaatio.metadata.yhteystiedot) {
                        for (var ytindex in model.organisaatio.metadata.yhteystiedot) {
                            var yt = model.organisaatio.metadata.yhteystiedot[ytindex];
                            if (yt.osoite) {
                                if (yt.osoiteTyyppi === 'muu') {
                                    // Muita osoitteita voi olla useita, lisää listaan
                                    model.mdyhteystiedot.postinumerot[yt.osoiteTyyppi].push(arvoByUri[yt.postinumeroUri]);
                                } else {
                                    model.mdyhteystiedot.postinumerot[yt.osoiteTyyppi] = arvoByUri[yt.postinumeroUri];
                                }
                            }
                        }
                    }
                });
            }
        };

        this.persistOrganisaatio = function() {
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
                if (!model.organisaatio.metadata.data[model.organisaatio.someplaceholder]) {
                    model.organisaatio.metadata.data[model.organisaatio.someplaceholder] = {'0': model.sometext[model.organisaatio.someplaceholder]};
                }
            }
            model.organisaatio.someplaceholder = $filter('i18n')("lisaasosiaalinenmedia");
        };

        this.removeSome = function(some) {
            if (model.organisaatio.metadata) {
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
                for (kentta in model.yhteystiedot.kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.yhteystiedot.posti[kentta] = model.yhteystiedot.kaynti[kentta];
                    }
                }
                for (kentta in model.yhteystiedot.ruotsi_kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.yhteystiedot.ruotsi_posti[kentta] = model.yhteystiedot.ruotsi_kaynti[kentta];
                    }
                }
                for (kentta in model.yhteystiedot.kv_kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        model.yhteystiedot.kv_posti[kentta] = model.yhteystiedot.kv_kaynti[kentta];
                    }
                }
            }
        };

        this.addAddress = function() {
            if (model.organisaatio.yhteystiedot) {
                var uusiYt = {
                    osoiteTyyppi: 'muu',
                    postinumeroUri: null,
                    postitoimipaikka: null,
                    osoite: null
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
                    osoiteTyyppi: 'muu',
                    postinumeroUri: null,
                    postitoimipaikka: null,
                    osoite: null
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
            if (model.organisaatio.metadata) {
                model.organisaatio.metadata.kuvaEncoded = undefined;
            }
        };
    };

    return model;
});



