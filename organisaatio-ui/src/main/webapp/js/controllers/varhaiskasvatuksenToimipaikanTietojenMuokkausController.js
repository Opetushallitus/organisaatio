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

        $scope.addKielipainotus = function () {
            var lisattavaKielipainotus = $scope.kielipainotus;
            if (!$scope.model.varhaiskasvatuksenKielipainotukset) {
                $scope.model.varhaiskasvatuksenKielipainotukset = [];
            }
            // Workaround for timezone issue with UIB https://github.com/angular-ui/bootstrap/issues/6235
            var alkupvmTimezoneOffsetInMinutes = -lisattavaKielipainotus.alkupvm.getTimezoneOffset();
            var loppupvmTimezoneOffsetInMinutes = -lisattavaKielipainotus.loppupvm.getTimezoneOffset();
            lisattavaKielipainotus.alkupvm = moment(lisattavaKielipainotus.alkupvm).add(alkupvmTimezoneOffsetInMinutes, 'minutes');
            lisattavaKielipainotus.loppupvm = moment(lisattavaKielipainotus.loppupvm).add(loppupvmTimezoneOffsetInMinutes, 'minutes');

            var isAlreadyAdded = $scope.model.varhaiskasvatuksenKielipainotukset.filter(function (kielipainotus) {
                    return kielipainotus.kielipainotus === lisattavaKielipainotus.kielipainotus
                        && kielipainotus.alkupvm.isSame(lisattavaKielipainotus.alkupvm)
                        && kielipainotus.loppupvm.isSame(lisattavaKielipainotus.loppupvm);
                }
            )[0];
            if (!isAlreadyAdded) {
                $scope.model.varhaiskasvatuksenKielipainotukset.push(angular.copy(lisattavaKielipainotus));
            }
            $scope.kielipainotus = {};
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
            var defaultPlaceholder = $filter('i18n')("Organisaationmuokkaus.lisaaVarhaiskasvatuksenToimintamuodot");
            var lisattavaToimintamuoto = $scope.varhaiskasvatuksenToimintamuodotPlaceholder;
            if (!$scope.model.varhaiskasvatuksenToimintamuodot) {
                $scope.model.varhaiskasvatuksenToimintamuodot = [];
            }
            var isNotAlreadyAdded = $scope.model.varhaiskasvatuksenToimintamuodot.indexOf(lisattavaToimintamuoto) === -1;
            if (lisattavaToimintamuoto && lisattavaToimintamuoto !== defaultPlaceholder && isNotAlreadyAdded) {
                $scope.model.varhaiskasvatuksenToimintamuodot.push(lisattavaToimintamuoto);
            }
        };

        $scope.removeVarhaiskasvatuksenToimintamuoto = function (poistettavaToimintamuoto) {
            var toimintamuodot = $scope.model.varhaiskasvatuksenToimintamuodot;
            $scope.model.varhaiskasvatuksenToimintamuodot = toimintamuodot.filter(function (toimintamuoto) {
                return toimintamuoto !== poistettavaToimintamuoto;
            })
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
