package com.sw.ff.platform.server.push.websockets.verticles;


import java.util.Date;
import java.util.Set;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.spi.BufferFactory;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
//import io.vertx.ext.web.handler.sockjs.BridgeEvent;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import com.sw.ff.platform.server.push.websockets.oms.handler.WebsocketServerBridgeEventHandler;


public class PushServerVerticle extends SyncVerticle {

    private static final Logger logger = LoggerFactory.getLogger(PushServerVerticle.class);

    private SockJSHandler sockJSHandler;
    
    
    public PushServerVerticle() {
    		
    }
    
	@Override
	@Suspendable
	public void start() {

		
		BridgeOptions options = new BridgeOptions().addOutboundPermitted(
				new PermittedOptions().setAddressRegex(""+ "auction\\.[0-9]+"));
                sockJSHandler = SockJSHandler.create(vertx).bridge(options, new BridgeEventHandler(vertx));
		vertx.deployVerticle(new BasicVerticle(), new DeploymentOptions().setWorker(true));
		Router router = Router.router(vertx);
		router.route("/eventbus/*").handler(sockJSHandler);
		router.route().failureHandler(errorHandler());
		router.route().handler(staticHandler());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		try {
			for (int i = 0; i < 5; i++) {
				vertx.deployVerticle(new SyncVerticleTest(false));
			}
			for (int i = 0; i < 5; i++) {
				vertx.deployVerticle(new SyncVerticleTest(true));
			}
		} catch (Exception e1) {
			logger.error(e1);
		}	
	}

    private ErrorHandler errorHandler() {
        return ErrorHandler.create(true);
    }

    private StaticHandler staticHandler() {
        return StaticHandler.create()
            .setCachingEnabled(false);
    }
}
