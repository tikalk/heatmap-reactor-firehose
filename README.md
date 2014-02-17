heatmap-reactor-firehose
========================
This project incudes both the reactor and firehose parts of the HeatMap demo


To run the Reactor you need to run both the HttpServer verticle and the mod-redis in a cluster mode as follow:

vertx run checkinsHttpServer.js -conf checkins-reactor.json  -cluster -cluster-host 127.0.0.1

vertx runmod com.zanox.vertx~mod-kafka~1.0.2 -conf checkins-reactor.json  -cluster -cluster-host 120.0.1


To run the firehose verticle you need to run the following command:
vertx run CheckinsFirehose.java -conf checkins-firehose.json

