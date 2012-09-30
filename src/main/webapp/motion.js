function MotionCntl($scope, $http) {
	$http.get('./rest/visonic/motionsensors').success(function(data) {
		console.log(data);
		$scope.motionsensors = data.items;
	});
	
}