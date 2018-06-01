
app.controller('LisatietotyyppiController', function ($filter, KoodistoClient, Lisatietotyypit, KoodistoKoodi, 
                                                      LisatietotyyppiNimi, Lisatietotyyppi) {
    var vm = this;
    var initialLisatietoDto = {
        nimi: 'lisatieto.',
        rajoitteet: []
    };

    var OPPILAITOSTYYPPI = 'OPPILAITOSTYYPPI';
    var ORGANISAATIOTYYPPI = 'ORGANISAATIOTYYPPI';

    var ORGANISAATIOTYYPPI_OPPILAITOS_FI = 'Oppilaitos';

    var lokalisoidutRajoitteet = [];

    vm.lisatietoDto = angular.copy(initialLisatietoDto);

    vm.model = {
        lisatietotyypit: getLisatietotyypit(),
        organisaatioTyypit: getOrganisaatioTyypit(),
        oppilaitostyypit: getOppilaitostyypit()
    };

    vm.valittuLisatietotyyppi = null;

    vm.valittuOppilaitostyyppi = null;

    vm.poistaRajoite = function(koodiUri) {
        vm.lisatietoDto.rajoitteet = vm.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.arvo !== koodiUri;
        });
    };

    vm.poistaLisatietotyyppi = function () {
        var lisatietotyyppiExists = vm.model.lisatietotyypit.filter(function (lisatietotyyppi) {
            return lisatietotyyppi === vm.valittuLisatietotyyppi;
        })[0];
        if (lisatietotyyppiExists) {
            LisatietotyyppiNimi.delete({nimi: lisatietotyyppiExists}, function () {
                vm.model.lisatietotyypit = getLisatietotyypit();
            });
        }
        clearModel();
    };

    vm.luoLisatietotyyppi = function () {
        Lisatietotyyppi.post(vm.lisatietoDto,
            function (lisatietotyyppi) {
                vm.model.lisatietotyypit = getLisatietotyypit();
                vm.valittuLisatietotyyppi = vm.lisatietoDto.nimi;
                vm.fetchValittuLisatietotyyppi();
            });
    };

    vm.tyhjennaLisatietotyyppi = function() {
        clearModel();
    };

    vm.fetchValittuLisatietotyyppi = function () {
        LisatietotyyppiNimi.get({nimi: vm.valittuLisatietotyyppi}, function (lisatietotyyppi) {
            vm.lisatietoDto.rajoitteet = lisatietotyyppi.rajoitteet;
        });
    };

    function clearModel() {
        vm.lisatietoDto = angular.copy(initialLisatietoDto);
        vm.lisatietoDto.rajoitteet = [];
        vm.valittuLisatietotyyppi = null;
        vm.valittuOppilaitostyyppi = null;
        lokalisoidutRajoitteet = [];
    }

    vm.uusiLisatietotyyppi = function () {
        vm.valittuLisatietotyyppi = 'vm.';
        vm.lisatietoDto = angular.copy(initialLisatietoDto);
    };

    vm.toggleOrganisaatiotyyppi = function(organisaatiotyyppiKoodi) {
        var organisaatiotyyppiNimiFi = KoodistoKoodi.getLangName(organisaatiotyyppiKoodi, 'FI');
        var rajoiteIndex = filterRajoiteByTyyppi(ORGANISAATIOTYYPPI, organisaatiotyyppiNimiFi);
        if (rajoiteIndex === -1) {
            vm.lisatietoDto.rajoitteet.push({rajoitetyyppi: ORGANISAATIOTYYPPI, arvo: organisaatiotyyppiNimiFi});
        }
        else {
            vm.lisatietoDto.rajoitteet.splice(rajoiteIndex, 1);
        }
    };

    vm.isOrganisaatiotyyppiSelected = function(organisaatiotyyppiKoodi) {
        var organisaatiotyyppiNimiFi = KoodistoKoodi.getLangName(organisaatiotyyppiKoodi, 'FI');
        return !!vm.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.arvo === organisaatiotyyppiNimiFi;
        })[0];
    };

    vm.toggleOppilaitostyyppi = function(valittuOppilaitostyyppi) {
        var rajoiteIndex = filterRajoiteByTyyppi(OPPILAITOSTYYPPI, valittuOppilaitostyyppi.koodiUri);
        if (rajoiteIndex === -1) {
            // There is no point selecting oppilaitostyyppi rajoite if oppilaitos organisaatiotyyppi is allowed as a whole
            vm.lisatietoDto.rajoitteet = vm.lisatietoDto.rajoitteet.filter(function (rajoite) {
                return rajoite.arvo !== ORGANISAATIOTYYPPI_OPPILAITOS_FI;
            });
            vm.lisatietoDto.rajoitteet.push({rajoitetyyppi: OPPILAITOSTYYPPI, arvo:valittuOppilaitostyyppi.koodiUri});
        }
        else {
            vm.lisatietoDto.rajoitteet.splice(rajoiteIndex, 1);
        }
    };

    function filterRajoiteByTyyppi(rajoitetyyppi, tyyppiValue) {
        var rajoiteIndex = -1;
        $filter('filter')(vm.lisatietoDto.rajoitteet, function (value, index) {
            if (value.rajoitetyyppi === rajoitetyyppi && value.arvo ===  tyyppiValue) {
                rajoiteIndex = index;
                return true;
            }
            return false;
        });
        return rajoiteIndex;
    }

    vm.isLisatietotyyppiNotNew = function () {
        return vm.model.lisatietotyypit.indexOf(vm.valittuLisatietotyyppi) !== -1;
    };

    vm.isOppilaitosAndOppilaitostyyppirajoiteSelected = function (organisaatiotyyppi) {
        return organisaatiotyyppi === ORGANISAATIOTYYPPI_OPPILAITOS_FI && vm.isOppilaitosRajoiteSelected();
    };

    function getLisatietotyypit() {
        Lisatietotyypit.get({}, function (lisatietotyypit) {
            vm.model.lisatietotyypit = lisatietotyypit;
        });
        return [];
    }

    function getOrganisaatioTyypit() {
        KoodistoClient.koodistoOrganisaatiotyypit.get({onlyValidKoodis: true}, function (organisaatiotyypit) {
            vm.model.organisaatioTyypit = organisaatiotyypit;
        });
        return [];
    }

    function getOppilaitostyypit() {
        KoodistoClient.koodistoOppilaitostyypit.get({onlyValidKoodis: true}, function (oppilaitostyypit) {
            vm.model.oppilaitostyypit = oppilaitostyypit;
        });
        return [];
    }

    vm.lokalisoiKoodi = function(koodi) {
        return KoodistoKoodi.getLocalizedName(koodi);
    };

    vm.getLokalisoidutRajoitteet = function () {
        var oppilaitostyyppiRajoitteet = getOppilaitostyyppiRajoitteet();
        if (lokalisoidutRajoitteet.length === oppilaitostyyppiRajoitteet.length) {
            return lokalisoidutRajoitteet;
        }
        lokalisoidutRajoitteet = oppilaitostyyppiRajoitteet.map(function (rajoite) {
            var koodiUri = rajoite.arvo;
            var koodi = vm.model.oppilaitostyypit.filter(function (oppilaitosKoodi) {
                return oppilaitosKoodi.koodiUri === koodiUri;
            })[0];
            return Object.assign({}, rajoite, {lokalitointi: KoodistoKoodi.getLocalizedName(koodi)});
        });
        return lokalisoidutRajoitteet;
    };

    function getOppilaitostyyppiRajoitteet() {
        return vm.lisatietoDto.rajoitteet.filter(function (rajoite) {
            return rajoite.rajoitetyyppi === OPPILAITOSTYYPPI;
        })
    }

    vm.isOppilaitosRajoiteSelected = function () {
        return vm.lisatietoDto.rajoitteet.some(function (rajoite) {
            return rajoite.rajoitetyyppi === OPPILAITOSTYYPPI;
        });
    };

    vm.isOppilaitosSelected = function () {
        return vm.lisatietoDto.rajoitteet.some(function (rajoite) {
            return rajoite.arvo === ORGANISAATIOTYYPPI_OPPILAITOS_FI;
        });
    };
});
