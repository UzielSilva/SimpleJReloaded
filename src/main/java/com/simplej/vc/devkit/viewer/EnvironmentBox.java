/*
 * EnvironmentBox.java
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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.*;

public class EnvironmentBox extends Box {

    private static class NameValuePair {
        String name;

        String value;

        boolean hasValue;

        int type;

        boolean isFinal;

        NameValuePair(String name, Object value, int type, boolean isFinal) {
            this.name = name;
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
            this.isFinal = isFinal;
        }

        NameValuePair(String name, int type, boolean isFinal) {
            this.name = name;
            this.type = type;
            this.isFinal = isFinal;
        }
    }

    private final static int GAP = 2;

    private String name;

    private int type;

    private List pairs = new ArrayList();

    private int maxNameWidth;

    private int maxValueWidth;

    private Object object;

    public EnvironmentBox(Dimensions dimensions, int type, Object object) {
        super(dimensions);
        this.type = type;
        this.object = object;
    }

    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getObject() {
        return object;
    }

    public Port addNamePort(String name, int type, boolean isFinal) {
        Port port = addPortAt(pairs.size() + 1);
        pairs.add(new NameValuePair(name, type, isFinal));
        return port;
    }
    
    public void addNameValue(String name, Object value, int type,
                             boolean isFinal) {
        pairs.add(new NameValuePair(name, value, type, isFinal));
    }
    
    public void paint(Graphics g, Rectangle clip) {
        if (!intersects(clip))
            return;
        g.setColor(dimensions.getColor(type));
        g.drawRoundRect(x * getUnit(), y * getUnit(),
                        width * getUnit(), height * getUnit(),
                        getUnit() * 2, getUnit() *2);
        Shape clipped = g.getClip();
        Graphics2D g2 = (Graphics2D) g;
        g2.clip(new Rectangle(x * getUnit(), y * getUnit(),
                              width * getUnit(), dimensions.getLargeUnit()));
        g.fillRoundRect(x * getUnit(), y * getUnit(),
                            width * getUnit(), height * getUnit(),
                            getUnit() * 2, getUnit() *2);
        g.setFont(Dimensions.NAMES_FONT);
        g.setClip(clipped);
        g.setColor(Color.WHITE);
        if (name != null) {
            int centerOffset =
                (width * getUnit() -
                 dimensions.stringWidth(name, dimensions.getNamesFM())) / 2;
            g.drawString(name,
                         x * getUnit() + centerOffset,
                         y * getUnit()
                         + dimensions.getValuesFM().getAscent());
        }
        g.setColor(dimensions.getColor(type));
        if (isCollapsed()) {
            int centerOffset =
                (width * getUnit() -
                 dimensions.stringWidth("More...", dimensions.getNamesFM()))/ 2;
            g.drawString("More...",
                         x * getUnit() + centerOffset,
                         y * getUnit() + dimensions.getLargeUnit() +
                         dimensions.getNamesFM().getAscent());
            return;
        }
        g.drawLine(x * getUnit() + maxNameWidth + getUnit() * 2,
                   y * getUnit() + dimensions.getLargeUnit(),
                   x * getUnit() + maxNameWidth + getUnit() * 2,
                   y * getUnit() + dimensions.getLargeUnit() +
                   dimensions.getLargeUnit() * pairs.size());
        for (int i = 1; i < pairs.size(); i++)
            g.drawLine(x * getUnit() + maxNameWidth + getUnit() * 2,
                       y * getUnit() + (i + 1) * dimensions.getLargeUnit(),
                       (x + width) * getUnit(),
                       y * getUnit() + (i + 1) * dimensions.getLargeUnit());
        int dy = 1;
        Iterator iter = pairs.iterator();
        while (iter.hasNext()) {
            NameValuePair pair = (NameValuePair) iter.next();
            String name = pair.name;
            String value = pair.value;
            g.setFont(Dimensions.NAMES_FONT);
            g.setColor(dimensions.getColor(pair.type));
            if (pair.isFinal) {
                g.drawImage(dimensions.getLockImage(pair.type),
                            x * getUnit() + getUnit(),
                            y * getUnit() + dy * dimensions.getLargeUnit() +
                            getUnit(),
                            null);
            }
            g.drawString(name,
                         x * getUnit() + getUnit() + maxNameWidth -
                         dimensions.stringWidth(name, dimensions.getNamesFM()),
                         y * getUnit() + dy * dimensions.getLargeUnit() +
                         dimensions.getNamesFM().getAscent());
            g.setFont(dimensions.VALUES_FONT);
            if (pair.hasValue)
                g.drawString(value,
                             (x + width) * getUnit() - getUnit() -
                             dimensions.stringWidth(value,
                                                    dimensions.getValuesFM()),
                             y * getUnit() + dy * dimensions.getLargeUnit() +
                             dimensions.getValuesFM().getAscent());
            dy++;
        }
    }

    public void calcSize() {
        width = dimensions.stringWidth("More...", dimensions.getNamesFM());
        int w;
        if (name != null) {
            w = dimensions.stringWidth(name,
                                       dimensions.getNamesFM());
            width = Math.max(width, w);
        }
        Iterator iter = pairs.iterator();
        while (iter.hasNext()) {
            int nameWidth;
            int valueWidth = 4;
            NameValuePair pair = (NameValuePair) iter.next();
            nameWidth = dimensions.stringWidth(pair.name,
                                               dimensions.getNamesFM());
            if (pair.isFinal)
                nameWidth += dimensions.getLockWidth() + getUnit() * 2;
            if (pair.hasValue)
                valueWidth = dimensions.stringWidth(pair.value,
                                                    dimensions.getValuesFM());
            maxNameWidth = Math.max(maxNameWidth, nameWidth);
            maxValueWidth = Math.max(maxValueWidth, valueWidth);
            w = maxNameWidth + GAP * 2 + maxValueWidth;
            width = Math.max(width, w);
        }
        width /= dimensions.getLargeUnit();
        width += 2;
        if (isCollapsed())
            height = 2;
        else
            height = pairs.size() + 1;
    }
    
}