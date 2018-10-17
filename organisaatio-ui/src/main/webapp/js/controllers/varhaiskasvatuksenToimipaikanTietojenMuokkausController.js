app.controller('VarhaiskasvatuksenToimipaikanTietojenMuokkausController',
    function NimenMuokkausController($scope, $uibModalInstance, $log, $filter, koodisto, varhaiskasvatuksenToimipaikanTiedot) {

        var log = $log.getInstance("VarhaiskasvatuksenToimipaikanTietojenMuokkausController");

        $scope.clear = function() {
            $scope.model = {
                jarjestamismuoto: '',
                kasvatusopillinenJarjestelma: '',
                toiminnallinenPainotus: '',
                paikkojenLukumaara: null,
                varhaiskasvatuksenToimintamuodot: [],
                varhaiskasvatuksenKielipainotukset: []
            };
            return $scope.model;
        };

        $scope.model = varhaiskasvatuksenToimipaikanTiedot || $scope.clear();

        $scope.koodisto = koodisto;

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
                existingEntities.push(angular.copy(lisattavaKielipainotus));
            }
            $scope[koodiFieldName] = {};

        };

        $scope.addKielipainotus = function () {
            addEntity($scope.model.varhaiskasvatuksenKielipainotukset, $scope.kielipainotus, 'kielipainotus');
        };

        $scope.removeKielipainotus = function (poistettavaKielipainotus) {
            var kielipainotukset = $scope.model.varhaiskasvatuksenKielipainotukset;
            $scope.model.varhaiskasvatuksenKielipainotukset = kielipainotukset.filter(function (kielipainotus) {
                return kielipainotus.kielipainotus !== poistettavaKielipainotus.kielipainotus
                    || !kielipainotus.alkupvm.isSame(poistettavaKielipainotus.alkupvm)
                    || !kielipainotus.loppupvm.isSame(poistettavaKielipainotus.loppupvm);
            });
        };

        $scope.addVarhaiskasvatuksenToimintamuoto = function () {
            addEntity($scope.model.varhaiskasvatuksenToimintamuodot, $scope.toimintamuoto, 'toimintamuoto');
        };

        $scope.removeVarhaiskasvatuksenToimintamuoto = function (poistettavaToimintamuoto) {
            var toimintamuodot = $scope.model.varhaiskasvatuksenToimintamuodot;
            $scope.model.varhaiskasvatuksenToimintamuodot = toimintamuodot.filter(function (toimintamuoto) {
                return toimintamuoto.toimintamuoto !== poistettavaToimintamuoto.toimintamuoto
                    || !toimintamuoto.alkupvm.isSame(poistettavaToimintamuoto.alkupvm)
                    || !toimintamuoto.loppupvm.isSame(poistettavaToimintamuoto.loppupvm);
            });
        };

        $scope.localiseVarhaiskasvatuksenToimintamuoto = function (koodiUri) {
            return $scope.koodisto.varhaiskasvatuksenToimintamuodot.filter(function (toimintamuotoKoodi) {
                return toimintamuotoKoodi.uri === koodiUri;
            })[0].nimi;
        };

        $scope.localiseKielipainotus = function (koodiUri) {
            return $scope.koodisto.kieli.filter(function (kieliKoodi) {
                return kieliKoodi.uri === koodiUri;
            })[0].nimi;
        };

        $scope.getUnselectedToimintamuodot = function () {
            return $scope.koodisto.varhaiskasvatuksenToimintamuodot.filter(function (toimintamuotoKoodi) {
                return $scope.model.varhaiskasvatuksenToimintamuodot.indexOf(toimintamuotoKoodi.uri) === -1;
            });
        };

        $scope.cancel = function() {
            $uibModalInstance.dismiss('cancel');
            $scope.clear();
        };

        $scope.accept = function() {
            $uibModalInstance.close($scope.model);
            $scope.clear();
        };
    });
