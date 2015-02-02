/*
 * Box.java
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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

public abstract class Box {

    protected Dimensions dimensions;

    protected int x;

    protected int y;

    protected int width;

    protected int height;

    protected List ports = new ArrayList();

    protected boolean collapsed;

    public Box(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean v) {
        collapsed = v;
    }

    public Object getObject() {
        return null;
    }

    public int getUnit() {
        return dimensions.getUnit();
    }

    public boolean intersects(Rectangle r) {
        return !(x * getUnit() + width * getUnit() < r.x ||
                 y * getUnit() + height * getUnit() < r.y ||
                 r.x + r.width < x * getUnit() ||
                 r.y + r.height < y * getUnit());
    }

    public boolean contains(Point p) {
        return p.x >= x * getUnit() &&
            p.x < x * getUnit() + width * getUnit() &&
            p.y >= y * getUnit() &&
            p.y < y * getUnit() + height * getUnit();
    }

    public abstract void paint(Graphics g, Rectangle clip);

    public abstract void calcSize();

    public void scale(int f) {
        x *= f;
        y *= f;
        width *= f;
        height *= f;
        for (int i = 0; i < getPortCount(); i++)
            getPort(i).scale(f);
    }

    public void placeAt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void incrX(int incr) {
        x += incr;
    }

    public void incrY(int incr) {
        y += incr;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPortCount() {
        return ports.size();
    }

    public Port getPort(int index) {
        return (Port) ports.get(index);
    }

    public Port getPortAt(int position) {
        for (int i = 0; i < getPortCount(); i++) {
            Port port = getPort(i);
            if (port.getPosition() == position)
                return port;
        }
        return null;
    }

    public boolean hasPortAt(int position) {
        return getPortAt(position) != null;
    }

    public Port addPortAt(int position) {
        if (hasPortAt(position))
            throw new RuntimeException("position isn't available");
        Port port = new Port(this, position);
        ports.add(port);
        return port;
    }

}