/*
 * SimpleJApplet.java Copyright (C) 2006 Gerardo Horvilleur Martinez
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package com.simplej.vc.applet;

import com.simplej.vc.hardware.*;
import com.simplej.language.interpreter.*;
import com.simplej.language.vm.ExecutionContext;
import com.simplej.vc.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

public class SimpleJApplet extends JApplet {

    private VideoCanvas videoCanvas;

    private IAVC iavc;

    private Interpreter interpreter;

    private SimpleJAdapter simpleJAdapter;

    private ExecutionContext ctx;

    private Thread thread;

    private boolean started;

    public void init() {
        try {
            interpreter = new Interpreter();
            videoCanvas = new VideoCanvas(getWidth());
            getContentPane().add(videoCanvas);
            iavc = videoCanvas.getIAVC();
            String game = getParameter("game");
            final ClassLoader classLoader = getClass().getClassLoader();
            interpreter.setSourceFinder(new SourceFinder() {
                    public Reader getReader(ExecutionContext ctx,
                                            String filename) 
                        throws CantAccessFileException {
                        InputStream is = 
                            classLoader.getResourceAsStream(filename);
                        return new InputStreamReader(is);
                    }
                    
                    public InputStream getInputStream(ExecutionContext ctx,
                                                      String filename)
                        throws CantAccessFileException {
                        return classLoader.getResourceAsStream(filename);
                    }
                });
            simpleJAdapter =
                new SimpleJAdapter(iavc, interpreter, classLoader);
            ctx = new ExecutionContext();
            thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            interpreter.interpretFile(ctx,
                                                      "main.sj",
                                                      interpreter.getEnv());
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                        thread = null;
                    }
                });
            thread.start();
            videoCanvas.start();
            started = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (!started) {
            videoCanvas.unfreeze();
            ctx.unfreeze();
            started = true;
        }
    }

    public void stop() {
        if (started) {
            videoCanvas.freeze();
            ctx.freeze();
            started = false;
        }
    }

}

