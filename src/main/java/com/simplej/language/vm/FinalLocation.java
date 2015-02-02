/*
 * FinalLocation.java
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

public class FinalLocation extends Location {

    private final String name;

    private FinalLocation() {
        // Can't happen, but the Java compiler isn't smart enough to understand
        name = null;
    }

    public FinalLocation(String name, Object value) {
        super(value);
        this.name = name;
    }
    
    public void setValue(Object value, ExecutionContext ctx)
        throws LocationIsFinalException {
        throw new LocationIsFinalException(ctx, "'" + name + "'");
    }

}
