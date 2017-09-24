package com.sw.ff.platform.server.push.websockets.verticles;


import java.util.Date;
import java.util.Map.Entry;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.spi.BufferFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;


public class PushServerVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(PushServerVerticle.class);

    private SockJSHandler sockJSHandler;
    
	@Override
	public void start() {


        try {
        		for(int i = 0; i < 5; i++) {
					vertx.deployVerticle(new SyncVerticleTest(false));
        		}
        		for(int i = 0; i < 5; i++) {
					vertx.deployVerticle(new SyncVerticleTest(true));
        		}
		} catch (Exception e1) {
			logger.error(e1);
			e1.printStackTrace();
			logger.info(e1);
		}
	}
}

