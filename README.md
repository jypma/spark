_Spark_ is an environment for home automation, consisting of a Java backend (the project you're looking at) combined with a Arduino-compatible USB adapter [spark-node](http://github.com/jypma/spark-node). The combination of these two gives the following feature set:

* Zone-based modular house configuration
* Java DSL to write rules for day/night dependencies, time calculations, playing alert sounds
* Phone-friendly AJAX web interface built in [angular](http://angularjs.org/)
* Integration with the [Visonic](www.visonic.com) wireless motion and door sensors (which are used by [Falck](www.falck.dk/)'s burglar alarms in Denmark)
* Full integration into the (relatively cheap) [FS20](http://www.conrad.com/ce/en/category/SHOP_AREA_17199/Conrad-FS20-Wireless-Control-Systems) radio control system's buttons and switches / dimmers
* Integration with [XBMC](http://xbmc.org/), to adjust lighting when you watch a movie
* Integration with [Cosm](https://cosm.com/) for temperature & humidity logging
* Receives battery, temperature and humidity data from the spark-roomsensor, spark-roomsensor-dht and spark-relay projects
* Receives battery and doorbell information from spark-doorbell
* Remote control of the [spark-rgb] RGB-LED based lamp

All of this combines into the following example use cases, which are easily expressed in Java:

* Lights go on when someone enters the room, but only when it is dark outside, and they turn off after half an hour of no movement.
* Main lights dim, decorative lights are lit when a movie is started. If the movie is paused, main lights are brightened a bit until the movie is resumed.
* A lamp for plants is turned on from 7am until 1 hour after sunrise, and from sunset to 22pm. But on weekends only from 9am :-).

