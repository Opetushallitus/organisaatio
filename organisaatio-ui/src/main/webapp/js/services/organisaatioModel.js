app.factory('OrganisaatioModel', function(Organisaatio, Aliorganisaatiot, KoodistoSearchKoodis, KoodistoKoodi,
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoPaikkakunnat, KoodistoMaat, KoodistoKielet,
        KoodistoPosti, KoodistoVuosiluokat, UusiOrganisaatio, YTJYritysTiedot, YhteystietoMetadata, Alert,
        KoodistoOpetuskielet, KoodistoPaikkakunta, AuthService, MyRolesModel, HenkiloVirkailijat, Henkilo,
        HenkiloKayttooikeus, $filter, $log, $timeout) {
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
            kielet: [],
            opetuskielet: [],
            vuosiluokat: [],
            kieliplaceholder: $filter('i18n')("lisaakieli"),
            //ktkieliplaceholder: $filter('i18n')("lisaakieli"),
            //hpkieliplaceholder: $filter('i18n')("lisaakieli"),
            //oekieliplaceholder: $filter('i18n')("lisaakieli"),
            postinumerot: [],
            //postinumerot2: [],
            nimetFI: {},
            nimetSV: {},
            yhteystietoTyypit: {}
        };

        this.henkilot = {
            virkailijat: []
        };

        // Koodin lokalisoitu nimi, avaimena uri
        this.uriLocalizedNames = {};

        // Koko koodi, avaimena uri
        this.uriKoodit = {};

        // Aliorganisaatioiden nimet listana
        this.aliorganisaatiot = [];

        // Metadatan yhteystiedot mäpättynä tyypin perusteella
        this.mdyhteystiedot = {kielivalikoima_fi: {}, kielivalikoima_sv: {}, kielivalikoima_en: {}};

        // Päätason yhteystiedot mäpättynä tyypin perusteella
        this.yhteystiedot = {kielivalikoima_fi: {}, kielivalikoima_sv: {}, kielivalikoima_en: {}};

        // Lisäyhteystiedot mäpättynä: oid => tyypin nimi molemmilla kielillä
        this.lisayhteystiedot = {};

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

        //this.kttabs = [];
        //this.hptabs = [];
        //this.oetabs = [];

        // Monikielisen tekstin valinta
        // kt: koulutustarjoajatiedot
        // hp: hakijapalvelut
        // oe: opiskelijan edut
        this.mkSections = {
            kt: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.kttypes
            },
            hp: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: []
            },
            oe: {
                placeholder: $filter('i18n')("lisaakieli"),
                tabs: [],
                types: this.oetypes
            }
        };

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
            model.mkSections.kt.tabs.length = 0;
            model.mkSections.hp.tabs.length = 0;
            model.mkSections.oe.tabs.length = 0;
            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getLocalizedValue(result.metadata.hakutoimistonNimi, "kielivalikoima_", false);
                if (result.metadata.data) {
                    var ktlangs = {'kielivalikoima_fi': true, 'kielivalikoima_sv': true, 'kielivalikoima_en': true};
                    for (var key in model.kttypes) {
                        for (var lang in result.metadata.data[model.kttypes[key]]) {
                            if (result.metadata.data[model.kttypes[key]].hasOwnProperty(lang)) {
                                ktlangs[lang] = true;
                            }

                        }
                    }
                    for (lang in ktlangs) {
                        model.mkSections.kt.tabs.push({lang: lang, active: false});
                    }
                    if (model.mkSections.kt.tabs.length > 0) {
                        $timeout(function() {
                            model.mkSections.kt.tabs[0].active = true;
                        }, 0);
                    }
                    var oelangs = {'kielivalikoima_fi': true, 'kielivalikoima_sv': true, 'kielivalikoima_en': true};
                    for (var key in model.oetypes) {
                        for (var lang in result.metadata.data[model.oetypes[key]]) {
                            if (result.metadata.data[model.oetypes[key]].hasOwnProperty(lang)) {
                                oelangs[lang] = true;
                            }

                        }
                    }
                    for (lang in oelangs) {
                        model.mkSections.oe.tabs.push({lang: lang, active: false});
                    }
                    if (model.mkSections.oe.tabs.length > 0) {
                        $timeout(function() {
                            model.mkSections.oe.tabs[0].active = true;
                        }, 0);
                    }
                }
                if (result.metadata.yhteystiedot) {
                    var hplangs = {'kielivalikoima_fi': true, 'kielivalikoima_sv': true, 'kielivalikoima_en': true};
                    for (var i = 0; i < model.organisaatio.metadata.yhteystiedot.length; i++) {
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
            }
        };

        // Alusta objektit joita ei vielä ole asetettu, luo mäppäys modelYhteystiedoista
        // organisaatioYhteystietoihin yhteystiedon tyypin perusteella
        initYhteystiedot = function(organisaatioYhteystiedot, modelYhteystiedot) {
            modelYhteystiedot.muu = [];
            for (var ytindex in organisaatioYhteystiedot) {
                var yt = organisaatioYhteystiedot[ytindex];
                var kieli = (yt.kieli === null ? 'kielivalikoima_fi' : yt.kieli);

                if (yt.osoite) {

                    if (yt.osoiteTyyppi === 'muu') {
                        // Muita osoitteita voi olla useita, lisää listaan
                        modelYhteystiedot[kieli][yt.osoiteTyyppi].push(yt);
                    } else {
                        if (yt.osoiteTyyppi.indexOf('ruotsi_') !== -1) {
                            yt.kieli = 'kielivalikoima_sv';
                        } else if (yt.osoiteTyyppi.indexOf('ulkomainen_') !== -1) {
                            yt.kieli = 'kielivalikoima_en';
                        } else {
                            yt.kieli = 'kielivalikoima_fi';
                        }
                        modelYhteystiedot[yt.kieli][yt.osoiteTyyppi] = yt;
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
                    {kielivalikoima_fi: ['kaynti', 'posti'],
                        kielivalikoima_sv: ['ruotsi_kaynti', 'ruotsi_posti'],
                        kielivalikoima_en: ['ulkomainen_posti', 'ulkomainen_kaynti']});
        };

        this.initYhteystiedotPlaceholder = function(organisaatioYhteystiedot, modelYhteystiedot, tyypit) {
            for (var phkieli in tyypit) {
                for (var i = 0; i < tyypit[phkieli].length; i++) {
                    if (!modelYhteystiedot[phkieli][tyypit[phkieli][i]]) {
                        var uusiYt = {osoiteTyyppi: tyypit[phkieli][i], kieli: phkieli};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][tyypit[phkieli][i]] = uusiYt;
                    }
                }
                var etyypit = ['email', 'www'];
                for (var i = 0; i < etyypit.length; ++i) {
                    if (!modelYhteystiedot[phkieli][etyypit[i]]) {
                        var uusiYt = {};
                        uusiYt[etyypit[i]] = null;
                        uusiYt['kieli'] = phkieli;
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][etyypit[i]] = uusiYt;
                    }
                }
                var ptyypit = ['puhelin', 'faksi'];
                for (var i = 0; i < ptyypit.length; ++i) {
                    if (!modelYhteystiedot[phkieli][ptyypit[i]]) {
                        var uusiYt = {tyyppi: ptyypit[i], kieli: phkieli};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][ptyypit[i]] = uusiYt;
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

        showAndExitError = function(msg, response) {
            Alert.add("error", $filter('i18n')(msg, ""), false);
            $log.error(msg + " (status: " + response.status + ")");
        };

        refresh = function(oid) {
            $log.info("refresh: mode=" + model.mode);
            // tyhjennetään mahdolliset vanhat ytj tiedot
            model.ytjTiedot = {};
            modelYhteystiedot = {};
            // haetaan organisaation tiedot
            Organisaatio.get({oid: oid}, function(result) {
                model.organisaatio = result;
                model.uriLocalizedNames["nimi"] = getLocalizedValue(result.nimi, "", false);
                convertToOpetuskieliKoodisto();

                Organisaatio.get({oid: result.parentOid}, function(parentResult) {
                    model.uriLocalizedNames["parentnimi"] = getLocalizedValue(parentResult.nimi, "", false);
                    model.parenttype = parentResult.tyypit[0];
                    model.parent = parentResult;

                    if (model.mode === 'edit') {
                        refreshKoodisto();
                        refreshHenkilo();
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
                        koodiUris[result.oppilaitosTyyppiUri] = true;                     // Poistetaan lopusta #x jotta editointi toimii
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
                    model.lisayhteystiedot = {};
                    updateLisayhteystietoArvos(model.organisaatio.yhteystietoArvos);
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

        updateLisayhteystietoArvos = function(lisatietos) {
            if (!model.organisaatio.yhteystietoArvos) {
                model.organisaatio.yhteystietoArvos = [];
            }
            lisatietos.forEach(function(yt) {
                // Lisätään jos arvoa ei ole
                var arvoFound = false;
                for (var arvo in model.organisaatio.yhteystietoArvos) {
                    if (yt['YhteystietoElementti.oid'] === model.organisaatio.yhteystietoArvos[arvo]['YhteystietoElementti.oid']) {
                        arvoFound = true;
                    }
                }
                // Jos arvoa ei vielä ole, lisätään muokkaus/uudenluontinäkymään bindausta varten
                if (!arvoFound) {
                    yt["YhteystietoArvo.arvoText"] = null;
                    model.organisaatio.yhteystietoArvos.push(yt);
                }
                // Mäpätään oidista nimeen. Mäppäys on oikeasti 1-1 vaikka nimi toistuu joka tietueessa.
                lan = (KoodistoKoodi.getLanguage() === "SV" ? "sv" : "fi");
                model.lisayhteystiedot[yt["YhteystietojenTyyppi.oid"]] =
                        {
                            nimi: yt["YhteystietojenTyyppi.nimi." + lan], oid: yt["YhteystietojenTyyppi.oid"]
                        };
                model.uriLocalizedNames[yt["YhteystietoElementti.oid"]] =
                        (KoodistoKoodi.getLanguage() === "SV" ? yt["YhteystietoElementti.nimiSv"] : yt["YhteystietoElementti.nimi"]);
            });
        };

        // Lisätään organisaatiorakenteeseen puuttuvat yhteystiedot nykyisen organisaatio- ja mahdollisen
        // oppilaitostyypin mukaisesti. Organisaatiorakenne pitää päivittää jos muokattaessa muutetaan
        // kumpaakaan tyyppiä
        // Lisätään koodisto.lisatietos => organisaatio.yhteystietoArvos
        updateLisayhteystiedot = function(result) {
            result.forEach(function(yhteystietoTyyppi) {
                for (tyyppi in model.organisaatio.tyypit) {
                    if (yhteystietoTyyppi.sovellettavatOrganisaatioTyyppis &&
                            yhteystietoTyyppi.sovellettavatOrganisaatioTyyppis.indexOf(model.organisaatio.tyypit[tyyppi]) !== -1) {                     // Lisätään aina jos organisaatiotyyppi on muu kuin oppilaitos, ja oppilaitokselle
                        // vain jos oppilaitostyypit vastaa yhteystiedolle määriteltyä
                        if (model.organisaatio.tyypit.indexOf('Oppilaitos') === -1 || (yhteystietoTyyppi.sovellettavatOppilaitosTyyppis && model.organisaatio.oppilaitosTyyppiUri &&
                                ((yhteystietoTyyppi.sovellettavatOppilaitosTyyppis.indexOf(model.organisaatio.oppilaitosTyyppiUri) !== -1) ||
                                        (yhteystietoTyyppi.sovellettavatOppilaitosTyyppis.indexOf(model.organisaatio.oppilaitosTyyppiUri + "#1") !== -1)))) {
                            updateLisayhteystietoArvos(yhteystietoTyyppi.lisatietos);
                        }
                    }
                }
            });
        };

        refreshKoodistoMetadata = function(organisaatioTyyppi) {
            YhteystietoMetadata.get({organisaatioTyyppi: organisaatioTyyppi}, function(result) {
                model.koodisto.yhteystietoTyypit[organisaatioTyyppi] = result;
                updateLisayhteystiedot(result);
            }, function(response) {
                // vuosiluokkia ei löytynyt
                showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
            });
        };

        refreshKoodisto = function(oid) {
            if (oid === null || (oid !== model.koodisto.oid)) {
                model.koodisto.localizedOppilaitos = "";
                model.koodisto.localizedKoulutustoimija = "";
                model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
                //model.koodisto.ktkieliplaceholder = $filter('i18n')("lisaakieli");
                //model.koodisto.hpkieliplaceholder = $filter('i18n')("lisaakieli");
                //model.koodisto.oekieliplaceholder = $filter('i18n')("lisaakieli");
                KoodistoOrganisaatiotyypit.get({}, function(result) {
                    model.koodisto.organisaatiotyypit.length = 0;
                    model.koodisto.ophOrganisaatiot.length = 0;
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
                        'Oppisopimustoimipiste': ["03", "04"]};
                    result.forEach(function(orgTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(orgTyyppiKoodi)) {
                            if (sallitutAlaOrganisaatiot[model.parenttype].indexOf(orgTyyppiKoodi.koodiArvo) !== -1) {
                                if (orgTyyppiKoodi.koodiArvo === "03") {
                                    // Koodistossa 'Opetuspiste' on 'Toimipiste'!?
                                    model.koodisto.organisaatiotyypit.push('Opetuspiste');
                                    localizedOrgType = 'Opetuspiste';
                                } else {
                                    model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                                    localizedOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                                }
                            }

                            if (orgTyyppiKoodi.koodiArvo === "01") {
                                model.koodisto.localizedKoulutustoimija = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            } else if (orgTyyppiKoodi.koodiArvo === "02") {
                                model.koodisto.localizedOppilaitos = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            }
                            if (orgTyyppiKoodi.koodiArvo !== "03" && orgTyyppiKoodi.koodiArvo !== "04") {
                                model.koodisto.ophOrganisaatiot.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                            }

                            var localizedKoodistoOrgType = "";
                            if (orgTyyppiKoodi.koodiArvo === "03") {
                                localizedKoodistoOrgType = 'Opetuspiste';
                            } else {
                                localizedKoodistoOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            }

                            refreshKoodistoMetadata(localizedKoodistoOrgType);
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
                }, function(response) {
                    // paikkakuntia ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoMaat.get({}, function(result) {
                    model.koodisto.maat.length = 0;
                    result.forEach(function(maaKoodi) {
                        model.koodisto.maat.push({uri: maaKoodi.koodiUri, nimi: KoodistoKoodi.getLocalizedName(maaKoodi)});
                    });
                }, function(response) {
                    // maita ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoKielet.get({}, function(result) {
                    model.koodisto.kielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        if (['97', 'VK'].indexOf(kieliKoodi.koodiArvo) === -1) {
                            model.koodisto.kielet.push({uri: kieliKoodi.koodiUri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                            model.uriLocalizedNames[kieliKoodi.koodiUri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                        }
                    });
                }, function(response) {
                    // kieliä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                KoodistoOpetuskielet.get({}, function(result) {
                    model.koodisto.opetuskielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        model.koodisto.opetuskielet.push({uri: kieliKoodi.koodiUri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                        model.uriLocalizedNames[kieliKoodi.koodiUri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
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
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "FI")};
                            model.koodisto.nimetSV[postiKoodi.koodiArvo] = {
                                uri: postiKoodi.koodiUri,
                                paikka: KoodistoKoodi.getLangName(postiKoodi, "SV")};

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

        refreshHenkilo = function() {
            model.henkilot.virkailijatTooltip = "";
            HenkiloVirkailijat.get({oid: model.organisaatio.oid}, function(result) {
                for (var i = 0; i < result.totalCount; i++) {
                    model.henkilot.virkailijat.push({
                        nimi: result.results[i].etunimet + " " + result.results[i].sukunimi,
                        tiedot: result.results[i]});
                    model.henkilot.virkailijatTooltip += result.results[0].etunimet + " " + result.results[0].sukunimi + "<br>";
                }
            }, function(response) {
                // Henkilöitä ei löytynyt
                $log.error($filter('i18n')("Organisaationtarkastelu.henkilohakuvirhe") + " (status: " + response.status + ")");
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
            model.organisaatio.nimi = {};
            model.organisaatio.nimi.fi = "";
            model.organisaatio.kieletUris = [];
            model.organisaatio.yhteystiedot = [];
            model.organisaatio.vuosiluokat = [];
            model.yhteystiedot = {};
            model.mdyhteystiedot = {};
            // oletusarvoisesti luodaan organisaatio Suomeen
            model.organisaatio.maaUri = "maatjavaltiot1_fin";

            Organisaatio.get({oid: parentoid}, function(result) {
                model.uriLocalizedNames["parentnimi"] = getLocalizedValue(result.nimi, "", false);
                model.parenttype = result.tyypit[0];
                model.parent = result;

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

        this.updateOrganisaatioYTunnuksella = function(ytunnus) {
            YTJYritysTiedot.get({'ytunnus': ytunnus}, function(result) {
                model.ytjTiedot = result;

                // Täytetään yritystiedot, niiltä osin kun koodistosta saatuja tietoja ei tarvitse käyttää
                model.fillYritysTiedot(result);

                // Täytetään yritystiedot, koodiston tietoja käyttävältä osalta
                model.addYtjLang();
                model.addYtjOsoite();
                model.addYtjKotipaikka();
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
                return new Date(parts[2], parts[1] - 1, parts[0]); // Note: months are 0-based
            };

            // Tarkistetaan "kenttien" olemassaolo, sillä yritystiedot voidaan täyttää myöhemminkin
            if (yritystiedot.nimi) {
                model.organisaatio.nimi.fi = yritystiedot.nimi;
            }
            if (yritystiedot.svNimi) {
                model.organisaatio.nimi.sv = yritystiedot.svNimi;
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
                model.yhteystiedot.email.email = yritystiedot.sahkoposti;
            }
            if (yritystiedot.www) {
                model.yhteystiedot.www.www = yritystiedot.www;
            }
            if (yritystiedot.puhelin) {
                model.yhteystiedot.puhelin.numero = yritystiedot.puhelin;
            }
            if (yritystiedot.faksi) {
                model.yhteystiedot.faksi.numero = yritystiedot.faksi;
            }
            // kotipaikka / kotipaikkaKoodi, sitten kun koodiston kotipaikat on saatu
            if (yritystiedot.aloitusPvm) {
                model.organisaatio.alkuPvm = parseDate(yritystiedot.aloitusPvm);
            }

            // YTunnuksella luotu organisaatio on oletusarvoisesti koulutustoimija
            this.toggleCheckOrganisaatio("Koulutustoimija");

            // asetetaan päivitys timestamp
            model.organisaatio.ytjpaivitysPvm = model.formatDate(new Date());
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
                    if (orgForm) {
                        orgForm.$setPristine();
                    }
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                    refresh(model.organisaatio.oid);
                }, function(response) {
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                });
            } else {
                UusiOrganisaatio.put(model.organisaatio, function(result) {
                    //console.log(result);
                    if (orgForm) {
                        orgForm.$setPristine();
                    }
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                }, function(response) {
                    showAndLogError("Organisaationmuokkaus.tallennusvirhe", response);
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennusvirhe");
                });
            }
        };

        this.toggleCheckOrganisaatio = function(organisaatiotyyppi) {
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                model.organisaatio.tyypit.push(organisaatiotyyppi);
            } else {
                model.organisaatio.tyypit.splice(model.organisaatio.tyypit.indexOf(organisaatiotyyppi), 1);
            }
            model.organisaatio.yhteystietoArvos = [];
            model.lisayhteystiedot = {};
            for (tyyppi in model.koodisto.yhteystietoTyypit) {
                updateLisayhteystiedot(model.koodisto.yhteystietoTyypit[tyyppi]);
            }
        };

        this.selectOppilaitosTyyppi = function() {
            model.organisaatio.yhteystietoArvos = [];
            model.lisayhteystiedot = {};
            for (tyyppi in model.koodisto.yhteystietoTyypit) {
                updateLisayhteystiedot(model.koodisto.yhteystietoTyypit[tyyppi]);
            }
        };

        this.toggleCheckVuosiluokka = function(vuosiluokka) {
            if (model.organisaatio.vuosiluokat.indexOf(vuosiluokka) === -1) {
                model.organisaatio.vuosiluokat.push(vuosiluokka);
            } else {
                model.organisaatio.vuosiluokat.splice(model.organisaatio.vuosiluokat.indexOf(vuosiluokka), 1);
            }
        };

        // Korvaa kielivalikoima-koodiston mukaiset kieliurit opetuskieli-koodiston vastaavalla urilla jos löytyy
        convertToOpetuskieliKoodisto = function() {
            var kielivalikoimaToOpetuskieli = {
                "kielivalikoima_fi": "oppilaitoksenopetuskieli_1",
                "kielivalikoima_sv": "oppilaitoksenopetuskieli_2",
                "kielivalikoima_en": "oppilaitoksenopetuskieli_4",
                "kielivalikoima_se": "oppilaitoksenopetuskieli_5",
                "kielivalikoima_xx": "oppilaitoksenopetuskieli_9"
            };
            var muuLoydetty = false;
            var len = model.organisaatio.kieletUris.length;
            while (len--) {
                vanhaKieli = model.organisaatio.kieletUris[len];
                if (vanhaKieli.indexOf("kielivalikoima_") === 0) {
                    var uusiKieli = kielivalikoimaToOpetuskieli[vanhaKieli] || "oppilaitoksenopetuskieli_9";
                    model.organisaatio.kieletUris[len] = uusiKieli;
                    if (uusiKieli === "oppilaitoksenopetuskieli_9") {
                        if (muuLoydetty) {
                            // poistetaan koska muu kieli on jo listassa
                            model.organisaatio.kieletUris.splice(len, 1);
                        } else {
                            muuLoydetty = true;
                        }
                    }
                }
            }
        };

        this.addLang = function() {
            if (model.organisaatio.kieletUris.indexOf(model.koodisto.kieliplaceholder) === -1) {
                if (model.koodisto.kieliplaceholder && (model.koodisto.kieliplaceholder !== $filter('i18n')("lisaakieli"))) {
                    convertToOpetuskieliKoodisto();
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
                    var tyypit = {};
                    tyypit[lang] = ['ulkomainen_posti', 'ulkomainen_kaynti'];
                    model.initYhteystiedotPlaceholder(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot,
                            tyypit);
                }
                for (field in model.mkSections[section].types) {
                    if (!model.organisaatio.metadata.data[model.mkSections[section].types[field]]) {
                        model.organisaatio.metadata.data[model.mkSections[section].types[field]] = {};
                    }
                }
                // Näytä juuri luotu uusi välilehti
                $timeout(function() {
                    tab.active = true;
                }, 0);
            }
        };

        this.removeMkLang = function(section, index) {
            for (field in model.mkSections[section].types) {
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
            mapOsoiteYhteystieto = function(ytjOsoite, yhteystieto, postinumeroField) {
                yhteystieto.osoite = ytjOsoite.katu;

                // asetetaam postinumero input kenttään
                model.yhteystiedot.postinumerot[postinumeroField] = ytjOsoite.postinumero;
                // asettaa postinumeroUrin ja toimipaikan
                model.setPostinumero(yhteystieto, ytjOsoite.postinumero);

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
                            model.yhteystiedot.posti,
                            "posti");
                }
                else if (model.ytjTiedot.postiOsoite.kieli === 2) {
                    mapOsoiteYhteystieto(model.ytjTiedot.postiOsoite,
                            model.yhteystiedot.ruotsi_posti,
                            "ruotsi_posti");
                }
                else {
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.postiOsoite);
                }

            }
            if ('kayntiOsoite' in model.ytjTiedot) {
                if (model.ytjTiedot.kayntiOsoite.kieli === 1) {
                    mapOsoiteYhteystieto(model.ytjTiedot.kayntiOsoite,
                            model.yhteystiedot.kaynti,
                            "kaynti");
                }
                else if (model.ytjTiedot.kayntiOsoite.kieli === 2) {
                    mapOsoiteYhteystieto(model.ytjTiedot.kayntiOsoite,
                            model.yhteystiedot.ruotsi_kaynti,
                            "ruotsi_kaynti");
                }
                else {
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.kayntiOsoite);
                }
            }
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

        this.setPostinumero = function(md, addressmodel, postcode) {
            sama = (md ? model.mdsamaosoite : model.samaosoite);
            yt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            if (sama === true) {
                if (yt.postinumerot.kaynti) {
                    yt.postinumerot.posti = yt.postinumerot.kaynti;
                    yt.posti.postinumeroUri = model.koodisto.nimetFI[postcode].uri;
                    yt.posti.postitoimipaikka = model.koodisto.nimetFI[postcode].paikka;
                }
            }
            if (addressmodel && postcode && model.koodisto.nimetFI[postcode]) {
                addressmodel.postinumeroUri = model.koodisto.nimetFI[postcode].uri;
                addressmodel.postitoimipaikka = model.koodisto.nimetFI[postcode].paikka;
                model.uriKoodit[addressmodel.postinumeroUri] = model.koodisto.nimetFI[postcode].paikka;
            }
        };

        this.setRuotsiPostinumero = function(md, addressmodel, postcode) {
            sama = (md ? model.mdsamaosoitesv : model.samaosoitesv);
            yt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            if (sama === true) {
                if (yt.postinumerot.ruotsi_kaynti) {
                    yt.postinumerot.ruotsi_posti = yt.postinumerot.ruotsi_kaynti;
                    yt.ruotsi_posti.postinumeroUri = model.koodisto.nimetSV[postcode].uri;
                    yt.ruotsi_posti.postitoimipaikka = model.koodisto.nimetSV[postcode].paikka;
                }
            }
            if (addressmodel && postcode && model.koodisto.nimetSV[postcode]) {
                addressmodel.postinumeroUri = model.koodisto.nimetSV[postcode].uri;
                addressmodel.postitoimipaikka = model.koodisto.nimetSV[postcode].paikka;
            }
        };

        this.copyAddress = function(md) {
            sama = (md ? model.mdsamaosoite : model.samaosoite);
            ytp = (md ? model.mdyhteystiedot : model.yhteystiedot);
            yt = (md ? model.mdyhteystiedot['kielivalikoima_' + model.ytlang] : model.yhteystiedot['kielivalikoima_' + model.ytlang]);
            if (sama === true) {
                if (!('posti' in yt)) {
                    yt.posti = {};
                }
                for (kentta in yt.kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        yt.posti[kentta] = yt.kaynti[kentta];
                    }
                }
                if (ytp.postinumerot.kaynti) {
                    ytp.postinumerot.posti = ytp.postinumerot.kaynti;
                }
            }
        };

        this.copyAddressSv = function(md) {
            sama = (md ? model.mdsamaosoitesv : model.samaosoitesv);
            ytp = (md ? model.mdyhteystiedot : model.yhteystiedot);
            yt = (md ? model.mdyhteystiedot['kielivalikoima_' + model.ytlang] : model.yhteystiedot['kielivalikoima_' + model.ytlang]);
            if (sama === true) {
                if (!('ruotsi_posti' in yt)) {
                    yt.ruotsi_posti = {};
                }
                for (kentta in yt.ruotsi_kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        yt.ruotsi_posti[kentta] = yt.ruotsi_kaynti[kentta];
                    }
                }
                if (ytp.postinumerot.ruotsi_kaynti) {
                    ytp.postinumerot.ruotsi_posti = ytp.postinumerot.ruotsi_kaynti;
                }
            }
        };

        this.copyAddressKv = function(md) {
            sama = (md ? model.mdsamaosoitekv : model.samaosoitekv);
            yt = (md ? model.mdyhteystiedot['kielivalikoima_' + model.ytlang] : model.yhteystiedot['kielivalikoima_' + model.ytlang]);
            if (sama === true) {
                if (!('ulkomainen_posti' in yt)) {
                    yt.ulkomainen_posti = {};
                }
                for (kentta in yt.ulkomainen_kaynti) {
                    if (kentta !== 'osoiteTyyppi') {
                        yt.ulkomainen_posti[kentta] = yt.ulkomainen_kaynti[kentta];
                    }
                }
            }
        };

        this.addAddress = function() {
            if (model.organisaatio.yhteystiedot) {
                var uusiYt = {
                    osoiteTyyppi: 'muu',
                    postinumeroUri: null, postitoimipaikka: null, osoite: null
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
                    postinumeroUri: null, postitoimipaikka: null, osoite: null
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

        this.getLocalizedPaikka = function(postikoodi) {
            if ((typeof postikoodi !== 'undefined') && (postikoodi in model.koodisto.nimetFI)) {
                return model.koodisto.nimetFI[postikoodi].paikka;
            }
        };

        this.getLocalizedPaikkaSv = function(postikoodi) {
            if ((typeof postikoodi !== 'undefined') && (postikoodi in model.koodisto.nimetFI)) {
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

    };

    return model;
});



