import com.github.aesteve.grooveex.todomvc.model.Todo
import com.github.aesteve.vertx.groovy.io.impl.JacksonMarshaller

router {
	marshaller '*/*', new JacksonMarshaller()
	cors('*') {
		methods GET, POST, PUT, PATCH, DELETE, OPTIONS
		headers CONTENT_TYPE
	}
	route('/') {
		get {
			yield todos
		}
		post {
			Todo newTodo = body as Todo
			newTodo.id = UUID.randomUUID()
			todos << newTodo
			response.created
			yield newTodo
		}
		delete {
			todos.clear()
			response.noContent()
		}
	}
	route('/todos/:id') {
		check {
			todos.find { it.id == request.params['id'] }
		} | 404
		get { yield payload }
		put | patch {
			payload.update(body as Map)
			yield payload
		}
		delete {
			todos.remove payload
			response.noContent
		}
	}
}