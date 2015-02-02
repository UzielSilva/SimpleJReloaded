/*
 * CodeChunkTests.java
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

import junit.framework.*;

import java.util.*;

public class CodeChunkTests extends TestCase {

    private CodeChunk codeChunk;

    private ExecutionContext ctx;

    protected void setUp() {
        codeChunk = new CodeChunk();
        ctx = new ExecutionContext();
    }

    protected void tearDown() {
        codeChunk = null;
        ctx = null;
    }

    public CodeChunkTests(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        Code[] codes = new Code[1024];
        for (int i = 0; i < codes.length; i++) {
            codes[i] = new CodeStub();
            codeChunk.addCode(codes[i]);
        }
        assertTrue(codeChunk.getSize() == codes.length);
        for (int i = 0; i < codes.length; i++)
            assertTrue(codeChunk.getCodeAt(i, ctx) == codes[i]);

        codeChunk.setCodeAt(100, codes[0]);
        assertTrue(codeChunk.getCodeAt(100, ctx) == codes[0]);
        
        boolean failed = true;
        try {
            codeChunk.getCodeAt(codes.length, ctx);
        } catch (EndOfCodeException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    private static class CodeStub implements Code {
        public void execute(ExecutionContext ctx) {
        }
    }

}