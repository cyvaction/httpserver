package org.github.httpserver.http;

public interface HttpHandler {

	public Object handleRequest(HttpRequest req);
}
