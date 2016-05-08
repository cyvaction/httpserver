package org.github.httpserver.utils;

import java.util.HashMap;
import java.util.Map;

public final class Singleton {
	private static Map<Class<?>, Object> pool = new HashMap<Class<?>, Object>();

	private Singleton() {
	}

	/**
	 * 获得指定类的单例对象<br>
	 * 对象存在于池中返回，否则创建，每次调用此方法获得的对象为同一个对象<br>
	 * 创建对象时调用其默认的无参构造方法，如果对象无此构造方法，会创建失败。
	 * 
	 * @param clazz
	 *            类
	 * @return 单例对象
	 */
	@SuppressWarnings("unchecked")
	public static synchronized <T> T get(Class<?> clazz) {
		T obj = (T) pool.get(clazz);

		if (null == obj) {
			try {
				obj = (T) clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			pool.put(clazz, obj);
		}

		return obj;
	}

	/**
	 * 获得指定类的单例对象<br>
	 * 对象存在于池中返回，否则创建，每次调用此方法获得的对象为同一个对象<br>
	 * 创建对象时调用其默认的无参构造方法，如果对象无此构造方法，会创建失败。
	 * 
	 * @param className
	 *            类名
	 * @return 单例对象
	 */
	public static <T> T get(String className) {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return get(clazz);
	}

	/**
	 * 移除指定Singleton对象
	 * 
	 * @param clazz
	 *            类
	 */
	public static synchronized void remove(Class<?> clazz) {
		pool.remove(clazz);
	}

	/**
	 * 清除所有Singleton对象
	 */
	public static synchronized void destroy() {
		pool.clear();
	}
}
