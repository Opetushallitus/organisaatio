
app.controller('LisatietotyyppiController', function ($scope, $filter, KoodistoClient, Lisatietotyypit, KoodistoKoodi, 
                                                      LisatietotyyppiNimi, Lisatietotyyppi) {
    var initialLisatietoDto = {
        nimi: 'lisatieto.',
        rajoitteet: []
    };

    var OPPILAITOSTYYPPI = 'OPPILAITOSTYYPPI';
    var ORGANISAATIOTYYPPI = 'ORGANISAATIOTYYPPI';

    var lokalisoidutRajoitteet = [];

    $scope.lisatietoDto = angular.copy(initialLisatietoDto);

    $scope.model = {
        lisatietotyypit: getLisatietotyypit(),
        organisaatioTyypit: getOrganisaatioTyypit(),
        oppilaitostyypit: getOppilaitostyypit()
    };

    $scope.valittuLisatietotyyppi = null;

    $scope.valittuOppilaitostyyppi = null;

    $scope.poistaRajoite = function(koodiUri) {
        $scope.lisatietoDto.rajoitteet = $scope.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.arvo !== koodiUri;
        });
    };

    $scope.poistaLisatietotyyppi = function () {
        var lisatietotyyppiExists = $scope.model.lisatietotyypit.filter(function (lisatietotyyppi) {
            return lisatietotyyppi === $scope.valittuLisatietotyyppi;
        })[0];
        if (lisatietotyyppiExists) {
            LisatietotyyppiNimi.delete({nimi: lisatietotyyppiExists}, function () {
                $scope.model.lisatietotyypit = getLisatietotyypit();
            });
        }
        clearModel();
    };

    $scope.luoLisatietotyyppi = function () {
        Lisatietotyyppi.post($scope.lisatietoDto,
            function (lisatietotyyppi) {
                $scope.model.lisatietotyypit = getLisatietotyypit();
                $scope.valittuLisatietotyyppi = $scope.lisatietoDto.nimi;
                $scope.fetchValittuLisatietotyyppi();
            });
    };

    $scope.tyhjennaLisatietotyyppi = function() {
        clearModel();
    };

    $scope.fetchValittuLisatietotyyppi = function () {
        LisatietotyyppiNimi.get({nimi: $scope.valittuLisatietotyyppi}, function (lisatietotyyppi) {
            $scope.lisatietoDto.rajoitteet = lisatietotyyppi.rajoitteet;
        });
    };

    function clearModel() {
        $scope.lisatietoDto = angular.copy(initialLisatietoDto);
        $scope.lisatietoDto.rajoitteet = [];
        $scope.valittuLisatietotyyppi = null;
        $scope.valittuOppilaitostyyppi = null;
        lokalisoidutRajoitteet = [];
    }

    $scope.uusiLisatietotyyppi = function () {
        $scope.valittuLisatietotyyppi = 'lisatieto.';
        $scope.lisatietoDto = angular.copy(initialLisatietoDto);
    };

    $scope.toggleOrganisaatiotyyppi = function(organisaatiotyyppiKoodi) {
        var organisaatiotyyppiNimiFi = KoodistoKoodi.getLangName(organisaatiotyyppiKoodi, 'FI');
        var rajoiteIndex = filterRajoiteByTyyppi(ORGANISAATIOTYYPPI, organisaatiotyyppiNimiFi);
        if (rajoiteIndex === -1) {
            $scope.lisatietoDto.rajoitteet.push({rajoitetyyppi: ORGANISAATIOTYYPPI, arvo: organisaatiotyyppiNimiFi});
        }
        else {
            $scope.lisatietoDto.rajoitteet.splice(rajoiteIndex, 1);
        }
    };

    $scope.isOrganisaatiotyyppiSelected = function(organisaatiotyyppiKoodi) {
        var organisaatiotyyppiNimiFi = KoodistoKoodi.getLangName(organisaatiotyyppiKoodi, 'FI');
        return !!$scope.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.arvo === organisaatiotyyppiNimiFi;
        })[0];
    };

    $scope.toggleOppilaitostyyppi = function(valittuOppilaitostyyppi) {
        var rajoiteIndex = filterRajoiteByTyyppi(OPPILAITOSTYYPPI, valittuOppilaitostyyppi.koodiUri);
        if (rajoiteIndex === -1) {
            $scope.lisatietoDto.rajoitteet.push({rajoitetyyppi: OPPILAITOSTYYPPI, arvo:valittuOppilaitostyyppi.koodiUri});
        }
        else {
            $scope.lisatietoDto.rajoitteet.splice(rajoiteIndex, 1);
        }
    };

    function filterRajoiteByTyyppi(rajoitetyyppi, tyyppiValue) {
        var rajoiteIndex = -1;
        $filter('filter')($scope.lisatietoDto.rajoitteet, function (value, index) {
            if (value.rajoitetyyppi === rajoitetyyppi && value.arvo ===  tyyppiValue) {
                rajoiteIndex = index;
                return true;
            }
            return false;
        });
        return rajoiteIndex;
    }

    $scope.isLisatietotyyppiNotNew = function () {
        return $scope.model.lisatietotyypit.indexOf($scope.valittuLisatietotyyppi) !== -1;
    };

    function getLisatietotyypit() {
        Lisatietotyypit.get({}, function (lisatietotyypit) {
            $scope.model.lisatietotyypit = lisatietotyypit;
        });
        return [];
    }

    function getOrganisaatioTyypit() {
        KoodistoClient.koodistoOrganisaatiotyypit.get({onlyValidKoodis: true}, function (organisaatiotyypit) {
            $scope.model.organisaatioTyypit = organisaatiotyypit;
        });
        return [];
    }

    function getOppilaitostyypit() {
        KoodistoClient.koodistoOppilaitostyypit.get({onlyValidKoodis: true}, function (oppilaitostyypit) {
            $scope.model.oppilaitostyypit = oppilaitostyypit;
        });
        return [];
    }

    $scope.lokalisoiKoodi = function(koodi) {
        return KoodistoKoodi.getLocalizedName(koodi);
    };

    $scope.getLokalisoidutRajoitteet = function () {
        var oppilaitostyyppiRajoitteet = $scope.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.rajoitetyyppi === OPPILAITOSTYYPPI;
        });
        if (lokalisoidutRajoitteet.length === oppilaitostyyppiRajoitteet.length) {
            return lokalisoidutRajoitteet;
        }
        lokalisoidutRajoitteet = oppilaitostyyppiRajoitteet.map(function (rajoite) {
            var koodiUri = rajoite.arvo;
            var koodi = $scope.model.oppilaitostyypit.filter(function (oppilaitosKoodi) {
                return oppilaitosKoodi.koodiUri === koodiUri;
            })[0];
            return Object.assign({}, rajoite, {lokalitointi: KoodistoKoodi.getLocalizedName(koodi)});
        });
        return lokalisoidutRajoitteet;
    };

});
