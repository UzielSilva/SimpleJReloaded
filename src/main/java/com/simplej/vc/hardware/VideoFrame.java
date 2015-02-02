/*
 VideoFrame.java
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

import java.io.*; 
import java.awt.event.*;
import javax.swing.*;

public class VideoFrame extends JFrame {

    private VideoCanvas videoCanvas;

    public VideoFrame() throws IOException {
        videoCanvas = new VideoCanvas();
        addWindowListener(new WindowAdapter() {
                public void windowOpened(WindowEvent e) {
                    videoCanvas.start();
                }
            });
        getContentPane().add(videoCanvas);
        pack();
        setVisible(true);
    }

    public IAVC getIAVC() {
        return videoCanvas.getIAVC();
    }

    public void setVBI(VBI vbi) {
        videoCanvas.setVBI(vbi);
    }

    public void setSFI(SFI sfi) {
        videoCanvas.setSFI(sfi);
    }

}
