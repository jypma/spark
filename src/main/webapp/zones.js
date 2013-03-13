function ZonesCntl($scope, $http, $timeout) {
	if (window.location.hostname.indexOf("localhost") != -1) {
		$scope.zone = {"name":"Area","subZones":[{"name":"Inside","subZones":[{"name":"Day","subZones":[{"name":"Bryggers","subZones":[],"devices":[{"name":"Ceiling","on":true,"type":"Switch","timedOn":false},{"name":"Bryggers door","type":"DoorSensor"},{"battery":null,"type":"RoomSensor"}],"temperature":26.74,"lastAction":"moments ago"},{"name":"Entree","subZones":[],"devices":[{"name":"Guest bathroom","brightness":0,"on":false,"type":"Dimmer","timedOn":false},{"name":"Main door","type":"DoorSensor"}],"temperature":null,"lastAction":"unknown"},{"name":"Guestroom","subZones":[],"devices":[{"name":"Guestroom","lastMovement":1358261515283,"type":"VisonicMotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Office","subZones":[],"devices":[{"name":"Office","lastMovement":1358261708952,"type":"VisonicMotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Kitchen","subZones":[],"devices":[{"name":"Kitchen","lastMovement":1358261710285,"type":"VisonicMotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Studio","subZones":[],"devices":[{"name":"Studio","lastMovement":1358261579974,"type":"VisonicMotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Livingroom","subZones":[],"devices":[{"name":"Ceiling","brightness":8,"on":true,"type":"Dimmer","timedOn":false},{"name":"Table lamp","on":true,"type":"Switch","timedOn":false},{"name":"Reading lamp","on":false,"type":"Switch","timedOn":false},{"name":"Corner lamp","on":true,"type":"Switch","timedOn":false},{"name":"RGB Lamp","on":true,"type":"Switch","timedOn":true},{"name":"Living room","lastMovement":1358261713275,"type":"VisonicMotionSensor"},{"battery":3.21,"type":"RoomSensor"}],"temperature":22.18,"lastAction":"moments ago"}],"devices":[],"temperature":24.46,"lastAction":"moments ago"},{"name":"Night","subZones":[{"name":"Bedroom","subZones":[],"devices":[{"name":"Cupboards","brightness":0,"on":false,"type":"Dimmer","timedOn":false},{"name":"LED strip","on":false,"type":"Switch","timedOn":false},{"name":"Bedroom","lastMovement":null,"type":"VisonicMotionSensor"}],"temperature":null,"lastAction":"unknown"}],"devices":[],"temperature":null,"lastAction":"unknown"}],"devices":[],"temperature":24.46,"lastAction":"moments ago"},{"name":"Outside","subZones":[{"name":"Driveway, left","subZones":[],"devices":[{"name":"Driveway, left side","lastMovement":1358261632950,"type":"FS20MotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Driveway, right","subZones":[],"devices":[{"name":"Driveway, right side","lastMovement":1358261658654,"type":"FS20MotionSensor"}],"temperature":null,"lastAction":"moments ago"},{"name":"Carport","subZones":[],"devices":[{"name":"Spots","brightness":16,"on":true,"type":"Dimmer","timedOn":true},{"name":"Floodlight","on":true,"type":"Switch","timedOn":true},{"battery":3.64,"type":"Doorbell","lastRingPretty":"moments ago"},{"name":"Carport","lastMovement":1358261365923,"type":"FS20MotionSensor"}],"temperature":0.0,"lastAction":"moments ago"}],"devices":[],"temperature":0.0,"lastAction":"moments ago"}],"devices":[],"temperature":12.23,"lastAction":"moments ago"};		
	} else {
		var load = function() {
			$http.get('./rest/home/zone').success(function(data) {
				$scope.zone = data;
				$timeout(load, 3000);
			}).error(function(){
				$timeout(load, 6000);
			});	
		};
		load();		
	}
}

function ZoneCntl($scope) {
	var indent = $scope.indent;
	$scope.indent = (indent === undefined) ? 0 : indent + 1;
}