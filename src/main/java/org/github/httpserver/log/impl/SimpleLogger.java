package org.github.httpserver.log.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.github.httpserver.log.LogLevel;
import org.github.httpserver.log.Logger;
import org.github.httpserver.utils.ExceptionUtils;
import org.github.httpserver.utils.StringUtils;

public class SimpleLogger implements Logger {

	private String name;

	public SimpleLogger(String name) {
		this.name = name;
	}

	public SimpleLogger(Class<?> clazz) {
		this.name = clazz.getName();
	}

	@Override
	public void debug(String format, Object... arguments) {
		String s = format(name, LogLevel.DEBUG, StringUtils.format(format, arguments));
		System.out.println(s);
	}

	@Override
	public void debug(Throwable t) {
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void debug(Throwable t, String format, Object... arguments) {
		String s = format(name, LogLevel.DEBUG, StringUtils.format(format, arguments));
		System.out.println(s);
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void info(String format, Object... arguments) {
		String s = format(name, LogLevel.INFO, StringUtils.format(format, arguments));
		System.out.println(s);
	}

	@Override
	public void info(Throwable t) {
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void info(Throwable t, String format, Object... arguments) {
		String s = format(name, LogLevel.INFO, StringUtils.format(format, arguments));
		System.out.println(s);
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void warn(String format, Object... arguments) {
		String s = format(name, LogLevel.WARN, StringUtils.format(format, arguments));
		System.out.println(s);
	}

	@Override
	public void warn(Throwable t) {
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void warn(Throwable t, String format, Object... arguments) {
		String s = format(name, LogLevel.WARN, StringUtils.format(format, arguments));
		System.out.println(s);
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void error(String format, Object... arguments) {
		String s = format(name, LogLevel.ERROR, StringUtils.format(format, arguments));
		System.out.println(s);
	}

	@Override
	public void error(Throwable t) {
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	@Override
	public void error(Throwable t, String format, Object... arguments) {
		String s = format(name, LogLevel.ERROR, StringUtils.format(format, arguments));
		System.out.println(s);
		System.out.println(ExceptionUtils.getStackTrace(t));
	}

	private String format(String name, LogLevel level, String log) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String s = df.format(new Date()) + " [" + level + "] " + name + " - " + log;
		return s;
	}
}
