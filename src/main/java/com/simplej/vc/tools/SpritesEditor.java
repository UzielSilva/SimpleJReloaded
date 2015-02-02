/*
 * SpritesEditor.java
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

import com.simplej.vc.env.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;

public class SpritesEditor extends JFrame implements SpritesDataListener {

    private final static Dimension toolbarGap = new Dimension(10, 10);

    private SpritesData spritesData;

    private LargeSpritesColors largeSpritesColors;

    private LargeSpritesSelector largeSpritesSelector;

    private SmallSpritesColors smallSpritesColors;

    private SmallSpritesSelector smallSpritesSelector;

    private SpritesColorSelector spritesColorSelector;

    private JToolBar toolbar;

    private Action flipVerticalAction;

    private Action flipHorizontalAction;

    private Action rotateClockwiseAction;

    private Action rotateCounterclockwiseAction;

    private Action openAction;

    private Action saveAction;

    private Action saveAsAction;

    private boolean modified;

    private String filename = "(no name)";

    private File currentDirectory;

    public SpritesEditor() throws IOException {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exit();
                }
            });
        buildSpritesEditor();
        Environment env = new Environment();
        File projectsPath = new File(env.getProjectsPath());
        if (projectsPath.exists())
            currentDirectory = projectsPath;
        setVisible(true);
        modified = false;
    }

    private void buildSpritesEditor() throws IOException {
        setTitle("simpleJ sprites editor");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        contentPane.add(toolbar, BorderLayout.NORTH);
        JPanel mainPane = new JPanel();
        contentPane.add(mainPane, BorderLayout.CENTER);
        mainPane.setLayout(new BorderLayout());

        spritesData = new SpritesData();
        spritesData.addSpritesDataListener(this);

        Box largeSprites = Box.createHorizontalBox();
        largeSprites.setBorder(new TitledBorder(new EtchedBorder(),
                                                "Large sprites"));
        largeSpritesColors = new LargeSpritesColors(spritesData);
        largeSprites.add(largeSpritesColors);
        largeSprites.add(Box.createHorizontalStrut(20));
        largeSpritesSelector = new LargeSpritesSelector(spritesData);
        largeSprites.add(largeSpritesSelector);
        mainPane.add(largeSprites, BorderLayout.NORTH);

        Box smallSprites = Box.createHorizontalBox();
        smallSprites.setBorder(new TitledBorder(new EtchedBorder(),
                                                "Small sprites"));
        smallSpritesColors = new SmallSpritesColors(spritesData);
        smallSprites.add(smallSpritesColors);
        smallSprites.add(Box.createHorizontalStrut(10));
        smallSpritesSelector = new SmallSpritesSelector(spritesData);
        smallSprites.add(smallSpritesSelector);
        mainPane.add(smallSprites, BorderLayout.CENTER);

        JPanel colors = new JPanel();
        colors.setLayout(new BorderLayout());
        colors.setBorder(new TitledBorder(new EtchedBorder(),
                                          "Sprites colors"));
        spritesColorSelector = new SpritesColorSelector(spritesData);
        colors.add(spritesColorSelector, BorderLayout.CENTER);
        mainPane.add(colors, BorderLayout.EAST);

        createActionsAndListeners();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        openAction.putValue(Action.NAME, "Open...");
        openAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/document-open.png"));
        JButton button = toolbar.add(openAction);
        button.setToolTipText("Open");
        menu.add(openAction);
        
        saveAction.putValue(Action.NAME, "Save");
        saveAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/document-save.png"));
        button = toolbar.add(saveAction);
        button.setToolTipText("Save");
        menu.add(saveAction);
        saveAction.setEnabled(false);
        
        saveAsAction.putValue(Action.NAME, "Save as...");
        saveAsAction.putValue(Action.SMALL_ICON,
                              getImageIcon("/images/document-save-as.png"));
        button = toolbar.add(saveAsAction);
        button.setToolTipText("Save as");
        menu.add(saveAsAction);
        
        toolbar.addSeparator(toolbarGap);
        menu = new JMenu("Sprites");
        menuBar.add(menu);
        flipVerticalAction.putValue(Action.NAME, "Flip Vertical");
        flipVerticalAction.putValue(Action.SMALL_ICON,
                              getImageIcon("/images/edit-flip-vertical.png"));
        button = toolbar.add(flipVerticalAction);
        button.setToolTipText("Flip Vertical");
        menu.add(flipVerticalAction);

        flipHorizontalAction.putValue(Action.NAME, "Flip Horizontal");
        flipHorizontalAction.putValue(Action.SMALL_ICON,
                              getImageIcon("/images/edit-flip-horizontal.png"));
        button = toolbar.add(flipHorizontalAction);
        button.setToolTipText("Flip Horizontal");
        menu.add(flipHorizontalAction);

        rotateClockwiseAction.putValue(Action.NAME, "Rotate Clockwise");
        rotateClockwiseAction.putValue(Action.SMALL_ICON,
                             getImageIcon("/images/edit-rotate-clockwise.png"));
        button = toolbar.add(rotateClockwiseAction);
        button.setToolTipText("Rotate Clockwise");
        menu.add(rotateClockwiseAction);

        rotateCounterclockwiseAction.putValue(Action.NAME, 
                                              "Rotate Counterclockwise");
        rotateCounterclockwiseAction.putValue(Action.SMALL_ICON,
                      getImageIcon("/images/edit-rotate-counterclockwise.png"));
        button = toolbar.add(rotateCounterclockwiseAction);
        button.setToolTipText("Rotate Counterclockwise");
        menu.add(rotateCounterclockwiseAction);
        pack();
    }

    private void createActionsAndListeners() {
        flipVerticalAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (spritesData.smallSelected())
                        spritesData.flipVerticalSmall();
                    else
                        spritesData.flipVerticalLarge();
                }
            };
    
        flipHorizontalAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (spritesData.smallSelected())
                        spritesData.flipHorizontalSmall();
                    else
                        spritesData.flipHorizontalLarge();
                }
            };
          
        rotateClockwiseAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (spritesData.smallSelected())
                        spritesData.rotateClockwiseSmall();
                    else
                        spritesData.rotateClockwiseLarge();
                }
            };
          
        rotateCounterclockwiseAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (spritesData.smallSelected())
                        spritesData.rotateCounterclockwiseSmall();
                    else
                        spritesData.rotateCounterclockwiseLarge();
                }
            };
          
        openAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (modified) {
                        int option =
                            JOptionPane.showConfirmDialog
                            (SpritesEditor.this,
                             "There are unsaved changes in " +
                             filename +
                             ". Discard?", "Unsaved changes!",
                             JOptionPane.YES_NO_OPTION);
                        if (option != JOptionPane.YES_OPTION)
                            return;
                    }
                    try {
                        open();
                        modified = false;
                    } catch (IOException ex) {
                        showError("Can't open " + filename, ex);
                    }
                }
            };
          
        saveAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        spritesData.writeData(currentDirectory.getAbsolutePath() +
                                           File.separator +
                                           filename);
                        modified = false;
                    } catch (IOException ex) {
                        showError("Can't save " + filename, ex);
                    }
                }
            };
          
        saveAsAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        saveAs();
                        modified = false;
                    } catch (IOException ex) {
                        showError("Can't save " + filename, ex);
                    }
                }
            };
    }

    public void spritesDataChanged() {
        modified = true;
    }
    
    private void saveAs() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if (currentDirectory != null)
            chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".smap") ||
                        f.isDirectory();
                }

                public String getDescription() {
                    return "simpleJ sprites editor Map";
                }
            });
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        if (f.exists()) {
            int option =
                JOptionPane.showConfirmDialog(this,
                                    "There is already a file with that name. " +
                                              "Overwrite?",
                                              "File with same name exists!",
                                              JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION)
                return;
        }
        currentDirectory = f.getParentFile();
        String path = f.getAbsolutePath();
        if (!path.endsWith(".smap"))
            path += ".smap";
        spritesData.writeData(path);
        filename = f.getName();
        if (!filename.endsWith(".smap"))
            filename += ".smap";
        saveAction.setEnabled(true);
    }
    
    private void open() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if (currentDirectory != null)
            chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".smap") ||
                        f.isDirectory();
                }

                public String getDescription() {
                    return "simpleJ sprites editor Map";
                }
            });
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        currentDirectory = f.getParentFile();
        String path = f.getAbsolutePath();
        if (!path.endsWith(".smap"))
            path += ".smap";
        spritesData.readData(path);
        filename = f.getName();
        if (!filename.endsWith(".smap"))
            filename += ".smap";
        saveAction.setEnabled(true);
        spritesColorSelector.selectIndex(0);
    }
        
    private ImageIcon getImageIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }

    private void showError(String title, Throwable t) {
        JTextArea jta = new JTextArea();
        jta.setColumns(40);
        jta.setEditable(false);
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setText(t.toString());
        JScrollPane jsp = new JScrollPane(jta);
        JOptionPane.showMessageDialog(this, jsp, title,
                                      JOptionPane.ERROR_MESSAGE);
    }
    
    private void exit() {
        if (modified) {
            int option =
                JOptionPane.showConfirmDialog
                (SpritesEditor.this,
                 "There are unsaved changes in " +
                 filename +
                 ". Exit anyway?", "Unsaved changes!",
                 JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION)
                return;
        }
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        if ("1.4.2".compareTo(System.getProperty("java.version")) > 0) {
            JOptionPane.showMessageDialog(null, 
                                          "Error in Java version. " +
                                          "Should be 1.4.2 or greater",
                                          "simpleJ sprites editor",  
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        new SpritesEditor();
    }

}