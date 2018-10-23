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
            // vm.model.varhaiskasvatuksenKielipainotukset.forEach(function (kielipainotus) {
            //     kielipainotus.alkupvm = moment(kielipainotus.alkupvm);
            //     kielipainotus.loppupvm = moment(kielipainotus.loppupvm);
            // });
            // vm.model.varhaiskasvatuksenToiminnallinenpainotukset.forEach(function (toimintamuoto) {
            //     toimintamuoto.alkupvm = moment(toimintamuoto.alkupvm);
            //     toimintamuoto.loppupvm = moment(toimintamuoto.loppupvm);
            // });
        }
        else {
            vm.model = {
                jarjestamismuoto: '',
                kasvatusopillinenJarjestelma: '',
                varhaiskasvatuksenToiminnallinenpainotukset: [],
                paikkojenLukumaara: null,
                varhaiskasvatuksenToimintamuodot: [],
                varhaiskasvatuksenKielipainotukset: []
            };
            $scope.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot = vm.model;
        }
    };

    vm.koodisto = $scope.model.koodisto;

    vm.initialiseModel();

    // vm.modelToOrganisaatio = function () {
    //     varhaiskasvatuksenToimipaikkaTiedotModel.varhaiskasvatuksenKielipainotukset.forEach(function (kielipainotus) {
    //         kielipainotus.alkupvm = kielipainotus.alkupvm.format('YYYY-MM-DD');
    //         kielipainotus.loppupvm = kielipainotus.loppupvm.format('YYYY-MM-DD');
    //     });
    //     varhaiskasvatuksenToimipaikkaTiedotModel.varhaiskasvatuksenToimintamuodot.forEach(function (toimintamuoto) {
    //         toimintamuoto.alkupvm = toimintamuoto.alkupvm.format('YYYY-MM-DD');
    //         toimintamuoto.loppupvm = toimintamuoto.loppupvm.format('YYYY-MM-DD');
    //     });
    //     $scope.model.organisaatio.varhaiskasvatuksenToimipaikkaTiedot = varhaiskasvatuksenToimipaikkaTiedotModel;
    //
    // };


    var addEntity = function(existingEntities, entityToAdd, koodiFieldName) {
        var lisattavaKielipainotus = entityToAdd;
        if (!existingEntities) {
            existingEntities = [];
        }
        // Workaround for timezone issue with UIB https://github.com/angular-ui/bootstrap/issues/6235
        var alkupvmTimezoneOffsetInMinutes = -entityToAdd.alkupvm.getTimezoneOffset();
        var loppupvmTimezoneOffsetInMinutes = -entityToAdd.loppupvm.getTimezoneOffset();
        entityToAdd.alkupvm = moment(entityToAdd.alkupvm).add(alkupvmTimezoneOffsetInMinutes, 'minutes');
        entityToAdd.loppupvm = moment(entityToAdd.loppupvm).add(loppupvmTimezoneOffsetInMinutes, 'minutes');

        var isAlreadyAdded = existingEntities.filter(function (existingEntity) {
                return existingEntity[koodiFieldName] === entityToAdd[koodiFieldName]
                    && existingEntity.alkupvm.isSame(entityToAdd.alkupvm)
                    && existingEntity.loppupvm.isSame(entityToAdd.loppupvm);
            }
        )[0];
        if (!isAlreadyAdded) {
            var newOrganisaatioEntity = angular.copy(lisattavaKielipainotus);
            newOrganisaatioEntity.alkupvm = lisattavaKielipainotus.alkupvm.format('YYYY-MM-DD');
            newOrganisaatioEntity.loppupvm = lisattavaKielipainotus.loppupvm.format('YYYY-MM-DD');
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

    vm.addVarhaiskasvatuksenToimintamuoto = function() {
        if (vm.toimintamuoto && vm.model.varhaiskasvatuksenToimintamuodot.indexOf(vm.toimintamuoto) === -1) {
            vm.model.varhaiskasvatuksenToimintamuodot.push(vm.toimintamuoto);
        }
    };

    vm.removeVarhaiskasvatuksenToimintamuoto = function (poistettavaToimintamuoto) {
        var toimintamuodot = vm.model.varhaiskasvatuksenToimintamuodot;
        vm.model.varhaiskasvatuksenToimintamuodot = toimintamuodot.filter(function (toimintamuoto) {
            return toimintamuoto !== poistettavaToimintamuoto;
        });
    };

    vm.localiseVarhaiskasvatuksenToimintamuoto = function (koodiUri) {
        return vm.koodisto.varhaiskasvatuksenToimintamuodot.filter(function (toimintamuotoKoodi) {
            return toimintamuotoKoodi.uri === koodiUri;
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

    vm.getUnselectedToimintamuodot = function () {
        return vm.koodisto.varhaiskasvatuksenToimintamuodot.filter(function (toimintamuotoKoodi) {
            return vm.model.varhaiskasvatuksenToimintamuodot.indexOf(toimintamuotoKoodi.uri) === -1;
        });
    };

    vm.dbFormatToUI = function (dbFormatDate) {
        return moment(dbFormatDate).format('DD.MM.YYYY');
    };

});
