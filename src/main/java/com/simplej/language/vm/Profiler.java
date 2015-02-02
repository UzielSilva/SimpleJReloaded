/*
 * Profiler.java
 * Copyright (C) 2007 Gerardo Horvilleur Martinez
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

public class Profiler implements ExecutionContextListener {

    private Map counters = new HashMap();

    private String fname;

    private Counter currentCounter;

    private void selectCounter(int line) {
        StringBuffer sb = new StringBuffer();
        sb.append(fname).append(':').append(line);
        String key = sb.toString();
        currentCounter = (Counter) counters.get(key);
        if (currentCounter == null) {
            currentCounter = new Counter(key);
            counters.put(key, currentCounter);
        }
    }

    public void sourceLineChanged(ExecutionContext ctx, int line) {
        selectCounter(line);
    }
    
    public void sourceLineChanged(ExecutionContext ctx, int line,
                                  String filename) {
        fname = filename;
        selectCounter(line);
    }

    public void executionFinished(ExecutionContext ctx) {
    }

    public void willExecute(ExecutionContext ctx, Code code) {
        currentCounter.count++;
    }

    public void modifiedLocation(ExecutionContext ctx, Location location) {
    }

    public void createdLocation(ExecutionContext ctx, Location location) {
    }

    public void print() {
        List info = new ArrayList();
        info.addAll(counters.values());
        Collections.sort(info);
        Iterator it = info.iterator();
        while (it.hasNext())
            System.out.println(it.next());
    }

    private static class Counter implements Comparable {
        int count;

        String key;

        public Counter(String key) {
            this.key = key;
        }

        public int compareTo(Object o) {
            Counter c = (Counter) o;
            return c.count - count;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            String s = Integer.toString(count);
            int n = 9 - s.length();
            for (int i = 0; i < n; i++)
                sb.append(' ');
            sb.append(s).append("  ").append(key);
            return sb.toString();
        }
    }

}