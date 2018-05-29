app.controller('RyhmatController', function RyhmatController(Ryhmat, RyhmaKoodisto, LocalisationService, $location, $routeParams) {

    var vm = this;

    vm.koodisto = RyhmaKoodisto;
    vm.filters = {aktiivinen: 'true'};

    vm.luoUusi = function() {
        $location.path('/ryhmat/uusi').search({parentOid: $routeParams.parentoid});
    };

    vm.hae = function() {
        Object.keys(vm.filters).forEach(function (key) {
            if (vm.filters[key] === '') delete vm.filters[key];
        });
        Ryhmat.get(vm.filters, function (ryhmat) {
            ryhmat.forEach(function (ryhma) {
                ryhma.lokalisoituNimi = lokalisoiNimi(ryhma);
                ryhma.lokalisoituTyyppi = lokalisoiKoodit(RyhmaKoodisto.ryhmatyypit, ryhma.ryhmatyypit);
                ryhma.lokalisoituKayttoryhma = lokalisoiKoodit(RyhmaKoodisto.kayttoryhmat, ryhma.kayttoryhmat);
            });
            vm.ryhmat = ryhmat;
            vm.ryhmatMaara = ryhmat.length;
        });
    };

    var lokalisoiKoodit = function (kaikkiKoodit, valitutKoodit) {
        return kaikkiKoodit.filter(function (koodi) {
            return valitutKoodit.includes(koodi.uri);
        }).map(function (koodi) {
            return koodi.nimi;
        }).join(', ');
    };

    var kieli = LocalisationService.getLocale();
    var lokalisoiNimi = function(ryhma) {
        var nimi = ryhma.nimi[kieli];
        if (nimi) return nimi;
        var fallbackKieli = Object.keys(ryhma.nimi)[0];
        if (fallbackKieli) return ryhma.nimi[fallbackKieli];
        return '';
    };

});
