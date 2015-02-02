/*
 * Util.java
 * Copyright (C) 2004 Gerardo Horvilleur Martinez
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.simplej.vc.tools;

import java.awt.Color;

public class Util {
    
    private final static int IAVC_RED_MASK = 0x7c00;
    
    private final static int IAVC_RED_SHIFT = 10;
    
    private final static int AWT_RED_SHIFT = 19;
    
    private final static int IAVC_GREEN_MASK = 0x03e0;
    
    private final static int IAVC_GREEN_SHIFT = 5;
    
    private final static int AWT_GREEN_SHIFT = 11;
    
    private final static int IAVC_BLUE_MASK = 0x001f;
    
    private final static int AWT_BLUE_SHIFT = 3;
    
    private Util() { }
    
    public static int toIAVCValue(int red, int green, int blue) {
        return (red << IAVC_RED_SHIFT) | (green << IAVC_GREEN_SHIFT) | blue;
    }
    
    public static int getIAVCRed(int value) {
        return (value & IAVC_RED_MASK) >> IAVC_RED_SHIFT;
    }
    
    public static int getIAVCGreen(int value) {
        return (value & IAVC_GREEN_MASK) >> IAVC_GREEN_SHIFT;
    }
    
    public static int getIAVCBlue(int value) {
        return value & IAVC_BLUE_MASK;
    }
    
    public static int toAWTValue(int red, int green, int blue) {
        return 0xff000000 | (red << AWT_RED_SHIFT) |
            (green << AWT_GREEN_SHIFT) |
            (blue << AWT_BLUE_SHIFT);
    }
    
    public static int toAWTValue(int iavcValue) {
        return toAWTValue(getIAVCRed(iavcValue),
                          getIAVCGreen(iavcValue),
                          getIAVCBlue(iavcValue));
    }
    
    public static Color toAWTColor(int red, int green, int blue) {
        return new Color(red << 3, green << 3, blue << 3);
    }
    
    public static Color toAWTColor(int iavcValue) {
        return toAWTColor(getIAVCRed(iavcValue),
                          getIAVCGreen(iavcValue),
                          getIAVCBlue(iavcValue));
    }

}
