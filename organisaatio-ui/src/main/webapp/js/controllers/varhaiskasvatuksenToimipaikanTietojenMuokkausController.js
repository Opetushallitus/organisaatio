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

        if (existingModel && Object.getOwnPropertyNames(existingModel).length) {
            vm.model = existingModel;
        }
        else {
            vm.model = {
                toimintamuoto: '',
                kasvatusopillinenJarjestelma: '',
                varhaiskasvatuksenToiminnallinenpainotukset: [],
                paikkojenLukumaara: null,
                varhaiskasvatuksenJarjestamismuodot: [],
                varhaiskasvatuksenKielipainotukset: []
            };
            $scope.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot = vm.model;
        }
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

        var isAlreadyAdded = existingEntities.filter(function (existingEntity) {
                return existingEntity[koodiFieldName] === entityToAdd[koodiFieldName]
                    && existingEntity.alkupvm.isSame(entityToAdd.alkupvm)
                    && existingEntity.loppupvm.isSame(entityToAdd.loppupvm);
            }
        )[0];
        if (!isAlreadyAdded) {
            var newOrganisaatioEntity = angular.copy(lisattavaKielipainotus);
            newOrganisaatioEntity.alkupvm = lisattavaKielipainotus.alkupvm.format('YYYY-MM-DD');
            newOrganisaatioEntity.loppupvm = lisattavaKielipainotus.loppupvm && lisattavaKielipainotus.loppupvm.format('YYYY-MM-DD');
            existingEntities.push(newOrganisaatioEntity);
        }
        vm[koodiFieldName] = {};
    };

    vm.addKielipainotus = function () {
        addEntity(vm.model.varhaiskasvatuksenKielipainotukset, vm.kielipainotus, 'kielipainotus');
    };

    vm.removeKielipainotus = function (poistettavaKielipainotus) {
        var kielipainotukset = vm.model.varhaiskasvatuksenKielipainotukset;
        vm.model.varhaiskasvatuksenKielipainotukset = kielipainotukset.filter(function (kielipainotus) {
            return kielipainotus.kielipainotus !== poistettavaKielipainotus.kielipainotus
                || !kielipainotus.alkupvm === poistettavaKielipainotus.alkupvm
                || !kielipainotus.loppupvm === poistettavaKielipainotus.loppupvm;
        });
    };

    vm.addVarhaiskasvatuksenToiminnallinenpainotus = function () {
        addEntity(vm.model.varhaiskasvatuksenToiminnallinenpainotukset, vm.toiminnallinenpainotus, 'toiminnallinenpainotus');
    };

    vm.removeVarhaiskasvatuksenToiminnallinepainotus = function (poistettavaToiminnallinenpainotus) {
        var toiminnallisetPainotukset = vm.model.varhaiskasvatuksenToiminnallinenpainotukset;
        vm.model.varhaiskasvatuksenToiminnallinenpainotukset = toiminnallisetPainotukset.filter(function (toiminnallinenpainotus) {
            return toiminnallinenpainotus.toiminnallinenpainotus !== poistettavaToiminnallinenpainotus.toiminnallinenpainotus
                || !toiminnallinenpainotus.alkupvm === poistettavaToiminnallinenpainotus.alkupvm
                || !toiminnallinenpainotus.loppupvm === poistettavaToiminnallinenpainotus.loppupvm;
        });
    };

    vm.addVarhaiskasvatuksenJarjestamismuoto = function() {
        if (vm.varhaiskasvatuksenJarjestamismuodot && vm.model.varhaiskasvatuksenJarjestamismuodot.indexOf(vm.varhaiskasvatuksenJarjestamismuodot) === -1) {
            vm.model.varhaiskasvatuksenJarjestamismuodot.push(vm.varhaiskasvatuksenJarjestamismuodot);
        }
    };

    vm.removeVarhaiskasvatuksenJarjestamismuoto = function (poistettavaJarjestamismuoto) {
        var jarjestamismuodot = vm.model.varhaiskasvatuksenJarjestamismuodot;
        vm.model.varhaiskasvatuksenJarjestamismuodot = jarjestamismuodot.filter(function (jarjestamismuoto) {
            return jarjestamismuoto !== poistettavaJarjestamismuoto;
        });
    };

    vm.localiseVarhaiskasvatuksenJarjestamismuoto = function (koodiUri) {
        return vm.koodisto.jarjestamismuoto.filter(function (jarjestamismuotoKoodi) {
            return jarjestamismuotoKoodi.uri === koodiUri;
        })[0].nimi;
    };

    vm.localiseVarhaiskasvatuksenToiminnallinenpainotus = function (koodiUri) {
        return vm.koodisto.toiminnallinenPainotus.filter(function (toiminnallinepainotusKoodi) {
            return toiminnallinepainotusKoodi.uri === koodiUri;
        })[0].nimi;
    };

    vm.localiseKielipainotus = function (koodiUri) {
        return vm.koodisto.kieli.filter(function (kieliKoodi) {
            return kieliKoodi.uri === koodiUri;
        })[0].nimi;
    };

    vm.getUnselectedJarjestamismuodot = function () {
        return vm.koodisto.jarjestamismuoto.filter(function (jarjestamismuotoKoodi) {
            return vm.model.varhaiskasvatuksenJarjestamismuodot.indexOf(jarjestamismuotoKoodi.uri) === -1;
        });
    };

    vm.dbFormatToUI = function (dbFormatDate) {
        return dbFormatDate && moment(dbFormatDate).format('DD.MM.YYYY');
    };

});
