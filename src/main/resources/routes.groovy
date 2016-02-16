import com.github.aesteve.grooveex.todomvc.model.Todo
import com.github.aesteve.vertx.groovy.io.impl.JacksonMarshaller

router {
	marshaller '*/*', new JacksonMarshaller()
	route('/') {
		get { yield todos }
		post {
			Todo newTodo = body as Todo
			newTodo.id = ++sequence
			todos << newTodo
			response.statusCode = 201
			yield newTodo
		}
		delete {
			todos.clear()
			response.noContent
		}
	}
	route('/todos/:id') {
		expect { Integer.parseInt params['id'] }
		check { todos.find { it.id == payload } } | 404
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