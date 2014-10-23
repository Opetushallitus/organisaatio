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

function OrganisaatioController($scope, $location,
                                $routeParams, $modal,
                                $log, $injector, $q,
                                OrganisaatioModel) {

    $log = $log.getInstance("OrganisaatioController");

    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.modalOpen = false; // Käytetään piilottamaan tallennuslaatikko, kun modaali dialogi auki
    $scope.model.mode = "show";
    $scope.nimenmuokkaus = null;
    $scope.voimassaolonmuokkaus = null;
    $scope.tempFileUrl = SERVICE_URL_BASE + 'tempfile/';

    // Destruktorityyliin putsataan jäljet
    $scope.clear = function() {
        if ($scope.nimenmuokkaus) {
            $scope.nimenmuokkaus.clear();
        }
    };

    // Käsitellään muokkausnäkymästä poistuminen
    $scope.$on("$locationChangeStart", function(event, next, current) {
        // Tallennetaan next url ja kysytään käyttäjältä haluaako siirtyä vai jatkaa.
        // Jos käyttäjä haluaa siirtyä seuraavalle sivulle --> location change
        var next = next;
        $log.log("Location change: " + current +" -> " + next);

        var changeLocation = function() {
            $log.debug('Poistutaan muokkauksesta');
            $scope.modalOpen = false;
            $scope.form.$setPristine();
            $scope.clear();
            $location.path(next);
        };

        if ($scope.form.$dirty) {
            event.preventDefault();
            $scope.modalOpen = true;
            var modalInstance = $modal.open({
                templateUrl: 'organisaationmuokkauksenperuutus.html',
                controller: OrganisaatioCancelController,
                resolve: {
                    invalid: function () {
                        return $scope.form.$invalid;
                    }
                }
            });

            // Jos varmistuskyselyssä käyttäjä haluaa tallentaa muokatun
            // organisaation, niin odotetaan tallennusvaihe loppuun ja
            // siirrytää vasta sitten uuteen osoitteeseen.
            modalInstance.result.then(function (save) {
                if (save) {
                    $scope.save().then(function() {
                        changeLocation();
                    }, function(reason) {
                        changeLocation();
                    });
                }
                else {
                   changeLocation();
                }
            }, function() {
                $scope.modalOpen = false;
                $log.debug('Jatketaan muokkausta');
            });
        }
        else {
            $scope.clear();
        }
    });

    // Tarkistetaan organisaation muokkauksen moodi, uusi organisaatio vai vanhan muokkaus
    // Uuden organisaation tapauksessa organisaation luonti voi perustua YTJ:stä saatavaan tietoon
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

    $log.info("Mode: " + $scope.model.mode);

    $scope.model.refreshIfNeeded($scope.oid);

    $scope.organisaatioNimiLangs = function(nimi) {
        if (nimi) {
            return Object.keys(nimi);
        } else {
            return undefined;
        }
    };

    // Kielet järjestykseen FI, SV, EN
    $scope.orderByLang = function(lang) {
        var m = {'fi': '0--', 'sv': '1--', 'en': '2--'};
        return m[lang] || '3--' + lang;
    };

    // Organisaation tallennus
    // Tallentaa myös mahdollisen nimihistorian ja voimassaolon periytymän
    $scope.save = function() {
        $log.info("save(): " + $scope.model.organisaatio.oid);

        var deferred = $q.defer();

        $log.info("Saving organisaatio: " + $scope.model.organisaatio.oid);
        $scope.model.persistOrganisaatio($scope.form).then(function(organisaatio) {
//        $scope.model.persistOrganisaatio($scope.form).then(function() {
            if ($scope.voimassaolonmuokkaus !== null) {
                $scope.voimassaolonmuokkaus.save().then(function() {
                    if ($scope.voimassaolonmuokkaus.newVersionNumber !== null) {
                        $scope.model.organisaatio.version = $scope.voimassaolonmuokkaus.newVersionNumber;
                    }
                    // Nimenmuokkauksen kautta on käyttäjä on luonut uuden nimen nimihistoriaan
                    // tai poistanut tulevan nimenmuutoksen
                    // --> suoritetaan ensin nimihistorian päivitys
                    if ($scope.nimenmuokkaus !== null) {
                        $scope.nimenmuokkaus.save().then(function() {
                            deferred.resolve(null);
                        }, function(reason) {
                            $log.warn("Failed with nimenmuokkaus.save()! " + reason);
                            deferred.reject();
                        });
                    }
                    else {
                        deferred.resolve(organisaatio);
                    }
                }, function(reason) {
                    $log.warn("Failed with voimassaolonmuokkaus.save()! " + reason);
                    deferred.reject();
                });
            } else {
                if ($scope.nimenmuokkaus !== null) {
                    $scope.nimenmuokkaus.save().then(function() {
                        deferred.resolve(null);
                    }, function(reason) {
                        $log.warn("Failed with nimenmuokkaus.save()! " + reason);
                        deferred.reject();
                    });
                }
                else {
                    deferred.resolve(organisaatio);
                }
            }
        }, function(reason) {
            $log.info("Failed to save organisaatio: " + $scope.model.organisaatio.oid + " " + reason);
            deferred.reject();
        });

        return deferred.promise;
    };

    // Organisaation tallennus ja refresh
    $scope.saveAndRefresh = function() {
        $log.info("saveAndRefresh(): " + $scope.model.organisaatio.oid);

        $scope.save().then(function(organisaatio) {
            if (organisaatio === null) {
                $scope.model.refreshIfNeeded($scope.oid);
            }
            else {
                $scope.model.refresh(organisaatio);
            }

        }, function(reason) {
            $scope.model.refreshIfNeeded($scope.oid);
        });
    };

    // Siirtyminen organisaatioiden pääsivulle organisaatiopuu näkymään
    $scope.cancel = function() {
        $location.path("/");
    };

    // Siirtyminen organisaation tarkastelunäkymään
    $scope.view = function() {
        if (/edit$/.test($location.path())) {
            $location.path($location.path().replace("/edit", ""));
        }
    };

    // Siirtyminen organisaation muokkausnäkymään
    $scope.edit = function() {
        $location.path($location.path() + "/edit");
    };

    // Nimenmuokkauksen modaalin dialogin avaus
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
                    $scope.model.resetCurrentNimi();
                }
                else {
                    $log.log('Nimenmuokkaus --> päivitys --> ' + nimenmuokkausModel.mode);
                    $scope.form.$setDirty();
                    $scope.model.setCurrentNimi(angular.copy($scope.nimenmuokkaus.nimi));
                    $scope.model.resetTulevaNimi();
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

    // Voimassaolon muokkauksen modaalin dialogin avaus
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

    // Ytj-tietojen haun modaalin dialogin avaus
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

    // Tarkastetaan ollaanko lataamassa vielä sisältöä
    // Jos lataus on kesken, ei kannata näyttää käyttäjälle ilmoitusta puuttuvista tiedoista
    $scope.isLoading = function() {
        var loadingService = $injector.get('LoadingService');
        if (loadingService) {
            return loadingService.isLoading();
        }
        return false;
    };
}
