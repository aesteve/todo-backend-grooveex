package com.github.aesteve.grooveex.todomvc

import com.github.aesteve.vertx.groovy.builder.RouterBuilder
import io.vertx.core.Future
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.http.HttpServer
import io.vertx.groovy.ext.web.Router
import io.vertx.lang.groovy.GroovyVerticle

class WebServer extends GroovyVerticle {

	final static String HOST = 'localhost'
	final static int PORT = 9000

	HttpServer server
	List todos = []


	@Override
	void start(Future<Void> future) {
		server = vertx.createHttpServer port:PORT, host:HOST
		Binding bindings = new Binding()
		bindings.setVariable 'todos', todos
		Router router = RouterBuilder.buildRouter(bindings, vertx, new File('src/main/resources/routes.groovy'))
		server.requestHandler router.&accept
		server.listen future.completer()
	}

	@Override
	void stop(Future<Void> future) {
		if (!server) future++
		server.close future.completer()
	}

	static void main(String... args) { // for debugging purpose only
		Vertx.vertx.deployVerticle("groovy:${WebServer.class.name}")
	}

}
