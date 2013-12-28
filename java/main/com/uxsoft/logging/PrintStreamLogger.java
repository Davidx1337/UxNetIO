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

import java.io.PrintStream;

/**
 *
 * @author David
 */
public class PrintStreamLogger extends SimpleLogger {

    private PrintStream out;

    public PrintStreamLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public void log(String msg) {
        out.println(msg);
    }
}
