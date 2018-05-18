
app.controller('TyyppienHallintaController', function ($scope) {
    $scope.templates = [
        {nimi: 'tyyppienhallinta.yhteystietotyyppi', url: TEMPLATE_URL_BASE + 'yhteystietojentyyppi.html'},
        {nimi: 'tyyppienhallinta.lisatietotyyppi', url: TEMPLATE_URL_BASE + 'lisatietotyyppi.html'}
    ];
    $scope.template = $scope.templates[0];

    $scope.setTemplate = function (template) {
        $scope.template = template;
    }
});
