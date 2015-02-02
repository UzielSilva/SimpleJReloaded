/*
 Notes.java
 Copyright (C) 2004 Gerardo Horvilleur

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.simplej.vc.util;

import com.simplej.vc.hardware.IAVC;

import java.util.*;

public class Notes {

    private static Map frequencies = new HashMap();

    static {
        double noteRatio = Math.exp(Math.log(2.0) / 12.0);
        double freq = 27.5;
        for (int i = 1; i < 7; i++) {
            double f = freq;
            Integer hwv = toHWValue(f);
            frequencies.put("A" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("A#" + i, hwv);
            frequencies.put("Bb" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("B" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("C" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("C#" + i, hwv);
            frequencies.put("Db" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("D" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("D#" + i, hwv);
            frequencies.put("Eb" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("E" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("F" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("F#" + i, hwv);
            frequencies.put("Gb" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("G" + i, hwv);
            f *= noteRatio;
            hwv = toHWValue(f);
            frequencies.put("G#" + i, hwv);
            frequencies.put("Ab" + (i + 1), hwv);
            freq *= 2.0;
        }
    }

    private static Integer toHWValue(double f) {
        return new Integer((int) (f * 23.77723356));
    }

    public static void playNote(IAVC iavc, String note) {
        Integer f = (Integer) frequencies.get(note);
        if (f == null)
            throw new IllegalArgumentException("No such note: " + note);
        iavc.storeWord(0x8c6, f.intValue());
        iavc.store(0x8ce, 0x8f);
    }

}
