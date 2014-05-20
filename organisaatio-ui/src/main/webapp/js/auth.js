var READ = "_READ";
var UPDATE = "_READ_UPDATE";
var CRUD = "_CRUD";
var RYHMA = "_RYHMA";
var OPH_ORG = "1.2.246.562.10.00000000001";
var ORGANISAATIO_URL_BASE = SERVICE_URL_BASE;
var CAS_URL = CAS_URL || "/cas/myroles";

app.factory('MyRolesModel', function ($q, $http) {
    var deferred = $q.defer();

    var factory = (function () {
        var instance = {};
        instance.myroles = [];

        $http.get(CAS_URL).success(function (result) {
            instance.myroles = result;
            deferred.resolve(instance);
        });

        return instance;
    })();


    return deferred.promise;

});


app.factory('AuthService', function ($q, $http, $timeout, MyRolesModel) {

    // organisation check
    var readAccess = function (service, org, model) {

        if (model.myroles.indexOf(service + READ + (org ? "_" + org : "")) > -1 ||
            model.myroles.indexOf(service + UPDATE + (org ? "_" + org : "")) > -1 ||
            model.myroles.indexOf(service + CRUD + (org ? "_" + org : "")) > -1) {
            return true;
        }
    };

    var updateAccess = function (service, org, model) {

        if (model.myroles.indexOf(service + UPDATE + (org ? "_" + org : "")) > -1 ||
            model.myroles.indexOf(service + CRUD + (org ? "_" + org : "")) > -1) {
            return true;
        }
    };

    var crudAccess = function (service, org, model) {

        if (model.myroles.indexOf(service + CRUD + (org ? "_" + org : "")) > -1) {
            return true;
        }
    };

    var accessCheck = function (service, orgs, accessFunction) {
        var deferred = $q.defer();

            MyRolesModel.then(function (model) {
                if (orgs && orgs.length > 0) {
                    orgs.forEach(function(orgOid) {
                        if (orgOid !== '') {
                            $http.get(ORGANISAATIO_URL_BASE + "organisaatio/" + orgOid + "/parentoids", { cache: true}).success(function (result) {
                                var found = false;
                                result.split("/").forEach(function (org) {
                                    if (accessFunction(service, org, model)) {
                                        found = true;
                                    }
                                });
                                if (found) {
                                    deferred.resolve();
                                } else {
                                    deferred.reject();
                                }
                            });
                        }
                    });
                } else {
                    if (accessFunction(service, "", model)) {
                        deferred.resolve();
                    } else {
                        deferred.reject();
                    }
                }
            });

        return deferred.promise;
    };

    // OPH check -- voidaan ohittaa organisaatioiden haku
    var ophRead = function (service, model) {
        return (model.myroles.indexOf(service + READ + "_" + OPH_ORG) > -1
            || model.myroles.indexOf(service + UPDATE + "_" + OPH_ORG) > -1
            || model.myroles.indexOf(service + CRUD + "_" + OPH_ORG) > -1);

    };

    var ophUpdate = function (service, model) {
        return (model.myroles.indexOf(service + UPDATE + "_" + OPH_ORG) > -1
            || model.myroles.indexOf(service + CRUD + "_" + OPH_ORG) > -1);
    };

    var ophCrud = function (service, model) {
        return (model.myroles.indexOf(service + CRUD + "_" + OPH_ORG) > -1);
    };

    var ophAccessCheck = function (service, accessFunction) {
        var deferred = $q.defer();

        MyRolesModel.then(function (model) {
            if (accessFunction(service, model)) {
                deferred.resolve();
            } else {
                deferred.reject();
            }
        });

        return deferred.promise;
    };

    var containsRyhmaRole = function (service, roles) {
        for (var i in roles) {
            if (roles[i].indexOf(service + RYHMA) > -1) {
                return true;
            }
        }
        return false;
    };

    var ryhmaAccessCheck = function (service) {
        var deferred = $q.defer();
        MyRolesModel.then(function(model) {
            if (ophCrud(service, model)) {
                deferred.resolve();
            } else if (containsRyhmaRole(service, model.myroles)) {
                deferred.resolve();
            } else {
                deferred.reject();
            }
        });
        return deferred.promise;
    };

    var auth = (function () {
        "use strict";

        var instance = {};

        instance.organisaatiot = {};

        instance.readOrg = function (service, orgs) {
            return accessCheck(service, orgs, readAccess);
        };

        instance.updateOrg = function (service, orgs) {
            return accessCheck(service, orgs, updateAccess);
        };

        instance.crudOrg = function (service, orgs) {
            return accessCheck(service, orgs, crudAccess);
        };

        instance.readOph = function (service) {
            return ophAccessCheck(service, ophRead);
        };

        instance.updateOph = function (service) {
            return ophAccessCheck(service, ophUpdate);
        };

        instance.crudOph = function (service) {
            return ophAccessCheck(service, ophCrud);
        };

        instance.crudRyhma = function (service) {
            return ryhmaAccessCheck(service);
        };

        instance.getOrganizations = function (service) {
            var deferred = $q.defer();
            instance.organisaatiot[service] = [];
            MyRolesModel.then(function (model) {
                model.myroles.forEach(function (role) {
                    // TODO: refaktor
                    var org;
                    if (role.indexOf(service + "_CRUD_") > -1) {
                        org = role.replace(service + "_CRUD_", '');
                    } else if (role.indexOf(service + "_READ_UPDATE_") > -1) {
                        org = role.replace(service + "_READ_UPDATE_", '');
                    } else if (role.indexOf(service + "_READ_UPDATE") === -1 && role.indexOf(service + "_READ_") > -1) {
                        org = role.replace(service + "_READ_", '');
                    }

                    if (org && instance.organisaatiot[service].indexOf(org) === -1) {

                        instance.organisaatiot[service].push(org);
                    }
                });

                deferred.resolve(instance.organisaatiot[service]);
            });
            return deferred.promise;
        };

        return instance;
    })();

    return auth;
});
