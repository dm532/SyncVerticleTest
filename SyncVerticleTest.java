package com.sw.ff.platform.server.push.websockets.verticles.sync;
import static io.vertx.ext.sync.Sync.awaitResult;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;


import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.BufferFactory;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.rabbitmq.RabbitMQClient;

public class SyncVerticleTest extends SyncVerticle {

	BufferFactory factory;
	private static final Logger logger = LoggerFactory.getLogger(SyncVerticleTest.class);
	boolean post;
	RabbitMQClient rmqClient;
	public static AtomicLong getCount = new AtomicLong(0);
	public static AtomicLong postCount = new AtomicLong(0);
	int firedCount = 0;

	public SyncVerticleTest(boolean post) {
		factory = new io.vertx.core.buffer.impl.BufferFactoryImpl();
		this.post = post;
	}

	@Override
	@Suspendable
	public void start() throws InterruptedException, Exception {
		JsonObject config = new JsonObject();
		config.put("virtualHost", "testHost");
		rmqClient = RabbitMQClient.create(this.vertx, config);
		rmqClient.start(handle -> {});
		client = new RMQClient(vertx, post);
		if (post) {
			post();
		} else {
			get();
		}
	}

	@Suspendable
	public void get() {
		final long timerID = vertx.setPeriodic(1, new Handler<Long>() {
			int count = 0;;

			public void handle(Long timerID) {
				if (rmqClient == null || !rmqClient.isConnected()) {
					return;
				}
				try {
					basicGet();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Suspendable
	public void basicGet() throws InterruptedException, Exception {

		try {
			if (!rmqClient.isConnected()) {
				return;
			}

			JsonObject obj = awaitResult(h -> rmqClient.basicGet("wbsqtest", true, h));
			if ((getCount.incrementAndGet() % 10000) == 0) {
				logger.info(new Date().toLocaleString() + " : Get Count : " + getCount.get());
				logger.info("Post Count : " + obj.getString("body"));
			}
			return;
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Suspendable
	public void post() {
		final long timerID = vertx.setPeriodic((1), new Handler<Long>() {
			int count = 0;;
			public void handle(Long timerID) {
				if (!rmqClient.isConnected()) {
					return;
				}
				try {
					basicPost();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (++count == 1000000) {
					vertx.cancelTimer(timerID);
				}
			}
		});
	}

	@Suspendable
	public void basicPost() throws InterruptedException, Exception {

		try {
			if (!rmqClient.isConnected()) {
				return;
			}
			JsonObject message = new JsonObject().put("body", Long.toString(postCount.incrementAndGet()));
			rmqClient.basicPublish("", "wbsqtest", message, Sync.fiberHandler(pubResult -> {}));
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
