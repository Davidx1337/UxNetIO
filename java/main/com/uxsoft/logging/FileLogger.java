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

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author David
 */
public class FileLogger extends SimpleLogger {

    private PrintStream out;

    public FileLogger(FileOutputStream out) {
        this.out = new PrintStream(out);
    }

    @Override
    public void log(String message) {
        out.println(message);
    }
}
