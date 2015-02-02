/*
 * ErrorHandlingSimpleJCompiler.java
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
import antlr.collections.AST;

import java.util.*;

public class ErrorHandlingSimpleJCompiler extends SimpleJCompiler {

    private List errors = new ArrayList();

    public void reportError(RecognitionException ex) {
        errors.add(ex);
    }

    public void reportError(String s) {
        throw new RuntimeException("Unexpected error: " + s);
    }

    public void reportWarning(String s) {
        throw new RuntimeException("Unexpected warning: " + s);
    }

    public SimpleJCode compileProgram(AST t) throws CompilerException {
        SimpleJCode code = null;
        try {
            code = compilationUnit(t);
        } catch (RecognitionException e) {
            errors.add(e);
        }
        if (errors.size() != 0)
            throw new CompilerException(errors);
        return code;
    }

    public SimpleJCode compileStatement(AST t) throws CompilerException {
        SimpleJCode code = null;
        try {
            code = statementCompilationUnit(t);
        } catch (RecognitionException e) {
            errors.add(e);
        }
        if (errors.size() != 0)
            throw new CompilerException(errors);
        return code;
    }

    public SimpleJCode compileExpr(AST t) throws CompilerException {
        SimpleJCode code = null;
        try {
            code = exprCompilationUnit(t);
        } catch (RecognitionException e) {
            errors.add(e);
        }
        if (errors.size() != 0)
            throw new CompilerException(errors);
        return code;
    }

}