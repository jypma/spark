function LampCntl($scope, $http) {
  $scope.r = 180;
  $scope.g = 180;
  $scope.b = 180;
  $scope.q = 100;
  
  var drawGradient = function(r,g,b) {
      var context = document.getElementById('canvas').getContext('2d');
      var lingrad = context.createLinearGradient(0,0,320,0);
      lingrad.addColorStop(0, 'black');
      lingrad.addColorStop(0.1, 'black');
      lingrad.addColorStop(0.9, "rgb(" + r + "," + g + "," + b + ")");
      lingrad.addColorStop(1, "rgb(" + r + "," + g + "," + b + ")");
      context.fillStyle = lingrad;
      context.fillRect(0,0,320,38);    	  	  
  };
  
  $http.get('./rest/rgblamp').success(function(data) {
	  console.log(data);
	  $scope.r = data.r;
	  $scope.g = data.g;
	  $scope.b = data.b;
	  $scope.q = data.q;
	  drawGradient(data.r,data.g,data.b);
  });

  var send = function (r,g,b,q) {
	  console.log("Sending " + r + " " + g + " " + b + " " + q);
	  //$http.post('./rest/node/rf12', { contents : [ 1,1,82,71,r,g,b,q,0,0,0,0 ]});
	  $http.post('./rest/rgblamp', { r:r, g:g, b:b, q:q });
  };
  
  var touchedAt = function (x,y) {
      var context = document.getElementById('canvas').getContext('2d');
      var imgdata = context.getImageData(x, y, 1, 1).data;
      if (y > 39) {
    	  drawGradient (imgdata[0], imgdata[1], imgdata[2]);
      }
      send(imgdata[0], imgdata[1], imgdata[2], 200);	  
  };
  
  $scope.down = function (x,y) {
	  console.log("Picked at " + x + "," + y);
      $scope.pressed = true;
      touchedAt(x,y);
  };
  
  $scope.move = function (x,y) {
	  if ($scope.pressed) {
		  console.log("Moving " + x + "," + y);
		  touchedAt(x,y);
	  }
  };
  
  $scope.up = function () {
	  $scope.pressed = false;
  };
  
}