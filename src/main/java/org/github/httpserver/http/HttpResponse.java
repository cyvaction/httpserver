package org.github.httpserver.http;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class HttpResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private HttpStatus statusCode;
    private byte[] body;
    private String contentType;
    private Map<String, List<String>> headers;
    
    public HttpResponse() {
    	this(HttpStatus.OK);
    }
    public HttpResponse(HttpStatus status) {
    	this(status, new byte[0]);
    }
    public HttpResponse(HttpStatus status, byte[] body) {
    	this(status, Consts.ContentTypes.TEXT_UTF_8, body);
    }
    public HttpResponse(HttpStatus status, String contentType, byte[] body) {
    	this.statusCode = status;
    	this.contentType = contentType;
    	this.body = body;
    }
    
	public HttpStatus getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}
	public byte[] getBody() {
		return body;
	}
	public void setBody(byte[] body) {
		this.body = body;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public Map<String, List<String>> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

}
