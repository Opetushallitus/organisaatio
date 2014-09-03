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
        this.dataLoaded = false;
        
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
        
        getTila = function() {
            if (model.muokataanAlkupvm) {
                if (pvmRajapintaMuotoon(model.alkuPvm) === pvmRajapintaMuotoon(model.originalAlkuPvm)) {
                    return model.Tila.MUOKKAAMATON;
                } else if (typeof model.alkuPvm === "undefined") {
                    return model.Tila.ALKU_PVM_POISTETTU;
                } else if (isAfterToday(model.alkuPvm)) {
                    return model.Tila.ALKU_PVM_SUUNNITELTU;
                } else {
                    return model.Tila.ALKU_PVM_ASETETTU;
                }
            } else  {
                if (pvmRajapintaMuotoon(model.lakkautusPvm) === pvmRajapintaMuotoon(model.originalLakkautusPvm)) {
                    return model.Tila.MUOKKAAMATON;
                } else if (typeof model.lakkautusPvm === "undefined") {
                    return model.Tila.LAKKAUTUS_PVM_POISTETTU;
                } else if (isBeforeToday(model.lakkautusPvm)) {
                    return model.Tila.LAKKAUTUS_PVM_ASETETTU;
                } else {
                    if (isBeforeToday(model.originalLakkautusPvm)) {
                        return model.Tila.LAKKAUTUS_PVM_JATKETTU;
                    }
                    return model.Tila.LAKKAUTUS_PVM_SUUNNITELTU;
                }
            }
        };
        
        this.makeDirty = function() {
            model.isDirty = true;
        };
        
        this.updateTila = function(dirty) {
            // TODO: mahdolliset alkuPvm:ään liittyvät valintojen pakotukset (esim. aliorganisaatio ei voi alkaa ennen organisaatiota).
            var uusiTila = getTila();
            var vanhaTilaPakottaaValinnat = model.muokkauksenTila === model.Tila.LAKKAUTUS_PVM_ASETETTU || model.muokkauksenTila === model.Tila.LAKKAUTUS_PVM_SUUNNITELTU;
            if (uusiTila !== model.muokkauksenTila) {
                
                var uusiTilaPakottaaValinnat = uusiTila === model.Tila.LAKKAUTUS_PVM_ASETETTU || uusiTila === model.Tila.LAKKAUTUS_PVM_SUUNNITELTU;
                if (uusiTilaPakottaaValinnat) {
                    pakotaValinnat(true, dirty);
                }
                if (vanhaTilaPakottaaValinnat && !uusiTilaPakottaaValinnat) {
                    pakotaValinnat(false, dirty);
                }
                model.muokkauksenTila = uusiTila;
            } else if (vanhaTilaPakottaaValinnat) {
                pakotaValinnat(true, dirty); // Lakkautettujen aliorganisaatioiden "uudelleenlakkauttamisen" huomioimiseksi.
            }
            
            model.isAcceptable = !(model.muokataanAlkupvm && typeof model.alkuPvm === "undefined");
            if (model.muokataanAlkupvm) {
                model.isModified = pvmRajapintaMuotoon(model.alkuPvm) !== pvmRajapintaMuotoon(model.originalAlkuPvm);
            } else {
                model.isModified = pvmRajapintaMuotoon(model.lakkautusPvm) !== pvmRajapintaMuotoon(model.originalLakkautusPvm);
            }
            model.isDirty = dirty;
        };
        
        // Jos organisaatio ollaan lakkauttamassa, käyttäjä ei voi valita lakkautettavia aliorganisaatioita,
        // vaan kaikki aliorganisaatiot pakotetaan lakkautettaviksi, poikkeuksena aliorganisaatiot joilla on jo lakkautus pvm.
        pakotaAliorganisaatioValinnat = function(treeLevel, pakotus, dirty) {
            for (var i = 0; i < treeLevel.length; i++) {
                treeLevel[i].readonly = pakotus;
                if (pakotus) {
                    var uusiPakotus = treeLevel[i].lakkautusPvm == ""; // Lähtökohtaisesti älä "uudelleenlakkauta" jo lakkautettua...
                    // ...mutta "uudelleenlakkauta" tarvittaessa
                    if (pvmRajapintaMuotoon(model.lakkautusPvm) < pvmRajapintaMuotoon(treeLevel[i].lakkautusPvm)) {
                        uusiPakotus = true;
                    }
                    if (dirty) { // Älä koske valintoihin jos ollaan avaamassa dialogia
                        treeLevel[i].valittu = uusiPakotus;
                    }
                    treeLevel[i].readonly = uusiPakotus;
                }
                pakotaAliorganisaatioValinnat(treeLevel[i].children);
            }
        };
        
        pakotaValinnat = function(pakotus, dirty) {
            for (var i = 0; i < model.aliorganisaatioTree.length; i++) {
                pakotaAliorganisaatioValinnat(model.aliorganisaatioTree[i].children, pakotus, dirty);
            }
        };
        
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
        
        this.configure = function(muokataanAlkupvm, oid, nimi, alkuPvm, lakkautusPvm, monikielinenTekstiLocalizer) {
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
            model.updateTila(false);
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
        
        this.userChanged = function(data) {
            asetaSamaValintaLapsille(data, data.valittu);
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
                        lakkautusPvmValittu: true,
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
        
        this.getAliorganisaatiot = function() {
            if (model.dataLoaded) {
                return;
            }
            
            var hakuParametrit = {};
            hakuParametrit.aktiiviset   = true;
            hakuParametrit.suunnitellut = true;
            hakuParametrit.lakkautetut  = true;
            hakuParametrit.oidRestrictionList = [model.oid];
            
            Organisaatiot.get(hakuParametrit, function(result) {
                model.aliorganisaatioTree.length = 0;
                //$log.log(JSON.stringify(result, null, 4));
                if (result && result.organisaatiot) {
                    for (var i = 0; i < result.organisaatiot.length; i++) {
                        var arr = [];                                                                                               
                        arr.push(result.organisaatiot[i]);                                                                                                          
                        var treeRoot = constructAliorganisaatioTree(arr, 0, null);
                        treeRoot[0].readonly = true;
                        model.aliorganisaatioTree.push(treeRoot[0]);
                    }
                }
                //$log.log("TREE:" + JSON.stringify(model.aliorganisaatioTree, null, 4));
                $log.log("Data loaded.");
                model.dataLoaded = true;
            }, function(response) {
                // aliorganisaatiohaku ei onnistunut
                // TODO!!
                $log.log('Error: ' + response);
                //showAndLogError("Organisaationtarkastelu.organisaatiohakuvirhe", response);

            });
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
            var deferred = $q.defer();
            
            if (!model.aliorganisaatioTree || !model.aliorganisaatioTree.length) {
                deferred.resolve();
                return deferred.promise;
            }
            
            var voimassaoloLista = [];
            addToRequestList(voimassaoloLista, model.aliorganisaatioTree);
            
            Muokkaamonta.put(voimassaoloLista, function(result) {
                $log.log(result);
                deferred.resolve();
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



