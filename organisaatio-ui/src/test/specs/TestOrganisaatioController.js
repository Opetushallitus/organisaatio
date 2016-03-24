describe("Testing OrganisaatioController", function() {
    var $scope = null;
    var $window;

    var AngularLocaleManager = {};


     var mockModel = {
        refreshIfNeeded : function (x){
            $scope.called=true;
        },
        persistOrganisaatio : function (x){
            $scope.called2=true;
        }
      };

    beforeEach(function () {
        AngularLocaleManager.setLocale = function(lang) {};
        module(function($provide) {
            $provide.value('AngularLocaleManager', AngularLocaleManager);
        });
        module('organisaatio');
    });

    beforeEach(inject(function($rootScope, $controller, $location, $routeParams, _$window_) {
        //create a scope object for us to use.
        $scope = $rootScope.$new();
        $window = _$window_;
        $window.APP_CAS_ME = {};
        $window.APP_CAS_ME.lang  = 'fi';

        //spyOn($window, APP_CAS_ME.lang);
        spyOn(mockModel, "refreshIfNeeded");
        spyOn(mockModel, "persistOrganisaatio");

        //now run that scope through the controller function,
        //injecting any services or other injectables we need.
        ctrl = $controller('OrganisaatioController', {
            $scope: $scope,
            $location: $location,
            $routeParams : $routeParams,
            OrganisaatioModel : mockModel
        });
    }));

    it('should have a OrganisaatioController controller', function() {
        expect(ctrl).toBeDefined();
    });

    it('controller should have a called refreshIfNeeded', function() {
            expect(mockModel.refreshIfNeeded).toHaveBeenCalled();
        });

    it('controller should not have a called persistOrganisaatio', function() {
                expect(mockModel.persistOrganisaatio).not.toHaveBeenCalled();
            });

    it('controller should have a set model', inject(['OrganisaatioModel',
        function(OrganisaatioModel) {
            expect($scope.model).toBeDefined;
        }])
    );
});
