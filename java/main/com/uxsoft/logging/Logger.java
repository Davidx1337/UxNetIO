/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * This file is part of the "Renoria" Game.
 * Copyright (C) 2009
 * IDGames.
 */

package com.uxsoft.logging;

/**
 *
 * @author David
 */
public interface Logger {
	public void log (Throwable ex);
	public void log (String msg, Throwable ex);
	public void log (String msg, Throwable ex, Object ... args);
	public void log (String msg, Object... args);
	public void log (String msg);
	public void warn (Throwable ex);
	public void warn (String msg, Throwable ex);
	public void warn (String msg, Throwable ex, Object ... args);
	public void warn (String msg);
	public void warn (String msg, Object... args);
	public void error (Throwable ex);
	public void error (String msg, Throwable ex);
	public void error (String msg, Throwable ex, Object ... args);
	public void error (String msg);
	public void error (String msg, Object... args);
	public void info (Throwable ex);
	public void info (String msg, Throwable ex);
	public void info (String msg, Throwable ex, Object ... args);
	public void info (String msg);
	public void info (String msg, Object... args);
	public void log (String msg, String... args);
	public void warn (String msg, String... args);
	public void error (String msg, String... args);
	public void info (String msg, String... args);
}
