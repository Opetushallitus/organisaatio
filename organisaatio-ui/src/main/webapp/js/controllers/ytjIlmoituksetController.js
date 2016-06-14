app.controller('YtjIlmoituksetController', ['$scope', '$log', 'YtjLoki', 'LocalisationService',
    function YtjIlmoituksetController($scope, $log, YtjLoki, LocalisationService) {
        $log = $log.getInstance("YtjIlmoituksetController");
        $scope.logs = [];

        $scope.$on('$routeChangeSuccess', function() {
            YtjLoki.get({}, function (result) {
                $log.info("resuls", result);
                $scope.logs.length = 0;
                angular.forEach(result, function (loki) {
                    var obj = {};
                    // Group by oid
                    angular.forEach(loki.ytjVirheet, function(virhe) {
                        if(angular.isUndefined(obj[virhe.oid])) {
                            obj[virhe.oid] = [virhe];
                        }
                        else {
                            obj[virhe.oid].push(virhe);
                        }
                    });
                    loki.ytjVirheet = obj;
                    $scope.logs.push(loki);
                });
            }, function () {
                $log.error("Could not fetch ytj log information.");
            })
        });
        
        $scope.getVirheHeader = function(log) {
            if(log.paivitysTila === 'ONNISTUNUT') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila);
            }
            else if(log.paivitysTila === 'ONNISTUNUT_VIRHEITA') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila, [log.ytjVirheet.length]);
            }
            else if(log.paivitysTila === 'EPAONNISTUNUT') {
                return LocalisationService.t('ilmoitukset.log.' + log.paivitysTila, [log.ytjVirheet[0].virheKohde]);
            }
            else {
                $log.error('Invalid paivitysTila');
            }
        }
    }
]);