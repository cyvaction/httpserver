package test;

import org.github.httpserver.http.server.HttpConfig;
import org.github.httpserver.http.server.HttpServer;
import org.github.httpserver.log.LoggerFactory;
import org.github.httpserver.log.LoggerFactory.LogType;

public class Main {
	public static void main(String[] args) {
		LoggerFactory.setLogType(LogType.CONSOLE);
		HttpConfig config = new HttpConfig();
		config.setPort(8989);
		HttpServer server = new HttpServer(config);
		server.start();
	}

}
