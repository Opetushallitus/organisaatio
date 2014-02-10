app.factory('OrganisaatioTreeModel', function($filter, $log, Alert, Organisaatiot) {
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
//        "organisaatiotyypit" : [ "OPETUSPISTE" ],
//        "aliOrganisaatioMaara" : 0
//      }

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

        deleteNode: function (node) {
            // Etsitään noodin parent
            findParent = function(parentNode) {
                var parent;
                for(var i=0; i < parentNode.children.length; i++) {
                    if (node === parentNode.children[i]) {
                        return parentNode;
                    }
                } 
                for(var i=0; i < parentNode.children.length; i++) {
                    parent = findParent(parentNode.children[i]);
                    if (parent) {
                        return parent;
                    }
                }
                return;
            };  

            // Etsitään noodin parenttia, jotta voidaan poistaa se parentista
            parent = findParent(tree);
            
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

        getStatus: function (node) {
           var today = +new Date(); 
           
           if ('alkuPvm' in node) {
              var alkuPvm = new Date(node.alkuPvm);
              
              if (alkuPvm > today) {
                  return ($filter('i18n')("Organisaatiot.suunnitteilla",""));
              }
           }
           if ('lakkautusPvm' in node) {
              var lakkautusPvm = new Date(node.lakkautusPvm);
              
              if (lakkautusPvm < today) {
                  return ($filter('i18n')("Organisaatiot.lakkautettu",""));
              }
           }
           return null;
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
            if ('fi' in node.nimi) {
                return node.nimi.fi;
            }
            if ('sv' in node.nimi) {
                return node.nimi.sv;
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
        
        getTyyppi: function (node) {
            function capitalize (text) {
                        return text.charAt(0).toUpperCase() + text.slice(1).toLowerCase();
            }

            if ('organisaatiotyypit' in node) {
                return capitalize(node.organisaatiotyypit[0]);
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
                hakuParametrit.organisaatiotyyppi = hakuehdot.organisaatiotyyppi;
            }

            // Lisää hakuun mahdollinen oppilaitostyyppi
            if (hakuehdot.oppilaitostyyppi) {
                hakuParametrit.oppilaitostyyppi = hakuehdot.oppilaitostyyppi + "#*";
            }

            // Vain aktiiviset tain vain lakkautetut
            if (hakuehdot.tila === hakuehdot.vainAktiiviset) {
                hakuParametrit.vainAktiiviset = true;
            }
            else if (hakuehdot.tila === hakuehdot.vainLakkautetut) {
                hakuParametrit.vainLakkautetut = true;
            }

            // Haetaan vain rajatuista organisaatioista
            if (hakuehdot.organisaatioRajaus) {
                hakuParametrit.oidRestrictionList = hakuehdot.rajatutOrganisaatiot;
            }

            $log.log(hakuParametrit);

            return hakuParametrit;
        },

        updateTree: function(numHits, organisaatiot) {
            $log.log('updateTree()');
            this.count = numHits;
            tree.children = organisaatiot;
            
            if (this.count === 0) {
                Alert.add("warning", $filter('i18n')("Organisaatiot.eiHakutuloksia", ""), true);
            }
            
            updateSubtree = function(node, level, expanded) {
                node.i18nNimi = model.getNimi(node);
                if (model.getStatus(node)) {
                    node.i18nNimi += " (" + model.getStatus(node) + ")";
                }
                node.tunnus = model.getTunnus(node);
                node.tyyppi = model.getTyyppi(node);
                node.tila   = model.getTila(node);
                node.level  = level;

                if (level === 2 && model.count > 20) {
                    expanded = false;
                }
                for(var i=0; i < node.children.length; i++) {
                    this.updateSubtree(node.children[i], level + 1, expanded);
                }
                node.expanded = expanded;
            };
            
            tree.children.forEach(function(node) {
                var expanded = true;
                
                // Jos ylimmällä tasolla on paljon noodeja, niin ei laajenneta puuta
                if (tree.children.length > 20) {
                    expanded = false;
                }
                this.updateSubtree(node, 1, expanded);
            });
        },

        refresh: function(hakuehdot) {
            $log.log('refresh()');
            var start = +new Date();
            
            var parametrit = this.buildHakuParametrit(hakuehdot);

            Organisaatiot.get(parametrit, function(result) {
                //$log.log("Solar returned: " + result.response.numFound + " entries for query: " + '"' + query + '"');

                var end = +new Date();  // log end timestamp
                var diff = end - start;
                $log.log("Haku kesti: " +diff);

                model.updateTree(result.numHits, result.organisaatiot);
            }, 
            // Error case
            function(response) {
                $log.error("Organisaatiot response: " + response.status);
                Alert.add("error", $filter('i18n')("Organisaatiot.hakuVirhe", ""), true);
            });
        }
    };

    return model;
});
