/*
 IAVC.java
 Copyright (C) 2004 Gerardo Horvilleur Martinez

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

package com.simplej.vc.hardware;

import java.awt.event.*;
import java.awt.image.*;
import java.io.*;

public class IAVC extends KeyAdapter implements Runnable {

    private final static int KILOBYTE = 1024;

    private final static int PIXEL_COLUMNS = 256;

    private final static int PIXEL_ROWS = 192;

    private final static int NUM_SPRITES = 32;

    private final static int SCREEN_RAM = 0;

    private final static int CONTROL_RAM = 2 * KILOBYTE;

    private final static int SMALL_SPRITE_RAM = 4 * KILOBYTE;

    private final static int CHARACTER_RAM = 8 * KILOBYTE;

    private final static int LARGE_SPRITE_RAM = 16 * KILOBYTE;

    private final static int CHARACTER_COLORMAP = CONTROL_RAM;

    private final static int SPRITE_COLORMAP = CHARACTER_COLORMAP + 2 * 16;

    private final static int SPRITE_CONTROL = SPRITE_COLORMAP + 2 * 16;

    private final static int SCREEN_CONTROL = SPRITE_CONTROL + 4 * NUM_SPRITES;

    private final static int BUTTONS_BITS = SCREEN_CONTROL + 4;

    private final static int BUTTONS_BITS_2 = SCREEN_CONTROL + 5;

    private final static int SAVE_CONTROL = 0x906;

    private final static int SAVE_AREA = 0xa00;

    private final static int SAVE_AREA_SIZE = 512;

    private final static int KEY_UP = 0x01;

    private final static int KEY_DOWN = 0x02;

    private final static int KEY_LEFT = 0x04;

    private final static int KEY_RIGHT = 0x08;

    private final static int KEY_ENTER = 0x10;

    private final static int KEY_CTRL = 0x20;

    private final static int KEY_SPACE = 0x40;

    private final static int KEY_P = 0x80;

    private final static int KEY_R = 0x01;

    private final static int KEY_F = 0x02;

    private final static int KEY_D = 0x04;

    private final static int KEY_G = 0x08;

    private final static int KEY_SHIFT = 0x10;

    private final static int KEY_Z = 0x20;

    private final static int KEY_X = 0x40;

    private final static int KEY_Q = 0x80;

    private final static int SAVE_WRITE = 0x01;

    private final static int SAVE_READ = 0x02;

    private final byte[] ram = new byte[32768];

    private final int[] colormap = new int[31];

    private int[] pixels;

    private final Audio audio;

    private VBI vbi;

    private ExceptionHandler exceptionHandler;

    private String saveFile;

    private Thread thread;

    public IAVC(int[] pixels) throws IOException {
        this.pixels = pixels;
        audio = new Audio(ram);
        reset();
        thread = new Thread(this);
    }

    public void start() {
        thread.start();
        audio.start();
    }

    public void freeze() {
        audio.freeze();
    }

    public void unfreeze() {
        audio.unfreeze();
    }

    public int read(int location) {
        return ram[location & 0x7fff] & 0xff;
    }

    public void store(int location, int value) {
        ram[location & 0x7fff] = (byte) value;
    }

    public void storeWord(int location, int value) {
        synchronized (ram) {
            ram[location & 0x7fff] = (byte) (value >> 8);
            ram[(location + 1) & 0x7fff] = (byte) value;
        }
    }

    public void arrayStore(int location, byte[] source, int offset, int count) {
        for (int i = 0; i < count; i++)
            ram[(location + i) & 0x7fff] = source[offset + i];
    }

    public void reset() throws IOException {
        // setVBI(null);
        // setSFI(null);
        InputStream is = getClass().getResourceAsStream("reset.dat");
        int count = 0;
        int offset = 0;
        while (count < 32768) {
            int n = is.read(ram, offset, 32768 - offset);
            offset += n;
            count += n;
        }
        is.close();
        audio.reset();
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        audio.setExceptionHandler(exceptionHandler);
    }

    public void setVBI(VBI vbi) {
        this.vbi = vbi;
    }

    public void setSFI(SFI sfi) {
        audio.setSFI(sfi);
    }

    public void setAudioDelayWorkaround(boolean v) {
        audio.setAudioDelayWorkaround(v);
    }

    public boolean isAudioDelayWorkaroundEnabled() {
        return audio.isAudioDelayWorkaroundEnabled();
    }

    void drawPixels() {
        fillColorMap();
        int screenBase =
            (ram[SCREEN_CONTROL + 1] & 0xff) +
            ((ram[SCREEN_CONTROL] & 0x7) << 8);
        int hscroll = ram[SCREEN_CONTROL + 2] & 0xf;
        int vscroll = (ram[SCREEN_CONTROL + 2] & 0xf0) >> 4;
        int pixelOffset = 0;
        for (int r = 0; r < PIXEL_ROWS; r++) {
            int rs = r + vscroll;
            int screenRowOffset = ((rs & 0xf8) << 3) + screenBase;
            int charRowOffset = ((rs & 0x7) << 2) + CHARACTER_RAM;
            for (int c = 0; c < PIXEL_COLUMNS; c++) {
                int cs = c + hscroll;
                int offset =
                    ((ram[((cs >> 3) + screenRowOffset) & 0x7ff] & 0xff) << 5) +
                                              charRowOffset + ((cs & 0x7) >> 1);
                if ((cs & 0x1) == 0)
                    pixels[pixelOffset] = colormap[(ram[offset] & 0xff) >> 4];
                else
                    pixels[pixelOffset] = colormap[(ram[offset] & 0xf)];
                pixelOffset++;
            }
        }
        int spriteCtrlOffset = SPRITE_CONTROL + (NUM_SPRITES - 1) * 4;
        for (int i = 0; i < NUM_SPRITES; i++) {
            int spriteNum = ram[spriteCtrlOffset] & 0xff;
            int xPos = (ram[spriteCtrlOffset + 1] & 0xff) +
                ((ram[spriteCtrlOffset + 3] & 0x1) << 8) - 16;
            int yPos = (ram[spriteCtrlOffset + 2] & 0xff) - 16;
            if ((spriteNum & 0x80) == 0)
                draw16by16Sprite(spriteNum & 0x7f, xPos, yPos);
            else
                draw8by8Sprite(spriteNum & 0x7f, xPos, yPos);
            spriteCtrlOffset -= 4;
        }
        if (vbi != null) {
            Throwable error = null;
            try {
                vbi.vbi();
            } catch (Throwable t) {
                error = t;
            }
            if (error != null && exceptionHandler != null)
                exceptionHandler.vbiException(error);
        }
    }

    private void fillColorMap() {
        for (int i = 0; i < 31; i++)
            colormap[i] = 0xff000000 |
                ((ram[i * 2 + CHARACTER_COLORMAP] & 0x7c) << 17) |
                ((ram[i * 2 + CHARACTER_COLORMAP] & 0x03) << 14) |
                ((ram[i * 2 + CHARACTER_COLORMAP + 1] & 0xe0) << 6) |
                ((ram[i * 2 + CHARACTER_COLORMAP + 1] & 0x1f) << 3);
    }

    private void draw16by16Sprite(int spriteNum, int xPos, int yPos) {
        if (xPos + 16 < 0 || xPos >= PIXEL_COLUMNS ||
            yPos + 16 < 0 || yPos >= PIXEL_ROWS)
            return;
        int minC = 0;
        if (xPos < 0)
            minC = -xPos;
        int maxC = 16;
        if (xPos + 16 > PIXEL_COLUMNS)
            maxC = PIXEL_COLUMNS - xPos;
        int minR = 0;
        if (yPos < 0) {
            minR = -yPos;
            yPos = 0;
        }
        int maxR = 16;
        if (yPos + 16 > PIXEL_ROWS)
            maxR = PIXEL_ROWS - yPos;
        int spriteOffset = LARGE_SPRITE_RAM + (spriteNum << 7);
        int rowStartPixelOffset = yPos * PIXEL_COLUMNS + xPos;
        for (int r = minR; r < maxR; r++) {
            int rowOffset = (r << 3) + spriteOffset;
            for (int c = minC; c < maxC; c++) {
                int color;
                if ((c & 0x1) == 0)
                    color = (ram[rowOffset + (c >> 1)] & 0xff) >> 4;
                else 
                    color = (ram[rowOffset + (c >> 1)] & 0xf);
                if (color != 0xf)
                    pixels[rowStartPixelOffset + c] = colormap[color + 16];
            }
            rowStartPixelOffset += PIXEL_COLUMNS;
        }
    }

    private void draw8by8Sprite(int spriteNum, int xPos, int yPos) {
        if (xPos + 8 < 0 || xPos >= PIXEL_COLUMNS ||
            yPos + 8 < 0 || yPos >= PIXEL_ROWS)
            return;
        int minC = 0;
        if (xPos < 0)
            minC = -xPos;
        int maxC = 8;
        if (xPos + 8 > PIXEL_COLUMNS)
            maxC = PIXEL_COLUMNS - xPos;
        int minR = 0;
        if (yPos < 0) {
            minR = -yPos;
            yPos = 0;
        }
        int maxR = 8;
        if (yPos + 8 > PIXEL_ROWS)
            maxR = PIXEL_ROWS - yPos;
        int spriteOffset = SMALL_SPRITE_RAM + (spriteNum << 5);
        int rowStartPixelOffset = yPos * PIXEL_COLUMNS + xPos;
        for (int r = minR; r < maxR; r++) {
            int rowOffset = (r << 2) + spriteOffset;
            for (int c = minC; c < maxC; c++) {
                int color;
                if ((c & 0x1) == 0)
                    color = (ram[rowOffset + (c >> 1)] & 0xff) >> 4;
                else 
                    color = (ram[rowOffset + (c >> 1)] & 0xf);
                if (color != 0xf)
                    pixels[rowStartPixelOffset + c] = colormap[color + 16];
            }
            rowStartPixelOffset += PIXEL_COLUMNS;
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int bits = ram[BUTTONS_BITS];
        if (keyCode == KeyEvent.VK_UP)
            bits |= KEY_UP;
        else if (keyCode == KeyEvent.VK_DOWN)
            bits |= KEY_DOWN;
        else if (keyCode == KeyEvent.VK_LEFT)
            bits |= KEY_LEFT;
        else if (keyCode == KeyEvent.VK_RIGHT)
            bits |= KEY_RIGHT;
        else if (keyCode == KeyEvent.VK_ENTER)
            bits |= KEY_ENTER;
        else if (keyCode == KeyEvent.VK_CONTROL)
            bits |= KEY_CTRL;
        else if (keyCode == KeyEvent.VK_SPACE)
            bits |= KEY_SPACE;
        else if (keyCode == KeyEvent.VK_P)
            bits |= KEY_P;
        ram[BUTTONS_BITS] = (byte) bits;
        bits = ram[BUTTONS_BITS_2];
        if (keyCode == KeyEvent.VK_R)
            bits |= KEY_R;
        else if (keyCode == KeyEvent.VK_F)
            bits |= KEY_F;
        else if (keyCode == KeyEvent.VK_D)
            bits |= KEY_D;
        else if (keyCode == KeyEvent.VK_G)
            bits |= KEY_G;
        else if (keyCode == KeyEvent.VK_SHIFT)
            bits |= KEY_SHIFT;
        else if (keyCode == KeyEvent.VK_Z)
            bits |= KEY_Z;
        else if (keyCode == KeyEvent.VK_X)
            bits |= KEY_X;
        else if (keyCode == KeyEvent.VK_Q)
            bits |= KEY_Q;
        ram[BUTTONS_BITS_2] = (byte) bits;
    }

    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        int bits = ram[BUTTONS_BITS];
        if (keyCode == KeyEvent.VK_UP)
            bits &= ~KEY_UP;
        else if (keyCode == KeyEvent.VK_DOWN)
            bits &= ~KEY_DOWN;
        else if (keyCode == KeyEvent.VK_LEFT)
            bits &= ~KEY_LEFT;
        else if (keyCode == KeyEvent.VK_RIGHT)
            bits &= ~KEY_RIGHT;
        else if (keyCode == KeyEvent.VK_ENTER)
            bits &= ~KEY_ENTER;
        else if (keyCode == KeyEvent.VK_CONTROL)
            bits &= ~KEY_CTRL;
        else if (keyCode == KeyEvent.VK_SPACE)
            bits &= ~KEY_SPACE;
        else if (keyCode == KeyEvent.VK_P)
            bits &= ~KEY_P;
        ram[BUTTONS_BITS] = (byte) bits;
        bits = ram[BUTTONS_BITS_2];
        if (keyCode == KeyEvent.VK_R)
            bits &= ~KEY_R;
        else if (keyCode == KeyEvent.VK_F)
            bits &= ~KEY_F;
        else if (keyCode == KeyEvent.VK_D)
            bits &= ~KEY_D;
        else if (keyCode == KeyEvent.VK_G)
            bits &= ~KEY_G;
        else if (keyCode == KeyEvent.VK_SHIFT)
            bits &= ~KEY_SHIFT;
        else if (keyCode == KeyEvent.VK_Z)
            bits &= ~KEY_Z;
        else if (keyCode == KeyEvent.VK_X)
            bits &= ~KEY_X;
        else if (keyCode == KeyEvent.VK_Q)
            bits &= ~KEY_Q;
        ram[BUTTONS_BITS_2] = (byte) bits;
    }

    public void setSaveFile(String saveFile) throws IOException {
        this.saveFile = saveFile;
    }

    public void run() {
        while (true) {
            try {
                int cntrl = ram[SAVE_CONTROL];
                if ((cntrl & SAVE_WRITE) != 0) {
                    if (saveFile != null) {
                        FileOutputStream fos = new FileOutputStream(saveFile);
                        fos.write(ram, SAVE_AREA, SAVE_AREA_SIZE);
                        fos.close();
                    }
                    ram[SAVE_CONTROL] &= ~SAVE_WRITE;
                }
                if ((cntrl & SAVE_READ) != 0) {
                    if (saveFile != null) {
                        File file = new File(saveFile);
                        if (file.exists()) {
                            FileInputStream fis = new FileInputStream(file);
                            fis.read(ram, SAVE_AREA, SAVE_AREA_SIZE);
                            fis.close();
                            // ram[SAVE_CONTROL] = (byte) 0;
                        }
                    }
                    ram[SAVE_CONTROL] &= ~SAVE_READ;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO: what?
            }
        }
    }

}
