function DimmersCntl($scope, $http) {
	$scope.brightnesses = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16];
	
	$http.get('./rest/fs20/dimmers').success(function(data) {
		console.log(data);
		$scope.dimmers = data.items;
	});
	
	$scope.toggle = function(name, brightness) {
		console.log("" + name  + " has just been toggled " + brightness);
		$http.post('./rest/fs20', {name: name, brightness: brightness});
	}
}