function OrganisaatioController($scope, $location, $routeParams, $modal, $log, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.modalOpen = false; // Käytetään piilottamaan tallennuslaatikko, kun modaali dialogi auki
    $scope.model.mode = "show";
    $scope.nimenmuokkaus = null;
    $scope.voimassaolonmuokkaus = null;


    if (/new$/.test($location.path())) {
        $scope.model.mode = "new";
        if ('ytunnus' in $routeParams) {
            $log.log("Uusi organisaatio Ytunnuksella: " + $routeParams.ytunnus);
            $scope.model.createOrganisaatioYTunnuksella($routeParams.parentoid, $routeParams.ytunnus);
        }
        else {
            $scope.model.createOrganisaatio($routeParams.parentoid);
        }
    } else if (/edit$/.test($location.path())) {
        $scope.model.mode = "edit";
    }

    $scope.model.refreshIfNeeded($scope.oid);

    $scope.organisaatioNimiLangs = function(nimi) {
        if (nimi) {
            return Object.keys(nimi);
        } else {
            return undefined;
        }
    };

    $scope.orderByLang = function(lang) {
        var m = {'fi': '0--', 'sv': '1--', 'en': '2--'};
        return m[lang] || '3--' + lang;
    };
    
    $scope.save2 = function() {
        if ($scope.voimassaolonmuokkaus !== null) {
            $scope.voimassaolonmuokkaus.save().then (function() {
                if ($scope.voimassaolonmuokkaus.newVersionNumber != null) {
                    $scope.model.organisaatio.version = $scope.voimassaolonmuokkaus.newVersionNumber;
                }
                $scope.model.persistOrganisaatio($scope.form);
            });
        } else {
            $scope.model.persistOrganisaatio($scope.form);
        }
    };

    $scope.save = function() {
        // Nimenmuokkauksen kautta on käyttäjä on luonut uuden nimen nimihistoriaan
        // tai poistanut tulevan nimenmuutoksen
        // --> suoritetaan ensin nimihistorian päivitys ja sitten organisaatio
        if ($scope.nimenmuokkaus !== null) {
            $scope.nimenmuokkaus.save().then (function() {
                $scope.save2();
            });
        }
        else {
            $scope.save2();
        }

    };

    $scope.cancel = function() {
        $location.path("/");
    };

    $scope.edit = function() {
        $location.path($location.path() + "/edit");
    };

    $scope.openNimenMuokkaus = function () {
        $scope.modalOpen = true;
        var modalInstance = $modal.open({
            templateUrl: 'nimenmuokkaus.html',
            controller: NimenMuokkausController,
            windowClass:'modal-wide',
            resolve: {
                oid: function () {
                    return $scope.oid;
                },
                nimihistoria: function () {
                    return $scope.model.nimihistoria;
                },
                organisaatioAlkuPvm: function () {
                    return $scope.model.organisaatio.alkuPvm;
                },
                koulutustoimija: function () {
                    return $scope.model.isKoulutustoimija();
                },
                oppilaitos: function () {
                    return $scope.model.isOppilaitos();
                },
                parentNimi: function () {
                    return $scope.model.parent.nimi;
                },
                nameFormat: function () {
                    return $scope.model.nameFormat;
                },
                parentPattern: function () {
                    return $scope.model.parentPattern;
                }
            }
        });

        modalInstance.result.then(function (nimenmuokkausModel) {
            $scope.modalOpen = false;
            $scope.nimenmuokkaus = nimenmuokkausModel;

            if (nimenmuokkausModel.mode === 'update' ||
                    nimenmuokkausModel.mode === 'new') {
                if ($scope.nimenmuokkaus.isAjastettuMuutos($scope.nimenmuokkaus.nimi)) {
                    $log.log('Nimenmuokkaus --> ajastus --> ' + nimenmuokkausModel.mode);
                    $scope.form.$setDirty();
                    $scope.model.setTulevaNimi(angular.copy($scope.nimenmuokkaus.nimi));
                }
                else {
                    $log.log('Nimenmuokkaus --> päivitys --> ' + nimenmuokkausModel.mode);
                    $scope.form.$setDirty();
                    $scope.model.setNimi(angular.copy($scope.nimenmuokkaus.nimi.nimi));
                }
            }
            else { // nimenmuokkausModel.mode === 'delete'
                $log.log('Nimenmuokkaus --> ajastuksen peruutus');
                $scope.form.$setDirty();
                $scope.model.deleteTulevaNimi();
            }
        }, function () {
            $scope.model.deleteTulevaNimi();
            $scope.modalOpen = false;
            $scope.nimenmuokkaus = null;
            $log.log('Nimenmuokkaus modal dismissed at: ' + new Date());
        });
    };
    
    $scope.openVoimassaolonMuokkaus = function (muokataanAlkupvm) {
        if ($scope.modalOpen) {
            return;
        }
        $scope.modalOpen = true;
        var modalInstance = $modal.open({
            templateUrl: 'voimassaolonmuokkaus.html',
            controller: VoimassaolonMuokkausController,
            windowClass:'modal-large',
            resolve: {
                muokataanAlkupvm: function() {
                    return muokataanAlkupvm;
                },
                oid: function () {
                    return $scope.model.organisaatio.oid;
                },
                nimi: function () {
                    return $scope.model.organisaatio.nimi;
                },
                alkuPvm: function() {
                    return $scope.model.organisaatio.alkuPvm;
                },
                lakkautusPvm: function() {
                    return $scope.model.organisaatio.lakkautusPvm;
                },
                aliorganisaatioHaunTulos: function () {
                    return $scope.model.aliorganisaatioHaunTulos;
                },
                monikielinenTekstiLocalizer: function () {
                    return $scope.model.getDecodedLocalizedValue;
                }
            }
        });
        
        modalInstance.result.then(function(voimassaolonmuokkausModel) {
            $scope.modalOpen = false;
            $scope.voimassaolonmuokkaus = voimassaolonmuokkausModel;
            $scope.form.$setDirty();
            if (voimassaolonmuokkausModel.muokataanAlkupvm) {
                $log.log("Alku pvm: " + voimassaolonmuokkausModel.alkuPvm);
                $scope.model.organisaatio.alkuPvm = voimassaolonmuokkausModel.alkuPvm;
            } else {
                $log.log("Lakkautus pvm: " + voimassaolonmuokkausModel.loppuPvm);
                $scope.model.organisaatio.lakkautusPvm = voimassaolonmuokkausModel.lakkautusPvm;
            }
            $scope.model.muutettaviaAliorganisaatioita = voimassaolonmuokkausModel.muutettaviaAliorganisaatioita;
            $scope.form.$setValidity("organisaationVoimassaolo", voimassaolonmuokkausModel.isVoimassaoloValid(), $scope.form);
            $scope.form.$setValidity("aliorganisaationVoimassaolo", voimassaolonmuokkausModel.isAliorganisaatioidenVoimassaoloValid(), $scope.form);
        }, function () {
            $scope.modalOpen = false;
            $log.log('Voimassaolonmuokkaus modal dismissed at: ' + new Date());
        });
    };

    $scope.haeYtjTiedot = function(organisaationYtunnus) {
        var modalInstance = $modal.open({
            templateUrl: 'yritysvalinta.html',
            controller: YritysValintaController,
            windowClass: 'modal-wide',
            resolve: {
                ytunnus: function() {
                    return organisaationYtunnus;
                }
            }
        });
        $scope.modalOpen = true;

        modalInstance.result.then(function(ytunnus) {
            if (ytunnus) {
                $log.log('Päivitetään organisaation tiedot tiedoilla YTynnukselta: ' + ytunnus);
                $scope.model.updateOrganisaatioYTunnuksella(ytunnus, $scope.form);
            }
            $scope.modalOpen = false;
        }, function() {
            $log.log('Modal dismissed at: ' + new Date());
            $scope.modalOpen = false;
        });
    };

    $scope.peruutaOrganisaationmuokkaus = function(dirty) {
        if (dirty === false) {
            // jos ei ole muutettu ei tarvitse kysyä vahvistusta
            $location.path($location.path().substr(0, $location.path().lastIndexOf("/")));
        } else {
            $scope.modalOpen = true;
            var modalInstance = $modal.open({
                templateUrl: 'organisaationmuokkauksenperuutus.html',
                controller: OrganisaatioCancelController,
                resolve: {}
            });

            modalInstance.result.then(function() {
                $log.debug('Peruutus vahvistettu');
                $scope.modalOpen = false;
                // Siirry tarkastelu-sivulle
                $location.path($location.path().substr(0, $location.path().lastIndexOf("/")));
            }, function() {
                $scope.modalOpen = false;
                $log.debug('Peruutusta ei vahvistettu');
            });
        }
    };

    // Muodostaa listan invalid-komponenteista
    // Hakee modelilta lokalisoidut nimet komponenteille
    $scope.listErrors = function() {
        $scope.model.errorsTooltip = "<ul>";

        for (var el in $scope.form) {
            if ($scope.form[el].$invalid) {
                var localizedEl = $scope.model.localize(el);
                if (el.match("_1.2.246.562.")) {
                    // haetaan nimi muokattavalle yhteystietotyypille
                    localizedEl = $scope.model.uriLocalizedNames[el.split("_")[1]];
                }
                $scope.model.errorsTooltip += ("<li>" + localizedEl + "</li>");
            }
        }
        $scope.model.errorsTooltip += "</ul>";
    };

    $scope.vahvistakopiointi = function(section) {
        var modalInstance = $modal.open({
            templateUrl: 'kopioinninvahvistus.html',
            controller: KuvailevatTiedotKopiointiController,
            resolve: {
                nimi: function() {
                    return $scope.model.uriLocalizedNames['parentnimi'];
                }
            }
        });

        modalInstance.result.then(function() {
            $scope.model.copyMkFromParent(section, $scope.form);
        }, function() {
            // peruutettiin
        });
    };

    $scope.openEditor = function(field, lang, userlang) {
        var tinymceOptions = {
            height: 350,
            theme: "modern",
            language: userlang,
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

        var origText = $scope.model.organisaatio.metadata.data[field][lang];
        $scope.modalOpen = true;
        var modalInstance = $modal.open({
            templateUrl: 'kuvailevientietojenmuokkaus.html',
            controller: KuvailevatTiedotMuokkausController,
            windowClass: 'modal-large',
            resolve: {
                nimi: function() {
                    return $scope.model.uriLocalizedNames['nimi'];
                },
                field: function() {
                    return field;
                },
                lang: function() {
                    return lang;
                },
                data: function() {
                    return $scope.model.organisaatio.metadata.data;
                },
                options: function() {
                    return tinymceOptions;
                }
            }
        });

        modalInstance.result.then(function() {
            $scope.form.$setDirty();
            $scope.modalOpen = false;
        }, function() {
            // peruutettiin
            $scope.model.organisaatio.metadata.data[field][lang] = origText;
            $scope.modalOpen = false;
        });
    };

    $scope.canUseFileReader = (function() {
        $log.info('can use file reader: ' + !!window.FileReader);
        return !!window.FileReader;
    }());

    $scope.tempFileUrl = SERVICE_URL_BASE + 'tempfile/';
}
