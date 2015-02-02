/*
 * Lock.java
 * Copyright (C) 2006 Gerardo Horvilleur Martinez
 * (based on code placed in the public domain by Doug Lea)
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

public class Lock {

    private Thread owner;

    private int count;

    public void acquire(ExecutionContext ctx) throws InterruptionException {
        Thread caller = Thread.currentThread();
        if (caller.isInterrupted())
            throw new InterruptionException(ctx);
        synchronized (this) {
            if (caller == owner)
                count++;
            else {
                try {
                    while (owner != null)
                        wait();
                    owner = caller;
                    count = 1;
                } catch (InterruptedException e) {
                    notify();
                    throw new InterruptionException(ctx);
                }
            }
        }
    }

    public synchronized void release(ExecutionContext ctx)
        throws IllegalLockUsageException {
        if (Thread.currentThread() != owner)
            throw new IllegalLockUsageException(ctx, "not owner");
        if (--count == 0) {
            owner = null;
            notify();
        }
    }

    public synchronized void release(ExecutionContext ctx,
                                     int n) throws IllegalLockUsageException {
        if (Thread.currentThread() != owner)
            throw new IllegalLockUsageException(ctx, "not owner");
        if (n > count)
            throw new IllegalLockUsageException(ctx, "too many releases");
        count -= n;
        if (count == 0) {
            owner = null;
            notify();
        }
    }

    public synchronized int getCount() {
        if (Thread.currentThread() != owner)
            return 0;
        return count;
    }

    public synchronized Thread getOwner() {
        return owner;
    }
    
}
