package org.github.httpserver.log;

import org.github.httpserver.log.impl.SimpleLogger;
import org.github.httpserver.log.impl.Slf4jLogger;


public class LoggerFactory {
	
	private static LogType logType = LogType.SLF4J;
	
    public static Logger getLogger(String name) {
        if(logType == LogType.SLF4J) {
        	return new Slf4jLogger(name);
        } else {
        	return new SimpleLogger(name);
        }
    }
    
    public static Logger getLogger(Class<?> clazz) {
    	if(logType == LogType.SLF4J) {
        	return new Slf4jLogger(clazz);
        } else {
        	return new SimpleLogger(clazz);
        }
    }
    
    public static LogType getLogType() {
		return logType;
	}

	public static void setLogType(LogType logType) {
		LoggerFactory.logType = logType;
	}

	public static enum LogType {
        CONSOLE,
        SLF4J,
        LOGBACK
    }
}
