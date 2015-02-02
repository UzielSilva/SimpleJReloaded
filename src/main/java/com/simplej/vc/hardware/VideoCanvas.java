/*
 VideoCanvas.java
 Copyright (C) 2004, 2007 Gerardo Horvilleur Martinez

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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class VideoCanvas extends JComponent implements Runnable {

    private final static long FRAMES_PER_SECOND = 25L;

    private final static long FREQUENCY = 1000L / FRAMES_PER_SECOND;

    private final static int FREEZE_DELAY = 2;

    private final static int IMAGE_WIDTH = 256;

    private final static int IMAGE_HEIGHT = 192;
    
    private final static int SMALL_WIDTH = 448;

    private final int width;

    private final int height;
    
    private final IAVC iavc;

    private final  BufferedImage image;

    private final WritableRaster raster;

    private final AffineTransform transform;

    private final Thread thread;

    private boolean frozen;

    private int freezeDelay;

    private Runnable repainter = new Runnable() {
            public void run() {
                paintImmediately(0, 0, width, height);
            }
        };

    public VideoCanvas() throws IOException {
        this(SMALL_WIDTH);
    }

    public VideoCanvas(int width) throws IOException {
        image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, 
                                  BufferedImage.TYPE_INT_RGB);
        raster = image.getRaster();
        iavc = new IAVC(((DataBufferInt) (raster.getDataBuffer())).getData());
        addKeyListener(iavc);
        transform = new AffineTransform();
        this.width = width;
        this.height = (int) (width * 0.75);
        double scale =  width * 1f / IMAGE_WIDTH;
        transform.setToScale(scale, scale);
        Dimension dim = new Dimension(width, height);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setMaximumSize(dim);
        setFocusable(true);
        setDoubleBuffered(false);
        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    requestFocus();
                }
            });
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY - 1);
    }

    public void start() {
        thread.start();
        iavc.start();
    }

    public IAVC getIAVC() {
        return iavc;
    }

    public void setVBI(VBI vbi) {
        iavc.setVBI(vbi);
    }

    public void setSFI(SFI sfi) {
        iavc.setSFI(sfi);
    }

    public void paint(Graphics g) {
        iavc.drawPixels();
        Graphics2D g2 = (Graphics2D) g;
        AffineTransform save = g2.getTransform();
        g2.transform(transform);
        g2.drawImage(image, 0, 0, null);
        g2.setTransform(save);
    }

    public void run() {
        try {
            while (true) {
                long now = System.currentTimeMillis();
                SwingUtilities.invokeAndWait(repainter);
                long timeToWait = now + FREQUENCY - System.currentTimeMillis();
                if (timeToWait > 0)
                    Thread.sleep(timeToWait);
                synchronized (this) {
                    if (freezeDelay > 0) {
                        freezeDelay--;
                        if (freezeDelay == 0)
                            frozen = true;
                    }
                    while (frozen)
                        wait();
                }
            }
        } catch (InterruptedException e) {
            // TODO: do what?
        } catch (java.lang.reflect.InvocationTargetException e) {
            // TODO: do what?
        }
    }

    public synchronized void freeze() {
        freezeDelay = FREEZE_DELAY;
        iavc.freeze();
    }

    public synchronized void unfreeze() {
        frozen = false;
        iavc.unfreeze();
        notify();
    }

}
