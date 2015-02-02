/*
 * LinesAST.java
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

import antlr.*;
import antlr.collections.*;

public class LinesAST extends CommonAST {

    private int line;

    private int column;

    public LinesAST() {
        super();
    }

    public LinesAST(Token tok) {
        super(tok);
    }

    public void initialize(int t, String txt) {
        super.initialize(t, txt);
        line = -1;
        column = -1;
    }

    public void initialize(AST t) {
        super.initialize(t);
        if (t instanceof LinesAST) {
            LinesAST lt = (LinesAST) t;
            line = lt.getLine();
            column = lt.getColumn();
        }
    }

    public void initialize(Token tok) {
        super.initialize(tok);
        line = tok.getLine();
        column = tok.getColumn();
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}