/*
 * Viewer.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez 
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

package com.simplej.vc.devkit.viewer;

import com.simplej.language.interpreter.*;
import com.simplej.language.vm.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

public class Viewer extends JPanel {

    private ViewerEnv viewerEnv;

    private ExecutionContext ctx;

    private SourceView sourceView;

    private JScrollPane sourceViewSP;

    private JViewport sourceViewVP;

    private MemoryView memoryView;

    private JScrollPane memoryViewSP;

    private JViewport memoryViewVP;

    private boolean started;

    private String sourceFilename;

    private boolean upToMode;

    private String upToFilename;

    private int upToLine;

    private JLabel filenameLabel;

    private JLabel lineLabel;

    public Viewer(final ViewerEnv viewerEnv) {
        this.viewerEnv = viewerEnv;
        setLayout(new BorderLayout());
        JPanel labelsPane = new JPanel();
        labelsPane.setLayout(new BorderLayout());
        filenameLabel = new JLabel("File:");
        labelsPane.add(filenameLabel, BorderLayout.WEST);
        lineLabel = new JLabel("Line:");
        labelsPane.add(lineLabel, BorderLayout.EAST);
        add(labelsPane, BorderLayout.NORTH);
        sourceView = new SourceView();
        sourceViewSP = new JScrollPane(sourceView);
        sourceViewVP = sourceViewSP.getViewport();
        sourceViewVP.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        memoryView = new MemoryView();
        memoryView.setFocusable(false);
        memoryViewSP = new JScrollPane(memoryView);
        memoryViewVP = memoryViewSP.getViewport();
        memoryViewVP.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        JSplitPane splitPane =
            new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                           sourceViewSP, memoryViewSP);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);
        splitPane.setDividerLocation(150);
        sourceView.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!viewerEnv.isSteppingEnabled())
                        return;
                    if (e.getClickCount() == 2) {
                        Point p = sourceViewVP.toViewCoordinates(e.getPoint());
                        int line = sourceView.pixelToLine(p.y);
                        runUpTo(sourceFilename, line);
                    }
                }
            });
        memoryView.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (!viewerEnv.isSteppingEnabled())
                        return;
                    if (e.getClickCount() == 2) {
                        viewerEnv.disableStepping();
                        Point p = memoryViewVP.toViewCoordinates(e.getPoint());
                        if (memoryView.toggleCollapsedAt(p))
                            displayEnv(ctx);
                    }
                }
            });
    }

    public void singleStep() {
        memoryView.reset();
        ctx.unfreeze();
    }

    public void runUpTo(String filename, int line) {
        viewerEnv.disableStepping();
        upToMode = true;
        upToFilename = filename;
        upToLine = line;
        sourceView.setUpToLine(line);
        memoryView.reset();
        ctx.unfreeze();
    }

    public void setup(Interpreter interpreter, ExecutionContext ctx) {
        this.ctx = ctx;
        upToMode = false;
        memoryView.setup(interpreter);
        memoryViewVP.setViewPosition(new Point(0, 0));
        // Set initial sizes (Windows 1.4.2 workaround!)
        sourceView.setPreferredSize(sourceViewVP.getSize());
        memoryView.setPreferredSize(memoryViewVP.getSize());
        ctx.addListener(new ExecutionContextListener() {
                public void sourceLineChanged(ExecutionContext ctx,
                                              int line) {
                    if (upToMode &&
                        (line != upToLine ||
                         !sourceFilename.equals(upToFilename)))
                        return;
                    if (sourceFilename != null &&
                        (sourceFilename.equals("_builtin_utils.sj") ||
                         sourceFilename.equals("_iavc_utils.sj")))
                        return;
                    upToMode = false;
                    gotoLine(line);
                    ctx.freeze();
                    displayEnv(ctx);
                }
                
                public void sourceLineChanged(ExecutionContext ctx,
                                              int line,
                                              String filename)
                    throws ExecutionException {
                    // Don't stop when returning from a builtin procedure
                    if (sourceFilename != null &&
                        (sourceFilename.equals("_builtin_utils.sj") ||
                         sourceFilename.equals("_iavc_utils.sj"))) {
                        sourceFilename = filename;
                        return;
                    }
                    sourceFilename = filename;
                    if (upToMode &&
                        (line != upToLine ||
                         !sourceFilename.equals(upToFilename)))
                        return;
                    if (sourceFilename != null &&
                        (sourceFilename.equals("_builtin_utils.sj") ||
                         sourceFilename.equals("_iavc_utils.sj")))
                        return;
                    upToMode = false;
                    loadSource(filename, ctx);
                    gotoLine(line);
                    if (line != 0) {
                        ctx.freeze();
                        displayEnv(ctx);
                    }
                }
                
                public void executionFinished(ExecutionContext ctx) {
                }
                
                public void willExecute(ExecutionContext ctx, Code code) {
                    if (code == Codes.RETURN) {
                        Object value = ctx.evalStackPeek(1);
                        if (value != Uninitialized.VALUE)
                            memoryView.setReturnedValue(value);
                    }
                }
                
                public void modifiedLocation(ExecutionContext ctx,
                                             Location location) {
                    memoryView.modified(location);
                }
                
                public void createdLocation(ExecutionContext ctx,
                                            Location location) {
                    memoryView.modified(location);
                    }
            });
    }

    private void loadSource(String filename, ExecutionContext ctx)
        throws ExecutionException {
        sourceView.setText(viewerEnv.getSourceText(filename, ctx));
        filenameLabel.setText("File: " + filename);
    }

    private void gotoLine(int line) {
        sourceView.selectLine(line);
        lineLabel.setText("Line: " + line);
    }

    private void displayEnv(final ExecutionContext ctx) {
        new Thread(new Runnable() {
                public void run() {
                    // Temporary (and ugly) workaround for bugs in graph
                    // layout code :(
                    try {
                        memoryView.updateEnv(ctx);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    memoryViewSP.invalidate();
                    memoryViewSP.repaint();
                    viewerEnv.enableStepping();
                }
            }).start();
    }

}
