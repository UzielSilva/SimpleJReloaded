/*
 * CodeChunk.java
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

package com.simplej.language.vm;

import java.io.*;
import java.util.*;

public class CodeChunk implements Serializable {

    private ArrayList codes = new ArrayList();

    public Code getCodeAt(int index, ExecutionContext ctx)
        throws EndOfCodeException {
        if (index >= codes.size())
            throw new EndOfCodeException(ctx);
        return (Code) codes.get(index);
    }

    public void setCodeAt(int index, Code code) {
        codes.set(index, code);
    }
    
    public int getSize() {
        return codes.size();
    }

    public void addCode(Code code) {
        codes.add(code);
    }

    public void append(CodeChunk code) {
        for (int i = 0; i < code.codes.size(); i++)
            codes.add(code.codes.get(i));
    }

}