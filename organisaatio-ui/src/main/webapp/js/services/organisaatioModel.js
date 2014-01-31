app.factory('OrganisaatioModel', function(Organisaatio, Aliorganisaatiot, KoodistoSearchKoodis, KoodistoKoodi,
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoPaikkakunnat, KoodistoMaat, KoodistoKielet,
        KoodistoPosti, KoodistoVuosiluokat, UusiOrganisaatio, YTJYritysTiedot, Alert, $filter, $log) {
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
            vuosiluokat: [],
            kieliplaceholder: $filter('i18n')("lisaakieli"),
            ktkieliplaceholder: $filter('i18n')("lisaakieli"),
            oekieliplaceholder: $filter('i18n')("lisaakieli"),
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
        this.kttypes = ['YLEISKUVAUS', 'ESTEETOMYYS', 'OPPIMISYMPARISTO', 'VUOSIKELLO', 'VASTUUHENKILOT',
            'VALINTAMENETTELY', 'AIEMMIN_HANKITTU_OSAAMINEN', 'KIELIOPINNOT', 'TYOHARJOITTELU', 'OPISKELIJALIIKKUVUUS',
            'KANSAINVALISET_KOULUTUSOHJELMAT'];
        this.oetypes = ['KUSTANNUKSET', 'TIETOA_ASUMISESTA', 'RAHOITUS', 'OPISKELIJARUOKAILU', 'TERVEYDENHUOLTOPALVELUT',
            'VAKUUTUKSET', 'OPISKELIJALIIKUNTA', 'VAPAA_AIKA', 'OPISKELIJA_JARJESTOT'];
        for (var st in this.sometypes) {
            this.some.push({'type': this.sometypes[st], 'nimi': $filter('i18n')('Organisaationtarkastelu.' + this.sometypes[st])});
        }

        // YTJ rajapinnan kautta saadut yrityksen tiedot
        this.ytjTiedot = {};

        this.OPHOid = "1.2.246.562.10.00000000001";

        this.savestatus = $filter('i18n')("Organisaationmuokkaus.tietojaeitallennettu");

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

        getMonikielinenTekstiLanguages = function(mkt) {
            ret = [];
            for (k in mkt) {
                ret.push(k);
            }
            return ret;
        };

        refreshMetadata = function(result) {
            model.kttabs = [];
            model.oetabs = [];
            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getLocalizedValue(result.metadata.hakutoimistonNimi, "kielivalikoima_", false);

                if (result.metadata.data) {
                    var ktlangs = {};
                    for (var key in model.kttypes) {
                        for (var lang in result.metadata.data[model.kttypes[key]]) {
                            if (result.metadata.data[model.kttypes[key]].hasOwnProperty(lang)) {
                                ktlangs[lang] = true;
                            }

                        }
                    }
                    for (lang in ktlangs) {
                        model.kttabs.push({lang: lang, active: false});
                    }
                    var oelangs = {};
                    for (var key in model.oetypes) {
                        for (var lang in result.metadata.data[model.oetypes[key]]) {
                            if (result.metadata.data[model.oetypes[key]].hasOwnProperty(lang)) {
                                oelangs[lang] = true;
                            }

                        }
                    }
                    for (lang in oelangs) {
                        model.oetabs.push({lang: lang, active: false});
                    }
                }
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
            if (!model.organisaatio.metadata) {
                model.organisaatio.metadata = {};
            }
            if (!model.organisaatio.metadata.yhteystiedot) {
                model.organisaatio.metadata.yhteystiedot = [];
            }
            if (!model.organisaatio.metadata.data) {
                model.organisaatio.metadata.data = {};
            }
            initYhteystiedot(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot);
        };

        // Näyttää käyttäjälle virheen Alert-servicen avulla ja loggaa responsen statuksen
        showAndLogError = function(msg, response) {
            Alert.add("error", $filter('i18n')(msg, ""), false);
            $log.error(msg + " (status: " + response.status + ")");
        };

        refresh = function(oid) {
            // tyhjennetään mahdolliset vanhat ytj tiedot
            model.ytjTiedot = {};

            // haetaan organisaation tiedot
            Organisaatio.get({oid: oid}, function(result) {
                model.organisaatio = result;
                model.uriLocalizedNames["nimi"] = getLocalizedValue(result.nimi, "", false);

                Organisaatio.get({oid: result.parentOid}, function(parentResult) {
                    model.uriLocalizedNames["parentnimi"] = getLocalizedValue(parentResult.nimi, "", false);
                    model.parenttype = parentResult.tyypit[0];
                    model.parent = parentResult;

                    if (model.mode === 'edit') {
                        model.refreshKoodisto();
                    }
                    finishModel();
                    refreshMetadata(result);

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
                    for (var i = 0; i < result["vuosiluokat"].length; i++) {
                        var param = result["vuosiluokat"][i];
                        if (param) {
                            koodiUris[param] = true;
                        }
                    }

                    for (yht in result.yhteystiedot) {
                        if (result.yhteystiedot[yht].postinumeroUri) {
                            koodiUris[result.yhteystiedot[yht].postinumeroUri] = true;
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
                    if (result.metadata && result.metadata.data) {
                        for (var key in result.metadata.data) {
                            if (result.metadata.data.hasOwnProperty(key)) {
                                for (var lang in result.metadata.data[key]) {
                                    if (result.metadata.data[key].hasOwnProperty(lang)) {
                                        koodiUris[lang] = (lang.indexOf("kielivalikoima_") === 0);
                                    }
                                }
                            }
                        }
                    }
                    if (result.oppilaitosTyyppiUri) {
                        koodiUris[result.oppilaitosTyyppiUri] = true;
                        // Poistetaan lopusta #x jotta editointi toimii
                        model.organisaatio.oppilaitosTyyppiUri = model.organisaatio.oppilaitosTyyppiUri.split("#")[0];
                    }

                    // Poistetaan versiotieto vuosiluokat-listasta
                    vuosiluokat = model.organisaatio.vuosiluokat.slice(0);
                    model.organisaatio.vuosiluokat.length = 0;
                    if (vuosiluokat) {
                        for (vl in vuosiluokat) {
                            model.organisaatio.vuosiluokat.push(vuosiluokat[vl].split("#")[0]);
                        }
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
                    model.koodisto.localizedKoulutustoimija = "Koulutustoimija";
                    model.koodisto.localizedOppilaitos = "Oppilaitos";
                }, function(response) {
                    // parenttia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
                });
                Aliorganisaatiot.get({oid: oid}, function(result) {
                    model.aliorganisaatiot.length = 0;
                    if (result && result.organisaatiot) {
                        for (var i = 0; i < result.organisaatiot.length; i++) {
                            if (!result.organisaatiot[i].lakkautusPvm) {
                                addAliorganisaatio(result.organisaatiot[i].children, 0);
                            }
                        }
                    }
                }, function(response) {
                    // aliorganisaatiohaku ei onnistunut
                    showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);

                });
            }, function(response) {
                // Organisaatiohaku ei onnistunut
                showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
            });
        };

        addAliorganisaatio = function(aliOrgList, level) {
            if (aliOrgList) {
                for (var j = 0; j < aliOrgList.length; j++) {
                    if (!aliOrgList[j].lakkautusPvm) {
                        model.aliorganisaatiot.push({nimi: getLocalizedValue(aliOrgList[j].nimi, ""), oid: aliOrgList[j].oid, level: level});
                        addAliorganisaatio(aliOrgList[j].children, level + 1);
                    }
                }
            }
        };

        isParentOrganisaatio = function() {
            return (!model.organisaatio.parentOid || model.organisaatio.parentOid === model.OPHOid);
        };

        this.refreshIfNeeded = function(oid) {
            if (oid) {// && (oid !== model.organisaatio.oid)) {
                refresh(oid);
            }
        };

        this.refreshKoodisto = function(oid) {
            if (oid === null || (oid !== model.koodisto.oid)) {
                model.koodisto.localizedOppilaitos = "";
                model.koodisto.localizedKoulutustoimija = "";
                model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
                model.koodisto.ktkieliplaceholder = $filter('i18n')("lisaakieli");
                model.koodisto.oekieliplaceholder = $filter('i18n')("lisaakieli");
                KoodistoOrganisaatiotyypit.get({}, function(result) {
                    model.koodisto.organisaatiotyypit.length = 0;
                    /* Jos organisaatio on OPPILAITOS, sillä on oltava yläorganisaatio tyypiltään KOULUTUSTOIMIJA.
                     Jos organisaatio on MUU ORGANISAATIO tai KOULUTUSTOMIJA ja sille on määritelty yläorganisaatio,
                     on yläorganisaation oltava joko OPH tai MUU ORGANISAATIO.
                     Jos organisaatio on OPETUSPISTE (eli toimipiste), sillä on oltava yläorganisaatio joka on tyypiltään joko
                     OPETUSPISTE, OPPILAITOS tai KOULUTUSTOIMIJA.
                     Siis: OPH [1] -> MUU ORGANISAATIO [0..n] -> KOULUTUSTOIMIJA [1] -> OPPILAITOS [0..1] -> OPETUSPISTE [0..n]
                     Koodiston tyypit: 01:Koulutustoimija, 02:Oppilaitos, 03:Toimipiste, 04:Oppisopimustoimipiste, 05:Muu organisaatio
                     OPH-organisaation tyyppi on 'Muu organisaatio'
                     */
                    sallitutAlaOrganisaatiot = {
                        'Muu organisaatio': ["01", "05"],
                        'Koulutustoimija': ["02", "03", "04"],
                        'Oppilaitos': ["03", "04"],
                        'Opetuspiste': ["03", "04"],
                        'Oppisopimustoimipiste': ["03", "04"]
                    };
                    result.forEach(function(orgTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(orgTyyppiKoodi)) {
                            if (sallitutAlaOrganisaatiot[model.parenttype].indexOf(orgTyyppiKoodi.koodiArvo) !== -1) {
                                if (orgTyyppiKoodi.koodiArvo === "03") {
                                    // Koodistossa 'Opetuspiste' on 'Toimipiste'!?
                                    model.koodisto.organisaatiotyypit.push('Opetuspiste');
                                } else {
                                    model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                                }
                            }

                            if (orgTyyppiKoodi.koodiArvo === "01") {
                                model.koodisto.localizedKoulutustoimija = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            } else if (orgTyyppiKoodi.koodiArvo === "02") {
                                model.koodisto.localizedOppilaitos = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            }
                        }
                    });
                }, function(response) {
                    // organisaatiotyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoOppilaitostyypit.get({}, function(result) {
                    model.koodisto.oppilaitostyypit.length = 0;
                    result.forEach(function(olTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(olTyyppiKoodi)) {
                            model.koodisto.oppilaitostyypit.push({uri: olTyyppiKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(olTyyppiKoodi)});
                        }
                    });
                }, function(response) {
                    // oppilaitostyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoPaikkakunnat.get({}, function(result) {
                    model.koodisto.kotipaikat.length = 0;
                    result.forEach(function(kpKoodi) {
                        model.koodisto.kotipaikat.push({uri: kpKoodi.koodiUri, arvo: kpKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kpKoodi)});
                    });
                    // jos ytj:stä saatu organisaatioon liityvää tietoa --> päivitetään kotipaikka
                    model.addYtjKotipaikka();
                }, function(response) {
                    // paikkakuntia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoMaat.get({}, function(result) {
                    model.koodisto.maat.length = 0;
                    result.forEach(function(maaKoodi) {
                        if (KoodistoKoodi.isValid(maaKoodi)) {
                            model.koodisto.maat.push({uri: maaKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(maaKoodi)});
                        }
                    });
                }, function(response) {
                    // maita ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoKielet.get({}, function(result) {
                    model.koodisto.kielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        if (KoodistoKoodi.isValid(kieliKoodi)) {
                            model.koodisto.kielet.push({uri: kieliKoodi.koodiUri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                            model.uriLocalizedNames[kieliKoodi.koodiUri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                        }
                    });
                    // jos ytj:stä saatu organisaatioon liittyvää tietoa --> päivitetään kieli
                    model.addYtjLang();
                }, function(response) {
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
                }, function(response) {
                    // vuosiluokkia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoPosti.get({}, function(result) {
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
                    // jos ytj:stä saatu organisaatioon osoite tietoa --> päivitetään osoitteet
                    model.addYtjOsoite();
                }, function(response) {
                    // postinumeroita ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
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

            Organisaatio.get({oid: parentoid}, function(result) {
                model.uriLocalizedNames["parentnimi"] = getLocalizedValue(result.nimi, "", false);
                model.parenttype = result.tyypit[0];
                model.parent = result;

                model.organisaatio = {};
                model.refreshKoodisto(null);
                model.organisaatio.tyypit = [];
                model.organisaatio.nimi = {};
                model.organisaatio.nimi.fi = "";
                model.organisaatio.parentOid = parentoid;
                model.organisaatio.kieletUris = [];
                model.organisaatio.yhteystiedot = [];
                model.organisaatio.vuosiluokat = [];
                model.yhteystiedot = {};
                model.mdyhteystiedot = {};
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
//                esimerkkiYritysTiedot = {
//                    versio : 1,
//                    nimi : "Esimerkki organisaatio",
//                    svNimi : "Exampel organisation",
//                    ytunnus : "2183469-6",
//                    yritysmuoto : "Osakeyhtiö",
//                    yritysmuotoKoodi : "oy", // mitäköhän tää pitäis olla
//                    toimiala : "Kartokiteollisuus",
//                    toimialaKoodi : "paper industri",
//                    yrityksenKieli : "ruotsi",
//                    postiOsoite : {
//                        katu : "Tie 1",
//                        postinumero : "00100",
//                        toimipaikka : "Helsinki",
//                        maa : "Suami",
//                        maakoodi : "FI",
//                        kieli : 0
//                    },
//                    kayntiOsoite : {
//                        katu : "Tie 1",
//                        postinumero : "00100",
//                        toimipaikka : "Helsinki",
//                        maa : "Suami",
//                        maakoodi : "FI",
//                        kieli : 0
//                    },
//                    sahkoposti: "esimerkki@organisaatio.fi",
//                    www: "www.esimerkki.com",
//                    puhelin: "0400-801092",
//                    faksi: "021-100111",
//                    kotiPaikka: "patikoikka", // mitäköhän tässä pitäis olla
//                    kotiPaikkaKoodi: "paikkakoodi", // mitäköhän tässä pitäis olla
//                    aloitusPvm: "12.11.1974"
//                };
//
//                model.createOrganisaatio(parentoid, esimerkkiYritysTiedot);

                model.createOrganisaatio(parentoid, result);
            }, function(response) {
                // yritystietoa ei löytynyt
                showAndLogError("Organisaationtarkastelu.ytunnushakuvirhe", response);
                model.createOrganisaatio(parentoid);
            });
        };

        this.fillYritysTiedot = function(yritystiedot) {
            // parse a date in dd.MM.yyyy format
            parseDate = function(input) {
                if (!input) {
                    return;
                }
                var parts = input.split('.');
                // new Date(year, month [, day [, hours[, minutes[, seconds[, ms]]]]])
                return new Date(parts[2], parts[1]-1, parts[0]); // Note: months are 0-based
            };

            model.organisaatio.nimi.fi = yritystiedot.nimi;
            model.organisaatio.nimi.sv = yritystiedot.svNimi;
            model.organisaatio.ytunnus = yritystiedot.ytunnus;
            model.organisaatio.yritysmuoto = yritystiedot.yritysmuoto;
            // yrityksenKieli, sitten kun koodiston kielet on saatu
            // postiOsoite, sitten kun koodiston postinumerot on saatu
            // kayntiOsoite, sitten kun koodiston postinumerot on saatu
            model.yhteystiedot.email.email = yritystiedot.sahkoposti;
            model.yhteystiedot.www.www = yritystiedot.www;
            model.yhteystiedot.puhelin.numero = yritystiedot.puhelin;
            model.yhteystiedot.faksi.numero = yritystiedot.faksi;
            // kotipaikka / kotipaikkaKoodi, sitten kun koodiston kotipaikat on saatu
            model.organisaatio.alkuPvm = parseDate(yritystiedot.aloitusPvm);

            // asetetaan päivitys timestamp
            model.organisaatio.ytjPaivitysPvm = new Date();
        };

        // Konvertoi päivämäärän rajapinnan hyväksymään muotoon yyyy-mm-dd
        this.formatDate = function(dateToFormat) {
            if (dateToFormat) {
                d = new Date(dateToFormat);
                curr_date = 100 + d.getDate();
                curr_month = 100 + d.getMonth() + 1;
                curr_year = d.getFullYear();
                return curr_year + "-" + curr_month.toString().slice(1) + "-" + curr_date.toString().slice(1);
            }
            return;
        };

        // Konvertoi päivämäärät rajapinnan hyväksymään muotoon yyyy-mm-dd
        formatDates = function() {
            if (model.organisaatio.alkuPvm) {
                model.organisaatio.alkuPvm = model.formatDate(model.organisaatio.alkuPvm);
            }
            if (model.organisaatio.lakkautusPvm) {
                model.organisaatio.lakkautusPvm = model.formatDate(model.organisaatio.lakkautusPvm);
            }
        };

        this.persistOrganisaatio = function(orgForm) {
            formatDates();
            if (model.organisaatio.$post) {
                Organisaatio.post(model.organisaatio, function(result) {
                    //console.log(result);
                    orgForm.$setPristine();
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                }, function(response) {
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                });
            } else {
                UusiOrganisaatio.put(model.organisaatio, function(result) {
                    //console.log(result);
                    orgForm.$setPristine();
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                }, function(response) {
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                });
            }
        };

        this.toggleCheck = function(organisaatiotyyppi) {
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                model.organisaatio.tyypit.push(organisaatiotyyppi);
            } else {
                model.organisaatio.tyypit.splice(model.organisaatio.tyypit.indexOf(organisaatiotyyppi), 1);
            }
        };

        this.toggleCheckVuosiluokka = function(vuosiluokka) {
            if (model.organisaatio.vuosiluokat.indexOf(vuosiluokka) === -1) {
                model.organisaatio.vuosiluokat.push(vuosiluokka);
            } else {
                model.organisaatio.vuosiluokat.splice(model.organisaatio.vuosiluokat.indexOf(vuosiluokka), 1);
            }
        };

        this.addLang = function() {
            if (model.organisaatio.kieletUris.indexOf(model.koodisto.kieliplaceholder) === -1) {
                if (model.koodisto.kieliplaceholder && (model.koodisto.kieliplaceholder !== $filter('i18n')("lisaakieli"))) {
                    model.organisaatio.kieletUris.push(model.koodisto.kieliplaceholder);
                }
            }
            model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
        };

        this.addYtjLang = function() {
            // Tämä tehdään vasta kun koodiston kielet on saatu ja ytj tiedot on olemassa
            if ('yrityksenKieli' in model.ytjTiedot === false) {
                return;
            }
            getKieliUri = function(kieli) {
                if (!kieli) {
                    $log.debug("fillYritysTiedot.getKieliUri(), tyhjä kieli");
                    return;
                }

                // yritystietojen mukana kieli tulee "suomeksi" --> muutetaan se kieliArvoksi
                var kieliArvo = null;
                switch (kieli.trim().toLowerCase()) {
                    case "suomi":
                        kieliArvo = "FI";
                        break;
                    case "ruotsi":
                        kieliArvo = "SV";
                        break;
                    case "englanti":
                        kieliArvo = "EN";
                        break;
                    default:
                        $log.warn("Failed to get kieli uri for language: " + kieli);
                        return;
                }

                // etsitään koodiston kielistä kieliArvoa ja palautetaan vastaava uri jos löytyy
                var found = $filter('filter')(model.koodisto.kielet, {arvo: kieliArvo}, true);
                if (found.length) {
                    return found[0].uri;
                }
                else {
                    $log.warn("Failed to found uri for kieli: " + kieli + " arvo: " + kieliArvo);
                }
                return;
            };
            kieliUri = getKieliUri(model.ytjTiedot.yrityksenKieli);
            if (kieliUri) {
                model.organisaatio.kieletUris.push(kieliUri);
            }

        };

        this.removeLang = function(lang) {
            var index = model.organisaatio.kieletUris.indexOf(lang);
            if (index !== -1) {
                model.organisaatio.kieletUris.splice(index, 1);
            }
        };

        this.addKtLang = function() {
            if (model.koodisto.ktkieliplaceholder !== $filter('i18n')("lisaakieli")) {
                var lang = model.koodisto.ktkieliplaceholder;
                model.koodisto.ktkieliplaceholder = null;
                if (lang) {
                    for (var i in model.kttabs) {
                        if (model.kttabs[i].lang === lang) {
                            return;
                        }
                    }
                    model.kttabs.push({lang: lang, active: true});
                }
                for (field in model.kttypes) {
                    if (!model.organisaatio.metadata.data[model.kttypes[field]]) {
                        model.organisaatio.metadata.data[model.kttypes[field]] = {};
                    }
                }
            }
        };

        this.removeKtLang = function(index) {
            for (field in model.kttypes) {
                if (model.organisaatio.metadata.data[model.kttypes[field]]) {
                    if (model.organisaatio.metadata.data[model.kttypes[field]][model.kttabs[i].lang]) {
                        delete model.organisaatio.metadata.data[model.kttypes[field]][model.kttabs[i].lang];
                    }
                }
            }
            model.kttabs.splice(index, 1);
        };

        this.addOeLang = function() {
            if (model.koodisto.oekieliplaceholder !== $filter('i18n')("lisaakieli")) {
                var lang = model.koodisto.oekieliplaceholder;
                model.koodisto.oekieliplaceholder = null;
                if (lang) {
                    for (var i in model.oetabs) {
                        if (model.oetabs[i].lang === lang) {
                            return;
                        }
                    }
                    model.oetabs.push({lang: lang, active: true});
                }
                for (field in model.oetypes) {
                    if (!model.organisaatio.metadata.data[model.oetypes[field]]) {
                        model.organisaatio.metadata.data[model.oetypes[field]] = {};
                    }
                }
            }
        };

        this.removeOeLang = function(index) {
            for (field in model.oetypes) {
                if (model.organisaatio.metadata.data[model.oetypes[field]]) {
                    if (model.organisaatio.metadata.data[model.oetypes[field]][model.oetabs[i].lang]) {
                        delete model.organisaatio.metadata.data[model.oetypes[field]][model.oetabs[i].lang];
                    }
                }
            }
            model.oetabs.splice(index, 1);
        };

        this.addYtjKotipaikka = function() {
            // Tämä tehdään vasta kun koodiston kotipaikat on saatu ja ytj tiedot on olemassa
            if ('kotiPaikkaKoodi' in model.ytjTiedot === false) {
                return;
            }

            // etsitään koodiston kotipaikoista kieliArvoa ja palautetaan vastaava uri jos löytyy
            found = $filter('filter')(model.koodisto.kotipaikat, {arvo: model.ytjTiedot.kotiPaikkaKoodi}, true);
            if (found.length) {
                model.organisaatio.kotipaikkaUri = found[0].uri;
            }
            else {
                $log.warn("Failed to found uri for kotipaikka: " + model.ytjTiedot.kotiPaikkaKoodi);
            }
            return;
        };

        isEmptyObject = function(obj) {
            for (var name in obj) {
                return false;
            }
            return true;
        };

        this.addYtjOsoite = function() {
            // Tämä tehdään vasta kun koodiston postinumerot on saatu ja ytj tiedot on olemassa
            if ('postiOsoite' in model.ytjTiedot) {
                if (model.ytjTiedot.postiOsoite.kieli === 1) {
                    model.yhteystiedot.posti.osoite = model.ytjTiedot.postiOsoite.katu;
                }
                else if (model.ytjTiedot.postiOsoite.kieli === 2) {
                    model.yhteystiedot.ruotsi_posti.osoite = model.ytjTiedot.postiOsoite.katu;
                }
                else {
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.postiOsoite);
                }

            }

            if ('kayntiOsoite' in model.ytjTiedot) {
                if (model.ytjTiedot.kayntiOsoite.kieli === 1) {
                    model.yhteystiedot.kaynti.osoite = model.ytjTiedot.kayntiOsoite.katu;
                }
                else if (model.ytjTiedot.kayntiOsoite.kieli === 2) {
                    model.yhteystiedot.ruotsi_kaynti.osoite = model.ytjTiedot.kayntiOsoite.katu;
                }
                else {
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.kayntiOsoite);                    
                }
            }

//            model.yhteystiedot.kaynti.postitoimipaikka
//            model.yhteystiedot.postinumerot.kaynti
//            model.yhteystiedot.kaynti.ytjPaivitysPvm
//            model.yhteystiedot.kaynti.maaUri
//            postinumeroUri
        };

        this.addSome = function() {
            if (model.organisaatio.metadata) {
                if (!model.organisaatio.metadata.data[model.someplaceholder] ||
                        isEmptyObject(model.organisaatio.metadata.data[model.someplaceholder])) {
                    model.organisaatio.metadata.data[model.someplaceholder] = {'0': model.sometext[model.someplaceholder]};
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
            for (key in model.sometypes) {
                for (key2 in model.organisaatio.metadata.data[model.sometypes[key]]) {
                    return true;
                }
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

        this.hasVuosiluokat = function() {
            if (model.organisaatio.tyypit) {
                if (model.organisaatio.tyypit.indexOf(model.koodisto.localizedOppilaitos) !== -1) {
                    tyyppi = model.organisaatio.oppilaitosTyyppiUri;
                    if (tyyppi) {
                        return (tyyppi === "oppilaitostyyppi_11" || tyyppi === "oppilaitostyyppi_12" || tyyppi === "oppilaitostyyppi_19");
                    }
                }
            }
            return false;
        };

        this.setPostinumero = function(addressmodel, postcode) {
            if (addressmodel && postcode && model.koodisto.nimetFI[postcode]) {
                addressmodel.postinumeroUri = model.koodisto.nimetFI[postcode].uri;
                addressmodel.postitoimipaikka = model.koodisto.nimetFI[postcode].paikka;
            }
        };

        this.setRuotsiPostinumero = function(addressmodel, postcode) {
            if (addressmodel && postcode && model.koodisto.nimetSV[postcode]) {
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



