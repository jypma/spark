function SwitchesCntl($scope, $http) {
	$http.get('./rest/fs20/switches').success(function(data) {
		console.log(data);
		$scope.switches = data.items;
	});
	
	$scope.toggle = function(zoneName, name, state) {
		console.log("" + name  + " has just been toggled " + state);
		$http.post('./rest/fs20', {zoneName: zoneName, name: name, on: state});
	}
}