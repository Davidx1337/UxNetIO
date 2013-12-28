/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uxsoft.net.io.util;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author David
 */
public class NewThreadExecutor
        implements Executor {

    private final AtomicInteger counter = new AtomicInteger(0);

    public void execute(Runnable command) {
        new Thread(command, "NewThreadExecutor-" + counter.getAndIncrement()).start();
    }
}
