function SettingsCntl($scope, $http) {
  $http.get('./rest/home/settings').success(function(data) {
	  console.log(data);
	  $scope.settings = data;
  });
	
  $scope.save = function () {
	  console.log("Saving:");
	  console.log($scope.settings);
	  $http.put('./rest/home/settings', $scope.settings);
  }
}