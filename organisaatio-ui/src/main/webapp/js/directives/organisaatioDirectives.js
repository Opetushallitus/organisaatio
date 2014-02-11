app.directive('editAddress', function() {
    return {
        restrict: 'E',
        scope: {
            optional: '=',
            form: '=',
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

app.directive('testField', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                $log.log("ret " + viewValue);
                return viewValue;
            });
        }
    };
});

app.directive('namesCombinedField', function() {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                scope.form.nimifi.$setValidity('namescombinedrequired', true);

                if (!viewValue && !scope.form.nimifi.$viewValue && 
                        !scope.form.nimisv.$viewValue && !scope.form.nimien.$viewValue) {
                    scope.form.nimifi.$setValidity('namescombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$parsers.unshift(parserValidator);

            var formatterValidator = function(viewValue) {
                scope.form.nimifi.$setValidity('namescombinedrequired', true);
                
                if (!viewValue && !scope.form.nimifi.$viewValue && 
                        !scope.form.nimisv.$viewValue && !scope.form.nimien.$viewValue) {
                    scope.form.nimifi.$setValidity('namescombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

app.directive('addressCombinedField', function() {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                var returnUndefined = false;
                scope.innerForm.osoitefi.$setValidity('addresscombinedrequired', true);
                if (scope.optional) {
                    return viewValue;
                }
                if (!(scope.innerForm.osoitefi.$viewValue && scope.innerForm.postifi.$viewValue)
                        && !(scope.innerForm.osoitesv.$viewValue && scope.innerForm.postisv.$viewValue)
                        && !scope.innerForm.kvosoite.$viewValue) {
                    scope.innerForm.osoitefi.$setValidity('addresscombinedrequired', false);
                    returnUndefined = true;
                }
                if (returnUndefined === true) {
                    return viewValue;
                } else {
                    return viewValue;
                }
            };
            ctrl.$parsers.unshift(parserValidator);

            var formatterValidator = function(viewValue) {
                if (scope.mode==="new") {
                    scope.innerForm.osoitefi.$setValidity('addresscombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});
