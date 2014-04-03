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

// Tätä voidaan käyttää ng-patternin sijaan, jos halutaan että epävalidi modelin arvo näytetään.
// Angular ei näytä epävalidia arvoa vaan tyhjän kentän (https://github.com/angular/angular.js/issues/1412).
app.directive('ophPattern', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                var isValid = (typeof viewValue === 'string' && viewValue.match(attrs.ophPattern));
                ctrl.$setValidity('ophPattern', isValid);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});

app.directive('ophNullIfZeroLength', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var parserValidator = function(viewValue) {
                if (viewValue === null) {
                    return viewValue;
                }
                if (typeof viewValue === 'undefined') {
                    return null;
                }
                return ((viewValue.length === 0) ? null : viewValue);
            };
            ctrl.$parsers.unshift(parserValidator);
            var formatterValidator = function(viewValue) {
                if (elm[0] && (elm[0].value !== null) && (elm[0].value.length === 0)) {
                    elm.value = null;
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
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
                scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', true);
                if (scope.optional) {
                    return viewValue;
                }
                if (!(scope.form.kayntiosoitefi.$viewValue && scope.form.postiosoitefi.$viewValue)
                        && !(scope.form.kayntiosoitesv.$viewValue && scope.form.postiosoitesv.$viewValue)
                        && !scope.form.kayntiosoitekv.$viewValue && scope.form.postiosoitekv.$viewValue) {
                    scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', false);
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
                if (scope.mode === "new") {
                    scope.form.kayntiosoitefi.$setValidity('addresscombinedrequired', false);
                }
                return viewValue;
            };
            ctrl.$formatters.unshift(formatterValidator);
        }
    };
});

// Validoi että syötetty postinumero löytyy koodistosta
app.directive('ophValidatePostcode', function($log) {
    return {
        require: 'ngModel',
        link: function(scope, elm, attrs, ctrl) {
            var validator = function(viewValue) {
                if (!viewValue) {
                    return viewValue;
                }
                var isValid = (typeof viewValue === 'string' && scope.model.koodisto.postinumerot.indexOf(viewValue) !== -1);
                ctrl.$setValidity('ophpostcode', isValid);
                return viewValue;
            };
            ctrl.$parsers.unshift(validator);
            ctrl.$formatters.unshift(validator);
        }
    };
});
