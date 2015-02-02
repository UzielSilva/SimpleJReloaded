/*
 * CustomSecurityManager.java Copyright (C) 2004-2005 Jorge Vargas Garcia
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

import java.security.*;
import java.util.*;

import com.simplej.vc.env.Environment;

public class CustomSecurityManager extends SecurityManager {

    private Environment environment;

    private String gameName;

    private static Set notAllowed;

    public CustomSecurityManager(Environment environment, String gameName) {
        this.environment = environment;
        this.gameName = gameName;
    }

    static {
        notAllowed = new HashSet();
        notAllowed.add("queuePrintJob");
        notAllowed.add("getClassLoader");
        notAllowed.add("modifyThread");
        notAllowed.add("setFactory");
    }

    public void checkPermission(Permission arg0) {
        if (notAllowed.contains(arg0.getName()))
            throw new SecurityException("Operation not allowed: " + arg0);
    }

    public void checkWrite(String filename) {
        if (!filename.equals(environment.toSavePath(gameName)))
            throw new SecurityException("This file can't be saved: " +
                                        filename);
    }

    public void checkConnect(String host, int port) {
        throw new SecurityException("Connect operation not allowed to: " + host
                                    + ":" + port);
    }

    public void checkDelete(String filename) {
        throw new SecurityException("Delete operation not allowed: " +
                                    filename);
    }

    public void checkExec(String command) {
        throw new SecurityException("Execute operation not allowed: " +
                                    command);
    }

}
