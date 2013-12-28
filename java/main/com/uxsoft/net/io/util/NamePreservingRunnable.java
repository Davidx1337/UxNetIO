/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.util;

/**
 * A class for Runnable commands that can preserve it's name.
 *
 * @author David
 */
public class NamePreservingRunnable
        implements Runnable {

    private final String name;
    private final Runnable command;

    public NamePreservingRunnable(String name, Runnable command) {
        this.name = name;
        this.command = command;
    }

    public void run() {
        try {
            command.run();
        } catch (Throwable t) {
            ExceptionMonitor.Monitor.error("Error in Runnable [" + name + "]", t);
        }
    }
}
