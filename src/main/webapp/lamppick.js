function LampCntl($scope, $http) {
  $scope.r = 180;
  $scope.g = 180;
  $scope.b = 180;
  $scope.q = 100;
  
  var send = function (r,g,b,q) {
	  console.log("Sending " + r + " " + g + " " + b + " " + q);
	  //$http.post('./rest/node/rf12', { contents : [ 1,1,82,71,r,g,b,q,0,0,0,0 ]});
	  $http.post('./rest/rgblamp', { r:r, g:g, b:b, q:q });
	  
  }
  
  var sendAt = function (x,y) {
      var context = document.getElementById('canvas').getContext('2d');
      var imgdata = context.getImageData(x, y, 1, 1).data;
      console.log(imgdata);
      context.fillStyle="rgb(" + imgdata[0] + "," + imgdata[1] + "," + imgdata[2] + ")";
      context.fillRect(0,0,320,39);
      send(imgdata[0], imgdata[1], imgdata[2], 100);	  
  }
  
  $scope.down = function (x,y) {
	  console.log("Picked at " + x + "," + y);
      $scope.pressed = true;
      sendAt(x,y);
  }
  
  $scope.move = function (x,y) {
	  if ($scope.pressed) {
		  console.log("Moving " + x + "," + y);
		  sendAt(x,y);
	  }
  }
  
  $scope.up = function () {
	  $scope.pressed = false;
  }
  
}