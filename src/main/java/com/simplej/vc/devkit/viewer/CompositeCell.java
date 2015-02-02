/*
 * CompositeCell.java
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

import java.util.*;

public class CompositeCell implements Cell {

    private List cells;

    public CompositeCell(List cells) {
        this.cells = cells;
    }

    public void placeBelow(Cell cell) {
        translate(0, cell.getMaxY() - getMinY() + 1);
        int maxdy = Integer.MAX_VALUE;
        for (int x = getMinX(); x < getMaxX(); x++)
            if (isAt(x))
                maxdy = Math.min(maxdy, getMinYAt(x));
        int xmin = Math.max(getMinX(), cell.getMinX());
        int xmax = Math.min(getMaxX(), cell.getMaxX());
        int ymin = Integer.MAX_VALUE;
        for (int x = xmin; x < xmax; x++) {
            if (isAt(x) && cell.isAt(x))
                ymin = Math.min(ymin, getMinYAt(x) - cell.getMaxYAt(x));
        }
        if (ymin != Integer.MAX_VALUE)
            translate(0, -Math.min(ymin, maxdy) + 1);
    }

    public void addCenteredLeft(Cell cell, int gap) {
        int centerY = (getMaxY() + getMinY()) / 2;
        int newY = centerY - cell.getHeight() / 2;
        if (newY >= 0)
            cell.translate(0, newY - cell.getMinY());
        else {
            centerY = (cell.getMaxY() + cell.getMinY()) / 2;
            newY = centerY - getHeight() / 2;
            translate(0, newY - getMinY());
        }
        translate(cell.getWidth() + gap, 0);
        cells.add(cell);
    }

    public int getWidth() {
        return getMaxX() - getMinX();
    }

    public int getHeight() {
        return getMaxY() - getMinY();
    }

    public int getMinX() {
        int min = Integer.MAX_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            min = Math.min(min, cell.getMinX());
        }
        return min;
    }

    public int getMinY() {
        int min = Integer.MAX_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            min = Math.min(min, cell.getMinY());
        }
        return min;
    }

    public int getMaxX() {
        int max = Integer.MIN_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            max = Math.max(max, cell.getMaxX());
        }
        return max;
    }

    public int getMaxY() {
        int max = Integer.MIN_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            max = Math.max(max, cell.getMaxY());
        }
        return max;
    }

    public boolean isAt(int x) {
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            if (cell.isAt(x))
                return true;
        }
        return false;
    }

    public int getMinYAt(int x) {
        int min = Integer.MAX_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            if (!isAt(x))
                continue;
            min = Math.min(min, cell.getMinYAt(x));
        }
        return min;
    }

    public int getMaxYAt(int x) {
        int max = Integer.MIN_VALUE;
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            if (!isAt(x))
                continue;
            max = Math.max(max, cell.getMaxYAt(x));
        }
        return max;
    }

    public void translate(int dx, int dy) {
        Iterator iter = cells.iterator();
        while (iter.hasNext()) {
            Cell cell = (Cell) iter.next();
            cell.translate(dx, dy);
        }
    }

}