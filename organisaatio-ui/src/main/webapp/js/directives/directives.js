app.directive('auth', function($animate, $timeout, $log, AuthService) {
    return {
        link: function($scope, element, attrs) {
            var disable = false;

            $log.log("auth-disable=" + attrs.authDisable);
            if (attrs.authDisable==="true") {
                disable = true;
            }

            if (!disable) {
                element.addClass("ng-hide");
            } else {
                $(element).attr("disabled","disabled");
            }

            var success = function() {
                if (!disable) {
                    element.removeClass("ng-hide");
                } else {
                    $(element).removeAttr("disabled");
                }
            };

            $timeout(function() {
                switch (attrs.auth) {

                    case "crudOph":
                        AuthService.crudOph(attrs.authService).then(success);
                        break;

                    case "updateOph":
                        AuthService.updateOph(attrs.authService).then(success);
                        break;

                    case "readOph":
                        AuthService.readOph(attrs.authService).then(success);
                        break;

                    case "crudOrg":
                        AuthService.crudOrg(attrs.authService, [attrs.authOrg]).then(success);
                        break;

                    case "updateOrg":
                        AuthService.updateOrg(attrs.authService, [attrs.authOrg]).then(success);
                        break;

                    case "readOrg":
                        AuthService.readOrg(attrs.authService, [attrs.authOrg]).then(success);
                        break;
                }
            }, 0);

            attrs.$observe('authOrg', function() {
                if (attrs.authOrg) {
                    $log.log("auth-org: " + attrs.authOrg);
                    switch (attrs.auth) {
                        case "crudOrg":
                            AuthService.crudOrg(attrs.authService, [attrs.authOrg]).then(success);
                            break;

                        case "updateOrg":
                            AuthService.updateOrg(attrs.authService, [attrs.authOrg]).then(success);
                            break;

                        case "readOrg":
                            AuthService.readOrg(attrs.authService, [attrs.authOrg]).then(success);
                            break;
                    }
                }
            });
        }
    };
});

app.directive("ngFileSelect", function() {
    return {
        link: function($scope, el) {

            el.bind("change", function(e) {
                if ((e.srcElement || e.target).files) {
                    $scope.getFile((e.srcElement || e.target).files[0]);
                } else {
                    // IE
                    $scope.getFile(e.srcElement.value);
                }
            });
        }
    };
});
