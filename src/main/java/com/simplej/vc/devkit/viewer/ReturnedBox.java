/*
 * ReturnedBox.java
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
import java.awt.Point;
import java.awt.Rectangle;

public class ReturnedBox extends Box {

    private String value;

    private boolean hasValue;

    public ReturnedBox(Dimensions dimensions) {
        super(dimensions);
    }

    public void setValue(Object value) {
        if (value instanceof String)
            this.value = "\"" + value + "\"";
        else if (value instanceof ProcedureRef)
            this.value = ((ProcedureRef) value).getName() + "()";
        else if (value == null)
            this.value = "null";
        else
            this.value = value.toString();
        hasValue = true;
    }

    public Port addPort() {
        return addPortAt(0);
    }

    public boolean contains(Point p) {
        return false;
    }

    public void paint(Graphics g, Rectangle clip) {
        if (!intersects(clip))
            return;
        g.setColor(dimensions.getColor(Dimensions.TYPE_RETURN));
        g.setFont(Dimensions.NAMES_FONT);
        g.drawString("Returned:   ",
                     x * getUnit() + getUnit(),
                     y * getUnit() + dimensions.getNamesFM().getAscent());
        if (hasValue)
            g.drawString(value,
                         x * getUnit() +
                         dimensions.stringWidth("Returned:   ",
                                                dimensions.getNamesFM()),
                         y * getUnit() + dimensions.getValuesFM().getAscent());
    }

    public void calcSize() {
        width = dimensions.stringWidth("Returned:   ", dimensions.getNamesFM());
        if (hasValue)
            width += dimensions.stringWidth(value,
                                            dimensions.getValuesFM());
        width /= dimensions.getLargeUnit();
        width += 2;
        height = 2;
    }

}
