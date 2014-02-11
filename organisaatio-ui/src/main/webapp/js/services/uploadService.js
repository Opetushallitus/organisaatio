app.factory('uploadService', function($q, $log) {
    var onLoad = function(reader, deferred, scope) {
        return function() {
            scope.$apply(function() {
                deferred.resolve(reader.result);
            });
        };
    };

    var onError = function(reader, deferred, scope) {
        return function() {
            scope.$apply(function() {
                deferred.reject(reader.result);
            });
        };
    };

    var getReader = function(deferred, scope) {
        var reader = null;
        if (window.FileReader) {
            reader = new FileReader();
        } else {
            // TODO: IE9 has no File API. Use flash?
            //  - http://aymkdn.github.io/FileToDataURI
            //  - https://github.com/Jahdrien/FileReader
            //  - https://github.com/mailru/FileAPI
        }
        reader.onload = onLoad(reader, deferred, scope);
        reader.onerror = onError(reader, deferred, scope);
        return reader;
    };

    var readAsDataURL = function(file, scope) {
        var deferred = $q.defer();

        var reader = getReader(deferred, scope);
        if (window.FileReader) {
            reader.readAsDataURL(file);
        } else {
            // TODO: IE9
        }

        return deferred.promise;
    };

    return {
        readAsDataUrl: readAsDataURL
    };

});


