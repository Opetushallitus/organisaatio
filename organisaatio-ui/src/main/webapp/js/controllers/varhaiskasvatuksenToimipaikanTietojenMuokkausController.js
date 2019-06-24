/**
 * Toiminnallisuus varhaiskasvatuken toimipaikkatietojen muokkaamista varten. Luottaa, että (ng-includatusta) scopesta
 * löytyy model joka sisältää organisaatio-modelin ja koodiston.
 */
app.controller('VarhaiskasvatuksenToimipaikanTietojenMuokkausController', function ($scope, $log, $filter, LocalisationService) {

    var vm = this;

    vm.t = function (key) {
        return LocalisationService.t(key);
    };

    vm.initialiseModel = function() {
        var existingModel = $scope.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot;

        if (!existingModel || !Object.getOwnPropertyNames(existingModel).length) {
            $scope.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot = {
                toimintamuoto: '',
                kasvatusopillinenJarjestelma: '',
                varhaiskasvatuksenToiminnallinenpainotukset: [],
                paikkojenLukumaara: null,
                varhaiskasvatuksenJarjestamismuodot: [],
                varhaiskasvatuksenKielipainotukset: [],
                piilotettu: false
            };
        }
        vm.model = $scope.model;
    };

    vm.koodisto = $scope.model.koodisto;

    vm.initialiseModel();

    var addEntity = function(existingEntities, entityToAdd, koodiFieldName) {
        var lisattavaKielipainotus = entityToAdd;
        if (!existingEntities) {
            existingEntities = [];
        }
        // Workaround for timezone issue with UIB https://github.com/angular-ui/bootstrap/issues/6235
        var alkupvmTimezoneOffsetInMinutes = -entityToAdd.alkupvm.getTimezoneOffset();
        entityToAdd.alkupvm = moment(entityToAdd.alkupvm).add(alkupvmTimezoneOffsetInMinutes, 'minutes');
        if (entityToAdd.loppupvm) {
            var loppupvmTimezoneOffsetInMinutes = -entityToAdd.loppupvm.getTimezoneOffset();
            entityToAdd.loppupvm = moment(entityToAdd.loppupvm).add(loppupvmTimezoneOffsetInMinutes, 'minutes');
        }

        var newOrganisaatioEntity = angular.copy(lisattavaKielipainotus);
        newOrganisaatioEntity.alkupvm = lisattavaKielipainotus.alkupvm.format('YYYY-MM-DD');
        newOrganisaatioEntity.loppupvm = lisattavaKielipainotus.loppupvm && lisattavaKielipainotus.loppupvm.format('YYYY-MM-DD');
        existingEntities.push(newOrganisaatioEntity);

        vm[koodiFieldName] = {};
    };

    vm.addKielipainotus = function () {
        addEntity(vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenKielipainotukset, vm.kielipainotus, 'kielipainotus');
    };

    vm.removeKielipainotus = function (poistettavaKielipainotus) {
        var kielipainotukset = vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenKielipainotukset;
        vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenKielipainotukset = kielipainotukset.filter(function (kielipainotus) {
            return !angular.equals(kielipainotus, poistettavaKielipainotus);
        });
    };

    vm.addVarhaiskasvatuksenToiminnallinenpainotus = function () {
        addEntity(vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToiminnallinenpainotukset, vm.toiminnallinenpainotus, 'toiminnallinenpainotus');
    };

    vm.removeVarhaiskasvatuksenToiminnallinepainotus = function (poistettavaToiminnallinenpainotus) {
        var toiminnallisetPainotukset = vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToiminnallinenpainotukset;
        vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenToiminnallinenpainotukset = toiminnallisetPainotukset.filter(function (toiminnallinenpainotus) {
            return !angular.equals(toiminnallinenpainotus, poistettavaToiminnallinenpainotus);
        });
    };

    vm.addVarhaiskasvatuksenJarjestamismuoto = function() {
        if (vm.varhaiskasvatuksenJarjestamismuodot && vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot.indexOf(vm.varhaiskasvatuksenJarjestamismuodot) === -1) {
            vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot.push(vm.varhaiskasvatuksenJarjestamismuodot);
        }
    };

    vm.removeVarhaiskasvatuksenJarjestamismuoto = function (poistettavaJarjestamismuoto) {
        var jarjestamismuodot = vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot;
        vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot = jarjestamismuodot.filter(function (jarjestamismuoto) {
            return jarjestamismuoto !== poistettavaJarjestamismuoto;
        });
    };

    vm.localiseVarhaiskasvatuksenJarjestamismuoto = function (koodiUri) {
        var localisedKoodi = vm.koodisto.jarjestamismuoto.filter(function (jarjestamismuotoKoodi) {
            return jarjestamismuotoKoodi.uri === koodiUri;
        })[0];
        return localisedKoodi && localisedKoodi.nimi;
    };

    vm.localiseVarhaiskasvatuksenToiminnallinenpainotus = function (koodiUri) {
        var localisedKoodi = vm.koodisto.toiminnallinenPainotus.filter(function (toiminnallinepainotusKoodi) {
            return toiminnallinepainotusKoodi.uri === koodiUri;
        })[0];
        return localisedKoodi && localisedKoodi.nimi;
    };

    vm.localiseKielipainotus = function (koodiUri) {
        var localisedKoodi =  vm.koodisto.kieli.filter(function (kieliKoodi) {
            return kieliKoodi.uri === koodiUri;
        })[0];
        return localisedKoodi && localisedKoodi.nimi;
    };

    vm.getUnselectedJarjestamismuodot = function () {
        return vm.koodisto.jarjestamismuoto.filter(function (jarjestamismuotoKoodi) {
            return vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.varhaiskasvatuksenJarjestamismuodot.indexOf(jarjestamismuotoKoodi.uri) === -1;
        });
    };

    vm.dbFormatToUI = function (dbFormatDate) {
        return dbFormatDate && moment(dbFormatDate).format('DD.MM.YYYY');
    };

    vm.isPaivakoti = function(){
        return vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.toimintamuoto === "vardatoimintamuoto_tm01";
    };

    vm.isPerhepaivakoti = function(){
        return vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.toimintamuoto === "vardatoimintamuoto_tm02";
    };

    vm.isRyhmaperhepaivakoti = function(){
        return vm.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot.toimintamuoto === "vardatoimintamuoto_tm03";
    };

});
