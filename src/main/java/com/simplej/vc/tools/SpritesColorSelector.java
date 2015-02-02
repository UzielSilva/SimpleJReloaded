/*
 * SpritesColorSelector.java
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
import javax.swing.*;
import javax.swing.event.*;

public class SpritesColorSelector extends JPanel implements SpritesDataListener,
                                                            ChangeListener {
    
    private SpritesData spritesData;
    
    private Colors colors;
    
    private JSlider redSlider;
    
    private JSlider greenSlider;
    
    private JSlider blueSlider;
    
    private boolean isSelect;
    
    public SpritesColorSelector(SpritesData spritesData) {
        this.spritesData = spritesData;
        spritesData.addSpritesDataListener(this);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        colors = new Colors();
        add(colors);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        add(btnPanel);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel red = new JLabel("Red");
        red.setForeground(Color.red);
        red.setAlignmentX(0f);
        panel.add(red);
        redSlider = new JSlider(0, 31, 0);
        redSlider.setMinorTickSpacing(1);
        redSlider.setMajorTickSpacing(4);
        redSlider.setPaintTicks(true);
        redSlider.setSnapToTicks(true);
        redSlider.addChangeListener(this);
        panel.add(redSlider);
        btnPanel.add(panel);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel green = new JLabel("Green");
        green.setForeground(Color.green);
        green.setAlignmentX(0f);
        panel.add(green);
        greenSlider = new JSlider(0, 31, 0);
        greenSlider.setMinorTickSpacing(1);
        greenSlider.setMajorTickSpacing(4);
        greenSlider.setPaintTicks(true);
        greenSlider.setSnapToTicks(true);
        greenSlider.addChangeListener(this);
        panel.add(greenSlider);
        btnPanel.add(panel);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel blue = new JLabel("Blue");
        blue.setForeground(Color.blue);
        blue.setAlignmentX(0f);
        panel.add(blue);
        blueSlider = new JSlider(0, 31, 0);
        blueSlider.setMinorTickSpacing(1);
        blueSlider.setMajorTickSpacing(4);
        blueSlider.setPaintTicks(true);
        blueSlider.setSnapToTicks(true);
        blueSlider.addChangeListener(this);
        panel.add(blueSlider);
        btnPanel.add(panel);
        selectIndex(0);
    }
    
    public void spritesDataChanged() {
        colors.repaint();
    }
    
    public void stateChanged(ChangeEvent e) {
        if (isSelect)
            return;
        spritesData.setColor(spritesData.getColorIndex(),redSlider.getValue(),
                          greenSlider.getValue(), blueSlider.getValue());
    }
    
    public void selectIndex(int index) {
        spritesData.setColorIndex(index);
        int v = spritesData.getIAVCValue(index);
        isSelect = true;
        redSlider.setValue(Util.getIAVCRed(v));
        greenSlider.setValue(Util.getIAVCGreen(v));
        blueSlider.setValue(Util.getIAVCBlue(v));
        isSelect = false;
        redSlider.setEnabled(true);
        greenSlider.setEnabled(true);
        blueSlider.setEnabled(true);
    }
    
    private class Colors extends JComponent {
        
        public Colors() {
            setPreferredSize(new Dimension(64, 64));
            setMaximumSize(new Dimension(64, 64));
            setMinimumSize(new Dimension(64, 64));
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    selectIndex((e.getY() / 16) * 4 + e.getX() / 16);
                    repaint();
                }
            });
        }
        
        public void paint(Graphics g) {
            for (int i = 0; i < 16; i++) {
                int x = (i % 4) * 16;
                int y = (i / 4) * 16;
                g.setColor(spritesData.getAWTColor(i));
                g.fillRect(x, y, 16, 16);
            }
            g.setColor(Color.darkGray);
            for (int i = 16; i < 64; i += 16) {
                g.drawLine(i, 0, i, 63);
                g.drawLine(0, i, 63, i);
            }
            int colorIndex = spritesData.getColorIndex();
            g.setColor(Color.white);
            int x= (colorIndex % 4) * 16;
            int y = (colorIndex / 4) * 16;
            g.drawRect(x, y, 16, 16);
        }
        
    }

}
