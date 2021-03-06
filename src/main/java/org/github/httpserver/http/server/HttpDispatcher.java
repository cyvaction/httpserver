package org.github.httpserver.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.github.httpserver.http.Consts;
import org.github.httpserver.http.HttpMethod;
import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.http.HttpResponse;
import org.github.httpserver.http.HttpStatus;
import org.github.httpserver.http.annotation.HttpController;
import org.github.httpserver.http.annotation.HttpMapping;
import org.github.httpserver.utils.ClassUtils;
import org.github.httpserver.utils.IoUtils;
import org.github.httpserver.utils.JsonUtils;
import org.github.httpserver.utils.StringUtils;
import org.github.httpserver.utils.Tuple;

public class HttpDispatcher {

	private static final String DEFAULT_ROOT_PATH = "/webapp";

	// Map<method, Map<path, handler>>
	private Map<HttpMethod, Map<String, HttpHandler>> handlers = new ConcurrentHashMap<HttpMethod, Map<String, HttpHandler>>();
	
	private Map<String, String> defaultMimeTypes = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("txt", "text/plain");
			put("css", "text/css");
			put("csv", "text/csv");
			put("htm", "text/html");
			put("html", "text/html");
			put("xml", "text/xml");
			put("js", "text/javascript"); // Technically it should be
											// application/javascript (RFC
											// 4329), but IE8 struggles with
											// that
			put("xhtml", "application/xhtml+xml");
			put("json", "application/json");
			put("pdf", "application/pdf");
			put("zip", "application/zip");
			put("tar", "application/x-tar");
			put("gif", "image/gif");
			put("jpeg", "image/jpeg");
			put("jpg", "image/jpeg");
			put("tiff", "image/tiff");
			put("tif", "image/tiff");
			put("png", "image/png");
			put("svg", "image/svg+xml");
			put("ico", "image/x-icon");
			put("mp3", "audio/mpeg");
		}
	};
	
	private Set<String> urlExts = new HashSet<String>() {
		private static final long serialVersionUID = 1L;
	{
		add("json");
		add("do");
		add("action");
	}};
	
	public HttpDispatcher() {
		scanAndRegisterHandlers();
	}
	
	public void dispatchRequest(HttpRequest request, HttpResponse response) throws IOException {
		String path = request.getPath();
		if ("/".equals(path)) {
			path = "/index.html";
		}
		String ext = getPathExtension(path);
		if (StringUtils.isBlank(ext) || urlExts.contains(ext)) {
			Map<String, HttpHandler> handlers = getHandlers(request.getMethod());
			
			String pathWithoutExt = removePathExtension(path);
			Set<String> mappingPaths = handlers.keySet();
			Tuple<String, Map<String, List<String>>> result = RestMatcher.match(pathWithoutExt, mappingPaths);
			String mappedPath = result.v1();
			if (StringUtils.isBlank(mappedPath)) {
				response.setStatusCode(HttpStatus.BAD_REQUEST);
				response.setBody(("No handler found for uri [" + request.getUri() + "] and method ["
						+ request.getMethod() + "]").getBytes(Consts.UTF_8));
				response.setContentType(Consts.ContentTypes.TEXT_UTF_8);
				return;
			} else {
				request.getParameters().putAll(result.v2());
			}
			
			HttpHandler handler = handlers.get(mappedPath);
			Object bodyObj = handler.invoke(request, response);
			if(bodyObj != null) {
				response.setBody(JsonUtils.toJsonString(bodyObj).getBytes(Consts.UTF_8));
			}
			response.setContentType(Consts.ContentTypes.JSON_UTF_8);
		} else {
			InputStream in = HttpDispatcher.class.getResourceAsStream(DEFAULT_ROOT_PATH + path);
			if (in == null) {
				if("/index.html".equals(path)) {
					in = HttpDispatcher.class.getResourceAsStream(path);
				} 
				if(in == null) {
					response.setStatusCode(HttpStatus.NOT_FOUND);
					return;
				}
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			IoUtils.copy(in, out);
			response.setContentType(guessMimeType(path));
			response.setBody(out.toByteArray());
			if (in != null) {
				in.close();
			}
		}
	}

	public void registerHandler(HttpMethod method, String path, HttpHandler handler) {
		if(method == null || path == null) {
			throw new IllegalArgumentException("Argument cannot be null. method [" + method + "], path [" + path + "]");
		}
		path = RestMatcher.trimSlash(path);
		Map<String, HttpHandler> h = getHandlers(method);
		if (h.containsKey(path)) {
			throw new IllegalArgumentException("Duplicate path mapping. method [" + method + "], path [" + path + "]");
		}

		putHandler(method, path, handler);
	}
	
	public void addUrlExt(String ext) {
		urlExts.add(ext);
	}
	
	private Map<String, HttpHandler> getHandlers(HttpMethod method) {
		Map<String, HttpHandler> h = handlers.get(method);
		if(h == null) {
			h = new ConcurrentHashMap<String, HttpHandler>();
			handlers.put(method, h);
		}
		return h;
	}
	
	private void putHandler(HttpMethod method, String path, HttpHandler handler) {
		Map<String, HttpHandler> h = handlers.get(method);
		if(h == null) {
			h = new ConcurrentHashMap<String, HttpHandler>();
			handlers.put(method, h);
		}
		h.put(path, handler);
	}
	
	private String removePathExtension(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot == -1) {
			return path;
		}
		return path.substring(0, lastDot);
	}

	private static String getPathExtension(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		}
		String extension = path.substring(lastDot + 1).toLowerCase(Locale.ROOT);
		return extension;
	}

	private String guessMimeType(String path) {
		int lastDot = path.lastIndexOf('.');
		if (lastDot == -1) {
			return "";
		}
		String extension = path.substring(lastDot + 1).toLowerCase(Locale.ROOT);
		String mimeType = defaultMimeTypes.get(extension);
		if (mimeType == null) {
			return "";
		}
		return mimeType;
	}
	
	private void scanAndRegisterHandlers() {
		Set<Class<?>> clazzes = ClassUtils.scanPackageByAnnotation(HttpController.class);
		for(Class<?> clazz: clazzes) {
			Method[] methods = clazz.getMethods();
			for(Method method : methods) {
				HttpMapping httpMapping = method.getAnnotation(HttpMapping.class);
				if(httpMapping == null) {
					continue;
				}
				HttpMethod[] ms = httpMapping.method();
				String[] paths = httpMapping.path();
				for(HttpMethod m : ms) {
					for(String path : paths) {
						Object obj = ClassUtils.newInstance(clazz);
						HttpHandler handler = new HttpHandler(obj, method);
						registerHandler(m, path, handler);
					}
				}
			}
		}
	}
}
 