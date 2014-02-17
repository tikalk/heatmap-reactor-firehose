import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Random;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;

import java.util.UUID;

public class CheckinsFirehose extends Verticle {
	private Logger log;
	private JsonObject config;
	private HttpClient client;

	@Override
	public void start() {
		log = container.logger();
		config = container.config();

		container.deployModule("io.vertx~mod-redis~1.1.4-SNAPSHOT", config, 1, new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> event) {
				if (event.failed()) {
					log.error("Failed connecting to Redis");
				} else {
					CheckinsFirehose.super.start();
					log.info("Connected to Redis");
					client = vertx.createHttpClient().setHost(config.getString("httpServerHost"))
							.setPort(config.getInteger("httpServerPort"));

					long timerID = vertx.setPeriodic(config.getInteger("httpIntervalToSend"), new Handler<Long>() {
						public void handle(Long timerID) {
							sendCheckinAddress();
						}
					});
				}
			}
		});
	}

	private void sendCheckinAddress() {
		int i = new Random().nextInt(config.getInteger("maxAddressSeqValue"));
		JsonObject redisCommand = new JsonObject("{\"command\": \"get\", \"args\": [\"ADDSEQ-" + i + "\"]}");
		
		vertx.eventBus().send(config.getString("address"), redisCommand, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> reply) {
				String value = reply.body().getString("value");
				if (value == null)
					return;
				
				String str = new java.util.Date().getTime() + "@" + value;
				log.info("Sending HTTP request with body: " + str);
				HttpClientRequest request = client.post("/", new Handler<HttpClientResponse>() {
					public void handle(HttpClientResponse resp) {
						log.info("HTTP status: " + resp.statusCode());
					}
				});
				
				request.putHeader("Content-Length", String.valueOf(str.length())).write(str).end();
				
			}
		});
	}

}