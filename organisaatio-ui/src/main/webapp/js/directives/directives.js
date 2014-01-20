app.directive('auth', function($animate, $timeout, $log, AuthService) {
    return {
        link : function($scope, element, attrs) {

            element.addClass('ng-hide');

            var success = function() {
                element.removeClass('ng-hide');
            };

            $timeout(function() {
                switch(attrs.auth) {

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
            },0);

            attrs.$observe('authOrg', function() {
                if(attrs.authOrg) {
                    $log.log("auth-org: " + attrs.authOrg);
                    switch(attrs.auth) {
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
