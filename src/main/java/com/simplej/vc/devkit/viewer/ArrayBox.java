/*
 * ArrayBox.java
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

import com.simplej.language.vm.*;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.*;

public class ArrayBox extends Box {

    private static class IndexValuePair {
        int index;

        Object value;

        boolean hasValue;

        int type;

        IndexValuePair(int index, Object value, int type) {
            this.index = index;
            if (value instanceof String)
                this.value = "\"" + value + "\"";
            else if (value instanceof ProcedureRef)
                this.value = ((ProcedureRef) value).getName() + "()";
            else if (value == null)
                this.value = "null";
            else if (value == Uninitialized.VALUE)
                this.value = "";
            else
                this.value = value.toString();
            this.hasValue = true;
            this.type = type;
        }

        IndexValuePair(int index, int type) {
            this.index = index;
            this.type = type;
        }
    }

    private final static int GAP = 2;

    private int type;

    private List pairs = new ArrayList();

    private int maxIndexWidth;

    private int maxValueWidth;

    private Object object;

    public ArrayBox(Dimensions dimensions, int type, Object object) {
        super(dimensions);
        this.type = type;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public Port addPort(int type) {
        Port port = addPortAt(pairs.size() + 1);
        pairs.add(new IndexValuePair(pairs.size(), type));
        return port;
    }

    public void addValue(Object value, int type) {
        pairs.add(new IndexValuePair(pairs.size(), value, type));
    }

    public void paint(Graphics g, Rectangle clip) {
        if (!intersects(clip))
            return;
        g.setColor(dimensions.getColor(type));
        g.drawRect(x * getUnit(), y * getUnit(),
                   width * getUnit(), height * getUnit());
        g.fillRect(x * getUnit(), y * getUnit(),
                   width * getUnit(), dimensions.getLargeUnit());
        g.setColor(Color.WHITE);
        g.setFont(Dimensions.NAMES_FONT);
        int centerOffset =
            (width * getUnit() -
             dimensions.stringWidth("Array", dimensions.getNamesFM())) / 2;
        g.drawString("Array",
                     x * getUnit() + centerOffset,
                     y * getUnit() + dimensions.getNamesFM().getAscent());
        g.setColor(dimensions.getColor(type));
        if (isCollapsed()) {
            centerOffset =
                (width * getUnit() -
                 dimensions.stringWidth("More...", dimensions.getNamesFM()))/ 2;
            g.drawString("More...",
                         x * getUnit() + centerOffset,
                         y * getUnit() + dimensions.getLargeUnit() +
                         dimensions.getNamesFM().getAscent());
            return;
        }
        g.drawLine(x * getUnit() + maxIndexWidth + getUnit() * 2,
                   y * getUnit() + dimensions.getLargeUnit(),
                   x * getUnit() + maxIndexWidth + getUnit() * 2,
                   y * getUnit() + dimensions.getLargeUnit() +
                   dimensions.getLargeUnit() * pairs.size());
        for (int i = 1; i < pairs.size(); i++)
            g.drawLine(x * getUnit() + maxIndexWidth + getUnit() * 2,
                       y * getUnit() + (i + 1) * dimensions.getLargeUnit(),
                       (x + width) * getUnit(),
                       y * getUnit() + (i + 1) * dimensions.getLargeUnit());
        int dy = 1;
        Iterator iter = pairs.iterator();
        while (iter.hasNext()) {
            IndexValuePair pair = (IndexValuePair) iter.next();
            if (y * getUnit() + dy * dimensions.getLargeUnit() >
                clip.y + clip.height
                ||
                y * getUnit() + (dy + 1) * dimensions.getLargeUnit() <
                clip.y) {
                dy++;
                continue;
            }
            String s = Integer.toString(dy - 1);
            g.setColor(dimensions.getColor(pair.type));
            g.setFont(Dimensions.NAMES_FONT);
            g.drawString(s,
                         x * getUnit() + getUnit() + maxIndexWidth -
                         dimensions.stringWidth(s, dimensions.getNamesFM()),
                         y * getUnit() + dy * dimensions.getLargeUnit() +
                         dimensions.getNamesFM().getAscent());
            if (pair.hasValue) {
                s = pair.value.toString();
                g.setFont(Dimensions.VALUES_FONT);
                g.drawString(s,
                             (x + width) * getUnit() - getUnit() -
                             dimensions.stringWidth(s,
                                                    dimensions.getValuesFM()),
                             y * getUnit() + dy * dimensions.getLargeUnit() +
                             dimensions.getValuesFM().getAscent());
            }
            dy++;
        }
    }

    public void calcSize() {
        width = dimensions.stringWidth("Array", dimensions.getNamesFM());
        width = Math.max(width,
                         dimensions.stringWidth("More...",
                                                dimensions.getNamesFM()));
        int index = 0;
        Iterator iter = pairs.iterator();
        while (iter.hasNext()) {
            IndexValuePair pair = (IndexValuePair) iter.next();
            int indexWidth =
                dimensions.stringWidth(Integer.toString(index),
                                       dimensions.getNamesFM());
            int valueWidth = 0;
            if (pair.hasValue)
                valueWidth =
                    dimensions.stringWidth(pair.value.toString(),
                                           dimensions.getValuesFM());
            maxIndexWidth = Math.max(maxIndexWidth, indexWidth);
            maxValueWidth = Math.max(maxValueWidth, valueWidth);
            int w = maxIndexWidth + GAP * 2 + maxValueWidth;
            width = Math.max(width, w);
            index++;
        }
        width /= dimensions.getLargeUnit();
        width += 2;
        if (isCollapsed())
            height = 2;
        else
            height = pairs.size() + 1;
    }
    
}