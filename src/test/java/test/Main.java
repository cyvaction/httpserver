package test;

import org.github.httpserver.http.HttpMethod;
import org.github.httpserver.http.server.HttpConfig;
import org.github.httpserver.http.server.HttpDispatcher;
import org.github.httpserver.http.server.HttpServer;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.log.LoggerFactory.LogType;
import org.github.httpserver.utils.Singleton;

public class Main {

	public static void main(String[] args) {
		LoggerFactory.setLogType(LogType.CONSOLE);
		HttpConfig config = new HttpConfig();
		config.setPort(8989);
		HttpServer server = new HttpServer(config);
		HttpDispatcher dispatcher = Singleton.get(HttpDispatcher.class);
		dispatcher.registerHandler(HttpMethod.GET, "/ddd/{aaa}", new TestHandler());
		server.start();
	}

}
