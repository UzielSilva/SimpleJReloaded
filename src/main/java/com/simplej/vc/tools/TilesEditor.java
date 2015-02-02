/*
 * TilesEditor.java
 * Copyright (C) 2004 Gerardo Horvilleur Martinez
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

public class TilesEditor extends JFrame implements TilesDataListener {
    
    private static final Dimension toolbarGap = new Dimension(10, 10);

    private TilesData tilesData;
    
    private ShowScreen showScreen;
    
    private TilesColorSelector colorSelector;
    
    private TilesSelector tilesSelector;
    
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
    
    public TilesEditor() throws IOException {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exit();
                }
            });
        buildTilesEditor();
        Environment env = new Environment();
        File projectsPath = new File(env.getProjectsPath());
        if (projectsPath.exists())
            currentDirectory = projectsPath;
        setSize(new Dimension(1000, 750));
        setVisible(true);
        modified = false;
    }

    private void buildTilesEditor() throws IOException {
        setTitle("simpleJ tiles editor");
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        contentPane.add(toolbar, BorderLayout.NORTH);
        JPanel mainPane = new JPanel();
        contentPane.add(mainPane, BorderLayout.CENTER);
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
            
        tilesData = new TilesData();
        tilesData.addTilesDataListener(this);

        colorSelector = new TilesColorSelector(tilesData);
        tilesSelector = new TilesSelector(tilesData);
        showScreen = new ShowScreen(tilesData, colorSelector, tilesSelector);
        mainPane.add(showScreen);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JPanel tilesPane = new JPanel();        
        tilesPane.setLayout(new BorderLayout());
        tilesPane.add(tilesSelector, BorderLayout.CENTER);
        tilesPane.setBorder(new TitledBorder(new EtchedBorder(),
                                             "Tiles"));
        panel.add(tilesPane);        
        JPanel colorPane = new JPanel();        
        colorPane.setLayout(new BorderLayout());
        colorPane.add(colorSelector, BorderLayout.CENTER);
        colorPane.setBorder(new TitledBorder(new EtchedBorder(), 
                                                 "Color registers"));
        panel.add(colorPane);
        mainPane.add(panel);

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
        menu = new JMenu("Tiles");
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
                    tilesData.flipVertical(tilesSelector.getTilesIndex());
                }
            };
    
        flipHorizontalAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    tilesData.flipHorizontal(tilesSelector.getTilesIndex());
                }
            };
          
        rotateClockwiseAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    tilesData.rotateClockwise(tilesSelector.getTilesIndex());
                }
            };
          
        rotateCounterclockwiseAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    tilesData.rotateCounterclockwise(tilesSelector.
                                                    getTilesIndex());
                }
            };
          
        openAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (modified) {
                        int option =
                            JOptionPane.showConfirmDialog
                            (TilesEditor.this,
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
                        tilesData.writeData(currentDirectory.getAbsolutePath() +
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

    public void tilesDataChanged() {
        modified = true;
    }
    
    private void saveAs() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if (currentDirectory != null)
            chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".tmap") ||
                        f.isDirectory();
                }

                public String getDescription() {
                    return "simpleJ tiles editor Map";
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
        if (!path.endsWith(".tmap"))
            path += ".tmap";
        tilesData.writeData(path);
        filename = f.getName();
        if (!filename.endsWith(".tmap"))
            filename += ".tmap";
        saveAction.setEnabled(true);
    }
    
    private void open() throws IOException {
        JFileChooser chooser = new JFileChooser();
        if (currentDirectory != null)
            chooser.setCurrentDirectory(currentDirectory);
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".tmap") ||
                        f.isDirectory();
                }

                public String getDescription() {
                    return "simpleJ tiles editor Map";
                }
            });
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return;
        File f = chooser.getSelectedFile();
        currentDirectory = f.getParentFile();
        String path = f.getAbsolutePath();
        if (!path.endsWith(".tmap"))
            path += ".tmap";
        tilesData.readData(path);
        filename = f.getName();
        if (!filename.endsWith(".tmap"))
            filename += ".tmap";
        saveAction.setEnabled(true);
        colorSelector.selectIndex(0);
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
                (TilesEditor.this,
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
                                          "simpleJ tiles editor",  
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        new TilesEditor();
    }

}
