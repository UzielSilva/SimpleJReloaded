/*
 * SmallSpritesSelector.java
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
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class SmallSpritesSelector extends JComponent implements
                                                         SpritesDataListener {
    
    private SpritesData spritesData;
    
    private BufferedImage image;
    
    private DataBuffer pixels;
    
    private AffineTransform transform;
    
    public SmallSpritesSelector(SpritesData spritesData) {
        this.spritesData = spritesData;
        spritesData.addSpritesDataListener(this);
        image = new BufferedImage(128, 64, BufferedImage.TYPE_INT_RGB);
        pixels = image.getRaster().getDataBuffer();
        transform = new AffineTransform();
        transform.setToScale(2, 2);
        setPreferredSize(new Dimension(256, 128));
        setMaximumSize(new Dimension(256, 128));
        setMinimumSize(new Dimension(256, 128));
        setBackground(Color.BLACK);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / 16;
                int y = e.getY() /  16;
                int newSpriteIndex = y * 16 + x;
                if (SwingUtilities.isRightMouseButton(e)) {
                    SmallSpritesSelector.this
                        .spritesData.copySmall(SmallSpritesSelector.this
                                               .spritesData
                                               .getSmallSpriteIndex(),
                                               newSpriteIndex);
                }
                SmallSpritesSelector.this
                    .spritesData.setSmallSpriteIndex(newSpriteIndex);
            }
        });
    }
    
    public void spritesDataChanged() {
        repaint();
    }
    
    public void paint(Graphics g) {
        drawPixels();
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform save = g2.getTransform();
        g2.transform(transform);
        g2.drawImage(image, 0, 0, null);
        g2.setTransform(save);
        if (spritesData.smallSelected()) {
            g2.setColor(Color.RED);
            int x = (spritesData.getSmallSpriteIndex() % 16) * 16;
            int y = (spritesData.getSmallSpriteIndex() / 16) * 16;
            g2.drawRect(x, y, 16, 16);
        }
    }
    
    private void drawPixels() {
        for (int i = 0; i < 128; i++) {
            for (int r = 0; r < 8; r++) {
                int offset = ((i / 16) * 8 + r) * 128 + (i % 16) * 8;
                for (int c = 0; c < 8; c++)
                    pixels.setElem(offset + c,
                                   spritesData.getSmallAWTValue(i, c, r));
            }
        }
    }

}