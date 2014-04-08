app.factory('OrganisaatioModel', function(Organisaatio, Aliorganisaatiot, KoodistoSearchKoodis, KoodistoKoodi,
        KoodistoOrganisaatiotyypit, KoodistoOppilaitostyypit, KoodistoPaikkakunnat, KoodistoMaat,
        KoodistoPosti, KoodistoVuosiluokat, UusiOrganisaatio, YTJYritysTiedot, Alert,
        KoodistoOpetuskielet, KoodistoPaikkakunta, AuthService, MyRolesModel, HenkiloVirkailijat, Henkilo,
        HenkiloKayttooikeus, KoodistoKieli, YhteystietojenTyyppi, $filter, $log, $timeout, $location) {
    var model = new function() {
        this.organisaatio = {};

        this.tinymceOptions = {
            theme: "modern",
            language: KoodistoKoodi.getLanguage().toLowerCase(),
            plugins: [
                "advlist autolink lists link image charmap print preview hr anchor pagebreak",
                "searchreplace visualblocks visualchars code fullscreen",
                "insertdatetime media nonbreaking save table contextmenu directionality",
                "emoticons template paste textcolor"
                        //"wordcount"
            ],
            paste_word_valid_elements: "b,strong,i,em,h1,h2,p,ol,ul,li,a",
            menubar: false,
            //toolbar1: "insertfile undo redo | styleselect | bold italic | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image",
            //toolbar2: "print preview media | forecolor backcolor emoticons",
            toolbar1: "undo redo | styleselect | bold italic | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | table | link image | preview | code"
                    //image_advtab: true
                    //height: "200px",
                    //width: "650px"
        };

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

        // Aliorganisaatioiden nimet listana
        this.aliorganisaatiot = [];

        // Metadatan yhteystiedot mäpättynä tyypin perusteella
        this.mdyhteystiedot = {
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

        // Sosiaalinen media
        this.some = [];
        this.sometypes = ['FACEBOOK', 'GOOGLE_PLUS', 'LINKED_IN', 'TWITTER', 'MUU'];
        this.someurls = {
            FACEBOOK: {
                autofill: 'https://www.facebook.com/',
                validator: '^https{0,1}://(?:www\.){0,1}facebook.com/.+'
            },
            GOOGLE_PLUS: {
                autofill: 'https://plus.google.com/',
                validator: '^https{0,1}://plus.google.com/.+'
            },
            LINKED_IN: {
                autofill: 'https://linkedin.com/',
                validator: '^https{0,1}://(?:www\.){0,1}linkedin.com/.+'
            },
            TWITTER: {
                autofill: 'https://twitter.com/',
                validator: '^https{0,1}://(?:www\.){0,1}twitter.com/.+'
            },
            MUU: {
                autofill: 'https://',
                validator: '^https{0,1}://.+'
            }
        };
        this.kttypes = ['YLEISKUVAUS', 'ESTEETOMYYS', 'OPPIMISYMPARISTO', 'VUOSIKELLO', 'VASTUUHENKILOT',
            'VALINTAMENETTELY', 'AIEMMIN_HANKITTU_OSAAMINEN', 'KIELIOPINNOT', 'TYOHARJOITTELU', 'OPISKELIJALIIKKUVUUS',
            'KANSAINVALISET_KOULUTUSOHJELMAT'];
        this.oetypes = ['KUSTANNUKSET', 'TIETOA_ASUMISESTA', 'RAHOITUS', 'OPISKELIJARUOKAILU', 'TERVEYDENHUOLTOPALVELUT',
            'VAKUUTUKSET', 'OPISKELIJALIIKUNTA', 'VAPAA_AIKA', 'OPISKELIJA_JARJESTOT'];
        for (var st in this.sometypes) {
            this.some.push({'type': this.sometypes[st], 'nimi': $filter('i18n')('Organisaationtarkastelu.' + this.sometypes[st])});
        }

        // Monikielisen tekstin valinta
        // kt: koulutustarjoajatiedot
        // hp: hakijapalvelut
        // oe: opiskelijan edut
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
            }
        };

        // Yhteystietojen kielivälilehdet
        this.yttabs = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1'];

        // Lisäyhteystietojen kielivälilehdet
        this.lttabs = ['kieli_fi#1', 'kieli_sv#1', 'kieli_en#1'];

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
        //   func({ "kieli_fi#1" : "Suomenkielinen nimi"}, "kielivalikoima_", "#1") => "Suomenkielinen nimi"
        // sv-lokaalilla esim:
        //   func({ "fi" : "Suomenkielinen nimi"}, "") => "Suomenkielinen nimi"
        //   func({ "fi" : "Suomenkielinen nimi" , "sv" : "Samma på svenska"}, "") => "Samma på svenska"
        getLocalizedValue = function(res, prefix, suffix, create, language) {
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

        initMk = function(mkSection) {
            var mkLangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
            for (var key in mkSection.types) {
                for (var lang in model.organisaatio.metadata.data[mkSection.types[key]]) {
                    if (model.organisaatio.metadata.data[mkSection.types[key]].hasOwnProperty(lang)) {
                        mkLangs[lang] = true;
                    }

                }
            }
            for (lang in mkLangs) {
                mkSection.tabs.push({lang: lang, active: false});
            }
            if (mkSection.tabs.length > 0) {
                $timeout(function() {
                    mkSection.tabs[0].active = true;
                }, 0);
            }
            for (field in mkSection.types) {
                if (!model.organisaatio.metadata.data[mkSection.types[field]]) {
                    model.organisaatio.metadata.data[mkSection.types[field]] = {};
                }
            }
        }

        refreshMetadata = function(result) {
            model.mkSections.kt.tabs.length = 0;
            model.mkSections.hp.tabs.length = 0;
            model.mkSections.oe.tabs.length = 0;
            model.mkSections.sm.tabs.length = 0;
            if (result.metadata) {
                model.uriLocalizedNames["hakutoimistonNimi"] =
                        getLocalizedValue(result.metadata.hakutoimistonNimi, "kieli_", "#1", false);
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
                }
                if (result.metadata.yhteystiedot) {
                    var hplangs = {'kieli_fi#1': true, 'kieli_sv#1': true, 'kieli_en#1': true};
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
        initYhteystiedot = function(organisaatioYhteystiedot, modelYhteystiedot, muoto) {
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
            var osoiteTyypit = ['kaynti', 'posti', 'ulkomainen_kaynti', 'ulkomainen_posti'];
            for (var kieli in kielet) {
                var phkieli = kielet[kieli];
                for (var i = 0; i < osoiteTyypit.length; i++) {
                    if (!modelYhteystiedot[phkieli][osoiteTyypit[i]]) {
                        var uusiYt = {osoiteTyyppi: osoiteTyypit[i], kieli: phkieli};
                        organisaatioYhteystiedot.push(uusiYt);
                        modelYhteystiedot[phkieli][osoiteTyypit[i]] = uusiYt;
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
            initYhteystiedot(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot, model.osoitemuoto.hp);
        };

        // Näyttää käyttäjälle virheen Alert-servicen avulla ja loggaa responsen statuksen
        showAndLogError = function(msg, response) {
            model.alert = Alert.add("error", $filter('i18n')(response.data.errorKey, msg), false);
            $log.error(msg + " (status: " + response.status + ")");
        };

        refreshLisayhteystietoArvos = function() {
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

        convOrgTyyppi = function(result, from, to) {
            var tyypit = [];
            for (var i in result.tyypit) {
                if (result.tyypit[i] === from) {
                    tyypit.push(to);
                } else {
                    tyypit.push(result.tyypit[i]);
                }
            }
            result.tyypit = tyypit;
        };

        convOpetuspisteToToimipiste = function(result) {
            convOrgTyyppi(result, 'Opetuspiste', 'Toimipiste');
        };

        convToimipisteToOpetuspiste = function(result) {
            convOrgTyyppi(result, 'Toimipiste', 'Opetuspiste');
        };


        refresh = function(result) {
            $log.info("refresh: mode=" + model.mode);
            // tyhjennetään mahdolliset vanhat ytj tiedot
            model.ytjTiedot = {};
            modelYhteystiedot = {};
            convOpetuspisteToToimipiste(result);
            model.organisaatio = result;
            model.uriLocalizedNames["nimi"] = getLocalizedValue(result.nimi, "", "", false);

            Organisaatio.get({oid: result.parentOid}, function(parentResult) {
                model.uriLocalizedNames["parentnimi"] = getLocalizedValue(parentResult.nimi, "", "", false);
                model.parenttype = parentResult.tyypit[0];
                model.parent = parentResult;

                if (model.mode === 'edit') {
                    refreshKoodisto();
                    refreshHenkilo();
                }
                finishModel();
                refreshMetadata(result);
                refreshLisayhteystietoArvos();
                // Hae kaikki koodi-urit kerralla
                var koodiUris = {};
                for (var i in model.yttabs) {
                    koodiUris[model.yttabs[i]] = true;
                }
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
                KoodistoSearchKoodis.get({uris: searchParams}, function(koodiResult) {
                    for (var i = 0; i < koodiResult.length; i++) {
                        // Lisää kaikki koodit myös #<versio> -päätteisenä, koska result.koodiUri:ssa #<versio>
                        // -päätettä ei ole vaikka olisi annettu hakuparametrina
                        model.uriLocalizedNames[koodiResult[i]["koodiUri"]] = KoodistoKoodi.getLocalizedName(koodiResult[i]);
                        model.uriLocalizedNames[koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = KoodistoKoodi.getLocalizedName(koodiResult[i]);
                        model.uriKoodit[koodiResult[i]["koodiUri"]] = koodiResult[i];
                        model.uriKoodit[koodiResult[i]["koodiUri"] + "#" + koodiResult[i]["versio"]] = koodiResult[i];
                    }
                });
                model.koodisto.localizedKoulutustoimija = "Koulutustoimija";
                model.koodisto.localizedOppilaitos = "Oppilaitos";
            }, function(response) {
                // parenttia ei löytynyt
                showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
            });
            Aliorganisaatiot.get({oid: result.oid}, function(childResult) {
                model.aliorganisaatiot.length = 0;
                if (childResult && childResult.organisaatiot) {
                    for (var i = 0; i < childResult.organisaatiot.length; i++) {
                        if (!childResult.organisaatiot[i].lakkautusPvm) {
                            addAliorganisaatio(childResult.organisaatiot[i].children, 0);
                        }
                    }
                }
            }, function(response) {
                // aliorganisaatiohaku ei onnistunut
                showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);

            });
        };

        addAliorganisaatio = function(aliOrgList, level) {
            if (aliOrgList) {
                for (var j = 0; j < aliOrgList.length; j++) {
                    if (!aliOrgList[j].lakkautusPvm) {
                        model.aliorganisaatiot.push({nimi: getLocalizedValue(aliOrgList[j].nimi, "", ""), oid: aliOrgList[j].oid, level: level});
                        addAliorganisaatio(aliOrgList[j].children, level + 1);
                    }
                }
            }
        };

        isParentOrganisaatio = function() {
            return (!model.organisaatio.parentOid || model.organisaatio.parentOid === model.OPHOid);
        };

        this.refreshIfNeeded = function(oid) {
            if (oid) {
                if (oid !== model.organisaatio.oid) {
                    if (model.keepsavestatus) {
                        model.keepsavestatus = false;
                    } else {
                        model.savestatus = $filter('i18n')("Organisaationmuokkaus.tietojaeitallennettu");
                    }
                }
                Organisaatio.get({oid: oid}, function(result) {
                    refresh(result);
                }, function(response) {
                    // Organisaatiohaku ei onnistunut
                    showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);
                });
            }
        };

        updateLisayhteystietoArvos = function(lisatieto) {
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
                    ytlang = ytlangs[i];
                    // Lisätään jos arvoa ei ole
                    var arvo = null;
                    for (var a in model.organisaatio.yhteystietoArvos) {
                        if ((lisatieto.oid === model.organisaatio.yhteystietoArvos[a]['YhteystietojenTyyppi.oid']) &&
                                (yt.oid === model.organisaatio.yhteystietoArvos[a]['YhteystietoElementti.oid']) &&
                                (ytlang === model.organisaatio.yhteystietoArvos[a]['YhteystietoArvo.kieli'])) {
                            arvo = model.organisaatio.yhteystietoArvos[a];
                        }
                    }

                    // Jos arvoa ei vielä ole, lisätään muokkaus/uudenluontinäkymään bindausta varten
                    if (arvo == null) {
                        var uusiyt = {};
                        uusiyt["YhteystietoArvo.arvoText"] = null;
                        uusiyt["YhteystietoArvo.kieli"] = ytlangs[i];
                        uusiyt["YhteystietojenTyyppi.oid"] = lisatieto.oid;
                        uusiyt["YhteystietoElementti.oid"] = yt.oid;
                        uusiyt["YhteystietoElementti.pakollinen"] = yt.pakollinen;
                        arvo = uusiyt;
                        model.organisaatio.yhteystietoArvos.push(arvo);
                    } else {
                        // jatketaan => mäpätään olemassaolevaan arvoon
                    }
                    // Mäpätään oidista nimeen. Mäppäys on oikeasti 1-1 vaikka nimi toistuu joka tietueessa.
                    if (!model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]]) {
                        model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]] = {};
                    }
                    if (!model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang]) {
                        model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang] = [];
                    }
                    model.lisayhteystiedot[arvo["YhteystietojenTyyppi.oid"]][ytlang].push(arvo);
                }
            });
        };

        updateLisayhteystiedot = function() {
            model.lisayhteystiedot = {};
            var kaikkiTyypit = model.organisaatio.tyypit;
            if (model.organisaatio.oppilaitosTyyppiUri) {
                kaikkiTyypit = kaikkiTyypit.concat(model.organisaatio.oppilaitosTyyppiUri);
            }
            for (tyyppi in kaikkiTyypit) {
                if (model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()]) {
                    model.yhteystietojentyyppi[kaikkiTyypit[tyyppi].toUpperCase()].forEach(function(t) {
                        updateLisayhteystietoArvos(t);
                    });
                }
            }
        };

        refreshKoodisto = function(oid) {
            if (oid === null || (oid !== model.koodisto.oid)) {
                model.koodisto.localizedOppilaitos = "";
                model.koodisto.localizedKoulutustoimija = "";
                model.koodisto.kieliplaceholder = $filter('i18n')("lisaakieli");
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
                        'Muu organisaatio': ["05", "03"],
                        'Koulutustoimija': ["02", "04"],
                        'Oppilaitos': ["03"],
                        'Opetuspiste': ["03"],
                        'Toimipiste': ["03"],
                        'Oppisopimustoimipiste': []};
                    result.forEach(function(orgTyyppiKoodi) {
                        if (KoodistoKoodi.isValid(orgTyyppiKoodi)) {
                            if (sallitutAlaOrganisaatiot[model.parenttype].indexOf(orgTyyppiKoodi.koodiArvo) !== -1) {
                                /*
                                 if (orgTyyppiKoodi.koodiArvo === "03") {
                                 // Koodistossa 'Opetuspiste' on 'Toimipiste'!?
                                 model.koodisto.organisaatiotyypit.push('Opetuspiste');
                                 localizedOrgType = 'Opetuspiste';
                                 } else {
                                 model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                                 localizedOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                                 }
                                 */
                                model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                                localizedOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                            } else if (model.organisaatio.parentOid === model.OPHOid
                                    && orgTyyppiKoodi.koodiArvo === "01") {
                                model.koodisto.organisaatiotyypit.push(KoodistoKoodi.getLocalizedName(orgTyyppiKoodi));
                                localizedOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
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
                            /*
                             if (orgTyyppiKoodi.koodiArvo === "03") {
                             localizedKoodistoOrgType = 'Opetuspiste';
                             } else {
                             localizedKoodistoOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                             }
                             */
                            localizedKoodistoOrgType = KoodistoKoodi.getLocalizedName(orgTyyppiKoodi);
                        }
                    });
                }, function(response) {
                    // organisaatiotyyppejä ei löytynyt
                    showAndLogError("Organisaationtarkastelu.koodistohakuvirhe", response);
                });
                YhteystietojenTyyppi.get({}, function(result) {
                    model.yhteystietojentyyppi = {};
                    for (var ytt in result) {
                        if (result[ytt]['sovellettavatOrganisaatios']) {
                            var tyypit = result[ytt]['sovellettavatOrganisaatios'].concat(result[ytt].sovellettavatOppilaitostyyppis);
                            for (tyyppi in tyypit) {
                                if (!model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()]) {
                                    model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()] = [];
                                }
                                model.yhteystietojentyyppi[tyypit[tyyppi].toUpperCase()].push(result[ytt]);
                            }
                        }
                    }
                    updateLisayhteystiedot();
                }, function(response) {
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
                KoodistoKieli.get({}, function(result) {
                    model.koodisto.isokielet.length = 0;
                    result.forEach(function(kieliKoodi) {
                        // TODO: filter ?
                        var uri = kieliKoodi.koodiUri + "#" + kieliKoodi.versio;
                        model.koodisto.isokielet.push({uri: uri, arvo: kieliKoodi.koodiArvo, nimi: KoodistoKoodi.getLocalizedName(kieliKoodi)});
                        model.uriLocalizedNames[uri] = KoodistoKoodi.getLocalizedName(kieliKoodi);
                    });
                }, function(response) {
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
                        for (var ytindex in model.organisaatio.metadata.yhteystiedot) {
                            var yt = model.organisaatio.metadata.yhteystiedot[ytindex];
                            if (yt.osoite) {
                                var lang = (yt.kieli === null ? "kieli_fi#1" : yt.kieli);
                                if (!(lang in model.mdyhteystiedot.postinumerot)) {
                                    model.mdyhteystiedot.postinumerot[lang] = {};
                                }
                                if (yt.osoiteTyyppi === 'muu') {
                                    // Muita osoitteita voi olla useita, lisää listaan
                                    model.mdyhteystiedot.postinumerot[lang][yt.osoiteTyyppi].push(arvoByUri[yt.postinumeroUri]);
                                } else {
                                    model.mdyhteystiedot.postinumerot[lang][yt.osoiteTyyppi] = arvoByUri[yt.postinumeroUri];
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
                model.uriLocalizedNames["parentnimi"] = getLocalizedValue(result.nimi, "", "", false);
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
                model.organisaatio.alkuPvm = parseDate(yritystiedot.aloitusPvm);
            }

            // YTunnuksella luotu organisaatio on oletusarvoisesti koulutustoimija
            // Ei kuitenkaan poisteta "Koulutustoimija" tyyppiä, jos se on jo asetettu
            var organisaatiotyyppi = "Koulutustoimija";
            if (model.organisaatio.tyypit.indexOf(organisaatiotyyppi) === -1) {
                this.toggleCheckOrganisaatio(organisaatiotyyppi);
            }

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

        clearAddress = function(address) {
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
        selectAddressType = function(md) {
            var ytt = (md ? model.mdyhteystiedot : model.yhteystiedot);
            var langs = (md ? model.mkSections.hp.tabs : [{lang: 'kieli_fi#1'}, {lang: 'kieli_sv#1'}, {lang: 'kieli_en#1'}]);
            for (var tab in langs) {
                var kv_lang = (md ? langs[tab].lang : langs[tab].lang);
                var yt = ytt[kv_lang];
                if (model.osoitemuoto.yt[langs[tab].lang] === 'suomalainen') {
                    clearAddress(yt.ulkomainen_kaynti);
                    clearAddress(yt.ulkomainen_posti);
                } else {
                    clearAddress(yt.kaynti);
                    clearAddress(yt.posti);
                }
            }
        };

        checkLisayhteystiedot = function() {
            for (var i = model.organisaatio.yhteystietoArvos.length - 1; i >= 0; i--) {
                if ((model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.arvoText'] === null) ||
                        (model.organisaatio.yhteystietoArvos[i]['YhteystietoArvo.kieli'] === null)) {
                    model.organisaatio.yhteystietoArvos.splice(i, 1);
                }
            }
        };

        this.persistOrganisaatio = function(orgForm) {
            formatDates();
            selectAddressType(false);
            selectAddressType(true);
            checkLisayhteystiedot();
            convToimipisteToOpetuspiste(model.organisaatio);
            if (model.organisaatio.$post) {
                Organisaatio.post(model.organisaatio, function(result) {
                    //console.log(result);
                    if (orgForm) {
                        orgForm.$setPristine();
                    }
                    model.savestatus = $filter('i18n')("Organisaationmuokkaus.tallennettu") + " " + new Date().toTimeString().substr(0, 8);
                    Alert.closeAlert(model.alert);
                    refresh(result);
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
                    model.keepsavestatus = true;
                    Alert.closeAlert(model.alert);
                    $location.path($location.path().split(model.organisaatio.parentOid)[0] + result.oid + "/edit");
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
                return;
            };
            kieliUri = getKieliUri(model.ytjTiedot.yrityksenKieli);
            if (kieliUri) {
                // lisätään kieli, jos organisaatiolla ei vielä ole YTJ:stä tullutta kieltä
                if (model.organisaatio.kieletUris.indexOf(kieliUri) === -1) {
                    model.organisaatio.kieletUris.push(kieliUri);
                }
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
                    model.initYhteystiedotPlaceholder(model.organisaatio.metadata.yhteystiedot, model.mdyhteystiedot,
                            [lang]);
                }
                for (var field in model.mkSections[section].types) {
                    if (!model.organisaatio.metadata.data[model.mkSections[section].types[field]]) {
                        model.organisaatio.metadata.data[model.mkSections[section].types[field]] = {};
                    }
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
                            model.yhteystiedot['kieli_fi#1'].posti,
                            "posti");
                }
                else {
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.postiOsoite);
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
                    $log.warn("Unknown language in ytj osoite: " + model.ytjTiedot.kayntiOsoite);
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
                for (key in model.sometypes) {
                    if (model.organisaatio.metadata.data[model.sometypes[key]]) {
                        for (key2 in model.organisaatio.metadata.data[model.sometypes[key]][model.smlang]) {
                            return true;
                        }
                    }
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
                        return (tyyppi === "oppilaitostyyppi_11#1" || tyyppi === "oppilaitostyyppi_12#1" || tyyppi === "oppilaitostyyppi_19#1");
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
                if (yt.postinumerot[lang].kaynti) {
                    yt.postinumerot[lang].posti = yt.postinumerot[lang].kaynti;
                    yt[lang].posti.postinumeroUri = koodistoPostiKoodi.uri;
                    yt[lang].posti.postitoimipaikka = koodistoPostiKoodi.paikka;
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
                    if (!('posti' in ytp[lang])) {
                        ytp[lang].posti = {};
                    }
                    for (var kentta in ytp[lang].kaynti) {
                        if ((kentta !== 'osoiteTyyppi') && (kentta !== 'id') && (kentta !== 'yhteystietoOid')) {
                            ytp[lang].posti[kentta] = ytp[lang].kaynti[kentta];
                        }
                    }
                    if (ytp.postinumerot[lang].kaynti) {
                        ytp.postinumerot[lang].posti = ytp.postinumerot[lang].kaynti;
                    }
                } else {                     // kopioi kansainvälinen osoitemuoto
                    if (!('ulkomainen_posti' in ytp[lang])) {
                        ytp[lang].ulkomainen_posti = {};
                    }
                    for (var kentta in ytp[lang].ulkomainen_kaynti) {
                        if ((kentta !== 'osoiteTyyppi') && (kentta !== 'id') && (kentta !== 'yhteystietoOid')) {
                            ytp[lang].ulkomainen_posti[kentta] = ytp[lang].ulkomainen_kaynti[kentta];
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
            if (ret.indexOf("[")===0) {
                ret = $filter('i18n')("Organisaationmuokkaus." + name);
            }
            return ret;
        };

    };

    return model;
});



