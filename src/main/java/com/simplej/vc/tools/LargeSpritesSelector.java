/*
 * LargeSpritesSelector.java
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

public class LargeSpritesSelector extends JComponent implements
                                                         SpritesDataListener {
    
    private SpritesData spritesData;
    
    private BufferedImage image;
    
    private DataBuffer pixels;
    
    private AffineTransform transform;
    
    public LargeSpritesSelector(SpritesData spritesData) {
        this.spritesData = spritesData;
        spritesData.addSpritesDataListener(this);
        image = new BufferedImage(256, 128, BufferedImage.TYPE_INT_RGB);
        pixels = image.getRaster().getDataBuffer();
        transform = new AffineTransform();
        transform.setToScale(2, 2);
        setPreferredSize(new Dimension(512, 256));
        setMaximumSize(new Dimension(512, 256));
        setMinimumSize(new Dimension(512, 256));
        setBackground(Color.BLACK);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int x = e.getX() / 32;
                int y = e.getY() /  32;
                int newSpriteIndex = y * 16 + x;
                if (SwingUtilities.isRightMouseButton(e)) {
                    LargeSpritesSelector.this
                        .spritesData.copyLarge(LargeSpritesSelector.this
                                               .spritesData
                                               .getLargeSpriteIndex(),
                                               newSpriteIndex);
                }
                LargeSpritesSelector.this
                    .spritesData.setLargeSpriteIndex(newSpriteIndex);
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
        if (spritesData.largeSelected()) {
            g2.setColor(Color.RED);
            int x = (spritesData.getLargeSpriteIndex() % 16) * 32;
            int y = (spritesData.getLargeSpriteIndex() / 16) * 32;
            g2.drawRect(x, y, 32, 32);
        }
    }
    
    private void drawPixels() {
        for (int i = 0; i < 128; i++) {
            for (int r = 0; r < 16; r++) {
                int offset = ((i / 16) * 16 + r) * 256 + (i % 16) * 16;
                for (int c = 0; c < 16; c++)
                    pixels.setElem(offset + c,
                                   spritesData.getLargeAWTValue(i, c, r));
            }
        }
    }

}