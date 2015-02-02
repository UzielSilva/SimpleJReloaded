/*
 * Environment.java
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

import java.io.*;
import java.util.*;

public class Environment implements Serializable {

    private Map locationsByName = new HashMap();

    private Environment parent;

    private String name;

    public Environment(Environment parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public Environment(String name) {
        this(null, name);
    }

    public Environment(Environment parent) {
        this(parent, null);
    }

    public Environment() {
        this(null, null);
    }

    public synchronized Environment getParent() {
        return parent;
    }

    public synchronized void setParent(Environment parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public synchronized boolean hasLocalLocation(String name) {
        return locationsByName.containsKey(name);
    }

    public synchronized boolean hasLocation(String name) {
        if (hasLocalLocation(name))
            return true;
        if (parent == null)
            return false;
        return parent.hasLocation(name);
    }

    public synchronized Location getLocation(String name,
                                             ExecutionContext ctx)
        throws NoSuchLocationException {
        if (!hasLocation(name))
            throw new NoSuchLocationException(ctx, "'" + name + "'");
        Location result = (Location) locationsByName.get(name);
        if (result != null)
            return result;
        return parent.getLocation(name, ctx);
    }

    public synchronized void createLocal(String name, ExecutionContext ctx)
        throws LocationIsFinalException, NoSuchLocationException {
        if (hasLocalLocation(name) &&
            getLocation(name, ctx) instanceof FinalLocation)
            throw new LocationIsFinalException(ctx, "'" + name + "'");
        Location location = new Location();
        locationsByName.put(name, location);
    }

    public synchronized void createLocal(String name, Object value,
                                         ExecutionContext ctx)
        throws LocationIsFinalException, NoSuchLocationException {
        if (hasLocalLocation(name) &&
            getLocation(name, ctx) instanceof FinalLocation)
            throw new LocationIsFinalException(ctx, "'" + name + "'");
        Location location = new Location(value);
        locationsByName.put(name, location);
    }

    public synchronized void createFinalLocal(String name, Object value,
                                              ExecutionContext ctx)
        throws LocationIsFinalException, NoSuchLocationException {
        if (hasLocalLocation(name) &&
            getLocation(name, ctx) instanceof FinalLocation)
            throw new LocationIsFinalException(ctx, "'" + name + "'");
        FinalLocation location = new FinalLocation(name, value);
        locationsByName.put(name, location);
    }

    public synchronized List getLocalLocationNames() {
        List result = new ArrayList();
        result.addAll(locationsByName.keySet());
        Collections.sort(result);
        return result;
    }

    public synchronized void removeLocal(String name) {
        locationsByName.remove(name);
    }

    public synchronized Environment copy(ExecutionContext ctx) {
        Environment result = new Environment(parent, null);
        Iterator iter = locationsByName.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            Location location = (Location) entry.getValue();
            try {
                Object value = location.getValue();
                if (value instanceof SimpleJProcedureRef) {
                    SimpleJProcedureRef proc =
                        (SimpleJProcedureRef) value;
                    if (proc.getParent() == this) {
                        proc = proc.copy();
                        proc.setParent(result, ctx);
                        value = proc;
                    }
                }
                if (location instanceof FinalLocation)
                    result.createFinalLocal(key, value, ctx);
                else
                    result.createLocal(key, value, ctx);
            } catch (LocationIsFinalException e) {
                // This should never happen
                e.printStackTrace();
                throw new RuntimeException("Error in Environment.copy()");
            } catch (NoSuchLocationException e) {
                // This should never happen
                e.printStackTrace();
                throw new RuntimeException("Error in Environment.copy()");
            } catch (NullParentException e) {
                // This should never happen
                e.printStackTrace();
                throw new RuntimeException("Error in Environment.copy()");
            }
        }
        return result;
    }

}
