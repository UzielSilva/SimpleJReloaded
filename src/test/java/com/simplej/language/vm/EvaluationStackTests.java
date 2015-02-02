/*
 * EvaluationStackTests.java
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

public class EvaluationStackTests extends TestCase {

    private EvaluationStack evaluationStack;

    private ExecutionContext ctx;

    protected void setUp() {
        evaluationStack = new EvaluationStack();
        ctx = new ExecutionContext();
    }

    protected void tearDown() {
        evaluationStack = null;
        ctx = null;
    }

    public EvaluationStackTests(String name) {
        super(name);
    }

    public void testAll() throws Exception {
        for (int i = 0; i < 1024; i++)
            evaluationStack.push(new Integer(i));
        for (int i = 1023; i >= 0; i--)
            assertTrue(evaluationStack.pop(ctx).equals(new Integer(i)));
        boolean failed = true;
        try {
            evaluationStack.pop(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
        evaluationStack.push(new Integer(0));
        evaluationStack.reset();
        failed = true;
        try {
            evaluationStack.pop(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
}