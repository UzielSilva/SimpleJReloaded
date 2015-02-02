/*
 * LogBean.java Copyright (C) 2006 Gerardo Horvilleur Martinez
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

package com.simplej.vc.devkit;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

public class LogBean extends JTextPane {

    private final static int H_INSET = 7;

    private final static int V_INSET = 5;

    private final static Color GREEN = new Color(240, 255, 240);

    private final static Color GRAY = new Color(240, 240, 240);

    private final static Color BANNER_COLOR = Color.BLACK;

    public LogBean() {
        setEditable(false);
        setMargin(new Insets(H_INSET, V_INSET, H_INSET, V_INSET));
        setOpaque(false);
        clear();
    }

    public void append(final String s, final Color color) {
        invokeAndWait(new Runnable() {
                public void run() {
                    setEditable(true);
                    int start = getDocument().getLength();
                    select(start, start);
                    replaceSelection(s);
                    int end = getDocument().getLength();
                    select(start, end);
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setForeground(attr,
                                                 color !=  null ?
                                                 color : Color.BLACK);
                    setCharacterAttributes(attr, true);
                    select(end, end);
                    setEditable(false);
                }
            });
    }

    public void append(String s) {
        append(s, null);
    }

    public void clear() {
        setText("");
        append("simpleJ devkit 1.0\n", BANNER_COLOR);
        append("Copyright (C) 2004, 2005, 2006 Gerardo Horvilleur\n",
               BANNER_COLOR);
        append("http://www.simplej.org\n", BANNER_COLOR);
    }

    public PrintStream getPrintStream(Color color) {
        return new PrintStream(new LogOutputStream(color), true);
    }

    public void paintComponent(Graphics g) {
        Dimension dim = getSize();
        int width = dim.width;
        int height = dim.height - V_INSET * 2;
        FontMetrics fm = getFontMetrics(getFont());
        int lineHeight = fm.getHeight();
        for (int v = V_INSET + 2; v < height; v += lineHeight * 2) {
            g.setColor(GREEN);
            g.fillRect(0, v, width, lineHeight);
            g.setColor(GRAY);
            g.fillRect(0, v + lineHeight, width, lineHeight);
        }
        super.paintComponent(g);
    }

    private void invokeAndWait(Runnable runnable) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else
            runnable.run();
    }

    private class LogOutputStream extends OutputStream {
        private Color color;

        private StringBuffer sb;

        LogOutputStream(Color color) {
            this.color = color;
            sb = new StringBuffer();
        }

        public void write(int b) {
            sb.append((char) b);
        }

        public void flush() {
            append(sb.toString(), color);
            sb = new StringBuffer();
        }
    }

}