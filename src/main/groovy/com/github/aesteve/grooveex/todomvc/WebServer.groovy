package com.github.aesteve.grooveex.todomvc

import com.github.aesteve.grooveex.todomvc.model.Todo
import com.github.aesteve.vertx.groovy.io.impl.JacksonMarshaller
import io.vertx.core.Future
import io.vertx.groovy.core.http.HttpServer
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.BodyHandler
import io.vertx.groovy.ext.web.handler.CorsHandler
import io.vertx.lang.groovy.GroovyVerticle

import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import static io.vertx.core.http.HttpMethod.*

class WebServer extends GroovyVerticle {

	final static String HOST = 'localhost'
	final static int PORT = 9000

	HttpServer server
	int sequence = 0
	List todos = []


	@Override
	void start(Future<Void> future) {
		server = vertx.createHttpServer port:PORT, host:HOST
		server.requestHandler router.&accept
		server.listen future.completer()
	}

	@Override
	void stop(Future<Void> future) {
		if (!server) future++
		server.close future.completer()
	}

	private Router getRouter() {
		Router router = Router.router vertx
		CorsHandler cors = CorsHandler.create '*'
		cors.allowedMethods GET, POST, PUT, PATCH, DELETE, OPTIONS
		cors.allowedHeaders CONTENT_TYPE
		router.route() >> cors
		router.route().consumes 'application/json'
		router.route() >> BodyHandler.create()
		router.route() >> {
			marshallers['*/*'] = new JacksonMarshaller()
			it++
		}
		router.route().last() >> {
			if (marshaller) {
				if (payload != null) response << marshaller.marshall(payload)
				else {
					response.statusCode = 204
					response.end()
				}
			} else fail 406
		}

		router.get('/todos/:id') >> findTodoById
		router.patch('/todos/:id') >> findTodoById
		router.patch('/todos/:id') >> {
			Map updates = body as Map
			payload.update updates
			it++
		}
		router.delete('/todos/:id') >> findTodoById
		router.delete('/todos/:id') >> {
			todos.remove payload
			response.statusCode = 204
			response.end()
		}

		router.get('/') >> { yield todos }
		router.post('/') >> {
			Todo newTodo = body as Todo
			newTodo.id = ++sequence
			todos << newTodo
			response.statusCode = 201
			yield newTodo
		}
		router.delete('/') >> {
			todos.clear()
			it++
		}
		router
	}

	Closure findTodoById = { ctx ->
		int id
		try {
			id = params['id'] as Integer
		} catch(all) {
			fail 400
			return
		}
		yield todos.find { it.id == id }
	}

}
