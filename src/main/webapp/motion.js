function MotionCntl($scope, $http) {
	$http.get('./rest/visonic/motionsensors').success(function(data) {
		console.log(data);
		$scope.visonicmotionsensors = data.items;
	});
	$http.get('./rest/fs20/motionsensors').success(function(data) {
		console.log(data);
		$scope.fs20motionsensors = data.items;
	});
	$http.get('./rest/home/doorbell').success(function(data) {
		console.log(data);
		$scope.doorbell = data;
	});
	
}