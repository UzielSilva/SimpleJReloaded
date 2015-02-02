/*
 * CodesTests.java
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

public class CodesTests extends TestCase {

    private ExecutionContext ctx;

    private Environment env;

    protected void setUp() throws Exception {
        ctx = new ExecutionContext();
        env = new Environment();
        ctx.executionStackPush(null, env, "", 0); // Just to set the current env
        env.createLocal("a", new Integer(5), ctx);
        env.createFinalLocal("b", new Integer(3), ctx);
        env.createLocal("c", new Double(3), ctx);
        env.createFinalLocal("t", Boolean.TRUE, ctx);
        env.createFinalLocal("f", Boolean.FALSE, ctx);
    }

    protected void tearDown() {
        ctx = null;
        env = null;
    }

    public CodesTests(String name) {
        super(name);
    }

    /*********
     * Store *
     *********/
    public void testStore() throws Exception {
        assertTrue(Codes.STORE.toString().equals("store"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(10));
        Codes.STORE.execute(ctx);
        assertTrue(env.getLocation("a",
                                   ctx).getValue().equals(new Integer(10)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(10)));

        env.createLocal("x", ctx);
        ctx.evaluationStackPush(env.getLocation("x", ctx));
        ctx.evaluationStackPush(new Integer(100));
        Codes.STORE.execute(ctx);
        assertTrue(env.getLocation("x",
                                   ctx).getValue().equals(new Integer(100)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(100)));

        boolean failed = true;
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(Uninitialized.VALUE);
        try {
            Codes.STORE.execute(ctx);
        } catch (VoidValueException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*********
     * Fetch *
     *********/
    public void testFetch() throws Exception {
        assertTrue(Codes.FETCH.toString().equals("fetch"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        Codes.FETCH.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(5)));

        boolean failed = true;
        env.createLocal("x", ctx);
        ctx.evaluationStackPush(env.getLocation("x", ctx));
        try {
            Codes.FETCH.execute(ctx);
        } catch (UninitializedLocationException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(1));
        try {
            Codes.FETCH.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /********
     * Drop *
     ********/
    public void testDrop() throws Exception {
        assertTrue(Codes.DROP.toString().equals("drop"));

        ctx.evaluationStackPush(new Integer(1));
        ctx.evaluationStackPush(new Integer(2));
        Codes.DROP.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        try {
            Codes.DROP.execute(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*******
     * Dup *
     *******/
    public void testDup() throws Exception {
        assertTrue(Codes.DUP.toString().equals("dup"));

        ctx.evaluationStackPush(new Integer(3));
        Codes.DUP.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        boolean failed = true;
        try {
            Codes.DUP.execute(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*******
     * Add *
     *******/
    public void testAdd() throws Exception {
        assertTrue(Codes.ADD.toString().equals("+"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(8)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(8)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(8)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(8)));

        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(3));
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopString().equals("string3"));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush("string");
        Codes.ADD.execute(ctx);
        assertTrue(ctx.evaluationStackPopString().equals("5string"));

        boolean failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.ADD.execute(ctx);
        } catch (NotANumberOrStringException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(Boolean.TRUE);
        try {
            Codes.ADD.execute(ctx);
        } catch (NotANumberOrStringException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreAdd() throws Exception {
        assertTrue(Codes.STORE_ADD.toString().equals("+="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(3));
        Codes.STORE_ADD.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(8)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(8)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_ADD.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /*******
     * Sub *
     *******/
    public void testSub() throws Exception {
        assertTrue(Codes.SUB.toString().equals("-"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.SUB.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.SUB.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(2)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.SUB.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(2)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.SUB.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(2)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.SUB.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush("string");
        try {
            Codes.SUB.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreSub() throws Exception {
        assertTrue(Codes.STORE_SUB.toString().equals("-="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(3));
        Codes.STORE_SUB.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(2)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_SUB.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /*******
     * Mul *
     *******/
    public void testMul() throws Exception {
        assertTrue(Codes.MUL.toString().equals("*"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.MUL.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(15)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.MUL.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(15)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.MUL.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(15)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.MUL.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(15)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.MUL.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush("string");
        try {
            Codes.MUL.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreMul() throws Exception {
        assertTrue(Codes.STORE_MUL.toString().equals("*="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(3));
        Codes.STORE_MUL.execute(ctx);
        assertTrue(env.getLocation("a",
                                   ctx).getValue().equals(new Integer(15)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(15)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_MUL.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /*******
     * Div *
     *******/
    public void testDiv() throws Exception {
        assertTrue(Codes.DIV.toString().equals("/"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.DIV.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.DIV.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(5.0/3.0)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.DIV.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(5.0/3.0)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.DIV.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(5.0/3.0)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.DIV.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush("string");
        try {
            Codes.DIV.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(0));
        try {
            Codes.DIV.execute(ctx);
        } catch (DivisionByZeroException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreDiv() throws Exception {
        assertTrue(Codes.STORE_DIV.toString().equals("/="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(3));
        Codes.STORE_DIV.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(1)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_DIV.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*********
     * Minus *
     *********/
    public void testMinus() throws Exception {
        assertTrue(Codes.MINUS.toString().equals("- (unary)"));
        
        ctx.evaluationStackPush(new Integer(5));
        Codes.MINUS.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(-5)));

        ctx.evaluationStackPush(new Double(5));
        Codes.MINUS.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(-5)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        try {
            Codes.MINUS.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*******
     * Mod *
     *******/
    public void testMod() throws Exception {
        assertTrue(Codes.MOD.toString().equals("%"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.MOD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.MOD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.MOD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.MOD.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.MOD.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush("string");
        try {
            Codes.MOD.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(0));
        try {
            Codes.MOD.execute(ctx);
        } catch (DivisionByZeroException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreMod() throws Exception {
        assertTrue(Codes.STORE_MOD.toString().equals("%="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(3));
        Codes.STORE_MOD.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(2)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(2)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_MOD.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /********
     * Incr *
     ********/
    public void testPreIncr() throws Exception {
        assertTrue(Codes.PRE_INCR.toString().equals("++ (pre)"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        Codes.PRE_INCR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(6)));
        assertTrue(env.getLocation("a",
                                   ctx).getValue().equals(new Integer(6)));

        ctx.evaluationStackPush(env.getLocation("c", ctx));
        Codes.PRE_INCR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(4)));
        assertTrue(env.getLocation("c", ctx).getValue().equals(new Double(4)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        try {
            Codes.PRE_INCR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(env.getLocation("t", ctx));
        try {
            Codes.PRE_INCR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    public void testPostIncr() throws Exception {
        assertTrue(Codes.POST_INCR.toString().equals("++ (post)"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        Codes.POST_INCR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(5)));
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(6)));

        ctx.evaluationStackPush(env.getLocation("c", ctx));
        Codes.POST_INCR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(3)));
        assertTrue(env.getLocation("c", ctx).getValue().equals(new Double(4)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        try {
            Codes.POST_INCR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(env.getLocation("t", ctx));
        try {
            Codes.POST_INCR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /********
     * Decr *
     ********/
    public void testPredecr() throws Exception {
        assertTrue(Codes.PRE_DECR.toString().equals("-- (pre)"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        Codes.PRE_DECR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(4)));

        ctx.evaluationStackPush(env.getLocation("c", ctx));
        Codes.PRE_DECR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(2)));
        assertTrue(env.getLocation("c", ctx).getValue().equals(new Double(2)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        try {
            Codes.PRE_DECR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(env.getLocation("t", ctx));
        try {
            Codes.PRE_DECR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    public void testPostdecr() throws Exception {
        assertTrue(Codes.POST_DECR.toString().equals("-- (post)"));

        ctx.evaluationStackPush(env.getLocation("a", ctx));
        Codes.POST_DECR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(5)));
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(4)));

        ctx.evaluationStackPush(env.getLocation("c", ctx));
        Codes.POST_DECR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Double(3)));
        assertTrue(env.getLocation("c", ctx).getValue().equals(new Double(2)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        try {
            Codes.POST_DECR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(env.getLocation("t", ctx));
        try {
            Codes.POST_DECR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /**********
     * BitAnd *
     **********/
    public void testBitAnd() throws Exception {
        assertTrue(Codes.BITAND.toString().equals("&"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITAND.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITAND.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITAND.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITAND.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.BITAND.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.BITAND.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreBitAnd() throws Exception {
        assertTrue(Codes.STORE_BITAND.toString().equals("&="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(6));
        Codes.STORE_BITAND.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(4)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(4)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_BITAND.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /*********
     * BitOr *
     *********/
    public void testBitOr() throws Exception {
        assertTrue(Codes.BITOR.toString().equals("|"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(7)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(7)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(7)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(7)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.BITOR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.BITOR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreBitOr() throws Exception {
        assertTrue(Codes.STORE_BITOR.toString().equals("|="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(6));
        Codes.STORE_BITOR.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(7)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(7)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_BITOR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /**********
     * BitXor *
     **********/
    public void testBitXor() throws Exception {
        assertTrue(Codes.BITXOR.toString().equals("^"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITXOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITXOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(6));
        Codes.BITXOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(6));
        Codes.BITXOR.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.BITXOR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.BITXOR.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreBitXor() throws Exception {
        assertTrue(Codes.STORE_BITXOR.toString().equals("^="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(6));
        Codes.STORE_BITXOR.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(3)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(3)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_BITXOR.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /**********
     * BitNot *
     **********/
    public void testBitNot() throws Exception {
        assertTrue(Codes.BITNOT.toString().equals("~"));
        
        ctx.evaluationStackPush(new Integer(5));
        Codes.BITNOT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(~5)));

        ctx.evaluationStackPush(new Double(5));
        Codes.BITNOT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(~5)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        try {
            Codes.BITNOT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*************
     * ShiftLeft *
     *************/
    public void testShiftLeft() throws Exception {
        assertTrue(Codes.SHIFTLEFT.toString().equals("<<"));
        
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        Codes.SHIFTLEFT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(20)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(2));
        Codes.SHIFTLEFT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(20)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(2));
        Codes.SHIFTLEFT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(20)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(2));
        Codes.SHIFTLEFT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(20)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.SHIFTLEFT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.SHIFTLEFT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreShiftLeft() throws Exception {
        assertTrue(Codes.STORE_SHIFTLEFT.toString().equals("<<="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(2));
        Codes.STORE_SHIFTLEFT.execute(ctx);
        assertTrue(env.getLocation("a",
                                   ctx).getValue().equals(new Integer(20)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(20)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_SHIFTLEFT.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /**************
     * ShiftRight *
     **************/
    public void testShiftRight() throws Exception {
        assertTrue(Codes.SHIFTRIGHT.toString().equals(">>"));
        
        ctx.evaluationStackPush(new Integer(1 << 31));
        ctx.evaluationStackPush(new Integer(31));
        Codes.SHIFTRIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(-1)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(2));
        Codes.SHIFTRIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(2));
        Codes.SHIFTRIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(2));
        Codes.SHIFTRIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.SHIFTRIGHT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.SHIFTRIGHT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreShiftRight() throws Exception {
        assertTrue(Codes.STORE_SHIFTRIGHT.toString().equals(">>="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(2));
        Codes.STORE_SHIFTRIGHT.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(1)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_SHIFTRIGHT.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /***************
     * ShiftURight *
     ***************/
    public void testShiftURight() throws Exception {
        assertTrue(Codes.SHIFTURIGHT.toString().equals(">>>"));
        
        ctx.evaluationStackPush(new Integer(1 << 31));
        ctx.evaluationStackPush(new Integer(31));
        Codes.SHIFTURIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Double(1 << 31));
        ctx.evaluationStackPush(new Double(31));
        Codes.SHIFTURIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Double(1 << 31));
        ctx.evaluationStackPush(new Integer(31));
        Codes.SHIFTURIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        ctx.evaluationStackPush(new Integer(1 << 31));
        ctx.evaluationStackPush(new Double(31));
        Codes.SHIFTURIGHT.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(new Integer(6));
        try {
            Codes.SHIFTURIGHT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(6));
        ctx.evaluationStackPush("string");
        try {
            Codes.SHIFTURIGHT.execute(ctx);
        } catch (NotANumberException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testStoreShiftURight() throws Exception {
        assertTrue(Codes.STORE_SHIFTURIGHT.toString().equals(">>>="));
        
        ctx.evaluationStackPush(env.getLocation("a", ctx));
        ctx.evaluationStackPush(new Integer(2));
        Codes.STORE_SHIFTURIGHT.execute(ctx);
        assertTrue(env.getLocation("a", ctx).getValue().equals(new Integer(1)));
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.STORE_SHIFTURIGHT.execute(ctx);
        } catch (NotALocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }
    
    /*******
     * And *
     *******/
    public void testAnd() throws Exception {
        assertTrue(Codes.AND.toString().equals("&&"));
        
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush(Boolean.TRUE);
        Codes.AND.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush(Boolean.FALSE);
        Codes.AND.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(Boolean.TRUE);
        try {
            Codes.AND.execute(ctx);
        } catch (NotABooleanException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("string");
        try {
            Codes.AND.execute(ctx);
        } catch (NotABooleanException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /******
     * Or *
     ******/
    public void testOr() throws Exception {
        assertTrue(Codes.OR.toString().equals("||"));
        
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush(Boolean.TRUE);
        Codes.OR.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush(Boolean.FALSE);
        Codes.OR.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        boolean failed = true;
        ctx.evaluationStackPush("string");
        ctx.evaluationStackPush(Boolean.TRUE);
        try {
            Codes.OR.execute(ctx);
        } catch (NotABooleanException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("string");
        try {
            Codes.OR.execute(ctx);
        } catch (NotABooleanException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*******
     * Not *
     *******/
    public void testNot() throws Exception {
        assertTrue(Codes.NOT.toString().equals("!"));
        
        ctx.evaluationStackPush(Boolean.TRUE);
        Codes.NOT.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(Boolean.FALSE);
        Codes.NOT.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        boolean failed = true;
        ctx.evaluationStackPush("string");
        try {
            Codes.NOT.execute(ctx);
        } catch (NotABooleanException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /***********
     * Greater *
     ***********/
    public void testGreater() throws Exception {
        assertTrue(Codes.GREATER.toString().equals(">"));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.GREATER.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        boolean failed = true;
        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.GREATER.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        try {
            Codes.GREATER.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        try {
            Codes.GREATER.execute(ctx);
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /****************
     * GreaterEqual *
     ****************/
    public void testGreaterEqual() throws Exception {
        assertTrue(Codes.GREATER_EQUAL.toString().equals(">="));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.GREATER_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        boolean failed = true;
        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.GREATER_EQUAL.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        try {
            Codes.GREATER_EQUAL.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        try {
            Codes.GREATER_EQUAL.execute(ctx);
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /********
     * Less *
     ********/
    public void testLess() throws Exception {
        assertTrue(Codes.LESS.toString().equals("<"));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.LESS.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        boolean failed = true;
        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.LESS.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        try {
            Codes.LESS.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        try {
            Codes.LESS.execute(ctx);
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*************
     * LessEqual *
     *************/
    public void testLessEqual() throws Exception {
        assertTrue(Codes.LESS_EQUAL.toString().equals("<="));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(3));
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.LESS_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        boolean failed = true;
        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.LESS_EQUAL.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        try {
            Codes.LESS_EQUAL.execute(ctx);
        } catch (CantCompareException e) {
            failed = false;
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        try {
            Codes.LESS_EQUAL.execute(ctx);
        } catch (NotAComparableException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /*********
     * Equal *
     *********/
    public void testEqual() throws Exception {
        assertTrue(Codes.EQUAL.toString().equals("=="));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Double(3));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(5));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);
        
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        Codes.EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);
    }

    /************
     * NotEqual *
     ************/
    public void testNotEqual() throws Exception {
        assertTrue(Codes.NOT_EQUAL.toString().equals("!="));

        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(3));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(5));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Double(3));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(3));
        ctx.evaluationStackPush(new Integer(3));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush(new Double(5));
        ctx.evaluationStackPush(new Double(5));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("b");
        ctx.evaluationStackPush("a");
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("a");
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.FALSE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush("b");
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush("a");
        ctx.evaluationStackPush(new Integer(3));
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);
        
        ctx.evaluationStackPush(Boolean.TRUE);
        ctx.evaluationStackPush("a");
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);

        ctx.evaluationStackPush(null);
        ctx.evaluationStackPush("a");
        Codes.NOT_EQUAL.execute(ctx);
        assertTrue(ctx.evaluationStackPopBoolean() == Boolean.TRUE);
    }
    
    /************
     * Constant *
     ************/
    public void testConstant() throws Exception {
        Code constant = Codes.makeConstant(new Integer(5));
        assertTrue(constant.toString().equals("5"));
        constant.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(5)));

        constant = Codes.makeConstant(null);
        assertTrue(constant.toString().equals("null"));
        constant.execute(ctx);
        assertTrue(ctx.evaluationStackPop() == null);
    }

    /************
     * Variable *
     ************/
    public void testVariable() throws Exception {
        Code variable = Codes.makeVariable("a");
        variable.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation() ==
                   env.getLocation("a", ctx));

        env.createLocal("z", ctx);
        variable = Codes.makeVariable("z");
        variable.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation() ==
                   env.getLocation("z", ctx));
    }

    /********
     * This *
     ********/
    public void testThis() throws Exception {
        assertTrue(Codes.THIS.toString().equals("this"));

        Codes.THIS.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation().getValue() == env);
    }

    /*********
     * Super *
     *********/
    public void testSuper() throws Exception {
        assertTrue(Codes.SUPER.toString().equals("super"));

        Environment parent = new Environment();
        env.setParent(parent);
        Codes.SUPER.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation().getValue() == parent);
    }

    /****************
     * MakeLocation *
     ****************/
    public void testMakeLocation() throws Exception {
        assertTrue(Codes.MAKE_LOCATION.toString().equals("make location"));
        ctx.evaluationStackPush(new Integer(23));
        Codes.MAKE_LOCATION.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation().getValue()
                   .equals(new Integer(23)));
    }

    /*********
     * Array *
     *********/
    public void testArrayValues() throws Exception {
        assertTrue(Codes.ARRAY_VALUES.toString().equals("array values"));

        Object[] values = new Object[3];
        values[0] = new Integer(3);
        ctx.evaluationStackPush(values[0]);
        values[1] = new Double(2);
        ctx.evaluationStackPush(values[1]);
        values[2] = "string";
        ctx.evaluationStackPush(values[2]);
        ctx.evaluationStackPush(new Integer(3));
        Codes.ARRAY_VALUES.execute(ctx);
        Location[] arr = ctx.evaluationStackPopArray();
        assertTrue(arr.length == 3);
        for (int i = 0; i < 3; i++)
            assertTrue(arr[i].getValue() == values[i]);

        ctx.evaluationStackPush(new Integer(0));
        Codes.ARRAY_VALUES.execute(ctx);
        arr = ctx.evaluationStackPopArray();
        assertTrue(arr.length == 0);

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(1));
        try {
            Codes.ARRAY_VALUES.execute(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testArrayDimensions() throws Exception {
        assertTrue(Codes.ARRAY_DIMENSIONS.toString()
                   .equals("array dimensions"));

        ctx.evaluationStackPush(new Integer(0));
        ctx.evaluationStackPush(new Integer(1));
        Codes.ARRAY_DIMENSIONS.execute(ctx);
        Location[] arr = ctx.evaluationStackPopArray();
        assertTrue(arr.length == 0);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(1));
        Codes.ARRAY_DIMENSIONS.execute(ctx);
        arr = ctx.evaluationStackPopArray();
        assertTrue(arr.length == 3);
        for (int i = 0; i < 3; i++)
            assertTrue(arr[i].getValue() == Uninitialized.VALUE);

        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(4));
        ctx.evaluationStackPush(new Integer(2));
        Codes.ARRAY_DIMENSIONS.execute(ctx);
        arr = ctx.evaluationStackPopArray();
        assertTrue(arr.length == 3);
        for (int i = 0; i < 3; i++) {
            Object o = arr[i].getValue();
            assertTrue(o instanceof Location[]);
            Location[] arr2 = (Location[]) o;
            assertTrue(arr2.length == 4);
            for (int j = 0; j < 4; j++)
                assertTrue(arr2[j].getValue() == Uninitialized.VALUE);
        }

        boolean failed = true;
        ctx.evaluationStackPush(new Integer(3));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.ARRAY_DIMENSIONS.execute(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /***************
     * Environment *
     ***************/
    public void testEnvironmentPairs() throws Exception {
        assertTrue(Codes.ENVIRONMENT_PAIRS.toString()
                   .equals("environment pairs"));

        ctx.evaluationStackPush("k1");
        ctx.evaluationStackPush(new Integer(10));
        ctx.evaluationStackPush("k2");
        ctx.evaluationStackPush(new Integer(5));
        ctx.evaluationStackPush(new Integer(2));
        Codes.ENVIRONMENT_PAIRS.execute(ctx);
        Environment nenv = ctx.evaluationStackPopEnvironment();
        assertTrue(nenv.getParent() == null);
        List names = nenv.getLocalLocationNames();
        assertTrue(names.size() == 2);
        assertTrue(names.get(0).equals("k1"));
        assertTrue(names.get(1).equals("k2"));
        assertTrue(nenv.getLocation("k1",
                                    ctx).getValue().equals(new Integer(10)));
        assertTrue(nenv.getLocation("k2",
                                    ctx).getValue().equals(new Integer(5)));

        ctx.evaluationStackPush(new Integer(0));
        Codes.ENVIRONMENT_PAIRS.execute(ctx);
        nenv = ctx.evaluationStackPopEnvironment();
        names = nenv.getLocalLocationNames();
        assertTrue(names.size() == 0);

        boolean failed = true;
        ctx.evaluationStackPush("k1");
        ctx.evaluationStackPush(new Integer(1));
        try {
            Codes.ENVIRONMENT_PAIRS.execute(ctx);
        } catch (EvaluationStackUnderflowException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    /************
     * SetSuper *
     ************/
    public void testSetSuper() throws Exception {
        Codes.SET_SUPER.toString().equals("->");

        ctx.evaluationStackPush(env);
        ctx.evaluationStackPush(null);
        Codes.SET_SUPER.execute(ctx);
        Environment copied = ctx.evaluationStackPopEnvironment();
        assertTrue(copied.getParent() == null);
        List names = copied.getLocalLocationNames();
        assertTrue(names.size() == 5);
        assertTrue(names.get(0).equals("a"));
        assertTrue(names.get(1).equals("b"));
        assertTrue(names.get(2).equals("c"));
        assertTrue(names.get(3).equals("f"));
        assertTrue(names.get(4).equals("t"));
        Location loc = copied.getLocation("a", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Integer(5)));
        loc = copied.getLocation("b", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Integer(3)));
        loc = copied.getLocation("c", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Double(3)));
        loc = copied.getLocation("f", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue() == Boolean.FALSE);
        loc = copied.getLocation("t", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue() == Boolean.TRUE);

        Environment parent = new Environment();
        ctx.evaluationStackPush(env);
        ctx.evaluationStackPush(parent);
        Codes.SET_SUPER.execute(ctx);
        copied = ctx.evaluationStackPopEnvironment();
        assertTrue(copied.getParent() == parent);
        names = copied.getLocalLocationNames();
        assertTrue(names.size() == 5);
        assertTrue(names.get(0).equals("a"));
        assertTrue(names.get(1).equals("b"));
        assertTrue(names.get(2).equals("c"));
        assertTrue(names.get(3).equals("f"));
        assertTrue(names.get(4).equals("t"));
        loc = copied.getLocation("a", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Integer(5)));
        loc = copied.getLocation("b", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Integer(3)));
        loc = copied.getLocation("c", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Double(3)));
        loc = copied.getLocation("f", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue() == Boolean.FALSE);
        loc = copied.getLocation("t", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue() == Boolean.TRUE);
    }

    /**********
     * EnvRef *
     **********/
    public void testEnvRef() throws Exception {
        Environment anEnv = new Environment();
        anEnv.createLocal("a", new Integer(1000), ctx);
        ctx.evaluationStackPush(anEnv);
        Code envRef = Codes.makeEnvRef("a");
        envRef.execute(ctx);
        Location loc = ctx.evaluationStackPopLocation();
        assertTrue(loc.getValue().equals(new Integer(1000)));
    }

    /************
     * ArrayRef *
     ************/
    public void testArrayRef() throws Exception {
        assertTrue(Codes.ARRAY_REF.toString().equals("[]"));

        Location[] arr = new Location[3];
        for (int i = 0; i < 3; i++)
            arr[i] = new Location(new Integer(i * 2 + 1));

        ctx.evaluationStackPush(arr);
        ctx.evaluationStackPush(new Integer(0));
        Codes.ARRAY_REF.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumberLocation().getValue()
                   .equals(new Integer(1)));
        ctx.evaluationStackPush(arr);
        ctx.evaluationStackPush(new Integer(1));
        Codes.ARRAY_REF.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumberLocation().getValue()
                   .equals(new Integer(3)));
        ctx.evaluationStackPush(arr);
        ctx.evaluationStackPush(new Integer(2));
        Codes.ARRAY_REF.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumberLocation().getValue()
                   .equals(new Integer(5)));

        boolean failed = true;
        ctx.evaluationStackPush(arr);
        ctx.evaluationStackPush(new Integer(-1));
        try {
            Codes.ARRAY_REF.execute(ctx);
        } catch (OutOfBoundsException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(arr);
        ctx.evaluationStackPush(new Integer(3));
        try {
            Codes.ARRAY_REF.execute(ctx);
        } catch (OutOfBoundsException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        ctx.evaluationStackPush(new Integer(1));
        ctx.evaluationStackPush(new Integer(2));
        try {
            Codes.ARRAY_REF.execute(ctx);
        } catch (NotAnArrayOrEnvironmentException e) {
            failed = false;
        }
        if (failed)
            fail();

        Environment anEnv = new Environment();
        anEnv.createLocal("a", new Integer(100), ctx);
        anEnv.createLocal("x", ctx);
        ctx.evaluationStackPush(anEnv);
        ctx.evaluationStackPush("a");
        Codes.ARRAY_REF.execute(ctx);
        assertTrue(ctx.evaluationStackPopNumberLocation().getValue()
                   .equals(new Integer(100)));
        ctx.evaluationStackPush(anEnv);
        ctx.evaluationStackPush("x");
        Codes.ARRAY_REF.execute(ctx);
        assertTrue(ctx.evaluationStackPopLocation().getValue() ==
                   Uninitialized.VALUE);
    }

    /**********
     * Branch *
     **********/
    public void testGoto() throws Exception {
        Codes.Branch cgoto = Codes.makeGoto();
        cgoto.setTarget(10);
        assertTrue(cgoto.toString().equals("goto 10"));

        cgoto.execute(ctx);
        assertTrue(ctx.getCodePtr() == 10);
    }

    public void testGotoIfTrue() throws Exception {
        Codes.Branch gotoIfTrue = Codes.makeGotoIfTrue();
        gotoIfTrue.setTarget(15);
        assertTrue(gotoIfTrue.toString().equals("goto if true 15"));

        ctx.evaluationStackPush(Boolean.FALSE);
        gotoIfTrue.execute(ctx);
        assertTrue(ctx.getCodePtr() == 0);

        ctx.evaluationStackPush(Boolean.TRUE);
        gotoIfTrue.execute(ctx);
        assertTrue(ctx.getCodePtr() == 15);
    }

    public void testGotoIfFalse() throws Exception {
        Codes.Branch gotoIfFalse = Codes.makeGotoIfFalse();
        gotoIfFalse.setTarget(25);
        assertTrue(gotoIfFalse.toString().equals("goto if false 25"));

        ctx.evaluationStackPush(Boolean.TRUE);
        gotoIfFalse.execute(ctx);
        assertTrue(ctx.getCodePtr() == 0);

        ctx.evaluationStackPush(Boolean.FALSE);
        gotoIfFalse.execute(ctx);
        assertTrue(ctx.getCodePtr() == 25);
    }

    /****************
     * Environments *
     ****************/
    public void testEnterNewLocalEnv() throws Exception {
        assertTrue(Codes.ENTER_NEW_LOCAL_ENV.toString()
                   .equals("enter new local env"));

        Codes.ENTER_NEW_LOCAL_ENV.execute(ctx);
        Environment currEnv = ctx.getCurrentEnvironment();
        assertTrue(currEnv.getParent() == env);
    }

    public void testExitLocalEnv() throws Exception {
        assertTrue(Codes.EXIT_LOCAL_ENV.toString().equals("exit local env"));

        Codes.ENTER_NEW_LOCAL_ENV.execute(ctx);
        Codes.EXIT_LOCAL_ENV.execute(ctx);
        assertTrue(ctx.getCurrentEnvironment() == env);
    }

    /*********
     * Debug *
     *********/
    public void testSetLine() throws Exception {
        Code setLine = Codes.makeSetLine(35);
        setLine.execute(ctx);
        assertTrue(ctx.getSourceLine() == 35);
    }

    /**************
     * Procedures *
     **************/
    private boolean called = false;
    public void testProcedureCall() throws Exception {
        ctx.evaluationStackPush(new ProcedureRef() {
                public void call(ExecutionContext ctx, Object[] args) {
                    called = true;
                    assertTrue(args.length == 2);
                    assertTrue(args[0].equals(new Integer(10)));
                    assertTrue(args[1].equals(new Integer(20)));
                }

                public String getName() {return "test";}
            });
        ctx.evaluationStackPush(new Integer(10));
        ctx.evaluationStackPush(new Integer(20));
        Code procedureCall = Codes.makeProcedureCall(2);
        procedureCall.execute(ctx);
        assertTrue(called);
    }

    public void testReturn() throws Exception {
        assertTrue(Codes.RETURN.toString().equals("return"));
        CodeChunk code = new CodeChunk();
        code.addCode(Codes.makeVariable("a"));
        code.addCode(Codes.FETCH);
        code.addCode(Codes.makeConstant(new Integer(10)));
        code.addCode(Codes.ADD);
        code.addCode(Codes.ADD);
        code.addCode(Codes.STOP);
        ctx.executionStackPush(code, env, "testReturn", 105);
        code = new CodeChunk();
        code.addCode(Codes.makeConstant(new Integer(1000)));
        code.addCode(Codes.RETURN);
        ctx.executionStackPush(code, new Environment(), "", -1);
        ctx.run();
        assertTrue(ctx.getCurrentEnvironment() == env);
        assertTrue(ctx.getSourceName().equals("testReturn"));
        assertTrue(ctx.getSourceLine() == 105);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1015)));
    }

    public void testProcedurePush() throws Exception {
        CodeChunk code = new CodeChunk();
        code.addCode(Codes.makeVariable("a"));
        code.addCode(Codes.FETCH);
        code.addCode(Codes.makeConstant(new Integer(200)));
        code.addCode(Codes.ADD);
        code.addCode(Codes.STOP);
        Code procedurePush = Codes.makeProcedurePush(code,
                                                     "testProcedurePush",
                                                     405,
                                                     new String[]{"a"}, "test");
        assertTrue(procedurePush.toString().equals("procedure push"));
        procedurePush.execute(ctx);
        ctx.evaluationStackPush(new Integer(300));
        Codes.makeProcedureCall(1).execute(ctx);
        ctx.run();
        assertTrue(ctx.getCurrentEnvironment().getLocation("a", ctx)
                   .getValue().equals(new Integer(300)));
        assertTrue(ctx.getSourceName().equals("testProcedurePush"));
        assertTrue(ctx.getSourceLine() == 405);
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(500)));
    }

    /*********
     * Locks *
     *********/
    // TODO (locks not implemented yet)

    /********
     * Stop *
     ********/
    public void testStop() throws Exception {
        CodeChunk code = new CodeChunk();
        code.addCode(Codes.makeConstant(new Integer(1)));
        code.addCode(Codes.STOP);
        code.addCode(Codes.makeConstant(new Integer(2)));
        ctx.executionStackPush(code, new Environment(), "", -1);
        ctx.run();
        assertFalse(ctx.isRunning());
        assertTrue(ctx.evaluationStackPopNumber().equals(new Integer(1)));
    }
    
}