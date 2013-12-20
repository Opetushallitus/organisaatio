app.directive('editAddress', function() {
    return {
        restrict: 'E',
        scope: {
            label: '=',
            addressmodelfi: '=',
            addressmodelsv: '=',
            addressmodelkv: '=',
            addresstype: '=',
            addressformat: '=',
            country: '=',
            postcodes: '=',
            postcodefi: '=',
            postcodesv: '=',
            setpostcodefi: '=',
            setpostcodesv: '=',
            disabled: '=',
            remove: '=',
            index: '@'
        },
        templateUrl: 'osoitteenmuokkaus.html'
    };
});

