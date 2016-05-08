package test;

import java.util.HashMap;
import java.util.Map;

import org.github.httpserver.http.HttpHandler;
import org.github.httpserver.http.HttpRequest;

public class TestHandler implements HttpHandler {

	@Override
	public Object handleRequest(HttpRequest req) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dddd", 1);
		return map;
	}

}
