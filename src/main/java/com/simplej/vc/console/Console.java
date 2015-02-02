/*
 * Console.java Copyright (C) 2004,2005,2006 Gerardo Horvilleur Martinez
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

package com.simplej.vc.console;

import com.simplej.vc.env.*;
import com.simplej.vc.hardware.*;
import com.simplej.language.interpreter.*;
import com.simplej.language.vm.ExecutionContext;
import com.simplej.vc.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

public class Console extends JFrame {

    private static final String LABEL_CARD = "Label";

    private static final String VIDEO_CARD = "Video";

    private static final String MENU_CARD = "Menu";

    private Environment environment;
    
    private String gameName;
    
    private VideoCanvas videoCanvas;
    
    private IAVC iavc;

    private JPanel videoPane; 

    private CardLayout videoPaneCard;
        
    private Action resetAction;
    
    private Action onOffAction;
        
    private Action cancelAction;
    
    private Action toggleAudioDelayWorkaroundAction;

    private Interpreter interpreter;
    
    private SimpleJAdapter simpleJAdapter;
    
    private URLClassLoader urlClassLoader;

    private Thread thread;

    private ExecutionContext ctx;
    
    public Console(final Environment env, String gmName) throws IOException {
        this.environment = env;
        this.gameName = gmName;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
                
                public void windowOpened(WindowEvent e) {
                    setSize(640, 480);
                    System.setSecurityManager
                        (new CustomSecurityManager(env,gameName));
                    new Thread(new Runnable() {
                            public void run() {
                                videoPaneCard.show(videoPane, LABEL_CARD);
                                try { 
                                    Thread.sleep(1500); 
                                } catch (InterruptedException err) { 
                                    err.printStackTrace();
                                    // Do Nothing.
                                }
                                createInterpreter();
                                videoCanvas.start();
                                startGame();
                            }
                        }).start();
                }
            });
        setSize(640, 480);
        buildConsole();
        setBackground(Color.BLACK);
        setUndecorated(true);
        try {
            GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
            gd.setFullScreenWindow(this);
            gd.setDisplayMode(new DisplayMode(640, 480, 32, DisplayMode.
                                              REFRESH_RATE_UNKNOWN));
        } catch (Exception e) {
            // Don't need to do anything here.
            e.printStackTrace();
        }
        validate();
        setVisible(true);
    }

    private void startGame() {
        videoPaneCard.show(videoPane, VIDEO_CARD);
        simpleJAdapter.reset();
        ctx = new ExecutionContext();
        thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        interpreter.interpretFile(ctx, 
                                                  "main.sj",
                                                  interpreter.getEnv());
                    } catch (Throwable t) {
                        t.printStackTrace();
                        Throwable e = getInnerException(t);
                        if (e instanceof SecurityException)
                            showError(e);
                        else
                            showError(t);
                    }
                    thread = null;
                }
            });
        thread.start();
        videoCanvas.requestFocus();
    }

    private void resetGame() {
        new Thread(new Runnable() {
                public void run() {
                    if (thread != null) {
                        thread.interrupt();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            // Do nothing
                        }
                        thread = null;
                    }
                    videoCanvas.unfreeze();
                    startGame();
                }
            }).start();
    }

    private void freezeGame() {
        videoCanvas.freeze();
        ctx.freeze();
        videoPaneCard.show(videoPane, MENU_CARD);
    }

    private void unfreezeGame() {
        videoPaneCard.show(videoPane, VIDEO_CARD);
        videoCanvas.unfreeze();
        ctx.unfreeze();
        videoCanvas.requestFocus();
    }
    
    private void buildConsole() throws IOException {
        setTitle("simpleJ virtual console: " + gameName);
        Container c = getContentPane();
        c.setBackground(Color.BLACK);
        videoCanvas = new VideoCanvas(getWidth() - 20);
        iavc = videoCanvas.getIAVC();
        iavc.setSaveFile(environment.toSavePath(gameName));
        videoPane = new JPanel();
        videoPaneCard = new CardLayout();
        videoPane.setLayout(videoPaneCard);
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.add(videoCanvas);
        videoPane.add(panel, VIDEO_CARD);
        JLabel label = new JLabel(createIcon("ilogo.png"));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        label.setBackground(Color.BLACK);
        panel = new JPanel(new BorderLayout());
        panel.add(label);
        videoPane.add(panel, LABEL_CARD);
        c.add(videoPane);
        
        createActionsAndListeners();

        panel = new JPanel(new BorderLayout());        
        label = new JLabel(createIcon("ilogo.png"));
        label.setBackground(Color.BLACK);
        panel.add(label, BorderLayout.NORTH);
        
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.add(Box.createVerticalGlue());
        resetAction.putValue(Action.NAME, "Reset");
        JButton button = new JButton(resetAction);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(button);
        toggleAudioDelayWorkaroundAction.putValue
            (Action.NAME, "Enable audio delay workaround");
        button = new JButton(toggleAudioDelayWorkaroundAction);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(button);
        cancelAction.putValue(Action.NAME, "Resume");
        button = new JButton(cancelAction);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(button); 
        onOffAction.putValue(Action.NAME, "Quit");
        button = new JButton(onOffAction);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        inner.add(button);
        inner.add(Box.createVerticalGlue());        
        panel.add(inner, BorderLayout.CENTER);
        videoPane.add(panel, MENU_CARD);

        videoCanvas.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                        System.exit(0);
                    else if (e.getKeyCode() == KeyEvent.VK_U)
                        resetGame();
                }
            });
                
        videoCanvas.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    freezeGame();
                }
            });
    }
    
    private void createActionsAndListeners() {        
        cancelAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    unfreezeGame();
                }
            };
        resetAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    resetGame();
                }
            };
        onOffAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            };
        toggleAudioDelayWorkaroundAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (iavc.isAudioDelayWorkaroundEnabled()) {
                        iavc.setAudioDelayWorkaround(false);
                        putValue(Action.NAME,
                                 "Enable audio delay workaround");
                    } else {
                        iavc.setAudioDelayWorkaround(true);
                        putValue(Action.NAME,
                                 "Disable audio delay workaround");
                    }
                    unfreezeGame();
                }
            };
    }
 
    private synchronized void createInterpreter() {
        try {
            interpreter = new Interpreter();
            urlClassLoader = new URLClassLoader
                (new URL[] { new URL("file://" + escapeBackslash
                                     (environment.toGamePath(gameName)))});
            interpreter.setSourceFinder(new SourceFinder() {
                    public Reader getReader(ExecutionContext ctx,
                                            String filename) 
                        throws CantAccessFileException {
                        InputStream is = 
                            urlClassLoader.getResourceAsStream(filename);
                        return new InputStreamReader(is);
                    }
                    
                    public InputStream getInputStream(ExecutionContext ctx,
                                                      String filename)
                        throws CantAccessFileException {
                        return urlClassLoader.getResourceAsStream(filename);
                    }
                });
            simpleJAdapter =
                new SimpleJAdapter(iavc, interpreter, urlClassLoader);
        } catch (Exception e) {
            showError(e);
        }
    }
    
    private void showError(Throwable t) {
        System.out.println(t.getMessage());
        try {
            GraphicsDevice gd = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getDefaultScreenDevice();
            gd.setFullScreenWindow(null);
            Console.this.toFront();
            JOptionPane.showMessageDialog
                (Console.this,
                 "An illegal operation was made by this game, \n"
                 + t.getMessage() + "\nConsole will be closed",
                 "Exception",
                 JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }    
    
    private Icon createIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }
    
    private String escapeBackslash(String s) {
        StringBuffer sb = new StringBuffer();
        if (s.charAt(0) != '/')
            sb.append('/');
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\')
                sb.append(ch);
            sb.append(ch);
        }
        return sb.toString();
    }

    private Throwable getInnerException(Throwable t) {
        while (t.getCause() != null)
            t = t.getCause();
        return t;
    }
    
    public static void main(String[] args) throws IOException {
        if ("1.4.2".compareTo(System.getProperty("java.version")) > 0) {
            JOptionPane.showMessageDialog(null, 
                                          "Error in Java version. " +
                                          "Should be 1.4.2 or greater",
                                          "simpleJ virtual console",  
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        Environment env = new Environment();
        env.extractGames("/games.zip");
        String gameName = Selector.select(env.getGameNames(),
                                          "simpleJ virtual console",
                                          "              Select a game --> ");
        if (gameName.length() == 0)
            System.exit(0);
        new Console(env, gameName);
    }
    
}
