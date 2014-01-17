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

app.directive('testField', function() {
    return {
        require: 'ngModel',
        
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                console.log("ret " + viewValue);
                return viewValue;
            });
        }
    };
});

app.directive('namesCombinedField', function() {
    return {
        require: 'ngModel',

        link: function(scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                var returnUndefined = false;
                scope.form.nimifi.$setValidity('namescombinedrequired', true);
                
                if (!scope.form.nimifi.$viewValue && !scope.form.nimisv.$viewValue && !scope.form.nimien.$viewValue) {
                    scope.form.nimifi.$setValidity('namescombinedrequired', false);
                    returnUndefined = true;
                }                
                if (returnUndefined === true) {
                    return undefined;
                } else {
                    return viewValue;
                }

            });
        }
    };
});

app.directive('addressCombinedField', function() {
    return {
        require: 'ngModel',
        
        link: function(scope, elm, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                var returnUndefined = false;
                scope.innerForm.osoitefi.$setValidity('addresscombinedrequired', true);
                if (scope.optional) {
                   return viewValue; 
                }               
                if (!(scope.innerForm.osoitefi.$viewValue && scope.innerForm.postifi.$viewValue) 
                        && !(scope.innerForm.osoitesv.$viewValue && scope.innerForm.postisv.$viewValue )
                        && !scope.innerForm.kvosoite.$viewValue) {
                    scope.innerForm.osoitefi.$setValidity('addresscombinedrequired', false);
                    returnUndefined = true;
                }                
                if (returnUndefined === true) {
                    return viewValue; //return undefined;
                } else {
                    return viewValue;
                }
            });
        }
    };
});
