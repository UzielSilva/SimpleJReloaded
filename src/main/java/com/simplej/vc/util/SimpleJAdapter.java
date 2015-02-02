/*
 * SimpleJAdapter.java
 * Copyright (C) 2005-2006 Gerardo Horvilleur Martinez
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

package com.simplej.vc.util;

import com.simplej.vc.hardware.*;
import com.simplej.language.compiler.*;
import com.simplej.language.interpreter.*;
import com.simplej.language.vm.*;

import antlr.SemanticException;

import java.io.*;

public class SimpleJAdapter implements VBI, SFI, Runnable {

    private final static Object[] EMPTY_ARGS = new Object[0];

    private final static ProcedureRef EMPTY_PROCEDURE = new EmptyProcedure();

    private final static int WATCHDOG_LIMIT = 50;

    private IAVC iavc;

    private Interpreter interpreter;

    private ClassLoader classLoader;

    private SimpleJCode vbiCallCode;

    private ExecutionContext vbiCtx;

    private int watchdogTimer;

    private SimpleJCode sfiCallCode;

    private boolean countVBI;

    private boolean countSFI;

    private int[] vbiCounts = new int[25];

    private int vbiCountsPtr;

    private int[] sfiCounts = new int[25];

    private int sfiCountsPtr;

    private Profiler vbiProfiler;

    public SimpleJAdapter(IAVC iavc, Interpreter interpreter,
                          ClassLoader classLoader) {
        this(iavc, interpreter, classLoader, false, false, false);
    }
    
    public SimpleJAdapter(IAVC iavc, Interpreter interpreter,
                          ClassLoader classLoader,
                          boolean countVBI, boolean countSFI,
                          boolean profileVBI) {
        if (iavc == null)
            throw new NullPointerException("iavc is null");
        this.iavc = iavc;
        if (interpreter == null)
            throw new NullPointerException("interpreter is null");
        this.interpreter = interpreter;
        if (classLoader == null)
            throw new NullPointerException("classLoader is null");
        this.classLoader = classLoader;
        this.countVBI = countVBI;
        this.countSFI = countSFI;
        if (profileVBI)
            vbiProfiler = new Profiler();
        reset();
        iavc.setVBI(this);
        iavc.setSFI(this);
        try {
            CodeGenerator cg = new CodeGenerator("vbiCallCode");
            cg.pushLocation("vbi");
            cg.fetch();
            cg.procedureCall(0);
            vbiCallCode = new SimpleJCode(cg.getCode(), "vbiCallCode");
            cg = new CodeGenerator("sfiCallCode");
            cg.pushLocation("sfi");
            cg.fetch();
            cg.procedureCall(0);
            sfiCallCode = new SimpleJCode(cg.getCode(), "sfiCallCode");
        } catch (SemanticException e) {
            // Should not happen
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    public void run() {
        while (true) {
            if (vbiCtx != null) {
                watchdogTimer++;
                if (watchdogTimer == WATCHDOG_LIMIT)
                    vbiCtx.stop();
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                // Should not happen
                e.printStackTrace();
            }
        }
    }

    public synchronized void vbi() throws ExecutionException {
        if (watchdogTimer < WATCHDOG_LIMIT) {
            watchdogTimer = 0;
            Environment env = interpreter.getEnv();
            vbiCtx = new ExecutionContext();
            if (vbiProfiler != null)
                vbiCtx.addListener(vbiProfiler);
            vbiCtx.execute(vbiCallCode, env);
            if (watchdogTimer == WATCHDOG_LIMIT)
                throw new ExecutionException(vbiCtx, "vbi() timeout");
            if (countVBI) {
                vbiCounts[vbiCountsPtr++] = vbiCtx.getInstructionCounter();
                if (vbiCountsPtr == vbiCounts.length) {
                    int total = 0;
                    for (int i = 0; i < vbiCounts.length; i++)
                        total += vbiCounts[i];
                    System.out.println("vbi: " +
                                       (total / vbiCounts.length));
                    vbiCountsPtr = 0;
                }
            }
        }
    }

    public synchronized void printVBIProfile() {
        vbiProfiler.print();
        vbiProfiler = new Profiler();
    }

    public synchronized void sfi() throws ExecutionException {
        Environment env = interpreter.getEnv();
        ExecutionContext ctx = new ExecutionContext();
        ctx.execute(sfiCallCode, env);
        if (countSFI) {
            sfiCounts[sfiCountsPtr++] = ctx.getInstructionCounter();
            if (sfiCountsPtr == sfiCounts.length) {
                int total = 0;
                for (int i = 0; i < sfiCounts.length; i++)
                    total += sfiCounts[i];
                System.out.println("sfi: " +
                                   (total / sfiCounts.length));
                sfiCountsPtr = 0;
            }
        }
    }

    public synchronized void reset() {
        LockManager.MGR.reset();
        interpreter.reset();
        Environment env = interpreter.getBuiltinEnv();
        try {
            env.createFinalLocal("poke", new ProcedurePoke(), null);
            env.createFinalLocal("peek", new ProcedurePeek(), null);
            env.createFinalLocal("arrayPoke", new ProcedureArrayPoke(), null);
            env.createFinalLocal("clear", new ProcedureClear(), null);
            env.createFinalLocal("putAt", new ProcedurePutAt(), null);
            env.createFinalLocal("showAt", new ProcedureShowAt(), null);
            env.createFinalLocal("note", new ProcedureNote(), null);
            env.createFinalLocal("readCtrlOne", new ProcedureReadCtrlOne(),
                                 null);
            env.createFinalLocal("readCtrlTwo", new ProcedureReadCtrlTwo(),
                                 null);
            env.createFinalLocal("setBackground", new ProcedureSetBackground(),
                                 null);
            env.createFinalLocal("setForeground", new ProcedureSetForeground(),
                                 null);
            env.createFinalLocal("pause", new ProcedurePause(), null);
            env.createFinalLocal("readFile", new ProcedureReadFile(), null);
            env.createFinalLocal("readTilesFile",
                                 new ProcedureReadTilesFile(), null);
            env.createFinalLocal("readSpritesFile",
                                 new ProcedureReadSpritesFile(), null);
            env.createLocal("vbi", EMPTY_PROCEDURE, null);
            env.createLocal("sfi", EMPTY_PROCEDURE, null);
        } catch (LocationIsFinalException e) {
            // Shouldn't happen
            e.printStackTrace();
        } catch (NoSuchLocationException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
        try {
            Reader reader =
                new InputStreamReader(getClass().getResourceAsStream(
                        "/com/simplej/vc/util/_iavc_utils.sj"));
            reader = new BufferedReader(reader);
            SimpleJCode code =
                com.simplej.language.compiler.Compiler.compileProgram(reader,
                                                              "_iavc_utils.sj");
            ExecutionContext ctx = new ExecutionContext();
            ctx.execute(code, env);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        interpreter.setModuleClassLoader(classLoader);
        try {
            iavc.reset();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        watchdogTimer = 0;
        vbiCountsPtr = 0;
        sfiCountsPtr = 0;
    }

    public synchronized void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        reset();
    }

    public synchronized boolean hasVBIOrSFI() {
        Environment env = interpreter.getEnv();
        ExecutionContext ctx = new ExecutionContext();
        try {
            if (env.getLocation("vbi", ctx).getValue() != EMPTY_PROCEDURE)
                return true;
            if (env.getLocation("sfi", ctx).getValue() != EMPTY_PROCEDURE)
                return true;
        } catch (NoSuchLocationException e) {
            // Shouldn't happen
            e.printStackTrace();
        }
        return false;
    }

    private static class EmptyProcedure extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 0)
                throw new InvalidNumberOfArgumentsException(ctx);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "emptyProcedure";
        }
    }

    private class ProcedurePoke extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 2)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int address = ((Number) o).intValue();
            o = args[1];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int value = ((Number) o).intValue();
            iavc.store(address, value);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "poke";
        }
    }

    private class ProcedurePeek extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int address = ((Number) o).intValue();
            int v = iavc.read(address);
            ctx.evaluationStackPush(new Integer(v));
        }

        public String getName() {
            return "peek";
        }
    }

    private class ProcedureArrayPoke extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 4)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int address = ((Number) o).intValue();
            o = args[1];
            if (!(o instanceof Location[]))
                throw new NotAnArrayException(ctx);
            Location[] array = (Location[]) o;
            o = args[2];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int offset = ((Number) o).intValue();
            o = args[3];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int count = ((Number) o).intValue();
            if (offset < 0 || offset + count > array.length)
                throw new OutOfBoundsException(ctx);
            for (int i = 0; i < count; i++) {
                o = array[offset + i].getValue();
                if (!(o instanceof Number))
                    throw new NotANumberException(ctx);
                iavc.store(address + i, ((Number) o).intValue());
            }
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "arrayPoke";
        }
     }

    private class ProcedureClear extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 0)
                throw new InvalidNumberOfArgumentsException(ctx);
            byte[] emptyLine = new byte[32];
            for (int i = 0; i < 32; i++)
                emptyLine[i] = 32;
            for (int i = 0; i < 24; i++)
                iavc.arrayStore(i * 64, emptyLine, 0, 32);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "clear";
        }
    }

    private class ProcedurePutAt extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 3)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int b = ((Number) o).intValue();
            o = args[1];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int col = ((Number) o).intValue();
            o = args[2];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int row = ((Number) o).intValue();
            iavc.store(row * 64 + col, b);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "putAt";
        }
    }

    private class ProcedureShowAt extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 3)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof String || o instanceof Number))
                throw new NotANumberOrStringException(ctx);
            String msg = o.toString();
            o = args[1];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int col = ((Number) o).intValue();
            o = args[2];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int row = ((Number) o).intValue();
            int offset = row * 64 + col;
            for (int i = 0; i < msg.length(); i++)
                iavc.store(offset + i, msg.charAt(i));
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "showAt";
        }
    }

    private class ProcedureNote extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof String))
                throw new NotAStringException(ctx);
            String note = (String) o;
            Notes.playNote(iavc, note);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "note";
        }
    }

    private class ProcedureReadCtrlOne extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 0)
                throw new InvalidNumberOfArgumentsException(ctx);
            int v;
            v = iavc.read(0x8c4);
            ctx.evaluationStackPush(new Integer(v));
        }

        public String getName() {
            return "readCtrlOne";
        }
    }

    private class ProcedureReadCtrlTwo extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 0)
                throw new InvalidNumberOfArgumentsException(ctx);
            int v;
            v = iavc.read(0x8c5);
            ctx.evaluationStackPush(new Integer(v));
        }

        public String getName() {
            return "readCtrlTwo";
        }
    }

    private class ProcedureSetBackground extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 3)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int red = ((Number) o).intValue();
            o = args[1];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int green = ((Number) o).intValue();
            o = args[2];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int blue = ((Number) o).intValue();
            red = Math.max(0, Math.min(31, red));
            green = Math.max(0, Math.min(31, green));
            blue = Math.max(0, Math.min(31, blue));
            int v = (red << 10) | (green << 5) | blue;
            iavc.store(2048, v >> 8);
            iavc.store(2049, v);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "setBackground";
        }
    }

    private class ProcedureSetForeground extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 3)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int red = ((Number) o).intValue();
            o = args[1];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int green = ((Number) o).intValue();
            o = args[2];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            int blue = ((Number) o).intValue();
            red = Math.max(0, Math.min(31, red));
            green = Math.max(0, Math.min(31, green));
            blue = Math.max(0, Math.min(31, blue));
            int v = (red << 10) | (green << 5) | blue;
            iavc.store(2050, v >> 8);
            iavc.store(2051, v);
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "setBackground";
        }
    }

    private class ProcedurePause extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof Number))
                throw new NotANumberException(ctx);
            Number n = (Number) o;
            try {
                Thread.sleep((long) (n.doubleValue() * 1000.0));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            ctx.evaluationStackPush(Uninitialized.VALUE);
        }

        public String getName() {
            return "pause";
        }
    }

    private class ProcedureReadFile extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args) 
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof String))
                throw new NotAStringException(ctx);
            String name = (String) o;
            byte[] data;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is =
                    interpreter.getSourceFinder().getInputStream(ctx, name);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) != -1)
                    baos.write(buffer, 0, n);
                data = baos.toByteArray();

            } catch (IOException e) {
                throw new ExecutionException(ctx, e.toString());
            }
            Location[] locations = new Location[data.length];
            for (int i = 0; i < data.length; i++) {
                locations[i] = new Location(new Integer(data[i] & 0xff));
            }
            ctx.evaluationStackPush(locations);
        }

        public String getName() {
            return "readFile";
        }
    }

    private class ProcedureReadTilesFile extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args)
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof String))
                throw new NotAStringException(ctx);
            String name = (String) o;
            byte[] data;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is =
                    interpreter.getSourceFinder().getInputStream(ctx, name);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) != -1)
                    baos.write(buffer, 0, n);
                data = baos.toByteArray();

            } catch (IOException e) {
                throw new ExecutionException(ctx, e.toString());
            }
            Environment env = new Environment();
            Location[] colors = new Location[16];
            for (int i = 0; i < 16; i++) {
                int offset = i * 2;
                Environment color = new Environment();
                color.createLocal("red",
                                  new Integer((data[offset] & 0x7c) >> 2),
                                  ctx);
                color.createLocal("green",
                                  new Integer(((data[offset] & 0x3) << 3) |
                                              ((data[offset + 1] & 0xe0) >> 5)),
                                  ctx);
                color.createLocal("blue",
                                  new Integer(data[offset + 1] & 0x1f),
                                  ctx);
                colors[i] = new Location(color);
            }
            env.createLocal("colors", colors, ctx);
            Location[] pixels = new Location[256];
            for (int i = 0; i < 256; i++) {
                int offset = i * 32 + 32;
                Location[] pix = new Location[64];
                for (int j = 0; j < 64; j += 2) {
                    pix[j] =
                        new Location(new Integer((data[offset] & 0xf0) >> 4));
                    pix[j + 1] =
                        new Location(new Integer(data[offset] & 0x0f));
                    offset++;
                }
                pixels[i] = new Location(pix);
            }
            env.createLocal("pixels", pixels, ctx);
            Location[] rows = new Location[24];
            for (int i = 0; i < 24; i++) {
                int offset = 32 + 8192 + i * 32;
                Location[] row = new Location[32];
                for (int j = 0; j < 32; j++)
                    row[j] =
                        new Location(new Integer(data[offset + j] & 0xff));
                rows[i] = new Location(row);
            }
            env.createLocal("rows", rows, ctx);
            ctx.evaluationStackPush(env);
        }

        public String getName() {
            return "readTilesFile";
        }
    }

    private class ProcedureReadSpritesFile extends JavaProcedureRef {
        public void call(ExecutionContext ctx, Object[] args)
            throws ExecutionException {
            if (args.length != 1)
                throw new InvalidNumberOfArgumentsException(ctx);
            Object o = args[0];
            if (!(o instanceof String))
                throw new NotAStringException(ctx);
            String name = (String) o;
            byte[] data;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream is =
                    interpreter.getSourceFinder().getInputStream(ctx, name);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = is.read(buffer)) != -1)
                    baos.write(buffer, 0, n);
                data = baos.toByteArray();

            } catch (IOException e) {
                throw new ExecutionException(ctx, e.toString());
            }
            Environment env = new Environment();
            Location[] colors = new Location[16];
            for (int i = 0; i < 16; i++) {
                int offset = i * 2;
                Environment color = new Environment();
                color.createLocal("red",
                                  new Integer((data[offset] & 0x7c) >> 2),
                                  ctx);
                color.createLocal("green",
                                  new Integer(((data[offset] & 0x3) << 3) |
                                              ((data[offset + 1] & 0xe0) >> 5)),
                                  ctx);
                color.createLocal("blue",
                                  new Integer(data[offset + 1] & 0x1f),
                                  ctx);
                colors[i] = new Location(color);
            }
            env.createLocal("colors", colors, ctx);
            Location[] smallPixels = new Location[128];
            for (int i = 0; i < 128; i++) {
                int offset = i * 32 + 32;
                Location[] pix = new Location[64];
                for (int j = 0; j < 64; j += 2) {
                    pix[j] =
                        new Location(new Integer((data[offset] & 0xf0) >> 4));
                    pix[j + 1] =
                        new Location(new Integer(data[offset] & 0x0f));
                    offset++;
                }
                smallPixels[i] = new Location(pix);
            }
            env.createLocal("smallPixels", smallPixels, ctx);
            Location[] largePixels = new Location[128];
            for (int i = 0; i < 128; i++) {
                int offset = i * 128 + 32 + 4096;
                Location[] pix = new Location[256];
                for (int j = 0; j < 256; j += 2) {
                    pix[j] =
                        new Location(new Integer((data[offset] & 0xf0) >> 4));
                    pix[j + 1] =
                        new Location(new Integer(data[offset] & 0x0f));
                    offset++;
                }
                largePixels[i] = new Location(pix);
            }
            env.createLocal("largePixels", largePixels, ctx);
            ctx.evaluationStackPush(env);
        }

        public String getName() {
            return "readSpritesFile";
        }
    }

}
