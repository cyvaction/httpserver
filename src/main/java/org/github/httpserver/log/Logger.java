package org.github.httpserver.log;


public interface Logger {
	
	public void debug(String format, Object... arguments);
    public void debug(Throwable t);
    public void debug(Throwable t, String format, Object... arguments);
    

    public void info(String format, Object... arguments);
    public void info(Throwable t);
    public void info(Throwable t, String format, Object... arguments);
    
    public void warn(String format, Object... arguments);
    public void warn(Throwable t);
    public void warn(Throwable t, String format, Object... arguments);
    

    public void error(String format, Object... arguments);
    public void error(Throwable t);
    public void error(Throwable t, String format, Object... arguments);
}
