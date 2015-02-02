/*
 * ExtensibleSourceFinder.java
 * Copyright (C) 2005,2006 Gerardo Horvilleur Martinez
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

package com.simplej.language.interpreter;

import com.simplej.language.vm.*;

import java.io.*;
import java.util.*;

public class ExtensibleSourceFinder implements SourceFinder {

    private List sourceFinders = new ArrayList();

    public synchronized void addSourceFinder(SourceFinder sourceFinder) {
        if (sourceFinder == null)
            throw new NullPointerException("source finder is null");
        sourceFinders.add(sourceFinder);
    }

    public synchronized void removeSourceFinder(SourceFinder sourceFinder) {
        if (sourceFinder == null)
            throw new NullPointerException("source finder is null");
        sourceFinders.remove(sourceFinder);
    }

    public synchronized Reader getReader(ExecutionContext ctx, String filename)
        throws CantAccessFileException {
        Iterator iter = sourceFinders.iterator();
        while (iter.hasNext()) {
            SourceFinder sourceFinder = (SourceFinder) iter.next();
            try {
                Reader reader = sourceFinder.getReader(ctx, filename);
                if (reader != null)
                    return reader;
            } catch (Exception e) {
                // Do nothing, just try the next reader
            }
        }
        throw new CantAccessFileException(ctx, filename);
    }

    public synchronized InputStream getInputStream(ExecutionContext ctx,
                                                   String filename)
      throws CantAccessFileException {
        Iterator iter = sourceFinders.iterator();
        while (iter.hasNext()) {
            SourceFinder sourceFinder = (SourceFinder) iter.next();
            try {
                InputStream inputStream =
                    sourceFinder.getInputStream(ctx, filename);
                if (inputStream != null)
                    return inputStream;
            } catch (Exception e) {
                // Do nothing, just try the next reader
            }
        }
        throw new CantAccessFileException(ctx, filename);
    }

}
