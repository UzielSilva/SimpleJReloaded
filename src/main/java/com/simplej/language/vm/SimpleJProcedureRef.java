/*
 * SimpleJProcedureRef.java
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

package com.simplej.language.vm;

public class SimpleJProcedureRef implements ProcedureRef {

    private CodeChunk code;

    private Environment parent;

    private String sourceFileName;

    private int sourceFileLine;

    private String[] argNames;

    private String name;

    public SimpleJProcedureRef(CodeChunk code, Environment parent,
                               String sourceFileName, int sourceFileLine,
                               String[] argNames, String name) {
        this.code = code;
        this.parent = parent;
        this.sourceFileName = sourceFileName;
        this.sourceFileLine = sourceFileLine;
        this.argNames = argNames;
        this.name = name;
    }

    public void call(ExecutionContext ctx, Object[] args)
        throws ExecutionException {
        if (args.length != argNames.length)
            throw new InvalidNumberOfArgumentsException(ctx);
        Environment env = new Environment(parent, getName());
        for (int i = 0; i < argNames.length; i++) {
            env.createLocal(argNames[i], args[i], ctx);
            Location loc = env.getLocation(argNames[i], ctx);
            ctx.createdLocation(loc);
        }
        ctx.executionStackPush(code, env, sourceFileName, sourceFileLine);
    }

    public SimpleJProcedureRef copy() {
        return new SimpleJProcedureRef(code, parent, sourceFileName,
                                       sourceFileLine, argNames, name);
    }

    public String getName() {
        return name;
    }

    public Environment getParent() {
        return parent;
    }

    public void setParent(Environment parent, ExecutionContext ctx)
        throws NullParentException {
        if (parent == null)
            throw new NullParentException(ctx);
        this.parent = parent;
    }

}