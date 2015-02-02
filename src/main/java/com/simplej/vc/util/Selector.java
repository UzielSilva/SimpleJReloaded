/*
 Selector.java
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

package com.simplej.vc.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Selector {

    public static String select(String[] options, String title, 
                                String messageSelect,
                                String messageCreate) {
        final JDialog jd = new JDialog((Frame) null, true);
        jd.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jd.setTitle(title);
        jd.setUndecorated(true);
        Container c = jd.getContentPane();
        JPanel pn = new JPanel();
        c.add(pn);
        pn.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createRaisedBevelBorder(),
                              BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel(createIcon("logo.png"));
        logo.setOpaque(false);
        pn.add(logo);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        pn.add(panel);
        panel.add(new JLabel(messageSelect));
        final JList jl = new JList(options);
        jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane js = new JScrollPane(jl);
        panel.add(js);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        pn.add(panel);
        panel.add(new JLabel(messageCreate));
        final JTextField name = new JTextField(20);
        panel.add(name);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        pn.add(panel);
        final JButton okBtn = new JButton("OK");
        okBtn.setEnabled(false);
        panel.add(okBtn);
        JButton cancelBtn = new JButton("Cancel");
        panel.add(cancelBtn);

        jl.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    name.setText((String) jl.getSelectedValue());
                }
            });
        jl.addMouseListener(new MouseAdapter() {
        		   public void mouseClicked(MouseEvent e) {
        		        if (e.getClickCount() == 2) {
                                    name.setText
                                        ((String) jl.getSelectedValue());
                                    jd.dispose();
        		        }
        		   }
            });	
        name.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    okBtn.setEnabled(name.getText().length() > 0);
                }

                public void removeUpdate(DocumentEvent e) {
                    okBtn.setEnabled(name.getText().length() > 0);
                }

                public void changedUpdate(DocumentEvent e) {
                }
            });
        name.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jd.dispose();
                }
            });
        okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    jd.dispose();
                }
            });
        cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    name.setText("");
                    jd.dispose();
                }
            });
        jd.pack();
        jd.setResizable(false);
        Dimension dim = jd.getSize();
        jd.setLocation(new Point((1024 - dim.width) / 2,
                                 (768 - dim.height) / 2));
        jd.setVisible(true);
        return name.getText();
    }

    public static String select(String[] options, String title, 
                                String messageSelect) {
        final JDialog jd = new JDialog((Frame) null, true);
        jd.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jd.setTitle(title);
        jd.setUndecorated(true);
        Container c = jd.getContentPane();
        JPanel pn = new JPanel();
        c.add(pn);
        pn.setBorder(BorderFactory.createCompoundBorder(
                              BorderFactory.createRaisedBevelBorder(),
                              BorderFactory.createEmptyBorder(10, 10, 10, 10)));
             pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        pn.add(new JLabel(createIcon("logo.png")));
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        pn.add(panel);
        panel.add(new JLabel(messageSelect));
        final JList jl = new JList(options);
        jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane js = new JScrollPane(jl);
        panel.add(js);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        pn.add(panel);
        final JButton okBtn = new JButton("OK");
        okBtn.setEnabled(false);
        panel.add(okBtn);
        JButton cancelBtn = new JButton("Cancel");
        panel.add(cancelBtn);
        final JTextField name = new JTextField();
        jl.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                name.setText((String) jl.getSelectedValue());
            }
        });
        jl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    name.setText((String) jl.getSelectedValue());
                    jd.dispose();
                }
            }
        });	
        name.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                okBtn.setEnabled(name.getText().length() > 0);
            }
            
            public void removeUpdate(DocumentEvent e) {
                okBtn.setEnabled(name.getText().length() > 0);
            }
            
            public void changedUpdate(DocumentEvent e) {
            }
        });
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jd.dispose();
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                name.setText("");
                jd.dispose();
            }
        });
        jd.pack();
        jd.setResizable(false);
        Dimension dim = jd.getSize();
        jd.setLocation(new Point((1024 - dim.width) / 2,
                                 (768 - dim.height) / 2));
        jd.setVisible(true);
        return name.getText();
    }

    private static Icon createIcon(String path) {
        return new ImageIcon(Selector.class.getResource(path));
    }
    
}