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

app.controller('YhteystietojentyyppiController', function YhteystietojentyyppiController($scope, $filter, $uibModal,
                                        $log, $injector,
                                        YhteystietojentyyppiModel,
                                        Alert, LocalisationService) {
    "use strict";

    $log = $log.getInstance("YhteystietojentyyppiController");
    var loadingService = $injector.get('LoadingService');

    var language;

    var vaihtoehtoisetKielikoodit = {
        fi: ['sv', 'en'],
        sv: ['fi', 'en'],
        en: ['fi', 'sv']
    };

    language = LocalisationService.getLocale();

    $scope.model = YhteystietojentyyppiModel;
    $scope.model.reload();
    $scope.valittuYhteystietotyyppi = null;

    $scope.localizeYhteystietotyypinNimi = function(yt) {
        var kaannokset = {};

        angular.forEach(yt.nimi.teksti, function (entry, key) {
            kaannokset[entry.kieliKoodi] = entry.value;
        });

        return kaannokset[language] ||
                kaannokset[vaihtoehtoisetKielikoodit[language][0]] ||
                kaannokset[vaihtoehtoisetKielikoodit[language][1]];
    };
    $scope.yttNimiLang = function(koodi) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            for (var i in $scope.valittuYhteystietotyyppi.nimi.teksti) {
                if ($scope.valittuYhteystietotyyppi.nimi.teksti[i].kieliKoodi.toLowerCase() === koodi.toLowerCase()) {
                    return $scope.valittuYhteystietotyyppi.nimi.teksti[i];
                }
            }
            var obj = {
                value: '',
                kieliKoodi: koodi
            };
            return $scope.valittuYhteystietotyyppi.nimi.teksti.push(obj);
        }
        return null;
    };

    $scope.yttNimiUnique = function(nimi) {
        if (nimi === "") {
            return true; // Ignore empty strings.
        }
        for (var i = 0; i < $scope.model.yhteystietotyypit.length; i++) {
            if ($scope.valittuYhteystietotyyppi === $scope.model.yhteystietotyypit[i]) {
                continue;
            }
            var teksti = $scope.model.yhteystietotyypit[i].nimi.teksti;
            for (var j = 0; j < teksti.length; j++) {
                if (teksti[j].value === nimi) {
                    return false;
                }
            }
        }
        return true;
    };

    function _match(item, filt) {
        for (var i in filt) {
            if (typeof filt[i] === 'string') {
                if (item[i] !== filt[i]) {
                    return false;
                }
            } else {
                if (!item[i].match(filt[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /* FIXME: käännökset jQueryllä */
    function getNimiForTyyppi(tyyppi, nimi, locale) {
        var tyypit = {
            'EMAIL': {fi: 'Sähköpostiosoite', sv: 'Epostadress', en: 'Email'},
            'NIMI': {fi: 'Nimi', sv: 'Namn', en: 'Name'},
            'NIMIKE': {fi: 'Nimike', sv: 'Benämning', en: 'Title'},
            'OSOITEKäyntiosoite': {fi: 'Käyntiosoite', sv: 'Besöksadress', en: 'Visiting Address'},
            'OSOITEPostiosoite': {fi: 'Postiosoite', sv: 'Postadress', en: 'Postal Address'},
            'OSOITE_ULKOMAA': {fi: 'Ulkomaan osoite', sv: 'Utrikes adress', en: 'Foreign Address'},
            'PUHELINMatkapuhelinnumero': {fi: 'Matkapuhelinnumero', sv: 'Mobiltelefonnummer', en: 'Mobile'},
            'PUHELINPuhelinnumero': {fi: 'Puhelinnumero', sv: 'Telefonnummer', en: 'Phone'},
            'WWWWww-osoite': {fi: 'Www-osoite', sv: 'Www-adress', en: 'WWW Address'}
        };
        return tyypit[tyyppi + nimi][locale];
    }

    $scope.yttFindTyyppi = function(filt) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            for (var i in $scope.valittuYhteystietotyyppi.allLisatietokenttas) {
                if (_match($scope.valittuYhteystietotyyppi.allLisatietokenttas[i], filt)) {
                    return [$scope.valittuYhteystietotyyppi.allLisatietokenttas[i]];
                }
            }
            var obj = {
                version: 0,
                oid: null,
                nimi: filt.nimi || getNimiForTyyppi(filt.tyyppi, filt.nimi || '', 'fi'),
                nimiSv: getNimiForTyyppi(filt.tyyppi, filt.nimi || '', 'sv'),
                nimiEn: getNimiForTyyppi(filt.tyyppi, filt.nimi || '', 'en'),
                tyyppi: filt.tyyppi,
                kaytossa: false,
                pakollinen: false
            };
            $scope.valittuYhteystietotyyppi.allLisatietokenttas.push(obj);
            return [obj];
        }
        return null;
    };

    $scope.yttFindTyyppiAll = function(filt) {
        var all = [];
        if ($scope.valittuYhteystietotyyppi !== null) {
            for (var i in $scope.valittuYhteystietotyyppi.allLisatietokenttas) {
                if (_match($scope.valittuYhteystietotyyppi.allLisatietokenttas[i], filt)) {
                    all.push($scope.valittuYhteystietotyyppi.allLisatietokenttas[i]);
                }
            }
        }
        return all;
    };

    $scope.regexp = function(r) {
        return new RegExp(r);
    };

    $scope.localizeMuuYttNimi = function(ytt) {
        if (language === 'SV') {
            return ytt.nimiSv.split(': ')[1];
        }
        return ytt.nimi.split(': ')[1];
    };

    function _orgTypeContains(t) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            return $scope.valittuYhteystietotyyppi.sovellettavatOrganisaatios.indexOf(t) !== -1;
        }
        return false;
    }

    function _orgTypeAdd(t) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            if (!_orgTypeContains(t)) {
                $scope.valittuYhteystietotyyppi.sovellettavatOrganisaatios.push(t);
            }
        }
    }

    function _orgTypeDel(t) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            var i = $scope.valittuYhteystietotyyppi.sovellettavatOrganisaatios.indexOf(t);
            if (i !== -1) {
                $scope.valittuYhteystietotyyppi.sovellettavatOrganisaatios.splice(i, 1);
            }
        }
    }

    function _orgTypeMod(v, ot) {
        if (v) {
            _orgTypeAdd(ot);
        } else {
            _orgTypeDel(ot);
        }
    }

    var obj = {};
    var rajatutOppilaitostyypit = false;

    Object.defineProperty(obj, 'koulutustoimija', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_01');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_01');
        }
    });

    Object.defineProperty(obj, 'toimipiste', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_03');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_03');
        }
    });

    Object.defineProperty(obj, 'oppisopimuspiste', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_04');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_04');
        }
    });

    Object.defineProperty(obj, 'muuOrganisaatio', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_05');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_05');
        }
    });

    Object.defineProperty(obj, 'tyoelamajarjesto', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_06');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_06');
        }
    });

    Object.defineProperty(obj, 'varhaiskasvatuksenJarjestaja', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_07');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_07');
        }
    });

    Object.defineProperty(obj, 'varhaiskasvatuksenToimipaikka', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_08');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_08');
        }
    });

    Object.defineProperty(obj, 'kaikkiOppilaitostyypit', {
        get: function() {
            return _orgTypeContains('organisaatiotyyppi_02');
        },
        set: function(t) {
            _orgTypeMod(t, 'organisaatiotyyppi_02');
            rajatutOppilaitostyypit = rajatutOppilaitostyypit && !t;
        }
    });

    Object.defineProperty(obj, 'rajatutOppilaitostyypit', {
        get: function() {
            return rajatutOppilaitostyypit;
        },
        set: function(t) {
            rajatutOppilaitostyypit = t;
            _orgTypeMod(!t && _orgTypeContains('organisaatiotyyppi_02'), 'organisaatiotyyppi_02');
        }
    });

    Object.defineProperty(obj, 'valitutOppilaitostyypit', {
        get: function() {
            return "";
        },
        set: function(v) {
            if ($scope.valittuYhteystietotyyppi.sovellettavatOppilaitostyyppis.indexOf(v.id) === -1) {
                $scope.valittuYhteystietotyyppi.sovellettavatOppilaitostyyppis.push(v.id);
            }
        }
    });

    $scope.valittuYhteystietotyyppiOrgTyypit = obj;

    $scope.removeValittuYtt = function(v) {
        var i = $scope.valittuYhteystietotyyppi.sovellettavatOppilaitostyyppis.indexOf(v);
        if (i !== -1) {
            $scope.valittuYhteystietotyyppi.sovellettavatOppilaitostyyppis.splice(i, 1);
        }
    };

    $scope.$watch('valittuYhteystietotyyppi', function() {
        if ($scope.valittuYhteystietotyyppi !== null) {
            $scope.valittuYhteystietotyyppiOrgTyypit.rajatutOppilaitostyypit =
                    $scope.valittuYhteystietotyyppi.sovellettavatOppilaitostyyppis.length > 0;
        }
    });

    $scope.uusiYhteystietotyyppi = function() {
        $scope.valittuYhteystietotyyppi = $scope.model.uusiYtt();
    };

    $scope.poistaYhteystietotyyppi = function() {
        Alert.clear();
        if ($scope.valittuYhteystietotyyppi !== null) {
            var modalInstance = $uibModal.open({
                templateUrl: 'yhteystiedonpoisto.html',
                controller: 'YhteystietoDeleteController',
                resolve: {
                    nimi: function () {
                        return $scope.yttNimiLang(language).value;
                    }
                },
                scope: $scope
            });

            modalInstance.result.then(function(force) {
                $log.debug('Yhteystietotyypin poisto vahvistettu (force=' + force + ')');

                var ind = $scope.model.yhteystietotyypit.indexOf($scope.valittuYhteystietotyyppi);
                if (ind !== -1) {
                    if ($scope.valittuYhteystietotyyppi.oid !== null) {
                        $scope.model.delete($scope.valittuYhteystietotyyppi, force, function(res) {

                            $scope.model.yhteystietotyypit.splice(ind, 1);
                            $scope.valittuYhteystietotyyppi = null;
                        },
                        // Error case
                        function(virhe) {
                            loadingService.onErrorHandled(virhe);
                            Alert.add("error", $filter('i18n')(virhe.data.errorKey || 'generic.error'), false);
                        });
                    } else {
                        $scope.model.yhteystietotyypit.splice(ind, 1);
                        $scope.valittuYhteystietotyyppi = null;
                    }
                }
            }, function () {
                $log.debug('Yhteystietotyypin poisto peruttu');
            });
        }
    };

    $scope.tallennaYhteystietotyyppi = function() {
        if ($scope.valittuYhteystietotyyppi !== null) {
            $scope.model.save($scope.valittuYhteystietotyyppi, function(tallennettuYtt) {
                $scope.valittuYhteystietotyyppi = tallennettuYtt;
            },
            // Error case
            function(virhe) {
                loadingService.onErrorHandled(virhe);
                Alert.add("error", $filter('i18n')(virhe.data.errorKey || 'generic.error'), false);
            });
        }
    };

    $scope.peruuta = function() {
        $scope.valittuYhteystietotyyppi = null;
        rajatutOppilaitostyypit = false;
        $scope.model.reload();
    };

    function addMuuYhteystieto(tyyppi, nimi) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            $scope.valittuYhteystietotyyppi.allLisatietokenttas.push({
                version: 0,
                oid: null,
                nimi: 'Muu: ' + nimi.fi,
                nimiSv: 'Muu: ' + nimi.sv,
                tyyppi: tyyppi,
                kaytossa: false,
                pakollinen: false
            });
        }
    }

    function _muokkaaMuuYhteystieto(callback, data) {
        var modalInstance = $uibModal.open({
            templateUrl: 'muuyhteystieto.html',
            controller: 'MuuYhteystietoController',
            resolve: {
                data: function() {
                    return data;
                }
            },
            scope: $scope
        });
        $scope.modalOpen = true;

        modalInstance.result.then(function(data) {
            $scope.modalOpen = false;
            callback(data);
        }, function() {
            $scope.modalOpen = false;
        });
    }

    $scope.uusiMuuYhteystieto = function(tyyppi) {
        _muokkaaMuuYhteystieto(function(data) {
            addMuuYhteystieto(tyyppi, data);
        });
    };

    function stripPrefix(s) {
        try {
            return s.split(': ')[1];
        } catch (e) {
            return '';
        }
    }

    $scope.muokkaaMuuYhteystieto = function(tyyppi) {
        _muokkaaMuuYhteystieto(function(data) {
            tyyppi.nimi = 'Muu: ' + data.fi;
            tyyppi.nimiSv = 'Muu: ' + data.sv;
            tyyppi.nimiEn = 'Muu: ' + data.en;
        }, {
            fi: stripPrefix(tyyppi.nimi),
            sv: stripPrefix(tyyppi.nimiSv),
            en: stripPrefix(tyyppi.nimiEn)
        });
    };

    $scope.poistaMuuYhteystieto = function(myt) {
        var ind = $scope.valittuYhteystietotyyppi.allLisatietokenttas.indexOf(myt);
        if (ind !== -1) {
            $scope.valittuYhteystietotyyppi.allLisatietokenttas.splice(ind, 1);
        }
    };

});
