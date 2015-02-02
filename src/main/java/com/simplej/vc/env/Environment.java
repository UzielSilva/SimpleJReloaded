/*
 Environment.java
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

package com.simplej.vc.env;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

public class Environment {
    
    private String simpleJSaves;

    private String simpleJGames;

    private String simpleJProjects;

    public Environment() throws IOException {
        String simpleJHome = System.getProperty("simpleJ.home");
        if (simpleJHome == null) {            
            String home = System.getProperty("user.home");
            simpleJHome = home + File.separator + "simpleJ";
        }
        File file = new File(simpleJHome);
        if (!file.exists())
            file.mkdir();
        simpleJSaves = simpleJHome + File.separator + "saves";
        file = new File(simpleJSaves);
        if (!file.exists())
            file.mkdir();
        simpleJGames = simpleJHome + File.separator + "games";
        file = new File(simpleJGames);
        if (!file.exists())
            file.mkdir();
        simpleJProjects = simpleJHome + File.separator + "projects";
        file = new File(simpleJProjects);
        if (!file.exists())
            file.mkdir();
    }

    public String toSavePath(String gameName) {
        return simpleJSaves + File.separator + gameName + ".data";
    }

    public String toGamePath(String gameName) {
        return simpleJGames + File.separator + gameName + ".cart";
    }

    public String toProjectPath(String projectName) {
        return simpleJProjects + File.separator + projectName;
    }

    public String toProjectFile(String projectName, String fileName) {
        return toProjectPath(projectName) + File.separator + fileName;
    }

    public String getProjectsPath() {
        return simpleJProjects;
    }

    public String[] getGameNames() {
        File file = new File(simpleJGames);
        String[] filenames = file.list();
        List names = new ArrayList();
        for (int i = 0; i < filenames.length; i++) {
            String s = filenames[i];
            if (s.endsWith(".cart"))
                names.add(s.substring(0, s.lastIndexOf('.')));
        }
        Collections.sort(names);
        return (String[]) names.toArray(new String[names.size()]);
    }

    public boolean gameExists(String gameName) {
        File file = new File(toGamePath(gameName));
        return file.exists();
    }

    public String[] getProjectNames() {
        File file = new File(simpleJProjects);
        String[] filenames = file.list();
        List names = new ArrayList();
        for (int i = 0; i < filenames.length; i++) {
            File f = new File(toProjectPath(filenames[i]));
            if (file.isDirectory())
                names.add(filenames[i]);
        }
        Collections.sort(names);
        return (String[]) names.toArray(new String[names.size()]);
    }

    public String[] getProjectFilenames(String projectName, String ext) {
        File file = new File(toProjectPath(projectName));
        String[] filenames = file.list();
        List names = new ArrayList();
        for (int i = 0; i < filenames.length; i++) {
            String s = filenames[i];
            if (s.endsWith(ext))
                names.add(s);
        }
        Collections.sort(names);
        return (String[]) names.toArray(new String[names.size()]);
    }

    public String[] getProjectFilenames(String projectName) {
        File file = new File(toProjectPath(projectName));
        String[] filenames = file.list();
        List names = new ArrayList();
        for (int i = 0; i < filenames.length; i++) {
            String s = filenames[i];
            names.add(s);
        }
        Collections.sort(names);
        return (String[]) names.toArray(new String[names.size()]);
    }

    public boolean projectExists(String projectName) {
        File file = new File(toProjectPath(projectName));
        return file.exists();
    }

    public String importProject(String host) throws IOException {
        Socket socket = new Socket(host, 8080);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        String projectName = in.readUTF();
        File file = new File(toProjectPath(projectName));
        file.mkdir();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            String filename = in.readUTF();
            int len = in.readInt();
            byte[] data = new byte[len];
            int bytesRead = 0;
            while (bytesRead < data.length) {
                int n = in.read(data, bytesRead, data.length - bytesRead);
                bytesRead += n;
            }      
            FileOutputStream out = 
                new FileOutputStream(toProjectFile(projectName, filename));
            out.write(data);
            out.close();            
        }
        return projectName;
    }
    
    public void createProject(String projectName) throws IOException {
        File file = new File(toProjectPath(projectName));
        file.mkdir();
        FileOutputStream fos = 
            new FileOutputStream(toProjectFile(projectName, "main.sj"));
        fos.close();
    }
    
    public void extractProjects(String archiveName) throws IOException {
        byte[] buffer = new byte[4096];
        Set newProjects = new HashSet();
        InputStream is = getClass().getResourceAsStream(archiveName);
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName();
            String projectName = getProjectName(entryName);
            if (entry.isDirectory()) {
                if (!projectExists(projectName)) {
                    createProject(projectName);
                    newProjects.add(projectName);
                }
            } else {
                if (newProjects.contains(projectName)) {
                    String filename = getProjectFileName(entryName);
                    FileOutputStream fos =
                        new FileOutputStream(toProjectFile(projectName, 
                                                           filename));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int n;
                    while ((n = zis.read(buffer, 0, 4096)) != -1)
                        bos.write(buffer, 0, n);
                    bos.close();
                }
            }
        }
    }

    public void extractGames(String archiveName) throws IOException {
        byte[] buffer = new byte[4096];
        InputStream is = getClass().getResourceAsStream(archiveName);
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                String entryName = entry.getName();
                String gameName = getGameName(entryName);
                if (!gameExists(gameName)) {
                    FileOutputStream fos = 
                        new FileOutputStream(toGamePath(gameName));
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    int n;
                    while ((n = zis.read(buffer, 0, 4096)) != -1)
                        bos.write(buffer, 0, n);
                    bos.close();
                }
            }
        }
    }
    
    private String getProjectName(String entryName) {
        return entryName.substring(0, entryName.indexOf("/"));
    }
    
    private String getProjectFileName(String entryName) {
        return entryName.substring(entryName.indexOf("/") + 1);
    }

    private String getGameName(String entryName) {
        return entryName.substring(0, entryName.indexOf(".cart"));
    }
    
}
