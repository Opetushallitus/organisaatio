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

app.factory('OrganisaatioTreeModel', function($q, $filter, $log, $injector,
                                              Alert, Organisaatiot, LocalisationService) {
// organisaatiot[]
//     {
//        "oid" : "1.2.246.562.10.71103955986",
//        "alkuPvm" : 694216800000,
//        "parentOid" : "1.2.246.562.10.45506210314",
//        "parentOidPath" : "1.2.246.562.10.71103955986/1.2.246.562.10.45506210314/1.2.246.562.10.45754497167/1.2.246.562.10.00000000001",
//        "match" : true,
//        "nimi" : {
//          "fi" : "Kappelimäen koulu"
//        },
//        "children" : [ ],
//        "organisaatiotyypit" : [ "TOIMIPISTE" ],
//        "aliOrganisaatioMaara" : 0
//      }

    $log = $log.getInstance("OrganisaatioTreeModel");
    var loadingService = $injector.get('LoadingService');

    var tree = {id: "ROOT", expanded: false, children: []};

    var model = {
        count: 0,

        getRootNodes: function () {
            return tree.children;
        },

        expand: function (node) {
            if (node.expanded !== true) {
                node.expanded = true;
            } else {
                node.expanded = false;
            }
        },

        isExpanded: function (data) {
            return data.expanded;
        },

        isCollapsed: function (data) {
            return !this.isExpanded(data);
        },

        isLeaf: function (data) {
            return data.children.length === 0;
        },

        hasChildren: function (node) {
            return node.aliOrganisaatioMaara !== 0;
        },

        deleteNode: function (node) {
            // Etsitään noodin parent
            var findParent = function(parentNode) {
                var parent;
                for(var i=0; i < parentNode.children.length; i++) {
                    if (node === parentNode.children[i]) {
                        return parentNode;
                    }
                }
                for(var j=0; j < parentNode.children.length; j++) {
                    parent = findParent(parentNode.children[j]);
                    if (parent) {
                        return parent;
                    }
                }
            };

            // Etsitään noodin parenttia, jotta voidaan poistaa se parentista
            var parent = findParent(tree);

            // Jos parent löytyi niin postetaan se children listalta
            if (parent) {
                var index = -1;
                for(var i=0; i < parent.children.length; i++) {
                    if (node === parent.children[i]) {
                        index = i;
                    }
                }
                if (index > -1) {
                    parent.children.splice(index, 1);

                    // Vähennetään myös listalla olevien organisaatioiden määrää
                    model.count = model.count-1;
                }
            }
        },

        getNimiForOid: function (oid) {
            // Etsitään noodi oidin perusteella
            var findOid = function(node) {
                var tempNode;
                if (node.oid && oid === node.oid) {
                    return node;
                }
                for(var i=0; i < node.children.length; i++) {
                    tempNode = findOid(node.children[i]);
                    if (tempNode) {
                        return tempNode;
                    }
                }
                return;
            };

            // Etsitään noodia jonka oid on annettu oid
            var node = findOid(tree);

            // Jos parent löytyi niin postetaan se children listalta
            if (node) {
                return model.getNimi(node);
            }
            return "--";
        },

        isAktiivinen: function (node) {
            var today = +new Date();

            if ('alkuPvm' in node) {
                var alkuPvm = new Date(node.alkuPvm);

                if (alkuPvm > today) {
                    // suunnitteilla
                    return false;
                }
            }
            if ('lakkautusPvm' in node) {
                var lakkautusPvm = new Date(node.lakkautusPvm);

                if (lakkautusPvm < today) {
                    // passivoitu
                    return false;
                }
            }
            return true;
        },

        isTyyppi: function (node, tyyppi) {
            if ('organisaatiotyypit' in node) {
                if (node.organisaatiotyypit.indexOf(tyyppi) > -1) {
                    return true;
                }
            }
            return false;
        },

        getTila: function(node) {
            var today = +new Date();

            if ('alkuPvm' in node) {
                var alkuPvm = new Date(node.alkuPvm);

                if (alkuPvm > today) {
                    return ($filter('i18n')("Organisaatiot.suunniteltu",""));
                }
            }
            if ('lakkautusPvm' in node) {
                var lakkautusPvm = new Date(node.lakkautusPvm);

                if (lakkautusPvm < today) {
                    return ($filter('i18n')("Organisaatiot.passivoitu",""));
                }
            }
            return ($filter('i18n')("Organisaatiot.aktiivinen",""));
        },

        getNimi: function (node) {
            if (LocalisationService.getLocale() in node.nimi &&
                    node.nimi[LocalisationService.getLocale()]) {
                return node.nimi[LocalisationService.getLocale()];
            }

            // Ei löytynyt nimeä käyttäjän kielellä, kokeillaan muut vaihtoehdot
            if ('fi' in node.nimi && node.nimi.fi) {
                return node.nimi.fi;
            }
            if ('sv' in node.nimi && node.nimi.sv) {
                return node.nimi.sv;
            }
            if ('en' in node.nimi && node.nimi.en) {
                return node.nimi.en;
            }
            return "--";
        },

        getTunnus: function (node) {
            if ('oppilaitosKoodi' in node) {
                return node.oppilaitosKoodi;
            }
            if ('ytunnus' in node) {
                return node.ytunnus;
            }
            return "\u00A0";
        },

        getTyypit: function (node) {
            if ('organisaatiotyypit' in node) {
                var tyypit = "";
                for(var i=0; i < node.organisaatiotyypit.length; i++) {
                    if (i !== 0) {
                        tyypit += ", ";
                    }
                    tyypit += $filter('i18n')("Organisaatiot."+node.organisaatiotyypit[i], "");
                }
                return tyypit;
            }
            return "\u00A0";
        },

        buildHakuParametrit: function(hakuehdot) {
            $log.log('buildHakuParametrit()');

            var hakuParametrit = {};

            // Lisää hakuun käyttäjän kirjoittama osa organisaation nimest' / tunnuksesta
            hakuParametrit.searchstr = hakuehdot.nimiTaiTunnus;

            // Lisää hakuun mahdollinen paikkakunta
            if (hakuehdot.kunta) {
                hakuParametrit.kunta = hakuehdot.kunta;
            }

            // Lisää hakuun mahdollinen organisaatiotyyppi
            if (hakuehdot.organisaatiotyyppi) {
                // TODO: Tämä pitäisi korvata koodisto-urilla
                hakuParametrit.organisaatiotyyppi = hakuehdot.organisaatiotyyppi;
            }

            // Lisää hakuun mahdollinen oppilaitostyyppi
            if (hakuehdot.oppilaitostyyppi) {
                hakuParametrit.oppilaitostyyppi = hakuehdot.oppilaitostyyppi + "#*";
            }

            // Lisää hakuun organisaation tilat
            hakuParametrit.aktiiviset   = hakuehdot.aktiiviset;
            hakuParametrit.suunnitellut = hakuehdot.suunnitellut;
            hakuParametrit.lakkautetut  = hakuehdot.lakkautetut;

            // Haetaan vain rajatuista organisaatioista
            if (hakuehdot.organisaatioRajaus) {
                hakuParametrit.oidRestrictionList = hakuehdot.rajatutOrganisaatiot;
            }

            $log.log("Hakuparametrit ", hakuParametrit);

            return hakuParametrit;
        },

        updateTree: function(numHits, organisaatiot) {
            $log.log('updateTree()');
            this.count = numHits;
            tree.children = organisaatiot;

            if (this.count === 0) {
                Alert.add("warning", $filter('i18n')("Organisaatiot.eiHakutuloksia", ""), true);
            }

            var updateSubtree = function(node, level, expanded, parent) {
                node.i18nNimi = model.getNimi(node);
                if (parent) {
                    node.i18nNimi = node.i18nNimi.replace(model.getNimi(parent) + ", ", "");
                }
                if (model.isAktiivinen(node) === false) {
                    node.i18nNimi += " (" + model.getTila(node) + ")";
                }
                node.tunnus = model.getTunnus(node);
                node.tyypit = model.getTyypit(node);
                node.tila   = model.getTila(node);
                node.level  = level;

                if (level === 2 && model.count > 20) {
                    expanded = false;
                }
                for(var i=0; i < node.children.length; i++) {
                    updateSubtree(node.children[i], level + 1, expanded, node);
                }
                node.expanded = expanded;
            };

            tree.children.forEach(function(node) {
                var expanded = true;

                // Jos ylimmällä tasolla on paljon noodeja, niin ei laajenneta puuta
                if (tree.children.length > 20) {
                    expanded = false;
                }
                updateSubtree(node, 1, expanded, null);
            });
        },

        refresh: function(hakuehdot) {
            $log.log('refresh()');
            var start = +new Date();

            var deferred = $q.defer();
            var parametrit = this.buildHakuParametrit(hakuehdot);

            Organisaatiot.get(parametrit, function(result) {
                var end = +new Date();  // log end timestamp
                var diff = end - start;
                $log.log("Haku kesti: " +diff);

                model.updateTree(result.numHits, result.organisaatiot);

                deferred.resolve();
            },
            // Error case
            function(response) {
                loadingService.onErrorHandled(response);
                $log.error("Organisaatiot response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaatiot.hakuVirhe", ""), true);

                deferred.reject();
            });
            return deferred.promise;
        }
    };

    return model;
});
