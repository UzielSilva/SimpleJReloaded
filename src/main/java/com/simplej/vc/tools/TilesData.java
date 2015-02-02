/*
 * TilesData.java
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

import java.awt.*;
import java.io.*;
import java.util.*;

public class TilesData {
    
    private final static int COLOR_MAP_SIZE = 16 * 2;
    
    private final static int TILES_MAP_SIZE = 32 * 256;
    
    private final static int SCREEN_SIZE = 32 * 24;
    
    private final static int DATA_SIZE =
        COLOR_MAP_SIZE + TILES_MAP_SIZE + SCREEN_SIZE;
    
    private final static int COLOR_MAP_OFFSET = 0;
    
    private final static int TILES_MAP_OFFSET =
        COLOR_MAP_OFFSET + COLOR_MAP_SIZE;
    
    private final static int SCREEN_OFFSET = TILES_MAP_OFFSET + TILES_MAP_SIZE;
    
    private byte[] data = new byte[DATA_SIZE];
    
    private java.util.List listeners = new ArrayList();
    
    public TilesData() throws IOException {
        InputStream is =
          getClass().getResourceAsStream("/com/simplej/vc/hardware/reset.dat");
        byte[] rdata = new byte[32768];
        int offset = 0;
        while (offset < 32768) {
            int n = is.read(rdata, offset, 32768 - offset);
            offset += n;
        }
        System.arraycopy(rdata, 2048, data, COLOR_MAP_OFFSET, COLOR_MAP_SIZE);
        System.arraycopy(rdata, 8192, data, TILES_MAP_OFFSET, TILES_MAP_SIZE);
        System.arraycopy(rdata, 0, data, SCREEN_OFFSET, SCREEN_SIZE);
    }
    
    public void readData(String filename) throws IOException {
        FileInputStream fis = new FileInputStream(filename);
        int bytesRead = 0;
        while (bytesRead < DATA_SIZE) {
            int n = fis.read(data, bytesRead, DATA_SIZE - bytesRead);
            bytesRead += n;
        }
        fis.close();
        fireTilesDataChanged();
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
    
    public void setColor(int index, int red, int green, int blue) {
        int v = Util.toIAVCValue(red, green, blue);
        data[index * 2] = (byte) (v >> 8);
        data[index * 2 + 1] = (byte) v;
        fireTilesDataChanged();
    }
    
    public int getAWTValue(int tilesIndex, int col, int row) {
        int offset = TILES_MAP_OFFSET + tilesIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            return getAWTValue(data[offset] & 0x0f);
        else
            return getAWTValue((data[offset] & 0xf0) >> 4);
    }
    
    public int getTilesPixel(int tilesIndex, int col, int row) {
        int offset = TILES_MAP_OFFSET + tilesIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            return (data[offset] & 0x0f);
        else
            return (data[offset] & 0xf0) >> 4;
        
    }
    
    public void internalSetTilesPixel(int tilesIndex, int col, int row,
                                     int colorIndex) {
        int offset = TILES_MAP_OFFSET + tilesIndex * 32 + row * 4 + col / 2;
        if ((col & 1) == 1)
            data[offset] = (byte) ((data[offset] & 0xf0) | colorIndex);
        else
            data[offset] = (byte) ((data[offset] & 0x0f) | (colorIndex << 4));
    }
    
    public void setTilesPixel(int tilesIndex, int col, int row, 
                              int colorIndex) {
        internalSetTilesPixel(tilesIndex, col, row, colorIndex);
        fireTilesDataChanged();
    }
    
    public void copyTiles(int fromIndex, int toIndex) {
        int fromOffset = TILES_MAP_OFFSET + fromIndex * 32;
        int toOffset = TILES_MAP_OFFSET + toIndex * 32;
        System.arraycopy(data, fromOffset, data, toOffset, 32);
        fireTilesDataChanged();
    }
    
    public void flipVertical(int tilesIndex) {
        int[] values = new int[8];
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++)
                values[row] = getTilesPixel(tilesIndex, col, row);
            for (int row = 0; row < 8; row++)
                internalSetTilesPixel(tilesIndex, col, row, values[7 - row]);
        }
        fireTilesDataChanged();
    }
    
    public void flipHorizontal(int tilesIndex) {
        int[] values = new int[8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++)
                values[col] = getTilesPixel(tilesIndex, col, row);
            for (int col = 0; col < 8; col++)
                internalSetTilesPixel(tilesIndex, col, row, values[7 - col]);
        }
        fireTilesDataChanged();
    }
    
    public void rotateClockwise(int tilesIndex) {
        int[] values = new int[64];
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                values[row * 8 + col] = getTilesPixel(tilesIndex, col, row);
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                internalSetTilesPixel(tilesIndex, col, row,
                                     values[(7 - col) * 8 + row]);
        fireTilesDataChanged();
    }
    
    public void rotateCounterclockwise(int tilesIndex) {
        int[] values = new int[64];
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                values[row * 8 + col] = getTilesPixel(tilesIndex, col, row);
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                internalSetTilesPixel(tilesIndex, col, row,
                                     values[col * 8 + 7 - row]);
        fireTilesDataChanged();
    }
    
    public int getScreenTiles(int col, int row) {
        return data[SCREEN_OFFSET + row * 32 + col] & 0xff;
    }
    
    public void setScreenTiles(int col, int row, int tilesIndex) {
        data[SCREEN_OFFSET + row * 32 + col] = (byte) tilesIndex;
        fireTilesDataChanged();
    }
    
    public synchronized void addTilesDataListener(TilesDataListener tdl) {
        listeners.add(tdl);
    }
    
    public synchronized void removeTilesDataListener(TilesDataListener tdl) {
        listeners.remove(tdl);
    }
    
    private synchronized void fireTilesDataChanged() {
        for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
            TilesDataListener tdl = (TilesDataListener) iter.next();
            tdl.tilesDataChanged();
        }
    }

}
