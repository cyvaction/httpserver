package org.github.httpserver.http.server;

import java.lang.reflect.Method;

import org.github.httpserver.http.HttpRequest;
import org.github.httpserver.http.HttpResponse;
import org.github.httpserver.utils.ClassUtils;

public class HttpHandler {
	
	private Object target;
	private Method method;

	public Object invoke(HttpRequest request, HttpResponse response) {
		Class<?>[] params = method.getParameterTypes();
		Object[] args = new Object[params.length];
		for(int i=0; i < params.length; i++) {
			Class<?> param = params[i];
			if(param == HttpRequest.class) {
				args[i] = request;
			} 
			if(param == HttpResponse.class) {
				args[i] = response;
			}
		}
		Object ret = ClassUtils.invoke(target, method, args);
		return ret;
	}
	
	public HttpHandler(Object obj, Method method) {
		this.target = obj;
		this.method = method;
	}
	
	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

}
