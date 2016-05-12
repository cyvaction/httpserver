package org.github.httpserver.http;

import java.util.Map;


public class MultipartHttpRequest extends HttpRequest {
	private static final long serialVersionUID = 1L;
	
    private Map<String, byte[]> files;

	public Map<String, byte[]> getFiles() {
		return files;
	}

	public void setFiles(Map<String, byte[]> files) {
		this.files = files;
	}

}
