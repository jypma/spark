function ValvesCntl($scope, $http) {
	$http.get('./rest/valves').success(function(data) {
		console.log(data);
		$scope.valves = data.items;
	});
	
	$scope.toggle = function(valve, zoneName, name, state) {
		console.log(valve);
		//console.log("valve " + name + " in " + zoneName + " has just been toggled " + state);
		$http.post('./rest/valves', valve);
	}
}