package com.github.aesteve.grooveex.todomvc.model

import static com.github.aesteve.grooveex.todomvc.WebServer.*

class Todo {

	Integer id
	String title
	boolean completed
	Integer order

	String getUrl() {
		"http://$HOST:$PORT/todos/$id"
	}

	void update(Map updates) {
		updates.each { key, value ->
			this.setProperty key, value
		}
	}
}
