package test;

import org.github.httpserver.http.HttpMethod;
import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.http.HttpResponse;
import org.github.httpserver.http.annotation.HttpController;
import org.github.httpserver.http.annotation.HttpMapping;

@HttpController
public class TestController {

	@HttpMapping(path="/ddd/{d}", method=HttpMethod.POST)
	public Object test(HttpRequest req, HttpResponse resp) {
		return req;
	}
}
