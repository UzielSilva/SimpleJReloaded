/*
 * Interpreter.java
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

package com.simplej.language.interpreter;

import com.simplej.language.builtin.*;
import com.simplej.language.compiler.*;
import com.simplej.language.vm.*;

import antlr.*;

import java.io.*;
import java.util.*;

public class Interpreter {

    private Environment env;

    private Environment builtinEnv;

    private Reader in;

    private PrintStream out;

    private PrintStream err;

    private Set printed;

    private SourceFinder sourceFinder;

    private ClassLoader moduleClassLoader;

    public Interpreter(Reader in, PrintStream out, PrintStream err,
                       SourceFinder sourceFinder) {
        this.in = in; // in can be null
        if (out == null)
            throw new NullPointerException("out is null");
        this.out = out;
        if (err == null)
            throw new NullPointerException("err is null");
        this.err = err;
        if (sourceFinder == null)
            throw new NullPointerException("sourceFinder is null");
        this.sourceFinder = sourceFinder;
        moduleClassLoader = getClass().getClassLoader();
        initEnv();
    }

    public Interpreter(Reader in, PrintStream out, PrintStream err) {
        this(in, out, err, new FileSystemSourceFinder());
    }

    public Interpreter() {
        this(new InputStreamReader(System.in), System.out, System.err,
             new FileSystemSourceFinder());
    }

    public SourceFinder getSourceFinder() {
        return sourceFinder;
    }

    public void setSourceFinder(SourceFinder sourceFinder) {
        if (sourceFinder == null)
            throw new NullPointerException("sourceFinder is null");
        this.sourceFinder = sourceFinder;
    }

    public ClassLoader getModuleClassLoader() {
        return moduleClassLoader;
    }

    public void setModuleClassLoader(ClassLoader moduleClassLoader) {
        if (moduleClassLoader == null)
            throw new NullPointerException("moduleClassLoader is null");
        this.moduleClassLoader = moduleClassLoader;
    }

    private void initEnv() {
        builtinEnv = new Environment("Builtin");
        env = new Environment(builtinEnv, "Global");
        ExecutionContext ctx = new ExecutionContext();
        try {
            builtinEnv.createFinalLocal("acos", new ACos(), ctx);  
            builtinEnv.createFinalLocal("asin", new ASin(), ctx); 
            builtinEnv.createFinalLocal("atan", new ATan(), ctx); 
            builtinEnv.createFinalLocal("atan2", new ATan2(), ctx); 
            builtinEnv.createFinalLocal("atof", new AToF(), ctx); 
            builtinEnv.createFinalLocal("atoi", new AToI(), ctx); 
            builtinEnv.createFinalLocal("appendChar", new AppendChar(), ctx); 
            builtinEnv.createFinalLocal("apply", new Apply(), ctx); 
            builtinEnv.createFinalLocal("arrayCopy", new ArrayCopy(), ctx); 
            builtinEnv.createFinalLocal("ceil", new Ceil(), ctx);
            builtinEnv.createFinalLocal("charAt", new CharAt(), ctx); 
            builtinEnv.createFinalLocal("cos", new Cos(), ctx);
            builtinEnv.createFinalLocal("error",
                                       new com.simplej.language.builtin.Error(),
                                        ctx);
            builtinEnv.createFinalLocal("exp", new Exp(), ctx); 
            builtinEnv.createFinalLocal("frandom", new FRandom(), ctx); 
            builtinEnv.createFinalLocal("floor", new Floor(), ctx); 
            builtinEnv.createFinalLocal("getNames", new GetNames(), ctx); 
            builtinEnv.createFinalLocal("getValues", new GetValues(), ctx); 
            builtinEnv.createFinalLocal("hasName", new HasName(), ctx); 
            builtinEnv.createFinalLocal("isArray", new IsArray(), ctx); 
            builtinEnv.createFinalLocal("isBoolean", new IsBoolean(), ctx); 
            builtinEnv.createFinalLocal("isEnv", new IsEnv(), ctx); 
            builtinEnv.createFinalLocal("isProcedure", new IsProcedure(), ctx); 
            builtinEnv.createFinalLocal("isNumber", new IsNumber(), ctx); 
            builtinEnv.createFinalLocal("isString", new IsString(), ctx); 
            builtinEnv.createFinalLocal("length", new Length(), ctx); 
            builtinEnv.createFinalLocal("log", new Log(), ctx); 
            builtinEnv.createFinalLocal("pow", new Pow(), ctx);           
            builtinEnv.createFinalLocal("print", new Print(this), ctx);
            builtinEnv.createFinalLocal("random",
                                      new com.simplej.language.builtin.Random(),
                                        ctx); 
            builtinEnv.createFinalLocal("removeName", new RemoveName(), ctx); 
            builtinEnv.createFinalLocal("round", new Round(), ctx); 
            builtinEnv.createFinalLocal("sin", new Sin(), ctx);
            builtinEnv.createFinalLocal("source", new Source(this), ctx);
            builtinEnv.createFinalLocal("sqrt", new Sqrt(), ctx); 
            builtinEnv.createFinalLocal("tan", new Tan(), ctx);
        } catch (LocationIsFinalException e) {
            e.printStackTrace(); // Shouldn't happen!
        } catch (NoSuchLocationException e) {
            e.printStackTrace(); // Shouldn't happen!
        }
        try {
            Reader reader =
                new InputStreamReader(getClass().getResourceAsStream(
                            "/com/simplej/language/builtin/_builtin_utils.sj"));
            reader = new BufferedReader(reader);
            SimpleJCode code =
                com.simplej.language.compiler.Compiler.compileProgram(reader,
                                                           "_builtin_utils.sj");
            ctx.execute(code, builtinEnv);
        } catch (ExecutionException e) {
            err.println(e.toString());
        }
    }

    public void reset() {
        initEnv();
    }

    public Environment getEnv() {
        return env;
    }

    public Environment getBuiltinEnv() {
        return builtinEnv;
    }

    public void interpretInteractive() {
        if (in == null)
            throw new RuntimeException("No input stream");
        ExecutionContext ctx = new ExecutionContext();
        while (true) {
            if (Thread.currentThread().isInterrupted())
                return;
            out.print("SJ> ");
            try {
                SimpleJCode code =
                    com.simplej.language.compiler.Compiler.compileStatement(in,
                                                                    "<stdin>");
                ctx.execute(code, env);
            } catch (ExecutionException e) {
                err.println(e.toString());
            } catch (Throwable e) {
                err.println(e);
            }
        }
    }

    public void interpretFile(ExecutionContext ctx,
                              String filename,
                              Environment env) throws ExecutionException {
        Reader reader = sourceFinder.getReader(ctx, filename);
        reader = new BufferedReader(reader);
        SimpleJCode code =
            com.simplej.language.compiler.Compiler.compileProgram(reader,
                                                                  filename);
        ctx.executionStackPush(code.getCode(), env,
                               code.getSourceFileName(), 0);
        ctx.run();
        ctx.executionStackPop();
        ctx.restart();
    }

    public void interpretString(ExecutionContext ctx,
                                String string,
                                String filename,
                                Environment env) throws ExecutionException {
        Reader reader = new StringReader(string);
        SimpleJCode code =
            com.simplej.language.compiler.Compiler.compileProgram(reader,
                                                                  filename);
        ctx.executionStackPush(code.getCode(), env,
                               code.getSourceFileName(), 0);
        ctx.run();
        ctx.executionStackPop();
        ctx.restart();
    }

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();
        if (args.length == 0)
            interpreter.interpretInteractive();
        else {
            ExecutionContext ctx = new ExecutionContext();
            try {
                for (int i = 0; i < args.length; i++)
                    interpreter.interpretFile(ctx, args[i],
                                              interpreter.getEnv());
            } catch (ExecutionException e) {
                System.err.println(e.toString());
            }
        }
    }

    public synchronized void print(Object o, ExecutionContext ctx) {
        printed = new HashSet();
        print("", o, ctx);
    }

    public synchronized void println(Object o, ExecutionContext ctx) {
        printed = new HashSet();
        println("", o, ctx);
    }

    public synchronized void println() {
        out.println();
    }

    public void print(String indent, Object o, ExecutionContext ctx) {
        print(indent, o, false, ctx);
    }

    private void print(String indent, Object o, boolean inEnv,
                       ExecutionContext ctx) {
        if (o instanceof ProcedureRef) {
            out.print(indent + "<PROCEDURE ");
            ProcedureRef procedure = (ProcedureRef) o;
            out.print(procedure.getName());
            out.print(">");
        }
        /*
        else if (o instanceof Template)
            out.print(indent + "<TEMPLATE>");
        */
        else if (o instanceof Location[]) {
            if (printed.contains(o)) {
                out.print("...");
                return;
            }
            printed.add(o);
            Location[] values = (Location[]) o;
            out.print(indent + "[");
            for (int i = 0; i < values.length; i++) {
                print(values[i].getValue(), true, ctx);
                if (i < values.length - 1)
                    out.print(", ");
            }
            out.print("]");
        } else if (o instanceof String && inEnv) {
            out.print(indent + "\"");
            out.print(o);
            out.print("\"");
        } else if (o instanceof Environment) {
            if (printed.contains(o)) {
                out.print("...");
                return;
            }
            printed.add(o);
            Environment e = (Environment) o;
            out.println(indent + "{");
            String newIndent = indent + "  ";
            List names = e.getLocalLocationNames();
            Iterator iter = names.iterator();
            while (iter.hasNext())
                try {
                    String name = (String) iter.next();
                    out.print(newIndent + name + ": ");
                    Location vh = e.getLocation(name, ctx);
                    Object v = vh.getValue();
                    if (v instanceof Environment) {
                        println();
                        print(newIndent, v, ctx);
                    } else
                        print(v, true, ctx);
                    if (iter.hasNext())
                        out.println(",");
                    else
                    println();
                } catch (NoSuchLocationException ex) {
                    // Shouldn't happen
                    ex.printStackTrace();
                }
            out.print(indent + "}");
        } else
            out.print(indent + o);
    }

    private void print(Object o, boolean inEnv, ExecutionContext ctx) {
        print("", o, inEnv, ctx);
    }

    private void println(String indent, Object o, ExecutionContext ctx) {
        print(indent, o, ctx);
        out.println();
    }

}