/*
 * IsBoolean.java
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

public class IsBoolean extends JavaProcedureRef {

    public void call(ExecutionContext ctx,
                     Object[] args) throws ExecutionException {
        if (args.length != 1)
            throw new InvalidNumberOfArgumentsException(ctx);
        Object o = args[0];
        ctx.evaluationStackPush(Boolean.valueOf(o instanceof Boolean));
    }
    
    public String getName() {
        return "isBoolean";
    }
    
}
