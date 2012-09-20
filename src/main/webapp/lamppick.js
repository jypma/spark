function LampCntl($scope, $http) {
  $scope.r = 180;
  $scope.g = 180;
  $scope.b = 180;
  $scope.q = 100;
  
  $scope.send = function (r,g,b,q) {
	  console.log("Sending " + r + " " + g + " " + b + " " + q);
	  $http.post('./rest/node/rf12', { contents : [ 1,1,82,71,r,g,b,q,0,0,0,0 ]});
  }
  
  $scope.pick = function (x,y) {
	  console.log("Picked at " + x + "," + y);
      var context = document.getElementById('canvas').getContext('2d');
      var imgdata = context.getImageData(x, y, 1, 1).data;
      console.log(imgdata);
      $scope.send(imgdata[0], imgdata[1], imgdata[2], 100);
  }
}