/*
 * ShowScreen.java
 * Copyright (C) 2004 Gerardo Horvilleur Martinez
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
import javax.swing.border.*;

public class ShowScreen extends JPanel implements TilesDataListener {
    
    private TilesData tilesData;
    
    private BufferedImage image;
    
    private DataBuffer pixels;
    
    private TilesColorSelector colorSelector;
    
    private TilesSelector tilesSelector;
    
    private PaintColors paintColors;
    
    private PaintTiles paintTiles;
    
    public ShowScreen(TilesData tilesData, TilesColorSelector colorSelector,
                    TilesSelector tilesSelector) {
        this.tilesData = tilesData;
        tilesData.addTilesDataListener(this);
        this.colorSelector = colorSelector;
        this.tilesSelector = tilesSelector;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        paintColors = new PaintColors();
        JScrollPane jsp = new JScrollPane(paintColors);
        JPanel jspPane = new JPanel();        
        jspPane.setLayout(new BorderLayout());
        jspPane.add(jsp, BorderLayout.CENTER);
        jspPane.setBorder(new TitledBorder(new EtchedBorder(),
                                             "Pixels"));
        add(jspPane);
        paintTiles = new PaintTiles();
        JPanel paintPane = new JPanel();        
        paintPane.setLayout(new BorderLayout());
        paintPane.add(paintTiles, BorderLayout.CENTER);
        paintPane.setBorder(new TitledBorder(new EtchedBorder(),
                                             "Screen"));
        add(paintPane);
        image = new BufferedImage(256, 192, BufferedImage.TYPE_INT_RGB);
        pixels = image.getRaster().getDataBuffer();
        drawPixels();
    }
    
    public void tilesDataChanged() {
        drawPixels();
        paintColors.repaint();
        paintTiles.repaint();
    }
    
    private void drawPixels() {
        for (int row = 0; row < 24; row++) {
            for (int col = 0; col < 32; col++) {
                int tilesIndex = tilesData.getScreenTiles(col, row);
                for (int r = 0; r < 8; r++) {
                    int offset = ((row * 8) + r) * 256 + col * 8;
                    for (int c = 0; c < 8; c++)
                        pixels.setElem(offset + c,
                                       tilesData.getAWTValue(tilesIndex, c, r));
                }
            }
        }
    }
    
    private class PaintColors extends JComponent
        implements MouseListener, MouseMotionListener {
        
        private AffineTransform transform;
        
        public PaintColors() {
            transform = new AffineTransform();
            transform.setToScale(8, 8);
            setPreferredSize(new Dimension(2048, 1536));
            setMaximumSize(new Dimension(2048, 1536));
            setMinimumSize(new Dimension(2048, 1536));
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform save = g2.getTransform();
            g2.transform(transform);
            g2.drawImage(image, 0, 0, null);
            g2.setTransform(save);
            int count = 0;
            for (int i = 8; i < 2048; i += 8) {
                g2.setColor((++count % 8) == 0 ? Color.white : Color.black);
                g2.drawLine(i, 0, i, 1535);
            }
            count = 0;
            for (int i = 8; i < 1536; i += 8) {
                g2.setColor((++count % 8) == 0 ? Color.white : Color.black);
                g2.drawLine(0, i, 2047, i);
            }
        }
        
        public void setPixel(int x, int y) {
            int row = y / 64;
            int col = x / 64;
            int tilesIndex = tilesData.getScreenTiles(col, row);
            int r = (y % 64) / 8;
            int c = (x % 64) / 8;
            tilesData.setTilesPixel(tilesIndex, c, r,
                                  colorSelector.getColorIndex());
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
    
    private class PaintTiles extends JComponent implements MouseListener,
                                                           MouseMotionListener {
        
        private boolean mouseIn;
        
        private AffineTransform transform;
        
        public PaintTiles() {
            transform = new AffineTransform();
            transform.setToScale(2, 2);
            setPreferredSize(new Dimension(512, 384));
            setMaximumSize(new Dimension(512, 384));
            setMinimumSize(new Dimension(512, 384));
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform save = g2.getTransform();
            g2.transform(transform);
            g2.drawImage(image, 0, 0, null);
            g2.setTransform(save);
            if (mouseIn) {
                int count = 0;
                for (int i = 16; i < 512; i += 16) {
                    g2.setColor((++count % 4) == 0 ?
                                Color.white : Color.black); 
                    g.drawLine(i, 0, i, 383);
                }
                count = 0;
                for (int i = 16; i < 384; i += 16) {
                    g2.setColor((++count % 4) == 0 ?
                                Color.white : Color.black); 
                    g.drawLine(0, i, 511, i);
                }
            }
        }
        
        public void setTiles(int x, int y) {
            int row = y / 16;
            int col = x / 16;
            tilesData.setScreenTiles(col, row, tilesSelector.getTilesIndex());
        }

        public void mousePressed(MouseEvent e) {
            setTiles(e.getX(), e.getY());
        }

        public void mouseDragged(MouseEvent e) {
            setTiles(e.getX(), e.getY());
        }

        public void mouseClicked(MouseEvent e) { }

        public void mouseReleased(MouseEvent e) { }

        public void mouseEntered(MouseEvent e) {
            mouseIn = true;
            repaint();
        }

        public void mouseExited(MouseEvent e) {
            mouseIn = false;
            repaint();
        }

        public void mouseMoved(MouseEvent e) { }
        
    }

}
