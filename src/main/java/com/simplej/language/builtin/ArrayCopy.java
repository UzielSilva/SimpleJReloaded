/*
 * ArrayCopy.java
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

package com.simplej.language.builtin;

import com.simplej.language.vm.*;

public class ArrayCopy extends JavaProcedureRef {

    public void call(ExecutionContext ctx,
                     Object[] args) throws ExecutionException {
        if (args.length != 5)
            throw new InvalidNumberOfArgumentsException(ctx);
        Object o = args[0];
        if (!(o instanceof Location[]))
            throw new NotAnArrayException(ctx);
        Location[] src = (Location[]) o;
        o = args[1];
        if (!(o instanceof Number))
            throw new NotANumberException(ctx);
        int srcOffset = ((Number) o).intValue();
        o = args[2];
        if (!(o instanceof Location[]))
            throw new NotAnArrayException(ctx);
        Location[] dst = (Location[]) o;
        o = args[3];
        if (!(o instanceof Number))
            throw new NotANumberException(ctx);
        int dstOffset = ((Number) o).intValue();
        o = args[4];
        if (!(o instanceof Number))
            throw new NotANumberException(ctx);
        int count = ((Number) o).intValue();
        if (srcOffset < 0 || srcOffset + count > src.length ||
            dstOffset < 0 || dstOffset + count > dst.length)
            throw new OutOfBoundsException(ctx);
        for (int i = 0; i < count; i++) {
            dst[dstOffset + i].setValue(src[srcOffset + i].getValue(), ctx);
            ctx.modifiedLocation(dst[dstOffset + i]);
        }
        ctx.evaluationStackPush(Uninitialized.VALUE);
    }
    
    public String getName() {
        return "arrayCopy";
    }
    
}
