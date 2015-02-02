/*
 * SpritesData.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez
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

import java.awt.*;
import java.io.*;
import java.util.*;

public class SpritesData {

    private final static int COLOR_MAP_SIZE = 16 * 2;

    private final static int SMALL_MAP_SIZE = 32 * 128;

    private final static int LARGE_MAP_SIZE = 128 * 128;

    private final static int DATA_SIZE =
        COLOR_MAP_SIZE + SMALL_MAP_SIZE + LARGE_MAP_SIZE;

    private final static int COLOR_MAP_OFFSET = 0;

    private final static int SMALL_MAP_OFFSET =
        COLOR_MAP_OFFSET + COLOR_MAP_SIZE;

    private final static int LARGE_MAP_OFFSET =
        SMALL_MAP_OFFSET + SMALL_MAP_SIZE;

    private final static int SMALL = 1;

    private final static int LARGE = 2;

    private byte[] data = new byte[DATA_SIZE];

    private java.util.List listeners = new ArrayList();

    private int smallOrLarge = SMALL;

    private int smallSpriteIndex = 0;

    private int largeSpriteIndex = 0;

    private int colorIndex = 0;

    public SpritesData() throws IOException {
        InputStream is =
          getClass().getResourceAsStream("/com/simplej/vc/hardware/reset.dat");
        byte[] rdata = new byte[32768];
        int offset = 0;
        while (offset < 32768) {
            int n = is.read(rdata, offset, 32768 - offset);
            offset += n;
        }
        System.arraycopy(rdata, 0x820, data, COLOR_MAP_OFFSET, COLOR_MAP_SIZE);
        System.arraycopy(rdata, 0x1000, data, SMALL_MAP_OFFSET, SMALL_MAP_SIZE);
        System.arraycopy(rdata, 0x4000, data, LARGE_MAP_OFFSET, LARGE_MAP_SIZE);
    }

    public void readData(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        int bytesRead = 0;
        while (bytesRead < DATA_SIZE) {
            int n = fis.read(data, bytesRead, DATA_SIZE - bytesRead);
            bytesRead += n;
        }
        fis.close();
        fireSpritesDataChanged();
    }
    
    public void writeData(String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        fos.write(data);
        fos.close();
    }
    
    public Color getAWTColor(int index) {
        return Util.toAWTColor(getIAVCValue(index));
    }
    
    public int getAWTValue(int index) {
        return Util.toAWTValue(getIAVCValue(index));
    }
    
    public int getIAVCValue(int index) {
        return ((data[index * 2] & 0xff) << 8) + (data[index * 2 + 1] & 0xff);
    }

    public boolean smallSelected() {
        return smallOrLarge == SMALL;
    }

    public boolean largeSelected() {
        return smallOrLarge == LARGE;
    }

    public int getSmallSpriteIndex() {
        return smallSpriteIndex;
    }

    public void setSmallSpriteIndex(int index) {
        smallSpriteIndex = index;
        smallOrLarge = SMALL;
        fireSpritesDataChanged();
    }

    public int getLargeSpriteIndex() {
        return largeSpriteIndex;
    }

    public void setLargeSpriteIndex(int index) {
        largeSpriteIndex = index;
        smallOrLarge = LARGE;
        fireSpritesDataChanged();
    }

    public int getColorIndex() {
        return colorIndex;
    }

    public void setColorIndex(int index) {
        colorIndex = index;
    }
    
    public void setColor(int index, int red, int green, int blue) {
        int v = Util.toIAVCValue(red, green, blue);
        data[index * 2] = (byte) (v >> 8);
        data[index * 2 + 1] = (byte) v;
        fireSpritesDataChanged();
    }
    
    public int getSmallAWTValue(int smallIndex, int col, int row) {
        int offset = SMALL_MAP_OFFSET + smallIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            return getAWTValue(data[offset] & 0x0f);
        else
            return getAWTValue((data[offset] & 0xf0) >> 4);
    }
    
    public int getSmallPixel(int smallIndex, int col, int row) {
        int offset = SMALL_MAP_OFFSET + smallIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            return (data[offset] & 0x0f);
        else
            return (data[offset] & 0xf0) >> 4;
        
    }
    
    public void internalSetSmallPixel(int smallIndex, int col, int row,
                                     int colorIndex) {
        int offset = SMALL_MAP_OFFSET + smallIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            data[offset] = (byte) ((data[offset] & 0xf0) | colorIndex);
        else
            data[offset] = (byte) ((data[offset] & 0x0f) | (colorIndex << 4));
    }
    
    public void setSmallPixel(int smallIndex, int col, int row, 
                              int colorIndex) {
        smallOrLarge = SMALL;
        internalSetSmallPixel(smallIndex, col, row, colorIndex);
        fireSpritesDataChanged();
    }
    
    public void copySmall(int fromIndex, int toIndex) {
        int fromOffset = SMALL_MAP_OFFSET + fromIndex * 32;
        int toOffset = SMALL_MAP_OFFSET + toIndex * 32;
        System.arraycopy(data, fromOffset, data, toOffset, 32);
        fireSpritesDataChanged();
    }
    
    public void flipVerticalSmall() {
        int[] values = new int[8];
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++)
                values[row] = getSmallPixel(smallSpriteIndex, col, row);
            for (int row = 0; row < 8; row++)
                internalSetSmallPixel(smallSpriteIndex,
                                      col, row, values[7 - row]);
        }
        fireSpritesDataChanged();
    }
    
    public void flipHorizontalSmall() {
        int[] values = new int[8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++)
                values[col] = getSmallPixel(smallSpriteIndex, col, row);
            for (int col = 0; col < 8; col++)
                internalSetSmallPixel(smallSpriteIndex,
                                      col, row, values[7 - col]);
        }
        fireSpritesDataChanged();
    }
    
    public void rotateClockwiseSmall() {
        int[] values = new int[64];
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                values[row * 8 + col] =
                    getSmallPixel(smallSpriteIndex, col, row);
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                internalSetSmallPixel(smallSpriteIndex, col, row,
                                     values[(7 - col) * 8 + row]);
        fireSpritesDataChanged();
    }
    
    public void rotateCounterclockwiseSmall() {
        int[] values = new int[64];
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                values[row * 8 + col] =
                    getSmallPixel(smallSpriteIndex, col, row);
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                internalSetSmallPixel(smallSpriteIndex, col, row,
                                     values[col * 8 + 7 - row]);
        fireSpritesDataChanged();
    }
    
    public int getLargeAWTValue(int largeIndex, int col, int row) {
        int offset = LARGE_MAP_OFFSET + largeIndex * 128 + row * 8 + col / 2;
        if ((col & 1) == 1)
            return getAWTValue(data[offset] & 0x0f);
        else
            return getAWTValue((data[offset] & 0xf0) >> 4);
    }
    
    public int getLargePixel(int largeIndex, int col, int row) {
        int offset = LARGE_MAP_OFFSET + largeIndex * 128 + row * 8 + col / 2;
        if ((col & 1) == 1)
            return (data[offset] & 0x0f);
        else
            return (data[offset] & 0xf0) >> 4;
        
    }
    
    public void internalSetLargePixel(int largeIndex, int col, int row,
                                     int colorIndex) {
        int offset = LARGE_MAP_OFFSET + largeIndex * 128 + row * 8 + col / 2;
        if ((col & 1) == 1)
            data[offset] = (byte) ((data[offset] & 0xf0) | colorIndex);
        else
            data[offset] = (byte) ((data[offset] & 0x0f) | (colorIndex << 4));
    }
    
    public void setLargePixel(int largeIndex, int col, int row, 
                              int colorIndex) {
        smallOrLarge = LARGE;
        internalSetLargePixel(largeIndex, col, row, colorIndex);
        fireSpritesDataChanged();
    }
    
    public void copyLarge(int fromIndex, int toIndex) {
        int fromOffset = LARGE_MAP_OFFSET + fromIndex * 128;
        int toOffset = LARGE_MAP_OFFSET + toIndex * 128;
        System.arraycopy(data, fromOffset, data, toOffset, 128);
        fireSpritesDataChanged();
    }
    
    public void flipVerticalLarge() {
        int[] values = new int[16];
        for (int col = 0; col < 16; col++) {
            for (int row = 0; row < 16; row++)
                values[row] = getLargePixel(largeSpriteIndex, col, row);
            for (int row = 0; row < 16; row++)
                internalSetLargePixel(largeSpriteIndex,
                                      col, row, values[15 - row]);
        }
        fireSpritesDataChanged();
    }
    
    public void flipHorizontalLarge() {
        int[] values = new int[16];
        for (int row = 0; row < 16; row++) {
            for (int col = 0; col < 16; col++)
                values[col] = getLargePixel(largeSpriteIndex, col, row);
            for (int col = 0; col < 16; col++)
                internalSetLargePixel(largeSpriteIndex,
                                      col, row, values[15 - col]);
        }
        fireSpritesDataChanged();
    }
    
    public void rotateClockwiseLarge() {
        int[] values = new int[256];
        for (int row = 0; row < 16; row++)
            for (int col = 0; col < 16; col++)
                values[row * 16 + col] =
                    getLargePixel(largeSpriteIndex, col, row);
        for (int row = 0; row < 16; row++)
            for (int col = 0; col < 16; col++)
                internalSetLargePixel(largeSpriteIndex, col, row,
                                     values[(15 - col) * 16 + row]);
        fireSpritesDataChanged();
    }
    
    public void rotateCounterclockwiseLarge() {
        int[] values = new int[256];
        for (int row = 0; row < 16; row++)
            for (int col = 0; col < 16; col++)
                values[row * 16 + col] =
                    getLargePixel(largeSpriteIndex, col, row);
        for (int row = 0; row < 16; row++)
            for (int col = 0; col < 16; col++)
                internalSetLargePixel(largeSpriteIndex, col, row,
                                     values[col * 16 + 15 - row]);
        fireSpritesDataChanged();
    }
    
    public synchronized void addSpritesDataListener(SpritesDataListener l) {
        listeners.add(l);
    }
    
    public synchronized void removeSpritesDataListener(SpritesDataListener l) {
        listeners.remove(l);
    }
    
    private synchronized void fireSpritesDataChanged() {
        for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            SpritesDataListener l = (SpritesDataListener) iter.next();
            l.spritesDataChanged();
        }
    }

}