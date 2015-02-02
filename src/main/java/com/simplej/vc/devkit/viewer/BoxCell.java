/*
 * BoxCell.java
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

public class BoxCell implements Cell {

    private final Box box;

    public BoxCell(Box box) {
        this.box = box;
    }

    public void placeBelow(Cell cell) {
        translate(0, cell.getMaxY() - getMinY() + 1);
    }

    public int getWidth() {
        return box.getWidth();
    }

    public int getHeight() {
        return box.getHeight();
    }

    public int getMinX() {
        return box.getX();
    }

    public int getMinY() {
        return box.getY();
    }

    public int getMaxX() {
        return box.getX() + box.getWidth();
    }

    public int getMaxY() {
        return box.getY() + box.getHeight();
    }

    public boolean isAt(int x) {
        return x >= getMinX() && x < getMaxX();
    }

    public int getMinYAt(int x) {
        return getMinY();
    }

    public int getMaxYAt(int x) {
        return getMaxY();
    }

    public void translate(int dx, int dy) {
        box.translate(dx, dy);
    }
    
}