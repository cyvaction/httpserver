package org.github.httpserver.utils;

import java.util.List;

import com.alibaba.fastjson.JSON;

public class JsonUtils {

	/**
	 * Bean convert to JSON String
	 * @param Bean
	 * @return JSON String
	 */
	public static String toJsonString(Object bean) {
		return JSON.toJSONString(bean);
	}
	
	/**
	 * JSON String convert to Bean
	 * @param json String
	 * @return T
	 */
	public static <T> T toBean(String json, Class<T> clazz) {
		return JSON.parseObject(json, clazz);
	}
	
	/**
	 * JSON String convert to Bean
	 * @param json String
	 * @return T
	 */
	public static <T> List<T> toList(String json, Class<T> clazz) {
		return JSON.parseArray(json, clazz);
	}
}
