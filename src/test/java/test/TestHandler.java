package test;

import java.util.HashMap;
import java.util.Map;

import org.github.httpserver.http.HttpHandler;
import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.utils.JsonUtils;

public class TestHandler implements HttpHandler {

	@Override
	public Object handleRequest(HttpRequest req) {
		System.out.println(JsonUtils.toJsonString(req));
		System.out.println(req.getRemoteAddress());
		System.out.println(req.getLocalAddress());
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("dddd", req);
		return map;
	}

}
