package org.github.httpserver.log.impl;

import org.github.httpserver.log.Logger;
import org.github.httpserver.utils.StringUtils;
import org.slf4j.LoggerFactory;

public class Slf4jLogger implements Logger {

	private final org.slf4j.Logger logger;
	
	public Slf4jLogger(String name) {
		logger = LoggerFactory.getLogger(name);
	}
	
	public Slf4jLogger(Class<?> clazz) {
		logger = LoggerFactory.getLogger(clazz);
	}
	
	@Override
	public void debug(String format, Object... arguments) {
		logger.debug(format, arguments);
	}

	@Override
	public void debug(Throwable t) {
		logger.debug(StringUtils.EMPTY_STRING, t);
	}

	@Override
	public void debug(Throwable t, String format, Object... arguments) {
		logger.debug(StringUtils.format(format, arguments), t);
	}

	@Override
	public void info(String format, Object... arguments) {
		logger.info(format, arguments);
	}

	@Override
	public void info(Throwable t) {
		logger.info(StringUtils.EMPTY_STRING, t);
	}

	@Override
	public void info(Throwable t, String format, Object... arguments) {
		logger.info(StringUtils.format(format, arguments), t);
	}

	@Override
	public void warn(String format, Object... arguments) {
		logger.warn(format, arguments);
	}

	@Override
	public void warn(Throwable t) {
		logger.warn(StringUtils.EMPTY_STRING, t);
	}

	@Override
	public void warn(Throwable t, String format, Object... arguments) {
		logger.warn(StringUtils.format(format, arguments), t);
	}

	@Override
	public void error(String format, Object... arguments) {
		logger.error(format, arguments);
	}

	@Override
	public void error(Throwable t) {
		logger.error(StringUtils.EMPTY_STRING, t);
	}

	@Override
	public void error(Throwable t, String format, Object... arguments) {
		logger.error(StringUtils.format(format, arguments), t);
	}

}
