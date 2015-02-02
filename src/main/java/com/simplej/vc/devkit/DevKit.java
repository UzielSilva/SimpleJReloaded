/*
 * DevKit.java Copyright (C) 2004,2005,2006 Gerardo Horvilleur Martinez
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

import com.simplej.vc.devkit.viewer.*;
import com.simplej.vc.env.*;
import com.simplej.vc.hardware.*;
import com.simplej.language.interpreter.*;
import com.simplej.language.vm.ExecutionContext;
import com.simplej.language.vm.ExecutionException;
import com.simplej.language.vm.SimpleJException;
import com.simplej.vc.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.util.jar.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public class DevKit extends JFrame implements ExceptionHandler, ViewerEnv {

    private static final Dimension toolbarGap = new Dimension(10, 10);

    private static final String EDITOR_CARD = "Editor";

    private static final String VIEWER_CARD = "Viewer";

    private Environment environment;

    private String projectName;

    private JToolBar toolbar;

    private VideoCanvas videoCanvas;

    private IAVC iavc;

    private LogBean logBean;

    private JPanel programPane;

    private CardLayout programPaneCard;

    private JLabel editorLabel;

    private JLabel caretLabel;

    private JTextArea editor;

    private Viewer viewer;

    private Action startAction;

    private Action stepAction;

    private Action stopAction;

    private Action clearLogAction;

    private Action packageAction;

    private Action packageStandaloneAction;

    private Action switchProjectAction;

    private Action exitAction;

    private Action newAction;

    private Action openAction;

    private Action saveAction;

    private Action saveAsAction;

    private Action printAction;

    private Action copyAction;

    private Action cutAction;

    private Action pasteAction;

    private Action indentAction;

    private Action unindentAction;

    private Action findAction;

    private Action replaceAction;

    private Action gotoAction;

    private Action undoAction;

    private Action redoAction;

    private Action deleteNextCharAction;

    private Action deleteLineAction;

    private Action toggleAudioDelayWorkaroundAction;

    private Matcher matcher;

    private Interpreter interpreter;

    private PrintStream out;

    private PrintStream err;

    private SimpleJAdapter simpleJAdapter;

    private URLClassLoader urlClassLoader;
    
    private Thread thread;

    private String editorFileName;

    private boolean modified = true;

    private UndoManager undo = new UndoManager();

    public final static int TAB_SIZE = 2; 

    private int compoundEditCount = -1;

    private CompoundEdit compoundEdit;

    private String lastFind = "";

    private boolean singleStepping;

    private JButton stepButton;

    private boolean countMain;

    private boolean countVBI;

    private boolean countSFI;

    private boolean profileVBI;

    public DevKit(Environment env, String projName,
                  boolean countMain, boolean countVBI, boolean countSFI,
                  boolean profileVBI)
        throws IOException {
        this.environment = env;
        this.projectName = projName;
        this.countMain = countMain;
        this.countVBI = countVBI;
        this.countSFI = countSFI;
        this.profileVBI = profileVBI;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    exit();
                }

                public void windowOpened(WindowEvent e) {
                    try {
                        loadSource("main.sj");
                    } catch (IOException ex) {
                        // TODO: what?
                    }
                    videoCanvas.start();
                    videoCanvas.freeze();
                    editor.requestFocus();
                }
            });
        buildDevkit();

        setSize(1000, 750);
        setVisible(true);
    }

    private void packageProject() throws IOException {
        String filename = environment.toGamePath(projectName);
        FileOutputStream fos = new FileOutputStream(filename);
        ZipOutputStream zos = new ZipOutputStream(fos);
        String[] filenames = environment.getProjectFilenames(projectName);
        for (int i = 0; i < filenames.length; i++)
            packageFile(zos, filenames[i], projectName);
        zos.finish();
        fos.close();
    }

    private void packageFile(ZipOutputStream zos, String filename, 
                             String pname)
        throws IOException {
        String filepath = environment.toProjectFile(pname, filename);
        File file = new File(filepath);
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int bytesRead = 0;
        while (bytesRead < data.length) {
            int n = bis.read(data, bytesRead, data.length - bytesRead);
            bytesRead += n;
        }
        zos.putNextEntry(new ZipEntry(filename));
        zos.write(data, 0, data.length);
        zos.closeEntry();
    }

    private String packageStandalone() throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    return f.getName().endsWith(".jar") ||
                        f.isDirectory();
                }

                public String getDescription() {
                    return "simpleJ game standalone jar";
                }
            });
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION)
            return null;
        File f = chooser.getSelectedFile();
        String path = f.getAbsolutePath();
        if (!path.endsWith(".jar"))
            path += ".jar";
        f = new File(path);
        if (f.exists()) {
            int option =
                JOptionPane.showConfirmDialog(this,
                                              "There is already a file with " +
                                              "that name. Overwrite?",
                                              "File with same name exists!",
                                              JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION)
                return null;
        }
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Main-Class",
                            "com.simplej.vc.console.StandaloneConsole");
        FileOutputStream fos = new FileOutputStream(path);
        JarOutputStream jos = new JarOutputStream(fos, manifest);
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream is = classLoader.getResourceAsStream("filelist.txt");
        BufferedReader files =
            new BufferedReader(new InputStreamReader(is));
        String file;
        while ((file = files.readLine()) != null) {
            jos.putNextEntry(new JarEntry(file));
            if (!file.endsWith("/")) {
                byte[] buffer = new byte[4096];
                BufferedInputStream bis =
                 new BufferedInputStream(classLoader.getResourceAsStream(file));
                int n;
                while ((n = bis.read(buffer, 0, 4096)) != -1)
                    jos.write(buffer, 0, n);
            }
        }
        is.close();
        String[] filenames = environment.getProjectFilenames(projectName);
        for (int i = 0; i < filenames.length; i++)
            packageFile(jos, filenames[i], projectName);
        jos.putNextEntry(new JarEntry("game-name.txt"));
        PrintStream ps = new PrintStream(jos);
        ps.println(projectName);
        ps.flush();
        jos.finish();
        fos.close();
        return path;
    }

    private void loadSource(String fileName) throws IOException {
        if (!fileName.endsWith(".sj"))
            fileName = fileName + ".sj";
        editorFileName = fileName;
        editor.setText("");
        Document document = editor.getDocument();
        FileReader in = new FileReader(environment.toProjectFile(projectName,
                                                                 fileName));
        char[] buff = new char[4096];
        int nch;
        try {
            while ((nch = in.read(buff, 0, buff.length)) != -1)
                document.insertString(document.getLength(), 
                                      new String(buff, 0, nch), null);
        } catch (BadLocationException e) {
            // Should not happen!
            e.printStackTrace();
        }
        editor.setCaretPosition(0);
        undo.discardAllEdits();
        modified = true;
        setModified(false);
        setUndoRedoState();
    }

    public String getSourceText(String filename,
                                ExecutionContext ctx)
        throws ExecutionException {
        if (filename.equals(getFilenameForDisplay()))
            return editor.getText();
        Reader reader;
        if (filename.equals("_builtin_utils.sj"))
            reader =
              new InputStreamReader(getClass().getResourceAsStream(
                      "/com/simplej/language/builtin/_builtin_utils.sj"));
        else if (filename.equals("_iavc_utils.sj"))
            reader =
              new InputStreamReader(getClass().getResourceAsStream(
                      "/com/simplej/vc/util/_iavc_utils.sj"));
        else
            reader = interpreter.getSourceFinder().getReader(ctx, filename);
        char[] buf = new char[4096];
        int n;
        StringBuffer sb = new StringBuffer();
        try {
        while ((n = reader.read(buf, 0, buf.length)) != -1)
            sb.append(new String(buf, 0, n));
        } catch (IOException e) {
            throw new ExecutionException(ctx, "Can't read file: " + filename);
        }
        return sb.toString();
    }

    public void enableStepping() {
        invokeAndWait(new Runnable() {
                public void run () {
                    stepAction.setEnabled(true);
                    Dimension dim = stepButton.getSize();
                    stepButton.paintImmediately(0, 0, dim.width, dim.height);
                }
            });
    }

    public void disableStepping() {
        invokeAndWait(new Runnable() {
                public void run() {
                    stepAction.setEnabled(false);
                    Dimension dim = stepButton.getSize();
                    stepButton.paintImmediately(0, 0, dim.width, dim.height);
                }
            });
    }

    public boolean isSteppingEnabled() {
        return stepAction.isEnabled();
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

    private void saveSourceAs(String fileName) throws IOException {
        if (!fileName.endsWith(".sj"))
            fileName = fileName + ".sj";
        String path = environment.toProjectFile(projectName, fileName);
        if (!fileName.equals(editorFileName)) {
            if (new File(path).exists()) {
                int option = JOptionPane.showConfirmDialog
                    (DevKit.this,
                     "There is already a file named " + fileName
                     + ". Overwrite?", "Overwrite file?",
                     JOptionPane.YES_NO_OPTION);
                if (option != JOptionPane.YES_OPTION)
                    return;
            }
        }
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(editor.getText().getBytes());
        fos.close();
        editorFileName = fileName;
        setModified(false);
        updateEditorLabel(editorFileName);
        setUndoRedoState();
    }

    private void setModified(boolean v) {
        if (v == true) {
            if (!modified) {
                if (editorFileName.length() > 0) {
                    saveAction.setEnabled(true);
                }
                String filename = getFilenameForDisplay();
                updateEditorLabel(filename + " (modified) ");
                modified = true;
            }
        } else {
            if (modified) {
                saveAction.setEnabled(false);
                updateEditorLabel(getFilenameForDisplay());
                modified = false;
            }
        }
        packageAction.setEnabled(!saveAction.isEnabled());
        packageStandaloneAction.setEnabled(!saveAction.isEnabled());
    }

    public void vbiException(Throwable t) {
        showError("VBI error:", t);
    }

    public void sfiException(Throwable t) {
        showError("SFI error:", t);
    }

    private void stop() {
        stopAction.setEnabled(false);
        stepAction.setEnabled(false);
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
                    singleStepping = false;
                    enableEdit();
                    reset();
                    editor.requestFocus();
                    videoCanvas.freeze();
                    startAction.setEnabled(true);
                    stepAction.setEnabled(true);
                    if (profileVBI)
                        simpleJAdapter.printVBIProfile();
                }
            }).start();
    }
    
    private void reset() {
        simpleJAdapter.reset();
    }

    private void enableEdit() {
        editor.setEnabled(true);
        packageAction.setEnabled(!modified);
        packageStandaloneAction.setEnabled(!modified);
        switchProjectAction.setEnabled(true);
        newAction.setEnabled(true);
        openAction.setEnabled(true);
        saveAction.setEnabled(modified);
        saveAsAction.setEnabled(true);
        printAction.setEnabled(true);
        copyAction.setEnabled(true);
        cutAction.setEnabled(true);
        pasteAction.setEnabled(true);
        indentAction.setEnabled(true);
        unindentAction.setEnabled(true);
        findAction.setEnabled(true);
        replaceAction.setEnabled(true);
        gotoAction.setEnabled(true);
        setUndoRedoState();
        programPaneCard.show(programPane, EDITOR_CARD);
    }

    private void disableEdit() {
        editor.setEnabled(false);
        packageAction.setEnabled(false);
        packageStandaloneAction.setEnabled(false);
        switchProjectAction.setEnabled(false);
        newAction.setEnabled(false);
        openAction.setEnabled(false);
        saveAction.setEnabled(false);
        saveAsAction.setEnabled(false);
        printAction.setEnabled(false);
        copyAction.setEnabled(false);
        cutAction.setEnabled(false);
        pasteAction.setEnabled(false);
        indentAction.setEnabled(false);
        unindentAction.setEnabled(false);
        findAction.setEnabled(false);
        replaceAction.setEnabled(false);
        gotoAction.setEnabled(false);
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);
    }

    private void setUndoRedoState() {
        undoAction.setEnabled(undo.canUndo());
        redoAction.setEnabled(undo.canRedo());
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

    private void showError(String title, Throwable t) {
        Throwable cause = t.getCause();
        // Don't display InterruptedException
        if (t instanceof InterruptedException ||
            cause instanceof InterruptedException)
            return;
        err.println("-------------------------------");
        err.println(title);
        err.println(cause != null ? cause : t);
        err.println("-------------------------------");
        Toolkit.getDefaultToolkit().beep();
    }

    private static String selectProject(Environment env) throws IOException {
        String projectName = Selector.select
            (env.getProjectNames(),
             "simpleJ devkit",
             "Select an existing project --> ",
             "or type the name of a new project -->");
        if (projectName.length() == 0)
            return null;
        if (!env.projectExists(projectName))
            env.createProject(projectName);
        return projectName;
    }

    private void switchToProject(String projName) {
        this.projectName = projName;
        setDevkitTitle();
        logBean.clear();
        try {
            resetAdapter();
            loadSource("main.sj");
        } catch (IOException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
    }

    private void setDevkitTitle() {
        setTitle("simpleJ devkit: " + projectName);
    }

    private void buildDevkit() throws IOException {
        setDevkitTitle();
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        contentPane.add(toolbar, BorderLayout.NORTH);
        JPanel mainPane = new JPanel();
        contentPane.add(mainPane, BorderLayout.CENTER);
        SpringLayout mainLayout = new SpringLayout();
        mainPane.setLayout(mainLayout);

        programPane = new JPanel();
        programPaneCard = new CardLayout();
        programPane.setLayout(programPaneCard);
        JPanel editorPane = new JPanel();
        programPane.add(editorPane, EDITOR_CARD);
        editorPane.setLayout(new BorderLayout());
        JPanel editorStatusPane = new JPanel();
        editorStatusPane.setLayout(new BorderLayout());
        editorLabel = new JLabel("File: <no name>");
        editorStatusPane.add(editorLabel, BorderLayout.WEST);
        caretLabel = new JLabel("line: 1, column: 1");
        editorStatusPane.add(caretLabel, BorderLayout.EAST);
        editorPane.add(editorStatusPane, BorderLayout.NORTH);
        editor = new JTextArea(new DevkitDocument());
        editor.setLineWrap(false);
        editor.setFont(new Font("monospaced", Font.PLAIN, 12));
        editor.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane editorScrollPane = new JScrollPane(editor);
        editorPane.add(editorScrollPane);
        viewer = new Viewer(this);
        programPane.add(viewer, VIEWER_CARD);
        programPane.setBorder(new TitledBorder(new EtchedBorder(), "Program"));
        mainPane.add(programPane);

        JPanel videoPane = new JPanel();
        videoPane.setLayout(new BorderLayout());
        videoCanvas = new VideoCanvas();
        iavc = videoCanvas.getIAVC();
        iavc.setSaveFile(environment.toSavePath(projectName));
        iavc.setExceptionHandler(this);
        videoPane.add(videoCanvas, BorderLayout.CENTER);
        videoPane.setBorder(new TitledBorder(new EtchedBorder(), "Video"));
        mainPane.add(videoPane);
        
        logBean = new LogBean();
        JScrollPane logPane = new JScrollPane(logBean);
        logPane.setBorder(new TitledBorder(new EtchedBorder(), "Log"));
        mainPane.add(logPane);

        mainLayout.putConstraint(SpringLayout.EAST, videoPane, -2,
                                 SpringLayout.EAST, mainPane);
        mainLayout.putConstraint(SpringLayout.NORTH, videoPane, 2,
                                 SpringLayout.NORTH, mainPane);
        
        mainLayout.putConstraint(SpringLayout.NORTH, logPane, 2,
                                 SpringLayout.SOUTH, videoPane);
        mainLayout.putConstraint(SpringLayout.EAST, logPane, 0,
                                 SpringLayout.EAST, videoPane);
        mainLayout.putConstraint(SpringLayout.WEST, logPane, 0,
                                 SpringLayout.WEST, videoPane);

        mainLayout.putConstraint(SpringLayout.NORTH, programPane, 0,
                                 SpringLayout.NORTH, videoPane);
        mainLayout.putConstraint(SpringLayout.EAST, programPane, -2,
                                 SpringLayout.WEST, videoPane);
        mainLayout.putConstraint(SpringLayout.SOUTH, programPane, 0,
                                 SpringLayout.SOUTH, logPane);

        mainLayout.putConstraint(SpringLayout.SOUTH, mainPane, 2,
                                 SpringLayout.SOUTH, logPane);
        SpringLayout.Constraints constraints =
            mainLayout.getConstraints(videoPane);
        Dimension dim = videoPane.getPreferredSize();
        constraints.setWidth(Spring.constant(dim.width));
        constraints.setHeight(Spring.constant(dim.height));
        constraints = mainLayout.getConstraints(programPane);
        constraints.setY(Spring.constant(2));
        constraints.setX(Spring.constant(2));

        createActionsAndListeners();

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menu = new JMenu("Project");
        menuBar.add(menu);

        startAction.putValue(Action.NAME, "Start");
        startAction.putValue(Action.SMALL_ICON,
                             getImageIcon("/images/media-playback-start.png"));
        JButton button = toolbar.add(startAction);
        button.setToolTipText("Start");
        menu.add(startAction);

        stepAction.putValue(Action.NAME, "Step");
        stepAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/media-playback-step.png"));
        stepButton = toolbar.add(stepAction);
        stepButton.setToolTipText("Step");
        menu.add(stepAction);

        stopAction.putValue(Action.NAME, "Stop");
        stopAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/media-playback-stop.png"));
        button = toolbar.add(stopAction);
        button.setToolTipText("Stop");
        menu.add(stopAction);

        clearLogAction.putValue(Action.NAME, "Clear Log");
        menu.add(clearLogAction);

        menu.addSeparator();
        packageAction.putValue(Action.NAME,
                               "Package game for simpleJ virtual console");
        menu.add(packageAction);
        packageStandaloneAction.putValue(Action.NAME,
                                         "Package game as standalone jar file");
        menu.add(packageStandaloneAction);

        menu.addSeparator();
        switchProjectAction.putValue(Action.NAME,
                                     "Switch to another project...");
        menu.add(switchProjectAction);

        menu.addSeparator();
        exitAction.putValue(Action.NAME, "Exit");
        menu.add(exitAction);

        toolbar.addSeparator(toolbarGap);
        menu = new JMenu("File");
        menuBar.add(menu);
        newAction.putValue(Action.NAME, "New");
        newAction.putValue(Action.SMALL_ICON,
                           getImageIcon("/images/document-new.png"));
        button = toolbar.add(newAction);
        button.setToolTipText("New");
        menu.add(newAction);
        
        openAction.putValue(Action.NAME, "Open...");
        openAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/document-open.png"));
        button = toolbar.add(openAction);
        button.setToolTipText("Open");
        menu.add(openAction);
        
        saveAction.putValue(Action.NAME, "Save");
        saveAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/document-save.png"));
        button = toolbar.add(saveAction);
        button.setToolTipText("Save");
        menu.add(saveAction);
        
        saveAsAction.putValue(Action.NAME, "Save as...");
        saveAsAction.putValue(Action.SMALL_ICON,
                              getImageIcon("/images/document-save-as.png"));
        button = toolbar.add(saveAsAction);
        button.setToolTipText("Save as");
        menu.add(saveAsAction);

        menu.addSeparator();
        printAction.putValue(Action.NAME, "Print");
        printAction.putValue(Action.SMALL_ICON,
                             getImageIcon("/images/document-print.png"));
        button = toolbar.add(printAction);
        button.setToolTipText("Print");
        menu.add(printAction);

        menu = new JMenu("Edit");
        menuBar.add(menu);
        toolbar.addSeparator(toolbarGap);

        ActionListener setEditorFocus = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    editor.requestFocus();
                }
            };
        
        JMenuItem menuItem = menu.add(copyAction);
        copyAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/edit-copy.png"));
        menuItem.setText("Copy");
        button = toolbar.add(copyAction);
        button.setToolTipText("Copy");
        button.addActionListener(setEditorFocus);

        menuItem = menu.add(cutAction);
        cutAction.putValue(Action.SMALL_ICON,
                           getImageIcon("/images/edit-cut.png"));
        menuItem.setText("Cut");
        button = toolbar.add(cutAction);
        button.setToolTipText("Cut");
        button.addActionListener(setEditorFocus);

        menuItem = menu.add(pasteAction);
        pasteAction.putValue(Action.SMALL_ICON,
                             getImageIcon("/images/edit-paste.png"));
        menuItem.setText("Paste");
        button = toolbar.add(pasteAction);
        button.setToolTipText("Paste");
        button.addActionListener(setEditorFocus);

        menu.addSeparator();
        indentAction.putValue(Action.NAME, "Indent more");
        indentAction.putValue(Action.SMALL_ICON,
                              getImageIcon("/images/format-indent-more.png"));
        button = toolbar.add(indentAction);
        button.setToolTipText("Indent more");
        menu.add(indentAction);

        unindentAction.putValue(Action.NAME, "Indent less");
        unindentAction.putValue(Action.SMALL_ICON,
                                getImageIcon("/images/format-indent-less.png"));
        button = toolbar.add(unindentAction);
        button.setToolTipText("Indent less");
        menu.add(unindentAction);

        menu.addSeparator();
        findAction.putValue(Action.NAME, "Find...");
        findAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/edit-find.png"));
        button = toolbar.add(findAction);
        button.setToolTipText("Find");
        menu.add(findAction);

        replaceAction.putValue(Action.NAME, "Replace...");
        replaceAction.putValue(Action.SMALL_ICON,
                               getImageIcon("/images/edit-find-replace.png"));
        button = toolbar.add(replaceAction);
        button.setToolTipText("Find and replace");
        menu.add(replaceAction);

        menu.addSeparator();
        undoAction.putValue(Action.NAME, "Undo");
        undoAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/edit-undo.png"));
        button = toolbar.add(undoAction);
        button.setToolTipText("Undo");
        menu.add(undoAction);

        redoAction.putValue(Action.NAME, "Redo");
        redoAction.putValue(Action.SMALL_ICON,
                            getImageIcon("/images/edit-redo.png"));
        button = toolbar.add(redoAction);
        button.setToolTipText("Redo");
        menu.add(redoAction);

        menu = new JMenu("Audio");
        menuBar.add(menu);
        toggleAudioDelayWorkaroundAction.putValue(Action.NAME,
                                               "Enable audio delay workaround");
        menu.add(toggleAudioDelayWorkaroundAction);
        
        out = logBean.getPrintStream(Color.BLACK);
        err = logBean.getPrintStream(Color.RED);
        interpreter =
            new Interpreter(null, out, err,
                            new SourceFinder() {
                                public Reader getReader(ExecutionContext ctx,
                                                        String filename) 
                                    throws CantAccessFileException {
                                    InputStream is =
                                        urlClassLoader
                                        .getResourceAsStream(filename);
                                    if (is == null)
                                        throw new CantAccessFileException(ctx,
                                                                      filename);
                                    return new InputStreamReader(is);
                                }
                                
                                public InputStream getInputStream(
                                                          ExecutionContext ctx,
                                                          String filename) 
                                    throws CantAccessFileException {
                                    InputStream is =
                                        urlClassLoader
                                        .getResourceAsStream(filename);
                                    if (is == null)
                                        throw new CantAccessFileException(ctx,
                                                                      filename);
                                    return is;
                                }
                            });
        urlClassLoader = new URLClassLoader
            (new URL[]{ new URL("file://" +
                                escapeBackslash(environment.
                                                toProjectPath(projectName)) +
                                "/")});
        simpleJAdapter = new SimpleJAdapter(iavc, interpreter, urlClassLoader,
                                            countVBI, countSFI, profileVBI);
    }

    private void resetAdapter() throws IOException {
        urlClassLoader = new URLClassLoader
            (new URL[]{ new URL("file://" +
                                escapeBackslash(environment.
                                                toProjectPath(projectName)) +
                                "/")});
        simpleJAdapter.setClassLoader(urlClassLoader);
    }

    private void createActionsAndListeners() {
        startAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    startAction.setEnabled(false);
                    stepAction.setEnabled(false);
                    stopAction.setEnabled(true);
                    disableEdit();
                    videoCanvas.unfreeze();
                    videoCanvas.requestFocus();
                    thread = new Thread(new Runnable() {
                            public void run() {
                                ExecutionContext ctx = null;
                                try {
                                    reset();
                                    ctx = new ExecutionContext();
                                    String filename =
                                        getFilenameForDisplay();
                                    interpreter
                                        .interpretString(ctx,
                                                         editor.getText(),
                                                       filename,
                                                       interpreter.getEnv());
                                    if (countMain)
                                        System.out.println("main: " +
                                                   ctx.getInstructionCounter());
                                } catch (SimpleJException e) {
                                    showError("Error:", e);
                                } catch (Throwable t) {
                                    showError("Error:", t);
                                }
                                if (!Thread.currentThread().isInterrupted()) {
                                    SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                if (!simpleJAdapter
                                                    .hasVBIOrSFI()) {
                                                    enableEdit();
                                                    videoCanvas.freeze();
                                                    startAction
                                                        .setEnabled(true);
                                                    stepAction
                                                        .setEnabled(true);
                                                    stopAction
                                                        .setEnabled(false);
                                                    editor.requestFocus();
                                                }
                                            }
                                        });
                                }
                            }
                        });
                    thread.start();
                }
            };

        stepAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    stepAction.setEnabled(false);
                    if (singleStepping) {
                        viewer.singleStep();
                        videoCanvas.requestFocus();
                    } else {
                        startAction.setEnabled(false);
                        stopAction.setEnabled(true);
                        disableEdit();
                        videoCanvas.unfreeze();
                        videoCanvas.requestFocus();
                        programPaneCard.show(programPane, VIEWER_CARD);
                        thread = new Thread(new Runnable() {
                                public void run() {
                                    ExecutionContext ctx = null;
                                    try {
                                        reset();
                                        ctx = new ExecutionContext();
                                        viewer.setup(interpreter, ctx);
                                        singleStepping = true;
                                        String filename =
                                            getFilenameForDisplay();
                                        interpreter
                                            .interpretString(ctx,
                                                             editor.getText(),
                                                             filename,
                                                             interpreter
                                                             .getEnv());
                                    } catch (SimpleJException e) {
                                        e.printStackTrace();
                                        showError("Error:", e);
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        showError("Error:", t);
                                    }
                                    if (!Thread.currentThread()
                                        .isInterrupted()) {
                                         SwingUtilities.invokeLater(
                                            new Runnable() {
                                                public void run() {
                                                    if (!simpleJAdapter
                                                        .hasVBIOrSFI()) {
                                                        enableEdit();
                                                        startAction
                                                            .setEnabled(true);
                                                        stepAction
                                                            .setEnabled(true);
                                                        stopAction
                                                            .setEnabled(false);
                                                        singleStepping = false;
                                                        editor.requestFocus();
                                                        videoCanvas.freeze();
                                                    }
                                                }
                                            });
                                    }
                                }
                            });
                        thread.start();
                    }
                }
            };

        stopAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    stop();
                }
            };
        stopAction.setEnabled(false);

        clearLogAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    logBean.clear();
                    editor.requestFocus();
                }
            };

        packageAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        packageProject();
                    } catch (Exception ex) {
                        showError("Package error:", ex);
                    }
                    JOptionPane.showMessageDialog(DevKit.this,
                                                  projectName +
                                                  " packaged for simpleJ " +
                                                  "Virtual Console",
                                                  "Game packaged",
                                               JOptionPane.INFORMATION_MESSAGE);
                    editor.requestFocus();
                }
            };

        packageStandaloneAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String path = null;
                    try {
                        path = packageStandalone();
                    } catch (Exception ex) {
                        showError("Package error:", ex);
                    }
                    if (path != null) {
                        JOptionPane.showMessageDialog(DevKit.this,
                                                      projectName +
                                                      " packaged as " + path,
                                                      "Game packaged",
                                              JOptionPane.INFORMATION_MESSAGE);
                    }
                    editor.requestFocus();
                }
            };

        switchProjectAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (modified) {
                        int option = JOptionPane.showConfirmDialog
                            (DevKit.this,
                             "There are unsaved changes in " + 
                             getFilenameForDisplay()
                             + ". Switch to another project anyway?",
                             "Unsaved changes!",
                             JOptionPane.YES_NO_OPTION);
                        if (option != JOptionPane.YES_OPTION) {
                            editor.requestFocus();
                            return;
                        }
                    }
                    try {
                        String projName = selectProject(environment);
                        if (projName != null)
                            switchToProject(projName);
                    } catch (IOException ex) {
                        // Shouldn't happen
                        ex.printStackTrace();
                    }
                    editor.requestFocus();
                }
            };

        exitAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    exit();
                }
            };

        InputMap inputMap = editor.getInputMap();
        int modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        newAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (modified) {
                        int option = JOptionPane.showConfirmDialog
                            (DevKit.this,
                             "There are unsaved changes in " + 
                             getFilenameForDisplay()
                             + ". Discard?", "Unsaved changes!",
                             JOptionPane.YES_NO_OPTION);
                        if (option != JOptionPane.YES_OPTION)
                            return;
                    }
                    editorFileName = "";
                    editor.setText("");
                    undo.discardAllEdits();
                    modified = true;
                    setModified(false);
                    setUndoRedoState();
                    editor.requestFocus();
                }
            };
        KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_N, modifier);
        inputMap.put(key, newAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        inputMap.put(key, newAction);

        openAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (modified) {
                    int option = JOptionPane.showConfirmDialog
                        (DevKit.this,
                         "There are unsaved changes in " + 
                         getFilenameForDisplay()
                         + ". Discard?", "Unsaved changes!",
                         JOptionPane.YES_NO_OPTION);
                    if (option != JOptionPane.YES_OPTION) {
                        editor.requestFocus();
                        return;
                    }
                }
                String newName = Selector
                    .select(environment
                            .getProjectFilenames(projectName, ".sj"), 
                            "Open...",
                            "Select an existing file --> ",
                            "or type the name of a file -->");
                if (newName.length() == 0) {
                    editor.requestFocus();
                    return;
                }
                try {
                    loadSource(newName);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog
                        (DevKit.this,
                         "Can't open file: " + newName, "Error",
                         JOptionPane.ERROR_MESSAGE);
                }
                editor.requestFocus();
            }
        };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_O, modifier);
        inputMap.put(key, openAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK);
        inputMap.put(key, openAction);

        saveAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    if (editorFileName != null && editorFileName.length() > 0) {
                        try {
                            saveSourceAs(editorFileName);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog
                                (DevKit.this,
                                 "Can't save file: " + 
                                 editorFileName, "Error",
                                 JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        String newName = Selector.select
                            (environment
                             .getProjectFilenames(projectName, ".sj"),
                             "Save As...", "Select an existing file --> ",
                             "or type the name of a new file -->");
                        if (newName.length() == 0) {
                            editor.requestFocus();
                            return;
                        }
                        try {
                            saveSourceAs(newName);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog
                                (DevKit.this,
                                 "Can't save file: " + newName, "Error",
                                 JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    editor.requestFocus();
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_S, modifier);
        inputMap.put(key, saveAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
        inputMap.put(key, saveAction);

        saveAsAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String newName = Selector
                        .select(environment
                                .getProjectFilenames(projectName, ".sj"),
                                "Save As...", "Select an existing file --> ",
                                "or type the name of a new file -->");
                    if (newName.length() == 0) {
                        editor.requestFocus();
                        return;
                    }
                    try {
                        saveSourceAs(newName);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog
                            (DevKit.this,
                             "Can't save file: " + newName, "Error",
                             JOptionPane.ERROR_MESSAGE);
                    }
                    editor.requestFocus();
                }
            };

        printAction = new AbstractAction("Print") {
            public void actionPerformed(ActionEvent e) { 
                Properties props = new Properties();
                String filename = getFilenameForDisplay();
                PrintJob pjob = getToolkit().getPrintJob(DevKit.this,
                        filename, props);
                if (pjob != null) {
                    Graphics pg = pjob.getGraphics();
                    if (pg != null) {
                        PrintUtil.print(pjob, pg, editor.getText(),
                                "simpleJ devkit. Project: "
                                + projectName + "   File: "
                                + filename,
                        "http://www.simplej.com");
                        pg.dispose();
                    }
                    pjob.end();
                    editor.requestFocus();
                }
            }
        };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, modifier);
        inputMap.put(key, printAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        inputMap.put(key, printAction);

        ActionMap actionMap = editor.getActionMap();
        cutAction = (Action)
            actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                              modifier)));
        copyAction = (Action)
            actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                              modifier)));
        pasteAction = (Action)
            actionMap.get(inputMap.get(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                              modifier)));
        findAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    String text = editor.getText();
                    String p = JOptionPane.showInputDialog("Find", lastFind);
                    if (p == null)
                        return;
                    lastFind = p;
                    Pattern pattern = 
                        Pattern.compile(p, Pattern.CASE_INSENSITIVE | 
                                        Pattern.UNICODE_CASE);
                    matcher = pattern.matcher(text);
                    int pos = editor.getCaretPosition();
                    if (!matcher.find(pos)) {                       
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    int start = matcher.start();
                    int end = matcher.end();
                    editor.requestFocus();
                    editor.select(start, end);
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, modifier);
        inputMap.put(key, findAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        inputMap.put(key, findAction);        

        replaceAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    final JTextField findField = new JTextField();
                    JTextField replaceField = new JTextField();
    
                    Object[] array = {"Find:", findField, "Replace:", 
                                       replaceField};
                    JOptionPane pane = 
                        new JOptionPane(array, JOptionPane.PLAIN_MESSAGE,
                                        JOptionPane.OK_CANCEL_OPTION);
                    JDialog dialog = pane.createDialog(null, "Find/Replace");
                    dialog.addWindowListener(new WindowAdapter() {
                            public void windowOpened(WindowEvent e) {
                                findField.requestFocus();
                            }
                        });
                    dialog.setVisible(true);
                    String text = editor.getText();
                    String find = findField.getText();
                    if (find.equals(""))
                        return;
                    String findText = escapeFindText(find);
                    String replaceText = 
                        escapeReplaceText(replaceField.getText());
                    text = text.replaceAll(findText, replaceText);
                    compoundEditCount = 1;
                    editor.requestFocus();
                    editor.setText(text);
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_R, modifier);
        inputMap.put(key, replaceAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_R, Event.CTRL_MASK);
        inputMap.put(key, replaceAction);

        gotoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    Element root = editor.getDocument().getDefaultRootElement();
                    int line = 
                        GotoDialog.showGotoDialog("Go to line", DevKit.this);
                    if (line == 0)
                        return;
                    Element element = root.getElement(line - 1);
                    if (element == null)
                        return;
                    int lineStart = element.getStartOffset();
                    editor.requestFocus();
                    editor.setCaretPosition(lineStart);
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_G, modifier);
        inputMap.put(key, gotoAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_G, Event.CTRL_MASK);
        inputMap.put(key, gotoAction);

        undoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        editor.requestFocus();
                        undo.undo();
                    } catch (CannotUndoException ex) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifier);
        inputMap.put(key, undoAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK);
        inputMap.put(key, undoAction);

        redoAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        editor.requestFocus();
                        undo.redo();
                    } catch (CannotRedoException ex) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                                     modifier | Event.SHIFT_MASK);
        inputMap.put(key, redoAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 
                                     Event.CTRL_MASK | Event.SHIFT_MASK);
        inputMap.put(key, redoAction);

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
                }
            };

        editor.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    setModified(true);
                    setUndoRedoState();
                }

                public void removeUpdate(DocumentEvent e) {
                    setModified(true);
                    setUndoRedoState();
                }

                public void changedUpdate(DocumentEvent e) {
                }
            });
        editor.getDocument().addUndoableEditListener
            (new UndoableEditListener() {
                public void undoableEditHappened(UndoableEditEvent e) {
                    if (compoundEditCount >=  0) {
                        if (compoundEdit == null) {
                            compoundEdit = new CompoundEdit();
                        }
                        compoundEdit.addEdit(e.getEdit());
                        if (compoundEditCount == 0) {
                            compoundEdit.end();
                            undo.addEdit(compoundEdit);
                            compoundEdit = null;
                        }
                        compoundEditCount --;
                    } else {
                        undo.addEdit(e.getEdit());
                    }
                    setUndoRedoState();
                }
            });
        deleteNextCharAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int pos = editor.getCaretPosition();
                        if (pos != editor.getDocument().getLength() - 1)
                            editor.getDocument().remove(pos, 1);
                    } catch (BadLocationException ex) {
                        // Should not happen!
                        ex.printStackTrace();
                    }
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_D, modifier);
        inputMap.put(key, deleteNextCharAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK);
        inputMap.put(key, deleteNextCharAction);
        deleteLineAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int pos = editor.getCaretPosition();
                        Element root = editor.getDocument().
                            getDefaultRootElement();
                        int line = root.getElementIndex(pos);
                        int lineEnd = root.getElement(line).getEndOffset();
                        int len = editor.getDocument().getLength();
                        int offset = lineEnd - pos;
                        if (lineEnd - 1 == len) {
                            Toolkit.getDefaultToolkit().beep();
                        } else if (offset == 1) {
                            editor.getDocument().remove(pos, 1);
                        } else if (lineEnd - 1 < len) {
                            editor.getDocument().remove(pos, offset - 1);
                        }
                    } catch (BadLocationException ex) {
                        // Should not happen!
                        ex.printStackTrace();
                    }
                }
            };
        key = KeyStroke.getKeyStroke(KeyEvent.VK_K, modifier);
        inputMap.put(key, deleteLineAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_K, Event.CTRL_MASK);
        inputMap.put(key, deleteLineAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        editor.getKeymap().removeKeyStrokeBinding(key);
        inputMap.put(key, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int pos = editor.getCaretPosition();
                        Element root = editor.getDocument().
                            getDefaultRootElement();
                        int line = root.getElementIndex(pos);
                        int lineStart = root.getElement(line).getStartOffset();
                        int lineEnd = root.getElement(line).getEndOffset();
                        String txt = editor.getDocument().
                            getText(lineStart, lineEnd - lineStart - 1);
                        char[] chars = txt.toCharArray();
                        int offset = pos - lineStart;
                        int i;
                        for (i = 0; i < chars.length; i++) {
                            if (chars[i] != ' ' || i == offset)
                                break;
                        }
                        StringBuffer sb = new StringBuffer();
                        sb.append('\n');
                        for (int j = 0; j < i; j++) {
                            sb.append(' ');
                        }
                        editor.getDocument().
                            insertString(pos, sb.toString(), null);
                    } catch (BadLocationException ex) {
                        // Should not happen!
                        ex.printStackTrace();
                    }
                }
            });
        key = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
        editor.getKeymap().removeKeyStrokeBinding(key);
        indentAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int startPos = editor.getSelectionStart();
                        int endPos = editor.getSelectionEnd();
                        Element root = editor.getDocument().
                            getDefaultRootElement();
                        int startLine = root.getElementIndex(startPos);
                        int endLine = root.getElementIndex(endPos);
                        if (root.getElementIndex(endPos - 1) == endLine - 1 &&
                            endLine != startLine)
                            endLine--;
                        compoundEditCount = endLine - startLine;
                        for (int line = startLine; line <= endLine; line++ ) {
                            int lineStart =
                                root.getElement(line).getStartOffset();
                            int lineEnd = root.getElement(line).getEndOffset();
                            String txt = editor.getDocument().
                                getText(lineStart, lineEnd - lineStart - 1);
                            char[] chars = txt.toCharArray();
                            int i;
                            for (i = 0; i < chars.length; i++) {
                                if (chars[i] != ' ')
                                    break;
                            }
                            int tab = TAB_SIZE - (i % TAB_SIZE);
                            StringBuffer sb = new StringBuffer();
                            for (int j = 0; j < tab; j++) {
                                sb.append(' ');
                            }
                            editor.getDocument().
                                insertString(lineStart, sb.toString(), null);
                        }
                    } catch (BadLocationException ex) {
                        // Should not happen!
                        ex.printStackTrace();
                    }
                    editor.requestFocus();
                }
            };
        inputMap.put(key, indentAction);
        
        key = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK);
        editor.getKeymap().removeKeyStrokeBinding(key);
        unindentAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        int startPos = editor.getSelectionStart();
                        int endPos = editor.getSelectionEnd();
                        Element root = editor.getDocument().
                            getDefaultRootElement();
                        int startLine = root.getElementIndex(startPos);
                        int endLine = root.getElementIndex(endPos);
                        if (root.getElementIndex(endPos - 1) == endLine - 1 &&
                            endLine != startLine)
                            endLine--;
                        compoundEditCount = endLine - startLine;
                        for (int line = startLine; line <= endLine; line++ ) {
                            int lineStart =
                                root.getElement(line).getStartOffset();
                            int lineEnd = root.getElement(line).getEndOffset();
                            String txt = editor.getDocument().
                                getText(lineStart, lineEnd - lineStart - 1);
                            char[] chars = txt.toCharArray();
                            int i;
                            for (i = 0; i < chars.length; i++) {
                                if (chars[i] != ' ')
                                    break;
                            }
                            if (i == 0)
                                return;
                            int tab = i % TAB_SIZE;
                            tab = (tab == 0) ? TAB_SIZE : tab;
                            editor.getDocument().remove(lineStart, tab);
                        }
                    } catch (BadLocationException ex) {
                        // Should not happen!
                        ex.printStackTrace();
                    }
                    editor.requestFocus();
                }
            };
        inputMap.put(key, unindentAction);
        editor.addCaretListener(new CaretListener() {
                public void caretUpdate(CaretEvent e) {
                    int pos = e.getDot();
                    Element root = editor.getDocument().getDefaultRootElement();
                    int line = root.getElementIndex(pos);
                    int lineStart = root.getElement(line).getStartOffset();
                    caretLabel.
                        setText("line: " + (line + 1) + ", " +
                                "column: " + (pos - lineStart + 1));
                }
            });
    }

    private String escapeFindText(String text) {
        StringBuffer sb = new StringBuffer();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '(':
                    sb.append("\\(");
                    break;
                case ')':
                    sb.append("\\)");
                    break;
                case '{':
                    sb.append("\\{");
                    break;
                case '}':
                    sb.append("\\}");
                    break;
                case '[':
                    sb.append("\\[");
                    break;
                case ']':
                    sb.append("\\]");
                    break;
                case '|':
                    sb.append("\\|");
                    break;
                case '$':
                    sb.append("\\$");
                    break;
                case '^':
                    sb.append("\\^");
                    break;
                case '.':
                    sb.append("\\.");
                    break;
                case '?':
                    sb.append("\\?");
                    break;
                case '+':
                    sb.append("\\+");
                    break;
                case '*':
                    sb.append("\\*");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                default:
                    sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private String escapeReplaceText(String text) {
        StringBuffer sb = new StringBuffer();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '\\':
                    sb.append("\\\\");
                    break;
                case '$':
                    sb.append("\\$");
                    break;
                case '%':
                    sb.append("\\%");
                    break;
                default:
                    sb.append(chars[i]);
            }
        }
        return sb.toString();
    }

    private ImageIcon getImageIcon(String path) {
        return new ImageIcon(getClass().getResource(path));
    }

    private void exit() {
        if (modified) {
            int option = JOptionPane.showConfirmDialog
                (DevKit.this,
                 "There are unsaved changes in " + 
                 getFilenameForDisplay()
                 + ". Exit anyway?", "Unsaved changes!",
                 JOptionPane.YES_NO_OPTION);
            if (option != JOptionPane.YES_OPTION)
                return;
        }
        System.exit(0);
    }

    private String getFilenameForDisplay() {
        String filename = editorFileName;
        if (filename.length() == 0)
            filename = "<no name>";
        return filename;
    }

    private void updateEditorLabel(String info) {
        editorLabel.setText("File: " + info);
    }

    public static void main(String[] args) throws IOException {
        if ("1.4.2".compareTo(System.getProperty("java.version")) > 0) {
            JOptionPane.showMessageDialog(null, 
                                          "Error in Java version. " +
                                          "Should be 1.4.2 or greater",
                                          "simplej devkit",  
                                          JOptionPane.ERROR_MESSAGE);
            System.exit(2);
        }
        boolean countMain = false;
        boolean countVBI = false;
        boolean countSFI = false;
        boolean profileVBI = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-main"))
                countMain = true;
            else if (args[i].equals("-vbi"))
                countVBI = true;
            else if (args[i].equals("-sfi"))
                countSFI = true;
            else if (args[i].equals("-vbiProf"))
                profileVBI = true;
            else {
                System.err.println("Unknown option: " + args[i]);
                System.exit(2);
            }
        }
            
        Environment env = new Environment();
        env.extractProjects("/demos.zip");
        String projectName = selectProject(env);
        if (projectName != null)
            new DevKit(env, projectName,
                       countMain, countVBI, countSFI, profileVBI);
        else
            System.exit(0);
    }

    private static class DevkitDocument extends PlainDocument {
        public void insertString(int offset, String s,
                                 AttributeSet attributeSet)
            throws BadLocationException {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < s.length(); i++) {
                char ch = s.charAt(i);
                if (ch == 10 || (ch >= 32 && ch <= 127))
                    sb.append(ch);
                else
                    Toolkit.getDefaultToolkit().beep();
            }
            s = sb.toString();
            if (s.length() > 0)
                super.insertString(offset, s, attributeSet);
        }

    }

}
