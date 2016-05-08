package org.github.httpserver.http;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HttpRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private HttpMethod method;
    private String uri;
    private String path;
    private String body;
    
    private Map<String, List<String>> parameters;
	private Map<String, List<String>> headers;
	private Set<Cookie> cookies;
	
	private SocketAddress localAddress;
	private SocketAddress remoteAddress;
	
	public HttpRequest() {
		
	}
	
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public SocketAddress getLocalAddress() {
		return localAddress;
	}
	public void setLocalAddress(SocketAddress localAddress) {
		this.localAddress = localAddress;
	}
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}
	public Map<String, List<String>> getParameters() {
		return parameters;
	}
	public void setParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
	}
	public Set<Cookie> getCookies() {
		return cookies;
	}
	public void setCookies(Set<Cookie> cookies) {
		this.cookies = cookies;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
}
