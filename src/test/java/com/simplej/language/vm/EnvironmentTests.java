/*
 * EnvironmentTests.java
 * Copyright (C) 2005, 2006 Gerardo Horvilleur Martinez
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

public class EnvironmentTests extends TestCase {

    private Environment parentEnv;

    private Environment childEnv;

    private ExecutionContext ctx;

    protected void setUp() {
        parentEnv = new Environment();
        childEnv = new Environment(parentEnv);
        ctx = new ExecutionContext();
        try {
            ctx.executionStackPush(null, childEnv,
                                   "", 0); // Just to set the current env
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void tearDown() {
        childEnv = null;
        parentEnv = null;
        ctx = null;
    }

    public EnvironmentTests(String name) {
        super(name);
    }

    public void testGetLocation() throws Exception {
        assertFalse(parentEnv.hasLocation("a"));
        assertFalse(childEnv.hasLocation("a"));
        childEnv.createLocal("a", ctx);
        Location loc = childEnv.getLocation("a", ctx);
        assertTrue(loc.getValue() == Uninitialized.VALUE);
        assertFalse(parentEnv.hasLocation("a"));
        assertFalse(parentEnv.hasLocalLocation("a"));
        assertTrue(childEnv.hasLocation("a"));
        assertTrue(childEnv.hasLocalLocation("a"));

        assertFalse(parentEnv.hasLocation("b"));
        assertFalse(childEnv.hasLocation("b"));
        parentEnv.createLocal("b", ctx);
        loc = parentEnv.getLocation("b", ctx);
        assertTrue(loc.getValue() == Uninitialized.VALUE);
        loc = childEnv.getLocation("b", ctx);
        assertTrue(loc.getValue() == Uninitialized.VALUE);
        assertTrue(parentEnv.hasLocation("b"));
        assertTrue(parentEnv.hasLocalLocation("b"));
        assertTrue(childEnv.hasLocation("b"));
        assertFalse(childEnv.hasLocalLocation("b"));

        boolean failed = true;
        try {
            childEnv.getLocation("xx",ctx);
        } catch (NoSuchLocationException e) {
            failed = false;
        }
        if (failed)
            fail();
    }

    public void testCreateLocal() throws Exception {
        assertFalse(parentEnv.hasLocation("a"));
        assertFalse(childEnv.hasLocation("a"));
        childEnv.createLocal("a", ctx);
        assertFalse(parentEnv.hasLocation("a"));
        assertTrue(childEnv.hasLocation("a"));
        assertTrue(childEnv.hasLocalLocation("a"));
        Location loc = childEnv.getLocation("a", ctx);
        assertTrue(loc.getValue() == Uninitialized.VALUE);

        assertFalse(parentEnv.hasLocation("b"));
        assertFalse(childEnv.hasLocation("b"));
        childEnv.createLocal("b", "value", ctx);
        assertFalse(parentEnv.hasLocation("b"));
        assertTrue(childEnv.hasLocation("b"));
        assertTrue(childEnv.hasLocalLocation("b"));
        loc = childEnv.getLocation("b", ctx);
        assertTrue(loc.getValue().equals("value"));
    }

    public void testCreateFinalLocal() throws Exception {
        assertFalse(parentEnv.hasLocation("a"));
        assertFalse(childEnv.hasLocation("a"));
        childEnv.createFinalLocal("a", "value", ctx);
        assertFalse(parentEnv.hasLocation("a"));
        assertTrue(childEnv.hasLocation("a"));
        assertTrue(childEnv.hasLocalLocation("a"));
        Location loc = childEnv.getLocation("a", ctx);
        assertTrue(loc.getValue().equals("value"));

        boolean failed = true;
        try {
            loc.setValue("new", ctx);
        } catch (LocationIsFinalException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        try {
            childEnv.createLocal("a", ctx);
        } catch (LocationIsFinalException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        try {
            childEnv.createLocal("a", "new", ctx);
        } catch (LocationIsFinalException e) {
            failed = false;
        }
        if (failed)
            fail();

        failed = true;
        try {
            childEnv.createFinalLocal("a", "new", ctx);
        } catch (LocationIsFinalException e) {
            failed = false;
        }
        if (failed)
            fail();

        loc = childEnv.getLocation("a", ctx);
        assertTrue(loc.getValue().equals("value"));

        parentEnv.createFinalLocal("b", "x", ctx);
        loc = childEnv.getLocation("b", ctx);
        failed = true;
        try {
            loc.setValue("new", ctx);
        } catch (LocationIsFinalException e) {
            failed = false;
        }
        if (failed)
            fail();

        childEnv.createLocal("b", "y", ctx);
        loc = childEnv.getLocation("b", ctx);
        assertTrue(loc.getValue().equals("y"));
        loc = parentEnv.getLocation("b", ctx);
        assertTrue(loc.getValue().equals("x"));
   }

    public void testGetLocationNames() throws Exception {
        childEnv.createLocal("a", ctx);
        childEnv.createLocal("c", "x", ctx);
        childEnv.createFinalLocal("b", "y", ctx);
        parentEnv.createLocal("d", ctx);
        List names = childEnv.getLocalLocationNames();
        assertTrue(names.size() == 3);
        assertTrue(names.get(0).equals("a"));
        assertTrue(names.get(1).equals("b"));
        assertTrue(names.get(2).equals("c"));
    }

    public void testRemoveLocal() throws Exception {
        childEnv.createLocal("a", ctx);
        childEnv.createLocal("b", ctx);
        childEnv.createFinalLocal("c", "x", ctx);
        childEnv.createLocal("d", ctx);
        parentEnv.createLocal("b", ctx);
        childEnv.removeLocal("b");
        childEnv.removeLocal("c");
        List names = childEnv.getLocalLocationNames();
        assertTrue(names.size() == 2);
        assertTrue(names.get(0).equals("a"));
        assertTrue(names.get(1).equals("d"));
    }

    public void testCopy() throws Exception {
        childEnv.createLocal("a", ctx);
        childEnv.createLocal("b", new Integer(2), ctx);
        childEnv.createFinalLocal("c", new Double(3), ctx);
        Environment env = childEnv.copy(ctx);
        assertTrue(env.getParent() == parentEnv);
        List names = env.getLocalLocationNames();
        assertTrue(names.size() == 3);
        assertTrue(names.get(0).equals("a"));
        assertTrue(names.get(1).equals("b"));
        assertTrue(names.get(2).equals("c"));
        Location loc = env.getLocation("a", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue() == Uninitialized.VALUE);
        loc = env.getLocation("b", ctx);
        assertFalse(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Integer(2)));
        loc = env.getLocation("c", ctx);
        assertTrue(loc instanceof FinalLocation);
        assertTrue(loc.getValue().equals(new Double(3)));
    }
    
}