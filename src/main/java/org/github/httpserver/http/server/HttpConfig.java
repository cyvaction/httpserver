package org.github.httpserver.http.server;

public class HttpConfig {

	/**
	 * 服务器端口
	 */
	private int port;
	/**
	 * url后缀名，默认: 空, do, action, json; 多个可用逗号隔开
	 */
	private String urlExt;
	
	private int bossThreads;
	
	private int workerThreads;
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUrlExt() {
		return urlExt;
	}

	public void setUrlExt(String urlExt) {
		this.urlExt = urlExt;
	}

	public int getBossThreads() {
		return bossThreads;
	}

	public void setBossThreads(int bossThreads) {
		this.bossThreads = bossThreads;
	}

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

}
