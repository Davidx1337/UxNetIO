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
public class SimpleLogger implements Logger {

    public void log(Throwable ex) {
        StringBuilder log = new StringBuilder();
        appendExceptionToBuffer(log, ex);
        log(log.toString());
    }

    public void log(String msg, Throwable ex) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        appendExceptionToBuffer(log, ex);
        log(log.toString());
    }

    public void log(String msg, Throwable ex, Object... args) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        formatString(log, args);
        appendExceptionToBuffer(log, ex);
        log(log.toString());
    }

    protected String logString(Throwable ex) {
        StringBuilder log = new StringBuilder();
        appendExceptionToBuffer(log, ex);
        return log.toString();
    }

    protected String logString(String msg, Throwable ex) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        appendExceptionToBuffer(log, ex);
        return log.toString();
    }

    protected String logString(String msg, Throwable ex, Object... args) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        formatString(log, args);
        appendExceptionToBuffer(log, ex);
        return log.toString();
    }

    protected String logString(String msg, Object... args) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        formatString(log, args);
        return log.toString();
    }

    protected String logString(String msg, String... args) {
        StringBuilder log = new StringBuilder(msg == null ? "" : msg);
        formatString(log, args);
        return log.toString();
    }

    protected void formatString(StringBuilder log, Object[] args) {
        for (Object arg : args) {
            int index = log.indexOf("{}");
            if (index != -1) {
                log.replace(index, index + 2, arg.toString());
            } else {
                break;
            }
        }
    }

    protected void appendExceptionToBuffer(StringBuilder log, Throwable ex) {
        StackTraceElement[] trace = ex.getStackTrace();
        log.append("\n");
        log.append(ex.getClass().getName());
        log.append(": ");
        log.append(ex.getMessage());
        for (StackTraceElement element : trace) {
            log.append("\n\tat ");
            log.append(element.getClassName());
            log.append(".");
            log.append(element.getMethodName());
            log.append("(");
            log.append(element.getFileName());
            log.append(":");
            log.append(element.getLineNumber());
            log.append(")");
            if (element.isNativeMethod()) {
                log.append(" (Native Method)");
            }
        }
    }

    /**
     * Override this method to cause it to be written to other places
     *
     * @param msg
     */
    public void log(String msg) {
        System.err.println(msg);
    }

    public void warn(Throwable ex) {
        warn(this.logString(ex));
    }

    public void warn(String msg, Throwable ex) {
        warn(this.logString(msg, ex));
    }

    public void warn(String msg, Throwable ex, Object... args) {
        warn(this.logString(msg, ex, args));
    }

    public void warn(String msg) {
        log("WARN: " + msg);
    }

    public void error(Throwable ex) {
        error(this.logString(ex));
    }

    public void error(String msg, Throwable ex) {
        error(this.logString(msg, ex));
    }

    public void error(String msg, Throwable ex, Object... args) {
        error(this.logString(msg, ex, args));
    }

    public void error(String msg) {
        log("ERROR: " + msg);
    }

    public void info(Throwable ex) {
        info(this.logString(ex));
    }

    public void info(String msg, Throwable ex) {
        info(this.logString(msg, ex));
    }

    public void info(String msg, Throwable ex, Object... args) {
        info(this.logString(msg, ex, args));
    }

    public void info(String msg) {
        log("INFO: " + msg);
    }

    public void log(String msg, Object... args) {
        log(this.logString(msg, args));
    }

    public void warn(String msg, Object... args) {
        warn(this.logString(msg, args));
    }

    public void error(String msg, Object... args) {
        error(this.logString(msg, args));
    }

    public void info(String msg, Object... args) {
        info(this.logString(msg, args));
    }

    public void log(String msg, String... args) {
        log(this.logString(msg, args));
    }

    public void warn(String msg, String... args) {
        warn(this.logString(msg, args));
    }

    public void error(String msg, String... args) {
        error(this.logString(msg, args));
    }

    public void info(String msg, String... args) {
        info(this.logString(msg, args));
    }
}
