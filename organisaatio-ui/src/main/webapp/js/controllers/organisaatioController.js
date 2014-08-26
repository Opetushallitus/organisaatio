function OrganisaatioController($scope, $location, $routeParams, $modal, $log, OrganisaatioModel) {
    $scope.oid = $routeParams.oid;
    $scope.model = OrganisaatioModel;
    $scope.modalOpen = false; // Käytetään piilottamaan tallennuslaatikko, kun modaali dialogi auki
    $scope.model.mode = "show";
    $scope.nimenmuokkaus = null;


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

    $scope.save = function() {
        $scope.model.persistOrganisaatio($scope.form);
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
                }
            }
        });

        modalInstance.result.then(function (nimenmuokkausModel) {
            $scope.modalOpen = false;
            $scope.nimenmuokkaus = nimenmuokkausModel;
            $log.log('Nimenmuokkaus mode: ' + nimenmuokkausModel.mode);

            if (nimenmuokkausModel.mode === 'update') {
                $scope.form.$setDirty();
                $scope.model.setNimi($scope.nimenmuokkaus.nimi.nimi);
            }
        }, function () {
            $scope.modalOpen = false;
            $scope.nimenmuokkaus = null;
            $log.log('Nimenmuokkaus modal dismissed at: ' + new Date());
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
