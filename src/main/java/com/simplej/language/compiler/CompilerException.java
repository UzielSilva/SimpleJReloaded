/*
 * CompilerException.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez
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

package com.simplej.language.compiler;

import com.simplej.language.vm.*;

import antlr.*;

import java.util.*;

public class CompilerException extends ExecutionException {

    private List errors;

    public CompilerException(List errors) {
        super(null);
        this.errors = errors;
    }

    public List getErrors() {
        return errors;
    }

    public String getName() {
        return "Compiler error";
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName()).append('\n');
        Iterator iter = errors.iterator();
        while (iter.hasNext()) {
            RecognitionException re =
                (RecognitionException) iter.next();
            sb.append(re.getMessage()).append(" at line ").
                append(re.getLine()).append(" in file ").
                append(re.getFilename()).append('\n');
        }
        return sb.toString();
    }
    
}