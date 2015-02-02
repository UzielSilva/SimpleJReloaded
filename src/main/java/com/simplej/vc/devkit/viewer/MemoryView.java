/*
 * MemoryView.java
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
import java.util.*;
import javax.swing.*;

public class MemoryView extends JComponent implements Scrollable {

    private final static int AUTO_COLLAPSE = 10;

    private ExecutionContext ctx;

    private Environment builtinEnv;

    private Dimensions dimensions;

    private boolean initialized;

    private java.util.List frames = new ArrayList();

    private Set visibles;

    private Set modifiedLocations = new HashSet();

    private Object returnedValue;

    private boolean hasReturnedValue;

    private PlaceAndRoute par;

    private Map valuesToBoxes;

    private int scopeNumber;

    private Set allCollapsed = new HashSet();

    private Set seenArrays = new HashSet();

    public void setup(Interpreter interpreter) {
        builtinEnv = interpreter.getBuiltinEnv();
        allCollapsed = new HashSet();
        reset();
    }

    public void initialize(Graphics g) {
        dimensions = new Dimensions(g);
        par = new PlaceAndRoute(dimensions);
        initialized = true;
    }

    public void reset() {
        modifiedLocations = new HashSet();
        returnedValue = null;
        hasReturnedValue = false;
    }

    public void modified(Location loc) {
        modifiedLocations.add(loc);
    }

    public void setReturnedValue(Object value) {
        returnedValue = value;
        hasReturnedValue = true;
    }

    public void paint(Graphics g) {
        if (!initialized) {
            initialize(g);
            return;
        }
        g.setColor(Color.WHITE);
        Rectangle clip = g.getClipBounds();
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        par.paint(g, clip);
    }

    private synchronized void placeAndRoute() {
        par.init();
        visibles = new HashSet();
        valuesToBoxes = new HashMap();
        ExecutionFrame[] execStack = ctx.getFrames();
        if (execStack.length == 0)
            return;
        Environment env = execStack[execStack.length - 1].getEnvironment();
        while (env != null) {
            visibles.add(env);
            env = env.getParent();
        }
        frames = new ArrayList();
        for (int i = 0; i < execStack.length; i++) {
            env = execStack[i].getEnvironment();
            Stack stack = new Stack();
            while (env != null) {
                if (!frames.contains(env)) {
                    stack.add(env);
                    env = env.getParent();
                } else
                    env = null;
            }
            while (!stack.isEmpty()) {
                env = (Environment) stack.pop();
                // if (env != builtinEnv)
                if (env.getName() != null ||
                    env.getLocalLocationNames().size() > 0)
                    frames.add(env);
            }
        }
        Collections.reverse(frames);
        ReturnedBox returnedBox = null;
        if (hasReturnedValue) {
            returnedBox = new ReturnedBox(dimensions);
            par.addMainBox(returnedBox);
        }
        for (int i = 0; i < frames.size(); i++) {
            env = (Environment) frames.get(i);
            createMainBox(env);
        }
        if (returnedBox != null) {
            if (returnedValue instanceof Environment ||
                returnedValue instanceof Location[]) {
                Port port = returnedBox.addPort();
                par.addLink(port, createBox(returnedValue));
            } else
                returnedBox.setValue(returnedValue);
            returnedBox.calcSize();
        }
        Set hidden = new HashSet();
        scopeNumber = 1;
        for (int i = 0; i < frames.size(); i++) {
            env = (Environment) frames.get(i);
            initMainBox(env, hidden);
        }
        par.placeAndRoute();
        int w = par.getWidth();
        int h = par.getHeight();
        w = Math.max(w, getParent().getSize().width);
        h = Math.max(h, getParent().getSize().height);
        Dimension dim = new Dimension(w, h);
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(dim);
    }

    private void createMainBox(Environment env) {
        Box box;
        if (visibles.contains(env))
            box = new EnvironmentBox(dimensions, Dimensions.TYPE_VISIBLE, env);
        else
            box = new EnvironmentBox(dimensions, Dimensions.TYPE_HIDDEN, env);
        par.addMainBox(box);
        valuesToBoxes.put(env, box);
    }

    private void initMainBox(Environment env, Set hidden) {
        try {
            EnvironmentBox box = (EnvironmentBox) valuesToBoxes.get(env);
            boolean collapsed = allCollapsed.contains(env);
            if (collapsed)
                box.setCollapsed(true);
            String envName = env.getName();
            int boxType = box.getType();
            if (boxType == Dimensions.TYPE_HIDDEN) {
                if (envName != null)
                    box.setName(envName);
            } else {
                if (envName != null)
                    box.setName(Integer.toString(scopeNumber++) + ". " +
                                envName);
                else
                    box.setName(Integer.toString(scopeNumber++) + ". ");
            }
            java.util.List names = env.getLocalLocationNames();
            Iterator iter = names.iterator();
            while (iter.hasNext()) {
                String name = (String) iter.next();
                Location loc = env.getLocation(name, ctx);
                boolean isFinal = (loc instanceof FinalLocation);
                Object val = loc.getValue();
                int type = box.getType();
                if (modifiedLocations.contains(loc))
                    type = Dimensions.TYPE_MODIFIED;
                else if (hidden.contains(name))
                    type = Dimensions.TYPE_HIDDEN;
                if (val instanceof Environment ||
                    val instanceof Location[]) {
                    Port port = box.addNamePort(name, type, isFinal);
                    if (!collapsed)
                        par.addLink(port, createBox(val));
                } else
                    box.addNameValue(name, val, type, isFinal);
                if (box.getType() == Dimensions.TYPE_VISIBLE)
                    hidden.add(name);
            }
            box.calcSize();
        } catch (Exception e) {
            // Shouldn't happen
            e.printStackTrace();
        }
    }

    private Box createBox(Object value) {
        if (valuesToBoxes.containsKey(value))
            return (Box) valuesToBoxes.get(value);
        try {
            if (value instanceof Environment) {
                Environment env = (Environment) value;
                EnvironmentBox box =
                    new EnvironmentBox(dimensions, Dimensions.TYPE_VISIBLE,
                                       env);
                boolean collapsed = allCollapsed.contains(env);
                if (collapsed)
                    box.setCollapsed(true);
                String envName = env.getName();
                if (envName != null)
                    box.setName(envName);
                java.util.List names = env.getLocalLocationNames();
                Iterator iter = names.iterator();
                while (iter.hasNext()) {
                    String name = (String) iter.next();
                    Location loc = env.getLocation(name, ctx);
                    boolean isFinal = (loc instanceof FinalLocation);
                    Object val = loc.getValue();
                    int type = Dimensions.TYPE_VISIBLE;
                    if (modifiedLocations.contains(loc))
                        type = Dimensions.TYPE_MODIFIED;
                    if (val instanceof Environment ||
                        val instanceof Location[]) {
                        Port port = box.addNamePort(name, type, isFinal);
                        if (!collapsed)
                            par.addLink(port, createBox(val));
                    } else
                        box.addNameValue(name, val, type, isFinal);
                }
                valuesToBoxes.put(value, box);
                box.calcSize();
                return box;
            }
            if (value instanceof Location[]) {
                Location[] array = (Location[]) value;
                ArrayBox box = new ArrayBox(dimensions,
                                            Dimensions.TYPE_VISIBLE, array);
                if (array.length > AUTO_COLLAPSE &&
                    !seenArrays.contains(array)) {
                    seenArrays.add(array);
                    allCollapsed.add(array);
                }
                boolean collapsed = allCollapsed.contains(array);
                if (collapsed)
                    box.setCollapsed(true);
                for (int i = 0; i < array.length; i++) {
                    int type = Dimensions.TYPE_VISIBLE;
                    if (modifiedLocations.contains(array[i]))
                        type = Dimensions.TYPE_MODIFIED;
                    Object val = array[i].getValue();
                    if (val instanceof Environment ||
                        val instanceof Location[]) {
                        Port port = box.addPort(type);
                        if (!collapsed)
                            par.addLink(port, createBox(val));
                    } else
                        box.addValue(val, type);
                }
                valuesToBoxes.put(value, box);
                box.calcSize();
                return box;
            }
        } catch (Exception e) {
            // Shouldn't happen
            e.printStackTrace();
        }
        throw new RuntimeException("Unsuported value type: " + value);
    }

    public boolean toggleCollapsedAt(Point p) {
        Box box = par.getBoxAt(p);
        if (box == null)
            return false;
        Object object = box.getObject();
        if (box.isCollapsed())
            allCollapsed.remove(object);
        else
            allCollapsed.add(object);
        return true;
    }

    public void updateEnv(ExecutionContext ctx) {
        this.ctx = ctx;
        if (!initialized)
            return;
        placeAndRoute();
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.VERTICAL)
            return visibleRect.height;
        else
            return visibleRect.width;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        if (dimensions != null && orientation == SwingConstants.VERTICAL)
            return dimensions.getNamesFM().getHeight();
        else
            return 10;
    }

}