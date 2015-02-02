/*
 * Compiler.java
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

import java.io.*;
import java.util.*;

public class Compiler {

    public static SimpleJCode compileProgram(Reader in, String filename)
        throws CompilerException {
        SimpleJLexer lexer = new SimpleJLexer(in);
        lexer.setFilename(filename);
        ErrorHandlingSimpleJParser parser =
            new ErrorHandlingSimpleJParser(lexer);
        parser.setFilename(filename);
        parser.setASTNodeClass("com.simplej.language.compiler.LinesAST");
        parser.parseProgram();
        AST t = parser.getAST();
        ErrorHandlingSimpleJCompiler compiler =
            new ErrorHandlingSimpleJCompiler();
        compiler.setFilename(filename);
        return compiler.compileProgram(t);
    }

    public static SimpleJCode compileStatement(Reader in, String filename)
        throws CompilerException {
        SimpleJLexer lexer = new SimpleJLexer(in);
        lexer.setFilename(filename);
        ErrorHandlingSimpleJParser parser =
            new ErrorHandlingSimpleJParser(lexer);
        parser.setFilename(filename);
        parser.setASTNodeClass("com.simplej.language.compiler.LinesAST");
        parser.parseStatement();
        AST t = parser.getAST();
        ErrorHandlingSimpleJCompiler compiler =
            new ErrorHandlingSimpleJCompiler();
        compiler.setFilename(filename);
        return compiler.compileStatement(t);
    }

    public static SimpleJCode compileExpr(Reader in, String filename)
        throws CompilerException {
        SimpleJLexer lexer = new SimpleJLexer(in);
        lexer.setFilename(filename);
        ErrorHandlingSimpleJParser parser =
            new ErrorHandlingSimpleJParser(lexer);
        parser.setFilename(filename);
        parser.setASTNodeClass("com.simplej.language.compiler.LinesAST");
        parser.parseExpr();
        AST t = parser.getAST();
        ErrorHandlingSimpleJCompiler compiler =
            new ErrorHandlingSimpleJCompiler();
        compiler.setFilename(filename);
        return compiler.compileExpr(t);
    }

}