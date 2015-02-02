/*
 * SourceView.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import javax.swing.*;

public class SourceView extends JComponent implements Scrollable {

    private static Font FONT = new Font("monospaced", Font.PLAIN, 12);

    private List lines = new ArrayList();

    private String longestLine;

    private int selectedLine;

    private int upToLine;

    public void setText(String text) {
        lines = new ArrayList();
        longestLine = "";
        StringBuffer sb = new StringBuffer();
        int len = text.length();
        for (int i = 0; i < len; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                String line = sb.toString();
                lines.add(line);
                if (line.length() > longestLine.length())
                    longestLine = line;
                sb = new StringBuffer();
            } else
                sb.append(ch);
        }
        if (sb.length() > 0) {
            String line = sb.toString();
            lines.add(line);
            if (line.length() > longestLine.length())
                longestLine = line;
        }
        Dimension dim = new Dimension(400, 400);
        Graphics g = getGraphics();
        if (g != null) {
            g.setFont(FONT);
            FontMetrics fm = g.getFontMetrics();
            Insets insets = getInsets();
            int w = 0;
            if (longestLine != null)
                for (int i = 0; i < longestLine.length(); i++)
                    w += fm.charWidth(longestLine.charAt(i));
            w = w + insets.left + insets.right;
            w = Math.max(w, getParent().getSize().width);
            int h = lines.size() * fm.getHeight() + insets.top + insets.bottom;
            h = Math.max(h, getParent().getSize().height);
            dim = new Dimension(w, h);
        }
        setSize(dim);
        setPreferredSize(dim);
        setMinimumSize(dim);
    }

    public void paint(Graphics g) {
        Dimension dim = getSize();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, dim.width, dim.height);
        g.setFont(FONT);
        Insets insets = getInsets();
        FontMetrics fm = g.getFontMetrics();
        if (selectedLine != 0) {
            g.setColor(Color.YELLOW);
            g.fillRect(insets.left,
                       (selectedLine - 1) * fm.getHeight() + insets.top,
                       dim.width - insets.left - insets.right, fm.getHeight());
        }
        if (upToLine != 0) {
            g.setColor(Color.GRAY);
            g.fillRect(insets.left,
                       (upToLine - 1) * fm.getHeight() + insets.top,
                       dim.width - insets.left - insets.right, fm.getHeight());
        }
        g.setColor(Color.BLACK);
        int dy = insets.top + fm.getAscent();
        for (int i = 0; i < lines.size(); i++) {
            String line = (String) lines.get(i);
            g.drawString(line, insets.left, dy);
            dy += fm.getHeight();
        }
    }

    public int pixelToLine(int y) {
        Graphics g = getGraphics();
        g.setFont(FONT);
        FontMetrics fm = g.getFontMetrics();
        Insets insets = getInsets();
        return (y - insets.top) / fm.getHeight() + 1;
    }

    public void selectLine(int line) {
        upToLine = 0;
        selectedLine = line;
        if (!(getParent() instanceof JViewport))
            return;
        JViewport viewport = (JViewport) getParent();
        Graphics g = getGraphics();
        if (g == null)
            return;
        g.setFont(FONT);
        FontMetrics fm = g.getFontMetrics();
        Dimension dim = viewport.getSize();
        Point viewPos = viewport.getViewPosition();
        Insets insets = getInsets();
        int yMin = (selectedLine - 1) * fm.getHeight() + insets.top;
        if (yMin < viewPos.y ||
            yMin + fm.getHeight() > viewPos.y + dim.height) {
            int visibleLines = dim.height / fm.getHeight();
            int firstVisibleLine = selectedLine - 1 - visibleLines / 2;
            if (firstVisibleLine + visibleLines / 2 >= lines.size())
                firstVisibleLine = lines.size() - 1 - visibleLines / 2;
            if (firstVisibleLine < 0)
                firstVisibleLine = 0;
            viewport.setViewPosition(new Point(0,
                                               firstVisibleLine *
                                               fm.getHeight()));
        }
        viewport.repaint();
    }

    public void setUpToLine(int line) {
        upToLine = line;
        repaint();
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
                                           int orientation,
                                           int direction) {
        if (orientation == SwingConstants.VERTICAL)
            return visibleRect.height;
        else
            return 100;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
                                          int orientation,
                                          int direction) {
        Graphics g = getGraphics();
        if (g == null || orientation == SwingConstants.HORIZONTAL)
            return 10;
        g.setFont(FONT);
        FontMetrics fm = g.getFontMetrics();
        return fm.getHeight();
    }

}