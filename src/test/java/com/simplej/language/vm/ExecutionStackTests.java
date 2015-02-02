/*
 * ExecutionStackTests.java
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

import junit.framework.*;

import java.util.*;

public class ExecutionStackTests extends TestCase {

    private ExecutionStack executionStack;

    private ExecutionContext ctx;

    protected void setUp() {
        executionStack = new ExecutionStack();
        ctx = new ExecutionContext();
    }

    protected void tearDown() {
        executionStack = null;
        ctx = null;
    }

    public ExecutionStackTests(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        CodeChunk[] codeChunks = new CodeChunk[1024];
        Environment[] environments = new Environment[1024];
        String[] names = new String[1024];
        for (int i = 0; i < 1024; i++) {
            codeChunks[i] = new CodeChunk();
            environments[i] = new Environment();
            names[i] = environments[i].toString();
            ExecutionFrame frame =
                new ExecutionFrame(codeChunks[i], environments[i], names[i], i);
            executionStack.push(frame);
        }
        for (int i = 1023; i >= 0; i--) {
            ExecutionFrame frame = executionStack.pop(ctx);
            assertTrue(frame.codeChunk == codeChunks[i]);
            assertTrue(frame.environment == environments[i]);
            assertTrue(frame.sourceFileName == names[i]);
            assertTrue(frame.sourceFileLine == i);
        }
        boolean failed = true;
        try {
            executionStack.pop(ctx);
        } catch (ExecutionStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
        executionStack.push(new ExecutionFrame(null, null, null, 0));
        executionStack.reset();
        failed = true;
        try {
            executionStack.pop(ctx);
        } catch (ExecutionStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
}