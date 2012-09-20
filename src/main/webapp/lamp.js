function LampCntl($scope, $http) {
  $scope.r = 180;
  $scope.g = 180;
  $scope.b = 180;
  $scope.q = 100;
  
  $scope.send = function (r,g,b,q) {
	  console.log("Sending " + r + " " + g + " " + b + " " + q);
	  $http.post('./rest/node/rf12', { contents : [ 1,1,82,71,r,g,b,q,0,0,0,0 ]});
  }
}