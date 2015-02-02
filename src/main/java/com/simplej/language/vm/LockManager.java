/*
 * LockManager.java
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

import java.util.*;

public class LockManager {

    final public static LockManager MGR = new LockManager();

    private Map locks;

    private LockManager() {
        reset();
    }

    public synchronized void reset() {
        locks = new HashMap();
    }

    public void acquire(ExecutionContext ctx, Object object)
        throws InterruptionException {
        Lock lock = getLock(object);
        lock.acquire(ctx);
    }

    public void release(ExecutionContext ctx, Object object)
        throws IllegalLockUsageException {
        Lock lock = getLock(object);
        lock.release(ctx);
    }

    public void release(ExecutionContext ctx, Object object, int n)
        throws IllegalLockUsageException {
        Lock lock = getLock(object);
        lock.release(ctx, n);
    }

    public synchronized void releaseAllLocksForThread(ExecutionContext ctx)
        throws IllegalLockUsageException {
        Thread caller = Thread.currentThread();
        Iterator iter = locks.values().iterator();
        while (iter.hasNext()) {
            Lock lock = (Lock) iter.next();
            if (lock.getOwner() == caller)
                lock.release(ctx, lock.getCount());
        }
    }

    public int getCount(Object object) {
        Lock lock = getLock(object);
        return lock.getCount();
    }

    public Thread getOwner(Object object) {
        Lock lock = getLock(object);
        return lock.getOwner();
    }

    private synchronized Lock getLock(Object object) {
        Lock lock = (Lock) locks.get(object);
        if (lock == null) {
            lock = new Lock();
            locks.put(object, lock);
        }
        return lock;
    }

}
