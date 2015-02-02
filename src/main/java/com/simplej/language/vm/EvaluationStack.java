/*
 * EvaluationStack.java
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

class EvaluationStack implements Serializable {

    private Object[] values = new Object[64];

    private int nextFree = 0;

    public void push(Object value) {
        if (nextFree == values.length) {
            Object[] tmp = new Object[values.length * 2];
            System.arraycopy(values, 0, tmp, 0, values.length);
            values = tmp;
        }
        values[nextFree++] = value;
    }

    public Object pop(ExecutionContext ctx)
        throws EvaluationStackUnderflowException {
        if (nextFree == 0)
            throw new EvaluationStackUnderflowException(ctx);
        Object result = values[--nextFree];
        values[nextFree] = null;
        return result;
    }

    public void reset() {
        for (int i = 0; i < nextFree; i++)
            values[nextFree] = null;
        nextFree = 0;
    }

    public Object peek(int offset) {
        return values[nextFree - offset];
    }

}