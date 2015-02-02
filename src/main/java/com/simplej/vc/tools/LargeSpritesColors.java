/*
 * LargeSpritesColors.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public as
 * License published by the Free Software Foundation; either version 2
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
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;


public class LargeSpritesColors extends JComponent implements
                                                       SpritesDataListener,
                                                       MouseListener,
                                                       MouseMotionListener {

    private SpritesData spritesData;

    private AffineTransform transform;

    private BufferedImage image;

    private DataBuffer pixels;

    public LargeSpritesColors(SpritesData spritesData) {
        this.spritesData = spritesData;
        spritesData.addSpritesDataListener(this);
        transform = new AffineTransform();
        transform.setToScale(8, 8);
        setPreferredSize(new Dimension(128, 128));
        setMaximumSize(new Dimension(128, 128));
        setMinimumSize(new Dimension(128, 128));
        addMouseListener(this);
        addMouseMotionListener(this);
        image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        pixels = image.getRaster().getDataBuffer();
        drawPixels();
    }

    public void spritesDataChanged() {
        drawPixels();
        repaint();
    }

    private void drawPixels() {
        int index = spritesData.getLargeSpriteIndex();
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 16; y++)
                pixels.setElem(y * 16 + x,
                               spritesData.getLargeAWTValue(index, x, y));
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform save = g2.getTransform();
        g2.transform(transform);
        g2.drawImage(image, 0, 0, null);
        g2.setTransform(save);
        g2.setColor(Color.GRAY);
        for (int i = 8; i < 128; i += 8) {
            g2.drawLine(0, i, 127, i);
            g2.drawLine(i, 0, i, 127);
        }
    }

    private void setPixel(int x, int y) {
        x /= 8;
        if (x < 0 || x > 15)
            return;
        y /= 8;
        if (y < 0 || y > 15)
            return;
        spritesData.setLargePixel(spritesData.getLargeSpriteIndex(),
                                  x, y,
                                  spritesData.getColorIndex());
    }
    
    public void mousePressed(MouseEvent e) {
        setPixel(e.getX(), e.getY());
    }
    
    public void mouseDragged(MouseEvent e) {
        setPixel(e.getX(), e.getY());
    }
    
    public void mouseClicked(MouseEvent e) { }
    
    public void mouseReleased(MouseEvent e) { }
    
    public void mouseEntered(MouseEvent e) { }
    
    public void mouseExited(MouseEvent e) { }
    
    public void mouseMoved(MouseEvent e) { }
    
}