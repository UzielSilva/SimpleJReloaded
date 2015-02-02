/*
 * ExecutionStack.java
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

class ExecutionStack implements Serializable {

    private ExecutionFrame[] frames = new ExecutionFrame[64];

    private int nextFree = 0;

    public void push(ExecutionFrame frame) {
        if (nextFree == frames.length) {
            ExecutionFrame[] tmp = new ExecutionFrame[frames.length * 2];
            System.arraycopy(frames, 0, tmp, 0, frames.length);
            frames = tmp;
        }
        frames[nextFree++] = frame;
    }

    public ExecutionFrame pop(ExecutionContext ctx)
        throws ExecutionStackUnderflowException {
        if (nextFree == 0)
            throw new ExecutionStackUnderflowException(ctx);
        ExecutionFrame result = frames[--nextFree];
        frames[nextFree] = null;
        return result;
    }

    public void reset() {
        for (int i = 0; i < nextFree; i++)
            frames[nextFree] = null;
        nextFree = 0;
    }

    public ExecutionFrame[] getFrames() {
        ExecutionFrame[] ret = new ExecutionFrame[nextFree];
        System.arraycopy(frames, 0, ret, 0, nextFree);
        return ret;
    }

}