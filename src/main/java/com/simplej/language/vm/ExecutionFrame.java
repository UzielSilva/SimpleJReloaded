/*
 * ExecutionFrame.java
 * Copyright (C) 2005 Gerardo Horvilleur Martinez
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

package com.simplej.language.vm;

import java.io.*;

public class ExecutionFrame implements Serializable {

    CodeChunk codeChunk;

    int codePtr;

    Environment environment;

    String sourceFileName;

    int sourceFileLine;

    ExecutionFrame(CodeChunk codeChunk, Environment environment,
                   String sourceFileName, int sourceFileLine) {
        this.codeChunk = codeChunk;
        this.environment = environment;
        this.sourceFileName = sourceFileName;
        this.sourceFileLine = sourceFileLine;
    }

    public Environment getEnvironment() {
        return environment;
    }

}