function SwitchesCntl($scope, $http) {
	$http.get('./rest/fs20/switches').success(function(data) {
		console.log(data);
		$scope.switches = data.items;
	});
	
	$scope.toggle = function(name, state) {
		console.log("" + name  + " has just been toggled " + state);
		$http.post('./rest/fs20', {name: name, on: state});
	}
}