app.factory('VoimassaolonMuokkausModel', function($q, $filter, $log, Organisaatiot, Muokkaamonta) {
    
    var model = new function() {
        this.muokataanAlkupvm = false;
        this.alkuPvm = 0;
        this.lakkautusPvm = 0;
        this.originalAlkuPvm = null;
        this.originalLakkautusPvm = null;
        this.oid = "";
        this.aliorganisaatioTree = [];
        this.localizeMonikielinenTeksti = function(){};
        this.organisaationNimi = "";
        this.aliorganisaatioTreeCreated = false;
        
        this.Tila = {
            MUOKKAAMATON: 0,
            ALKU_PVM_POISTETTU: 1,
            ALKU_PVM_ASETETTU: 2,
            ALKU_PVM_SUUNNITELTU: 3,
            LAKKAUTUS_PVM_POISTETTU: 4,
            LAKKAUTUS_PVM_ASETETTU: 5,
            LAKKAUTUS_PVM_SUUNNITELTU: 6,
            LAKKAUTUS_PVM_JATKETTU: 7 // Lakkautetun organisaation viimeinen voimassaolopäivä asetettu suunnitelluksi
        };
        this.muokkauksenTila = this.Tila.MUOKKAAMATON;
        this.isDirty = false; // Käyttäjä on muuttanut voimassaoloa (mutta se voi olla sama kuin alkuperäinen)
        this.isModified = false; // Voimassaolo poikkeaa alkuperäisestä (mutta käyttäjä ei välttämättä ole muuttanut sitä tällä dialogin avauskerralla)
        this.isAcceptable = true; // alkuPvm ei saa olla tyhjä mikäli muokataanAlkupvm, koska se on pakollinen tieto.
        
        this.newVersionNumber = null; // Tallennuksen yhteydessä muuttuvan organisaation versionumeron välitykseen.
        
        this.cancel = function() {
        };
        
        this.accept = function() {
            otaTalteenValintojenLopputilanne(model.aliorganisaatioTree);
        };
        
        isBeforeToday = function(date) {
            var today = +new Date();
            today = pvmRajapintaMuotoon(today);
            var timeToCompare = pvmRajapintaMuotoon(date);
            return timeToCompare < today;
        };
        
        isAfterToday = function(date) {
            var today = +new Date();
            today = pvmRajapintaMuotoon(today);
            var timeToCompare = pvmRajapintaMuotoon(date);
            return timeToCompare > today;
        };
        
        asetaTila = function() {
            var uusiTila = model.muokkauksenTila;
            if (model.muokataanAlkupvm) {
                if (pvmRajapintaMuotoon(model.alkuPvm) === pvmRajapintaMuotoon(model.originalAlkuPvm)) {
                    uusiTila = model.Tila.MUOKKAAMATON;
                } else if (typeof model.alkuPvm === "undefined") {
                    uusiTila = model.Tila.ALKU_PVM_POISTETTU;
                } else if (isAfterToday(model.alkuPvm)) {
                    uusiTila = model.Tila.ALKU_PVM_SUUNNITELTU;
                } else {
                    uusiTila = model.Tila.ALKU_PVM_ASETETTU;
                }
            } else  {
                if (pvmRajapintaMuotoon(model.lakkautusPvm) === pvmRajapintaMuotoon(model.originalLakkautusPvm)) {
                    uusiTila = model.Tila.MUOKKAAMATON;
                } else if (typeof model.lakkautusPvm === "undefined") {
                    uusiTila = model.Tila.LAKKAUTUS_PVM_POISTETTU;
                } else if (isBeforeToday(model.lakkautusPvm)) {
                    uusiTila = model.Tila.LAKKAUTUS_PVM_ASETETTU;
                } else {
                    if (isBeforeToday(model.originalLakkautusPvm)) {
                        uusiTila = model.Tila.LAKKAUTUS_PVM_JATKETTU;
                    } else {
                        uusiTila = model.Tila.LAKKAUTUS_PVM_SUUNNITELTU;
                    }
                }
            }
            if (uusiTila !== model.muokkauksenTila) {
                model.muokkauksenTila = uusiTila;
            }
        };
        
        asetaAcceptable = function() {
            model.isAcceptable = !(model.muokataanAlkupvm && typeof model.alkuPvm === "undefined");
            if (model.muokataanAlkupvm) {
                model.isModified = pvmRajapintaMuotoon(model.alkuPvm) !== pvmRajapintaMuotoon(model.originalAlkuPvm);
            } else {
                model.isModified = pvmRajapintaMuotoon(model.lakkautusPvm) !== pvmRajapintaMuotoon(model.originalLakkautusPvm);
            }
        }
        
        this.checkboxChanged = function(data) {
            model.isDirty = true;
            asetaSamaValintaLapsille(data, data.valittu);
            asetaValintojenPakotus();
        };
        
        this.voimassaoloChanged = function() {
            model.isDirty = true;
            asetaAcceptable();
            asetaTila();
            asetaValintojenPakotus();
        };
        
        // Jos organisaatio ollaan lakkauttamassa:
        // - Lähtökohtaisesti käyttäjä ei voi valita lakkautettavia aliorganisaatioita, vaan kaikki aliorganisaatiot pakotetaan lakkautettaviksi.
        // - Poikkeuksena aliorganisaatiot joilla on jo lakkautus pvm. Näiden kohdalta käyttäjä saa päättää (kunhan lakkautuspäivä on sama tai aiempi).
        // Jos organisaation alkupäivää ollaan muuttamassa:
        // - Aliorganisaatiot joiden alkupvm olisi jäämässä aikaisemmaksi, pakotetaan muutettaviksi.
        // Lisäksi huolehditaan, että oranisaatio valitaan jos sen aliorganisaatiopuuhun on tulossa sitä laajempi voimassaolo
        pakotaValinnat = function(treeLevel, alkuPvm, lakkautusPvm, alkuPvmMuutettu, loppuPvmMuutettu) {
            var valintoja = false;
            for (var i = 0; i < treeLevel.length; i++) {
                var treeItem = treeLevel[i];
                
                if (treeItem.level === 0) {
                    // No need to touch root
                } else if (loppuPvmMuutettu) {
                    // Pakota aktiiviset aliorganisaatiot lakkautettaviksi
                    if (treeItem.lakkautusPvm == "") {
                        treeItem.valittu = true;
                        treeItem.readonly = true;
                    }
                    // Tarkista ettei aliorganisaation voimassaolo pääse jatkumaan yli lakkautuspäivän.
                    else if (treeItem.lakkautusPvm > lakkautusPvm) {
                        treeItem.valittu = true;
                        treeItem.readonly = true;
                    } else {
                        treeItem.readonly = false;
                    }
                } else if (alkuPvmMuutettu) {
                    // Pakota alkupäivättömät aliorganisaatiot (niitä ei tosin pitäisi olla)
                    if (treeItem.alkuPvm == "") {
                        treeItem.valittu = true;
                        treeItem.readonly = true;
                    }
                    // Tarkista ettei aliorganisaation voimassaolo pääse jatkumaan yli lakkautuspäivän.
                    else if (treeItem.alkuPvm < alkuPvm) {
                        treeItem.valittu = true;
                        treeItem.readonly = true;
                    } else {
                        treeItem.readonly = false;
                    }
                }
                
                var aliorganisaatioPuussaValintoja = 
                        pakotaValinnat(treeItem.children, alkuPvm, lakkautusPvm, alkuPvmMuutettu, loppuPvmMuutettu);
                
                if (!treeItem.valittu && aliorganisaatioPuussaValintoja) {
                    // Tarkista ettei aliorganisaatioissa olla siirtämässä luontipäivää aikaisemmaksi
                    // tai lakkautuspäivää myöhäisemmäksi kuin tällä niiden parentilla (jos, niin valitse tämäkin).
                    $log.log(treeItem.nimi + " " + treeItem.alkuPvm + " " + alkuPvm + " " + alkuPvmMuutettu + " " + (alkuPvm < treeItem.alkuPvm));
                    if (alkuPvmMuutettu && (alkuPvm < treeItem.alkuPvm)) {
                        treeItem.valittu = true;
                    } else if (loppuPvmMuutettu && (treeItem.lakkautusPvm < lakkautusPvm)) {
                        treeItem.valittu = true;
                    }
                }
                
                if (treeItem.valittu || aliorganisaatioPuussaValintoja) {
                    valintoja = true;
                }
            }
            return valintoja;
        };
        
        asetaValintojenPakotus = function() {
            var alkuPvm = new Date(model.alkuPvm);
            var lakkautusPvm = new Date(model.lakkautusPvm);
            
            var alkuPvmMuutettu = (model.muokkauksenTila === model.Tila.ALKU_PVM_ASETETTU ||
                           model.muokkauksenTila === model.Tila.ALKU_PVM_SUUNNITELTU);
                   
            var loppuPvmMuutettu = (model.muokkauksenTila === model.Tila.LAKKAUTUS_PVM_ASETETTU ||
                           model.muokkauksenTila === model.Tila.LAKKAUTUS_PVM_SUUNNITELTU ||
                           model.muokkauksenTila === model.Tila.LAKKAUTUS_PVM_JATKETTU);
                   
            pakotaValinnat(model.aliorganisaatioTree, alkuPvm.getTime(), lakkautusPvm.getTime(), alkuPvmMuutettu, loppuPvmMuutettu);
        }
        
        // aliorganisaatioTree:n 'valittu' kenttä näkyy käyttöliittymässä checkboxeina.
        // Tätä dialogin modelia käytetään sekä alku että lakkautuspäivämäärää muokattaessa (model.muokataanAlkupvm).
        // Riippuen kumpaa ollaan muokkaamassa, 'valittu' kenttiin kopioidaan joko 'alkuPvmValittu' tai 'lakkautusPvmValittu'.
        // Kun dialogi suljetaan, riippuen siitä hyväksytäänkö dialogi vai perutaanko, 'valittu' arvot kopioidaan takaisin
        // 'alkuPvmValittu' tai 'lakkautusPvmValittu' kenttiin, tai jätetään kopioimatta (ks. accept ja cancel funktiot).
        asetaValintojenAlkutilanne = function(treeLevel) {
            for (var i = 0; i < treeLevel.length; i++) {
                treeLevel[i].valittu = model.muokataanAlkupvm ? treeLevel[i].alkuPvmValittu : treeLevel[i].lakkautusPvmValittu;
                asetaValintojenAlkutilanne(treeLevel[i].children);
            }
        };
        
        otaTalteenValintojenLopputilanne = function(treeLevel) {
            for (var i = 0; i < treeLevel.length; i++) {
                if (model.muokataanAlkupvm) {
                    treeLevel[i].alkuPvmValittu = treeLevel[i].valittu;
                } else {
                    treeLevel[i].lakkautusPvmValittu = treeLevel[i].valittu;
                }
                otaTalteenValintojenLopputilanne(treeLevel[i].children);
            }
        };
        
        this.configure = function(muokataanAlkupvm, oid, nimi, alkuPvm, lakkautusPvm, aliorganisaatioHaunTulos, monikielinenTekstiLocalizer) {
            if (model.originalAlkuPvm === null) {
                model.originalAlkuPvm = alkuPvm;
                model.originalLakkautusPvm = lakkautusPvm;
            }
            model.muokataanAlkupvm = muokataanAlkupvm;
            asetaValintojenAlkutilanne(model.aliorganisaatioTree);
            model.alkuPvm = alkuPvm;
            model.lakkautusPvm = lakkautusPvm;
            model.localizeMonikielinenTeksti = monikielinenTekstiLocalizer;
            model.oid = oid;
            model.organisaationNimi = model.localizeMonikielinenTeksti(nimi, "", "");
            asetaAcceptable();
            asetaTila();
            asetaValintojenPakotus();
            
            luoAliorganisaatioPuu(aliorganisaatioHaunTulos);
        };
        
        
        this.expand = function (node) {
            if (node.expanded !== true) {
                node.expanded = true;
            } else {
                node.expanded = false;
            }
        };

        this.isExpanded = function (data) {
            return data.expanded;
        };

        this.isCollapsed = function (data) {
            return !this.isExpanded(data);
        };

        this.isLeaf = function(aliorganisaatio) {
            //$log.log(aliorganisaatio.nimi);
            return aliorganisaatio.children.length === 0;
        };
        
        asetaSamaValintaLapsille = function(item, valinta) {
            for (var i = 0; i < item.children.length; i++) {
                if (!item.children[i].readonly) {
                    item.children[i].valittu = valinta;
                }
                asetaSamaValintaLapsille(item.children[i]);
            }
        };
        
        this.getTimes = function(n){
            return new Array(n);
        };
        
        // Konvertoi päivämäärän rajapinnan hyväksymään muotoon yyyy-mm-dd
        pvmRajapintaMuotoon = function(dateToFormat) {
            if (dateToFormat) {
                d = new Date(dateToFormat);
                date = 100 + d.getDate();
                month = 100 + d.getMonth() + 1;
                year = d.getFullYear();
                return year + "-" + month.toString().slice(1) + "-" + date.toString().slice(1);
            }
            return;
        };
        
        // Konvertoi päivämäärän rajapinnan hyväksymään muotoon yyyy-mm-dd
        pvmKatseltavaanMuotoon = function(dateToFormat) {
            if (dateToFormat) {
                d = new Date(dateToFormat);
                date = d.getDate();
                month = d.getMonth() + 1;
                year = d.getFullYear();
                return date.toString() + "." + month.toString() + "." + year.toString();
            }
            return;
        };
        
        isAliorganisaatioPassivoitu = function(alkuPvm, lakkautusPvm) {
            var today = +new Date();
            today = pvmRajapintaMuotoon(today);
            var lakkautus = pvmRajapintaMuotoon(lakkautusPvm);
            if (lakkautus && lakkautus < today) {
                return true;
            }
            return false;
        };
        
        getAliorganisaationTila = function(alkuPvm, lakkautusPvm) {

            var today = +new Date();
            today = pvmRajapintaMuotoon(today);

            var aloitus = pvmRajapintaMuotoon(alkuPvm);
            var lakkautus = pvmRajapintaMuotoon(lakkautusPvm);
            
            //$log.log(today + " " + aloitus + " " + lakkautus);

            if (aloitus) {
                
                if (aloitus > today) {
                    return ($filter('i18n')("Organisaatiot.suunniteltu","")) + " " + pvmKatseltavaanMuotoon(aloitus);
                }
            }

            if (lakkautus) {
                
                if (lakkautus < today) {
                    return ($filter('i18n')("Organisaatiot.passivoitu","")) + " " + pvmKatseltavaanMuotoon(lakkautus);
                } else {
                    return ($filter('i18n')("Organisaatiot.passivoidaan","")) + " " + pvmKatseltavaanMuotoon(lakkautus);
                }
            }
            return ($filter('i18n')("Organisaatiot.aktiivinen",""));
        };
        
        constructAliorganisaatioTree = function(aliOrgList, level, localizedNameOfParent) {
            var constructedTree = [];
            if (aliOrgList) {
                var expanded = false;
                if (aliOrgList.length < 20) {
                    expanded = true;
                }
                
                for (var i = 0; i < aliOrgList.length; i++) {
                    var item = aliOrgList[i];
                    //$log.log("ITEM:" + JSON.stringify(item, null, 4));
                    
                    var lakkautusPvm = "";
                    if (item.lakkautusPvm) {
                        lakkautusPvm = item.lakkautusPvm;
                    }
                    
                    var nimi = model.localizeMonikielinenTeksti(item.nimi, "", "");                    
                    
                    var tila = getAliorganisaationTila(item.alkuPvm, item.lakkautusPvm);
                    
                    var lakkautettu = isBeforeToday(item.lakkautusPvm);
                    
                    var tyyppi = "";
                    
                    if (item.organisaatiotyypit) {
                        for (var j = 0; j < item.organisaatiotyypit.length; j++) {
                            if (j !== 0) {
                                tyyppi += ", ";
                            }
                            tyyppi += $filter('i18n')("Organisaatiot."+item.organisaatiotyypit[0], "");
                        }
                    }
                    
                    var passivoituText = isAliorganisaatioPassivoitu(item.alkuPvm, item.lakkautusPvm) ? " (Passivoitu)" : "";
                    
                    var children = constructAliorganisaatioTree(item.children, level + 1, nimi);
                    
                    if (localizedNameOfParent) {
                        nimi = nimi.replace(localizedNameOfParent + ", ", "");
                    }
                    
                    nimi = nimi + passivoituText
                    
                    var treeItem = {
                        //original: item,
                        oid: item.oid,
                        nimi: nimi,
                        tila: tila,
                        lakkautettu: lakkautettu,
                        tyyppi: tyyppi,
                        alkuPvm: item.alkuPvm,
                        lakkautusPvm: lakkautusPvm,
                        level: level,
                        valittu: true, // joko lakkautusPvmValittu tai valittuAlkuPvm riippuen onko muokataanAlkupvm
                        lakkautusPvmValittu: false,
                        alkuPvmValittu: true,
                        readonly: false,
                        children: children,
                        expanded: expanded};
                    //$log.log("PUSH:" + JSON.stringify(treeItem, null, 4));
                    constructedTree.push(treeItem);
                }
            }
            return constructedTree;
        };
        
        luoAliorganisaatioPuu = function(aliorganisaatioHaunTulos) {
            if (model.aliorganisaatioTreeCreated) {
                return;
            }
            
            if (!aliorganisaatioHaunTulos) {
                return;
            }
            
            model.aliorganisaatioTreeCreated = true;
            
            for (var i = 0; i < aliorganisaatioHaunTulos.length; i++) {
                var arr = [];
                arr.push(aliorganisaatioHaunTulos[i]);
                var treeRoot = constructAliorganisaatioTree(arr, 0, null);
                treeRoot[0].valittu = true;
                treeRoot[0].readonly = true;
                model.aliorganisaatioTree.push(treeRoot[0]);
            }
        };
        
        addToRequestList = function(voimassaoloLista, treeLevel) {
            for (var i = 0; i < treeLevel.length; i++) {
                if (treeLevel[i].alkuPvmValittu || treeLevel[i].lakkautusPvmValittu) {
                    
                    var alkuPvm = treeLevel[i].alkuPvm;
                    var lakkautusPvm = treeLevel[i].lakkautusPvm;
                    
                    if (treeLevel[i].alkuPvmValittu) {
                        alkuPvm = model.alkuPvm;
                    }
                    if (treeLevel[i].loppuPvmValittu) {
                        lakkautusPvm = model.lakkautusPvm;
                    }
                    voimassaoloLista.push({oid: treeLevel[i].oid, alkuPvm: pvmRajapintaMuotoon(alkuPvm), loppuPvm: pvmRajapintaMuotoon(lakkautusPvm)});
                }
                addToRequestList(voimassaoloLista, treeLevel[i].children);
            }
        };
        
        // Tallennus
        this.save = function() {
            model.newVersionNumber = null;
            var deferred = $q.defer();
            
            if (!model.aliorganisaatioTree || !model.aliorganisaatioTree.length) {
                deferred.resolve();
                return deferred.promise;
            }
            
            var voimassaoloLista = [];
            addToRequestList(voimassaoloLista, model.aliorganisaatioTree);
            
            Muokkaamonta.put(voimassaoloLista, function(result) {
                $log.log(result);
                if (!result.ok) {
                    $log.error("Voimassaolon muokkaus virhe: " + result.message);
                    deferred.reject();
                    // TODO: Virheen näyttö UI:ssa
                } else {
                    // Pick the new version
                    for (var i = 0; i < result.tulokset.length; i++) {
                        if (result.tulokset[i].oid == model.oid) {
                            model.newVersionNumber = result.tulokset[i].version;
                            break;
                        }
                    }
                    deferred.resolve();
                }
            },
            // Error case
            function(response) {
                $log.error("Voimassaolon muokkaus response: " + response.status);
                Alert.add("error", $filter('i18n')("Voimassaolonmuokkaus.virhe", ""), true); // TODO: oikea virhekoodi
                deferred.reject();
            });

            return deferred.promise;
        };
        
    };
        
    return model;
});



