function YhteystietojentyyppiController($scope, $window, $filter, YhteystietojentyyppiModel) {
    var language = $window.navigator.userLanguage || $window.navigator.language;
    console.log(language);
    if (language) {
        language = language.substr(0,2).toUpperCase();
        if (language!=="FI" && language!=="SV") {
            language = "FI";
        }
    } else {
        language = "FI";
    }

    $scope.model = YhteystietojentyyppiModel;
    $scope.valittuYhteystietotyyppi = null;

    $scope.save = function() {
        $scope.model.save();
    };

    $scope.localizeYhteystietotyypinNimi = function(yt) {
        for (var k in yt.nimi.teksti) {
            if (yt.nimi.teksti[k].kieliKoodi === language.toLowerCase()) {
                return yt.nimi.teksti[k].value;
            }
        }
    };

    $scope.yttNimiLang = function(koodi) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            for (var i in $scope.valittuYhteystietotyyppi.nimi.teksti) {
                if ($scope.valittuYhteystietotyyppi.nimi.teksti[i].kieliKoodi === koodi) {
                    return i;
                }
            }
        }
        return 0;
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
            'EMAIL': { fi: 'Sähköpostiosoite', sv: 'Epostadress' },
            'NIMI': { fi: 'Nimi', sv: 'Namn' },
            'NIMIKE': { fi: 'Nimike', sv: 'Benämning' },
            'OSOITEKäyntiosoite': { fi: 'Käyntiosoite', sv: 'Besöksadress' },
            'OSOITEPostiosoite': { fi: 'Postiosoite', sv: 'Postadress' },
            'OSOITE_ULKOMAA': { fi: 'Ulkomaan osoite', sv: 'Utrikes adress' },
            'PUHELINMatkapuhelinnumero': { fi: 'Matkapuhelinnumero', sv: 'Mobiltelefonnummer' },
            'PUHELINPuhelinnumero': { fi: 'Puhelinnumero', sv: 'Telefonnummer' },
            'FAKSI': { fi: 'Faksinumero', sv: 'faxnummer' },
            'WWW': { fi: 'Www-osoite', sv: 'Www-adress' }
        };
        return tyypit[tyyppi+nimi][locale];
    }

    $scope.yttFindTyyppi = function(filt) {
        if ($scope.valittuYhteystietotyyppi !== null) {
            for (var i in $scope.valittuYhteystietotyyppi.allLisatietokenttas) {
                if (_match($scope.valittuYhteystietotyyppi.allLisatietokenttas[i], filt)) {
                    return i;
                }
            }
            var obj = {
                version: 0,
                oid: null,
                nimi: filt.nimi || getNimiForTyyppi(filt.tyyppi, filt.nimi || '', 'fi'),
                nimiSv: getNimiForTyyppi(filt.tyyppi, filt.nimi || '', 'sv'),
                tyyppi: filt.tyyppi,
                kaytossa: false,
                pakollinen: false
            };
            $scope.valittuYhteystietotyyppi.allLisatietokenttas.push(obj);
            return obj;
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

    var obj = new Object();
    var rajatutOppilaitostyypit = false;

    Object.defineProperty(obj, 'koulutustoimija', {
        get: function() {
            return _orgTypeContains('KOULUTUSTOIMIJA');
        },
        set: function(t) {
            _orgTypeMod(t, 'KOULUTUSTOIMIJA');
        }
    });

    Object.defineProperty(obj, 'opetuspiste', {
        get: function() {
            return _orgTypeContains('OPETUSPISTE');
        },
        set: function(t) {
            _orgTypeMod(t, 'OPETUSPISTE');
        }
    });

    Object.defineProperty(obj, 'oppisopimuspiste', {
        get: function() {
            return _orgTypeContains('OPPISOPIMUSTOIMIPISTE');
        },
        set: function(t) {
            _orgTypeMod(t, 'OPPISOPIMUSTOIMIPISTE');
        }
    });

    Object.defineProperty(obj, 'muuOrganisaatio', {
        get: function() {
            return _orgTypeContains('MUU_ORGANISAATIO');
        },
        set: function(t) {
            _orgTypeMod(t, 'MUU_ORGANISAATIO');
        }
    });

    Object.defineProperty(obj, 'kaikkiOppilaitostyypit', {
        get: function() {
            return _orgTypeContains('OPPILAITOS');
        },
        set: function(t) {
            _orgTypeMod(t, 'OPPILAITOS');
            rajatutOppilaitostyypit &= !t;
        }
    });

    Object.defineProperty(obj, 'rajatutOppilaitostyypit', {
        get: function() {
            return rajatutOppilaitostyypit;
        },
        set: function(t) {
            rajatutOppilaitostyypit = t;
            _orgTypeMod(!t && _orgTypeContains('OPPILAITOS'), 'OPPILAITOS');
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
}
