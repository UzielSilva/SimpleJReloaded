/*
 * Dimensions.java
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

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

public class Dimensions {

    public final static Font NAMES_FONT = new Font("SansSerif",
                                                   Font.ITALIC, 14);
    
    public final static Font VALUES_FONT = new Font("SansSerif",
                                                     Font.PLAIN, 14);

    public final static int TYPE_VISIBLE = 1;

    public final static int TYPE_HIDDEN = 2;

    public final static int TYPE_MODIFIED = 3;

    public final static int TYPE_RETURN = 4;

    public final static Color COLOR_VISIBLE = Color.BLACK;

    public final static Color COLOR_HIDDEN = Color.GRAY;

    public final static Color COLOR_MODIFIED = Color.RED;

    public final static Color COLOR_RETURN = Color.GREEN;

    private FontMetrics namesFM;

    private FontMetrics valuesFM;

    private int unit;

    private int lockWidth;

    private int lockHeight;

    private BufferedImage visibleLock;

    private BufferedImage hiddenLock;

    private BufferedImage modifiedLock;

    public Dimensions(Graphics g) {
        g.setFont(NAMES_FONT);
        namesFM = g.getFontMetrics();
        g.setFont(VALUES_FONT);
        valuesFM = g.getFontMetrics();
        unit = valuesFM.getHeight() / getScaling();
        lockWidth = namesFM.getAscent() - unit;
        lockHeight = namesFM.getAscent() - unit;
        visibleLock = createLockImage(COLOR_VISIBLE);
        hiddenLock = createLockImage(COLOR_HIDDEN);
        modifiedLock = createLockImage(COLOR_MODIFIED);
    }

    public Color getColor(int type) {
        Color result = null;
        switch (type) {
            case TYPE_VISIBLE:
                result = COLOR_VISIBLE;
                break;

            case TYPE_HIDDEN:
                result = COLOR_HIDDEN;
                break;

            case TYPE_MODIFIED:
                result = COLOR_MODIFIED;
                break;

            case TYPE_RETURN:
                result = COLOR_RETURN;
                break;
        }
        return result;
    }

    public Image getLockImage(int type) {
        Image result = null;
        switch (type) {
            case TYPE_VISIBLE:
                result = visibleLock;
                break;

            case TYPE_HIDDEN:
                result = hiddenLock;
                break;

            case TYPE_MODIFIED:
                result = modifiedLock;
                break;
        }
        return result;
    }

    public int getUnit() {
        return unit;
    }

    public int getLockWidth() {
        return lockWidth;
    }

    public int getLockHeight() {
        return lockHeight;
    }

    public int getScaling() {
        return 4;
    }

    public int getLargeUnit() {
        return getUnit() * getScaling();
    }

    public FontMetrics getNamesFM() {
        return namesFM;
    }

    public FontMetrics getValuesFM() {
        return valuesFM;
    }

    public int stringWidth(String s, FontMetrics fm) {
        int width = 0;
        for (int i = 0; i < s.length(); i++)
            width += fm.charWidth(s.charAt(i));
        return width;
    }

    private BufferedImage createLockImage(Color color) {
        BufferedImage img = new BufferedImage(128, 128,
                                              BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 128, 128);
        g.setColor(color);
        g.fillRect(0, 50, 128, 78);
        Stroke stroke = g.getStroke();
        g.setStroke(new BasicStroke(16));
        g.drawArc(16, 0, 96, 96, 0, 180);
        g.setStroke(stroke);
        g.setColor(Color.WHITE);
        g.fillOval(50, 70, 28, 28);
        g.fillRect(60, 80, 8, 30);
        BufferedImage simg = new BufferedImage(lockWidth, lockHeight,
                                               BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) simg.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(img,
                    AffineTransform.getScaleInstance(lockWidth / 128.0,
                                                     lockHeight / 128.0),
                    null);
        return simg;
    }

}