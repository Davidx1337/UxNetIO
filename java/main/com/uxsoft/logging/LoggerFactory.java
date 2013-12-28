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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 *
 * @author David
 */
public class LoggerFactory {

    private LoggerFactory() {
    }

    public static Logger getFileLogger(FileOutputStream out) {
        return new FileLogger(out);
    }

    public static Logger getFileLogger(File out)
            throws FileNotFoundException {
        return new FileLogger(new FileOutputStream(out));
    }

    public static Logger getErrLogger() {
        return new SimpleLogger();
    }

    public static Logger getPrintStreamLogger(PrintStream out) {
        return new PrintStreamLogger(out);
    }
}
