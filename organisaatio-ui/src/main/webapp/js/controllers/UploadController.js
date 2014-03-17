 function UploadController($scope, uploadService) {
    $scope.getFile = function (filename) {
        $scope.progress = 0;
        uploadService.readAsDataUrl(filename, $scope)
                      .then(function(result) {
                          organisaatio = $scope.$parent.model.organisaatio;
                          if (!organisaatio.metadata) {
                              organisaatio.metadata = {};
                          }
                          organisaatio.metadata.kuvaEncoded = result.split(',')[1];
                      });
    };
}


