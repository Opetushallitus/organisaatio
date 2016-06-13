app.controller('YtjIlmoituksetController', ['$scope', '$log', 'YtjLoki',
    function YtjIlmoituksetController($scope, $log, YtjLoki) {
    $log = $log.getInstance("YtjIlmoituksetController");
    $scope.logs = [];

    $scope.$on('$routeChangeSuccess', function() {
        YtjLoki.get({}, function (result) {
            $log.info("resuls", result);
            $scope.logs.length = 0;
            angular.forEach(result, function (loki) {
                $scope.logs.push(loki);
            });
            $scope.logs = result;
        }, function () {
            $log.error("Could not fetch ytj log information.");
        })
    });
}]);