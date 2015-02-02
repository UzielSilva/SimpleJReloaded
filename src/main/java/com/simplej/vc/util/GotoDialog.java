/*
 * GotoDialog.java Copyright (C) 2004-2005 Gerardo Horvilleur Martinez
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

package com.simplej.vc.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GotoDialog {
        
    public static int showGotoDialog(String title, Frame parent) {
        final JDialog jd = new JDialog(parent, true);
        jd.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jd.setTitle(title);
        Container pane = jd.getContentPane();
        pane.setLayout(new BorderLayout());
        JPanel panel = new JPanel(new FlowLayout());
        pane.add(panel, BorderLayout.NORTH);
        JLabel gotoLabel = new JLabel("Go to:");
        panel.add(gotoLabel);
        final JTextField gotoField = new JTextField(10);
        panel.add(gotoField);
        JPanel errorPanel = new JPanel();
        final JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorPanel.add(errorLabel);
        pane.add(errorPanel, BorderLayout.SOUTH);
        JPanel buttons = new JPanel(new FlowLayout());
        pane.add(buttons, BorderLayout.CENTER);
        ActionListener listener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int line = Integer.parseInt(gotoField.getText());
                        if (line < 1) 
                            errorLabel.setText("Error: line number < 1");
                        else
                            jd.dispose();
                    } catch (NumberFormatException err) {
                        errorLabel.setText(err.toString());
                    }
                }
            };
        gotoField.addActionListener(listener);
        JButton okButton = new JButton("OK");
        okButton.addActionListener(listener);
        buttons.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        buttons.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    gotoField.setText("0");
                    jd.dispose();
                }
            });
        jd.pack();
        jd.setSize(500, 120);
        jd.setLocation(((parent.getWidth() - parent.getX()) / 2) - 250, 
                       ((parent.getHeight() - parent.getY()) / 2) - 60);
        jd.setResizable(false);
        jd.setVisible(true);
        return Integer.parseInt(gotoField.getText());
    }
}
