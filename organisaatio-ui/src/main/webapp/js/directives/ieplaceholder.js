app.directive("iePlaceholder", function($timeout) {
    return {
        restrict: 'A',
        link: function(scope, element, attr) {
            var _placeholder = attr.iePlaceholder || attr.placeholder;
            var _form = element[0].form;
            
            // Check for HTML5 placeholder support.
            // Thanks to Modernizr for exmaple (http://modernizr.com/)
            var PLACEHOLDER_SUPPORT = (function() {
                var input_element = document.createElement('input');
                return 'placeholder' in input_element;
            }());
            
            // If browser has HTML5 placeholder ensure it is set and leave
            if (PLACEHOLDER_SUPPORT) {
                element.attr('placeholder', _placeholder);
                return;
            }
            
            // Without HTML5 placeholder support set the value of the
            // input by default and blur, and unset on focus.
            element.bind('focus', function(e) {
                if (element.val() === _placeholder) {
                    element.val('');
                    element.removeClass('greyed');
                }
            });
            element.bind('blur', function(e) {
                $timeout(function() {
                    if (element.val() === '') {
                        element.val(_placeholder);
                        element.addClass('greyed');
                    }
                });
            });
            // If this element is part of a form ensure that the placeholder
            // value is not sent to the server on submit
            if (_form !== null) {
                angular.element(_form).bind('submit', function(e) {
                    if (element.val() === _placeholder) {
                        element.val('');
                        element.removeClass('greyed');
                    }
                    return true;
                });
            }
            // Initialise placeholder
            $timeout(function() {
                if (element.val() === '') {
                    element.val(_placeholder);
                    element.addClass('greyed');
                }
            });
        }
    };
});