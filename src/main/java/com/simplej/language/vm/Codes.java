/*
 * Codes.java
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

package com.simplej.language.vm;

import java.io.*;

public class Codes {

    /*********
     * Store *
     *********/
    public final static Code STORE = new Store();

    private static class Store implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object b = ctx.evaluationStackPop();
            if (b == Uninitialized.VALUE)
                throw new VoidValueException(ctx);
            Location aLoc = ctx.evaluationStackPopLocation();
            aLoc.setValue(b, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(b);
        }

        public String toString() {return "store";}
    }

    /*********
     * Fetch *
     *********/
    public final static Code FETCH = new Fetch();

    private static class Fetch implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location loc = ctx.evaluationStackPopLocation();
            Object v = loc.getValue();
            if (v == Uninitialized.VALUE)
                throw new UninitializedLocationException(ctx);
            ctx.evaluationStackPush(v);
        }

        public String toString() {return "fetch";}
    }

    /********
     * Drop *
     ********/
    public final static Code DROP = new Drop();

    private static class Drop implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.evaluationStackPop();
        }

        public String toString() {return "drop";}
    }

    /*******
     * Dup *
     *******/
    public final static Code DUP = new Dup();

    private static class Dup implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object o = ctx.evaluationStackPop();
            ctx.evaluationStackPush(o);
            ctx.evaluationStackPush(o);
        }

        public String toString() {return "dup";}
    }
    
    /*******
     * Add *
     *******/
    private static Object doAdd(Object a, Object b, ExecutionContext ctx)
        throws NotANumberOrStringException {
        if (a instanceof String || b instanceof String)
            return a.toString() + b.toString();
        if (!(a instanceof Number))
            throw new NotANumberOrStringException(ctx);
        Number na = (Number) a;
        if (!(b instanceof Number))
            throw new NotANumberOrStringException(ctx);
        Number nb = (Number) b;
        if (na instanceof Double || nb instanceof Double)
            return new Double(na.doubleValue() + nb.doubleValue());
        else
            return new Integer(na.intValue() + nb.intValue());
    }

    public final static Code ADD = new Add();

    private static class Add implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object b = ctx.evaluationStackPop();
            Object a = ctx.evaluationStackPop();
            ctx.evaluationStackPush(doAdd(a, b, ctx));
        }

        public String toString() {return "+";}
    }

    public final static Code STORE_ADD = new StoreAdd();

    private static class StoreAdd implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Object b = ctx.evaluationStackPop();
            Location aLoc = ctx.evaluationStackPopLocation();
            Object result = doAdd(aLoc.getValue(), b, ctx);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "+=";}
    }

    /*******
     * Sub *
     *******/
    private static Number doSub(Number a, Number b) {
        if (a instanceof Double || b instanceof Double)
            return new Double(a.doubleValue() - b.doubleValue());
        else
            return new Integer(a.intValue() - b.intValue());
    }

    public final static Code SUB = new Sub();

    private static class Sub implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doSub(a, b));
        }

        public String toString() {return "-";}
    }

    public final static Code STORE_SUB = new StoreSub();

    private static class StoreSub implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doSub((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "-=";}
    }

    /*******
     * Mul *
     *******/
    private static Number doMul(Number a, Number b) {
        if (a instanceof Double || b instanceof Double)
            return new Double(a.doubleValue() * b.doubleValue());
        else
            return new Integer(a.intValue() * b.intValue());
    }

    public final static Code MUL = new Mul();

    private static class Mul implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doMul(a, b));
        }

        public String toString() {return "*";}
    }

    public final static Code STORE_MUL = new StoreMul();

    private static class StoreMul implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doMul((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "*=";}
    }

    /*******
     * Div *
     *******/
    private static Number doDiv(Number a, Number b, ExecutionContext ctx)
        throws DivisionByZeroException {
        if (b.doubleValue() == 0.0)
            throw new DivisionByZeroException(ctx);
        if (a instanceof Double || b instanceof Double)
            return new Double(a.doubleValue() / b.doubleValue());
        else
            return new Integer(a.intValue() / b.intValue());
    }

    public final static Code DIV = new Div();

    private static class Div implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doDiv(a, b, ctx));
        }

        public String toString() {return "/";}
    }

    public final static Code STORE_DIV = new StoreDiv();

    private static class StoreDiv implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doDiv((Number) aLoc.getValue(), b, ctx);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "/=";}
    }

    /*******
     * Mod *
     *******/
    private static Number doMod(Number a, Number b, ExecutionContext ctx)
        throws DivisionByZeroException {
        if (b.intValue() == 0)
            throw new DivisionByZeroException(ctx);
        return new Integer(a.intValue() % b.intValue());
    }

    public final static Code MOD = new Mod();

    private static class Mod implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doMod(a, b, ctx));
        }

        public String toString() {return "%";}
    }

    public final static Code STORE_MOD = new StoreMod();

    private static class StoreMod implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doMod((Number) aLoc.getValue(), b, ctx);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "%=";}
    }

    /*********
     * Minus *
     *********/
    private static Number doMinus(Number a) {
        if (a instanceof Double)
            return new Double(-a.doubleValue());
        else
            return new Integer(-a.intValue());
    }

    public final static Code MINUS = new Minus();

    private static class Minus implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doMinus(a));
        }

        public String toString() {return "- (unary)";}
    }

    /********
     * Incr *
     ********/
    private static Number doIncr(Location aLoc) {
        Number a = (Number) aLoc.getValue();
        if (a instanceof Double)
            return new Double(a.doubleValue() + 1.0);
        else
            return new Integer(a.intValue() + 1);
    }

    public final static Code PRE_INCR = new PreIncr();

    private static class PreIncr implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doIncr(aLoc);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "++ (pre)";}
    }

    public final static Code POST_INCR = new PostIncr();

    private static class PostIncr implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            ctx.evaluationStackPush(aLoc.getValue());
            aLoc.setValue(doIncr(aLoc), ctx);
            ctx.modifiedLocation(aLoc);
        }

        public String toString() {return "++ (post)";}
    }

    /********
     * Decr *
     ********/
    private static Number dodecr(Location aLoc) {
        Number a = (Number) aLoc.getValue();
        if (a instanceof Double)
            return new Double(a.doubleValue() - 1.0);
        else
            return new Integer(a.intValue() - 1);
    }

    public final static Code PRE_DECR = new Predecr();

    private static class Predecr implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = dodecr(aLoc);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "-- (pre)";}
    }

    public final static Code POST_DECR = new Postdecr();

    private static class Postdecr implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            ctx.evaluationStackPush(aLoc.getValue());
            aLoc.setValue(dodecr(aLoc), ctx);
            ctx.modifiedLocation(aLoc);
        }

        public String toString() {return "-- (post)";}
    }

    /**********
     * BitAnd *
     **********/
    private static Number doBitAnd(Number a, Number b) {
        return new Integer(a.intValue() & b.intValue());
    }

    public final static Code BITAND = new BitAnd();

    private static class BitAnd implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doBitAnd(a, b));
        }

        public String toString() {return "&";}
    }

    public final static Code STORE_BITAND = new StoreBitAnd();

    private static class StoreBitAnd implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doBitAnd((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "&=";}
    }

    /*********
     * BitOr *
     *********/
    private static Number doBitOr(Number a, Number b) {
        return new Integer(a.intValue() | b.intValue());
    }

    public final static Code BITOR = new BitOr();

    private static class BitOr implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doBitOr(a, b));
        }

        public String toString() {return "|";}
    }

    public final static Code STORE_BITOR = new StoreBitOr();

    private static class StoreBitOr implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doBitOr((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "|=";}
    }

    /**********
     * BitXor *
     **********/
    private static Number doBitXor(Number a, Number b) {
        return new Integer(a.intValue() ^ b.intValue());
    }

    public final static Code BITXOR = new BitXor();

    private static class BitXor implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doBitXor(a, b));
        }

        public String toString() {return "^";}
    }

    public final static Code STORE_BITXOR = new StoreBitXor();

    private static class StoreBitXor implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doBitXor((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "^=";}
    }

    /*********
     * BitNot *
     *********/
    private static Number doBitNot(Number a) {
        return new Integer(~a.intValue());
    }

    public final static Code BITNOT = new BitNot();

    private static class BitNot implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doBitNot(a));
        }

        public String toString() {return "~";}
    }

    /*************
     * ShiftLeft *
     *************/
    private static Number doShiftLeft(Number a, Number b) {
        return new Integer(a.intValue() << b.intValue());
    }

    public final static Code SHIFTLEFT = new ShiftLeft();

    private static class ShiftLeft implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doShiftLeft(a, b));
        }

        public String toString() {return "<<";}
    }

    public final static Code STORE_SHIFTLEFT = new StoreShiftLeft();

    private static class StoreShiftLeft implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doShiftLeft((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "<<=";}
    }

    /**************
     * ShiftRight *
     **************/
    private static Number doShiftRight(Number a, Number b) {
        return new Integer(a.intValue() >> b.intValue());
    }

    public final static Code SHIFTRIGHT = new ShiftRight();

    private static class ShiftRight implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doShiftRight(a, b));
        }

        public String toString() {return ">>";}
    }

    public final static Code STORE_SHIFTRIGHT = new StoreShiftRight();

    private static class StoreShiftRight implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doShiftRight((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return ">>=";}
    }

    /***************
     * ShiftURight *
     ***************/
    private static Number doShiftURight(Number a, Number b) {
        return new Integer(a.intValue() >>> b.intValue());
    }

    public final static Code SHIFTURIGHT = new ShiftURight();

    private static class ShiftURight implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Number a = ctx.evaluationStackPopNumber();
            ctx.evaluationStackPush(doShiftURight(a, b));
        }

        public String toString() {return ">>>";}
    }

    public final static Code STORE_SHIFTURIGHT = new StoreShiftURight();

    private static class StoreShiftURight implements Code {
        public void execute (ExecutionContext ctx) throws ExecutionException {
            Number b = ctx.evaluationStackPopNumber();
            Location aLoc = ctx.evaluationStackPopNumberLocation();
            Number result = doShiftURight((Number) aLoc.getValue(), b);
            aLoc.setValue(result, ctx);
            ctx.modifiedLocation(aLoc);
            ctx.evaluationStackPush(result);
        }

        public String toString() {return ">>>=";}
    }

    /*******
     * And *
     *******/
    private static Boolean doAnd(Boolean a, Boolean b) {
        return Boolean.valueOf(a.booleanValue() && b.booleanValue());
    }

    public final static Code AND = new And();

    private static class And implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Boolean b = ctx.evaluationStackPopBoolean();
            Boolean a = ctx.evaluationStackPopBoolean();
            ctx.evaluationStackPush(doAnd(a, b));
        }

        public String toString() {return "&&";}
    }

    /*******
     * Or *
     *******/
    private static Boolean doOr(Boolean a, Boolean b) {
        return Boolean.valueOf(a.booleanValue() || b.booleanValue());
    }

    public final static Code OR = new Or();

    private static class Or implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Boolean b = ctx.evaluationStackPopBoolean();
            Boolean a = ctx.evaluationStackPopBoolean();
            ctx.evaluationStackPush(doOr(a, b));
        }

        public String toString() {return "||";}
    }

    /*******
     * Not *
     *******/
    private static Boolean doNot(Boolean a) {
        return Boolean.valueOf(!a.booleanValue());
    }

    public final static Code NOT = new Not();

    private static class Not implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Boolean a = ctx.evaluationStackPopBoolean();
            ctx.evaluationStackPush(doNot(a));
        }

        public String toString() {return "!";}
    }

    /***********
     * Greater *
     ***********/
    private static Boolean doGreater(Comparable a, Comparable b,
                                     ExecutionContext ctx)
        throws CantCompareException {
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() > nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() > nb.intValue());
        }
        try {
            return Boolean.valueOf(a.compareTo(b) > 0);
        } catch (ClassCastException e) {
            throw new CantCompareException(ctx);
        }
    }

    public final static Code GREATER = new Greater();

    private static class Greater implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Comparable b = ctx.evaluationStackPopComparable();
            Comparable a = ctx.evaluationStackPopComparable();
            ctx.evaluationStackPush(doGreater(a, b, ctx));
        }

        public String toString() {return ">";}
    }
    
    /****************
     * GreaterEqual *
     ****************/
    private static Boolean doGreaterEqual(Comparable a, Comparable b,
                                          ExecutionContext ctx)
        throws CantCompareException {
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() >= nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() >= nb.intValue());
        }
        try {
            return Boolean.valueOf(a.compareTo(b) >= 0);
        } catch (ClassCastException e) {
            throw new CantCompareException(ctx);
        }
    }

    public final static Code GREATER_EQUAL = new GreaterEqual();

    private static class GreaterEqual implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Comparable b = ctx.evaluationStackPopComparable();
            Comparable a = ctx.evaluationStackPopComparable();
            ctx.evaluationStackPush(doGreaterEqual(a, b, ctx));
        }

        public String toString() {return ">=";}
    }
    
    /********
     * Less *
     ********/
    private static Boolean doLess(Comparable a, Comparable b,
                                  ExecutionContext ctx)
        throws CantCompareException {
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() < nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() < nb.intValue());
        }
        try {
            return Boolean.valueOf(a.compareTo(b) < 0);
        } catch (ClassCastException e) {
            throw new CantCompareException(ctx);
        }
    }

    public final static Code LESS = new Less();

    private static class Less implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Comparable b = ctx.evaluationStackPopComparable();
            Comparable a = ctx.evaluationStackPopComparable();
            ctx.evaluationStackPush(doLess(a, b, ctx));
        }

        public String toString() {return "<";}
    }
    
    /*************
     * LessEqual *
     *************/
    private static Boolean doLessEqual(Comparable a, Comparable b,
                                       ExecutionContext ctx)
        throws CantCompareException {
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() <= nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() <= nb.intValue());
        }
        try {
            return Boolean.valueOf(a.compareTo(b) <= 0);
        } catch (ClassCastException e) {
            throw new CantCompareException(ctx);
        }
    }

    public final static Code LESS_EQUAL = new LessEqual();

    private static class LessEqual implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Comparable b = ctx.evaluationStackPopComparable();
            Comparable a = ctx.evaluationStackPopComparable();
            ctx.evaluationStackPush(doLessEqual(a, b, ctx));
        }

        public String toString() {return "<=";}
    }

    /*********
     * Equal *
     *********/
    private static Boolean doEqual(Object a, Object b) {
        if (a instanceof String && b instanceof String)
            return Boolean.valueOf(a.equals(b));
        if (a instanceof Boolean && b instanceof Boolean)
            return Boolean.valueOf(a.equals(b));
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() == nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() == nb.intValue());
        }
        return Boolean.valueOf(a == b);
    }

    public final static Code EQUAL = new Equal();

    private static class Equal implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object a = ctx.evaluationStackPop();
            Object b = ctx.evaluationStackPop();
            ctx.evaluationStackPush(doEqual(a, b));
        }

        public String toString() {return "==";}
    }
    
    /************
     * NotEqual *
     ************/
    private static Boolean doNotEqual(Object a, Object b) {
        if (a instanceof String && b instanceof String)
            return Boolean.valueOf(!a.equals(b));
        if (a instanceof Boolean && b instanceof Boolean)
            return Boolean.valueOf(!a.equals(b));
        if (a instanceof Number && b instanceof Number) {
            Number na = (Number) a;
            Number nb = (Number) b;
            if (na instanceof Double || nb instanceof Double)
                return Boolean.valueOf(na.doubleValue() != nb.doubleValue());
            else
                return Boolean.valueOf(na.intValue() != nb.intValue());
        }
        return Boolean.valueOf(a != b);
    }

    public final static Code NOT_EQUAL = new NotEqual();

    private static class NotEqual implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object a = ctx.evaluationStackPop();
            Object b = ctx.evaluationStackPop();
            ctx.evaluationStackPush(doNotEqual(a, b));
        }

        public String toString() {return "!=";}
    }

    /************
     * Constant *
     ************/
    public static Code makeConstant(Object value) {
        return new Constant(value);
    }

    private static class Constant implements Code {
        Object value;

        Constant(Object value) {
            this.value = value;
        }

        public void execute(ExecutionContext ctx) {
            ctx.evaluationStackPush(value);
        }

        public String toString() {
            if (value == null)
                return "null";
            return value.toString();
        }
    }

    /************
     * Variable *
     ************/
    public static Code makeVariable(String varName) {
        return new Variable(varName);
    }

    private static class Variable implements Code {
        String varName;

        public Variable(String varName) {
            this.varName = varName;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Location loc =
                ctx.getCurrentEnvironment().getLocation(varName, ctx);
            ctx.evaluationStackPush(loc);
        }

        public String toString() {return "variable " + varName;}
    }

    public static Code makeCreateLocalValue(String varName) {
        return new CreateLocalValue(varName);
    }

    private static class CreateLocalValue implements Code {
        String varName;

        public CreateLocalValue(String varName) {
            this.varName = varName;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object value = ctx.evaluationStackPop();
            ctx.getCurrentEnvironment().createLocal(varName, value, ctx);
            Location loc =
                ctx.getCurrentEnvironment().getLocation(varName, ctx);
            ctx.createdLocation(loc);
        }

        public String toString() {return "create local value " + varName;}
    }

    public static Code makeCreateLocal(String varName) {
        return new CreateLocal(varName);
    }

    private static class CreateLocal implements Code {
        String varName;

        public CreateLocal(String varName) {
            this.varName = varName;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.getCurrentEnvironment().createLocal(varName, ctx);
            Location loc =
                ctx.getCurrentEnvironment().getLocation(varName, ctx);
            ctx.createdLocation(loc);
        }

        public String toString() {return "create local " + varName;}
    }

    public static Code makeCreateFinal(String varName) {
        return new CreateFinal(varName);
    }

    private static class CreateFinal implements Code {
        String varName;

        public CreateFinal(String varName) {
            this.varName = varName;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object value = ctx.evaluationStackPop();
            ctx.getCurrentEnvironment().createFinalLocal(varName, value, ctx);
            Location loc =
                ctx.getCurrentEnvironment().getLocation(varName, ctx);
            ctx.createdLocation(loc);
        }

        public String toString() {return "create final " + varName;}
    }

    /********
     * This *
     ********/
    public final static Code THIS = new This();

    private static class This implements Code {
        public void execute(ExecutionContext ctx) {
            ctx.evaluationStackPush(new FinalLocation("this",
                                                  ctx.getCurrentEnvironment()));
        }

        public String toString() {return "this";}
    }

    /*********
     * Super *
     *********/
    public final static Code SUPER = new Super();

    private static class Super implements Code {
        public void execute(ExecutionContext ctx) {
            ctx.evaluationStackPush(
                             new FinalLocation("super",
                                      ctx.getCurrentEnvironment().getParent()));
        }

        public String toString() {return "super";}
    }

    /****************
     * MakeLocation *
     ****************/
    public final static Code MAKE_LOCATION = new MakeLocation();

    private static class MakeLocation implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.evaluationStackPush(
                                    new Location(ctx.evaluationStackPop()));
        }

        public String toString() {return "make location";}
    }

    /*********
     * Array *
     *********/
    public final static Code ARRAY_VALUES = new ArrayValues();

    private static class ArrayValues implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            int size = ctx.evaluationStackPopNumber().intValue();
            Location[] result = new Location[size];
            for (int i = size - 1; i >= 0; i--)
                result[i] = new Location(ctx.evaluationStackPop());
            ctx.evaluationStackPush(result);
        }

        public String toString() {return "array values";}
    }
    
    private static Location[] createArray(ExecutionContext ctx,
                                          int[] dimensions, int index) {
        if (index == dimensions.length)
            return null;
        Location[] result = new Location[dimensions[index]];
        for (int i = 0; i < result.length; i++) {
            Object value = createArray(ctx, dimensions, index + 1);
            if (value != null)
                result[i] = new Location(value);
            else
                result[i] = new Location();
            ctx.createdLocation(result[i]);
        }
        return result;
    }

    public final static Code ARRAY_DIMENSIONS = new ArrayDimensions();

    private static class ArrayDimensions implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            int count = ctx.evaluationStackPopNumber().intValue();
            int[] dimensions = new int[count];
            for (int i = count - 1; i >= 0; i--)
                dimensions[i] = ctx.evaluationStackPopNumber().intValue();
            ctx.evaluationStackPush(createArray(ctx, dimensions, 0));
        }

        public String toString() {return "array dimensions";}
    }

    /***************
     * Environment *
     ***************/
    public final static Code ENVIRONMENT_PAIRS = new EnvironmentPairs();

    private static class EnvironmentPairs implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            int count = ctx.evaluationStackPopNumber().intValue();
            Environment env = new Environment(null, null);
            for (int i = count - 1; i >= 0; i--) {
                Object value = ctx.evaluationStackPop();
                String key = ctx.evaluationStackPopString();
                env.createLocal(key, value, ctx);
                Location loc = env.getLocation(key, ctx);
                ctx.createdLocation(loc);
            }
            ctx.evaluationStackPush(env);
        }

        public String toString() {return "environment pairs";}
    }

    /************
     * SetSuper *
     ************/
    public final static Code SET_SUPER = new SetSuper();

    private static class SetSuper implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            // TODO: add support for procedures
            // Can't use popEnvironment because null is a valid value
            Object o = ctx.evaluationStackPop();
            if (!(o instanceof Environment) && o != null)
                throw new NotAnEnvironmentException(ctx);
            Environment parent = (Environment) o;
            o = ctx.evaluationStackPop();
            if (!(o instanceof Environment) &&
                !(o instanceof SimpleJProcedureRef))
                throw new NotAnEnvironmentOrProcedureException(ctx);
            if (o instanceof Environment) {
                Environment env = (Environment) o;
                Environment result = env.copy(ctx);
                result.setParent(parent);
                ctx.evaluationStackPush(result);
            } else {
                SimpleJProcedureRef proc = (SimpleJProcedureRef) o;
                SimpleJProcedureRef result = proc.copy();
                result.setParent(parent, ctx);
                ctx.evaluationStackPush(result);
            }
        }

        public String toString() {return "->";}
    }

    /**********
     * EnvRef *
     **********/
    public static Code makeEnvRef(String locationName) {
        return new EnvRef(locationName);
    }
    
    private static class EnvRef implements Code {
        String locationName;

        public EnvRef(String locationName) {
            this.locationName = locationName;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Environment env = ctx.evaluationStackPopEnvironment();
            ctx.evaluationStackPush(env.getLocation(locationName, ctx));
        }

        public String toString() {return "env ref " + locationName;}
    }

    /************
     * ArrayRef *
     ************/
    public static Code ARRAY_REF = new ArrayRef();

    private static class ArrayRef implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object idx = ctx.evaluationStackPop();
            Object o = ctx.evaluationStackPop();
            if (o instanceof Location[]) {
                Location[] array = (Location[]) o;
                if (!(idx instanceof Number))
                    throw new NotANumberException(ctx);
                int index = ((Number) idx).intValue();
                if (index < 0 || index >= array.length)
                    throw new OutOfBoundsException(ctx);
                ctx.evaluationStackPush(array[index]);
            } else if (o instanceof Environment) {
                Environment env = (Environment) o;
                if (idx == null)
                    throw new NullReferenceException(ctx);
                String name = idx.toString();
                if (!env.hasLocation(name)) {
                    env.createLocal(name, ctx);
                    Location loc = env.getLocation(name, ctx);
                    ctx.createdLocation(loc);
                }
                ctx.evaluationStackPush(env.getLocation(name, ctx));
            } else
                throw new NotAnArrayOrEnvironmentException(ctx);
        }

        public String toString() {return "[]";}
    }

    /**********
     * Branch *
     **********/

    public interface Branch extends Code {
        void setTarget(int target);
    }

    public static Branch makeGoto() {
        return new Goto();
    }

    private static class Goto implements Branch {
        int target = -1;

        public void execute(ExecutionContext ctx) {
            ctx.setCodePtr(target);
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public String toString() {return "goto " + target;}
    }

    public static Branch makeGotoIfTrue() {
        return new GotoIfTrue();
    }

    private static class GotoIfTrue implements Branch {
        int target = -1;

        public void execute(ExecutionContext ctx) throws ExecutionException {
            Boolean b = ctx.evaluationStackPopBoolean();
            if (b.booleanValue())
                ctx.setCodePtr(target);
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public String toString() {return "goto if true " + target;}
    }

    public static Branch makeGotoIfFalse() {
        return new GotoIfFalse();
    }

    private static class GotoIfFalse implements Branch {
        int target = -1;

        public void execute(ExecutionContext ctx) throws ExecutionException {
            Boolean b = ctx.evaluationStackPopBoolean();
            if (!b.booleanValue())
                ctx.setCodePtr(target);
        }

        public void setTarget(int target) {
            this.target = target;
        }

        public String toString() {return "goto if false " + target;}
    }

    /****************
     * Environments *
     ****************/
    public static Code ENTER_NEW_LOCAL_ENV = new EnterNewLocalEnv();

    private static class EnterNewLocalEnv implements Code {
        public void execute(ExecutionContext ctx) {
            Environment parent = ctx.getCurrentEnvironment();
            ctx.setCurrentEnvironment(new Environment(parent, null));
        }

        public String toString() {return "enter new local env";}
    }

    public static Code EXIT_LOCAL_ENV = new ExitLocalEnv();

    private static class ExitLocalEnv implements Code {
        public void execute(ExecutionContext ctx) {
            Environment env = ctx.getCurrentEnvironment();
            ctx.setCurrentEnvironment(env.getParent());
        }

        public String toString() {return "exit local env";}
    }

    /*********
     * Debug *
     *********/
    public static Code makeSetLine(int line) {
        return new SetLine(line);
    }

    private static class SetLine implements Code {
        int line;

        SetLine(int line) {
            this.line = line;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.setSourceLine(line);
        }

        public String toString() {return "set line " + line;}
    }

    /**************
     * Procedures *
     **************/
    public static Code makeProcedureCall(int argCount) {
        return new ProcedureCall(argCount);
    }

    private static class ProcedureCall implements Code {
        int argCount;

        public ProcedureCall(int argCount) {
            this.argCount = argCount;
        }
        
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object[] args = new Object[argCount];
            for (int i = argCount - 1; i >= 0; i--)
                args[i] = ctx.evaluationStackPop();
            ProcedureRef mr = ctx.evaluationStackPopProcedureRef();
            mr.call(ctx, args);
        }

        public String toString() {return "procedure call " + argCount;}
    }

    public static Code RETURN = new Return();

    private static class Return implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.executionStackPop();
        }

        public String toString() {return "return";}
    }

    public static Code PROCEDURE_END = new ProcedureEnd();

    private static class ProcedureEnd implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            ctx.evaluationStackPush(Uninitialized.VALUE);
            ctx.executionStackPop();
        }

        public String toString() {return "procedureEnd";}
    }

    public static Code makeProcedurePush(CodeChunk code, String sourceFileName,
                                         int sourceFileLine, String[] argNames,
                                         String name) {
        return new ProcedurePush(code, sourceFileName, sourceFileLine, argNames,
                                 name);
    }

    private static class ProcedurePush implements Code {
        private CodeChunk code;

        private String sourceFileName;

        private int sourceFileLine;

        private String[] argNames;

        private String name;

        public ProcedurePush(CodeChunk code, String sourceFileName,
                             int sourceFileLine, String[] argNames,
                             String name) {
            this.code = code;
            this.sourceFileName = sourceFileName;
            this.sourceFileLine = sourceFileLine;
            this.argNames = argNames;
            this.name = name;
        }
        
        public void execute(ExecutionContext ctx) {
            ProcedureRef mr =
                new SimpleJProcedureRef(code, ctx.getCurrentEnvironment(),
                                        sourceFileName, sourceFileLine,
                                        argNames, name);
            ctx.evaluationStackPush(mr);
        }

        public String toString() {return "procedure push";}
    }
    
    /*********
     * Locks *
     *********/
    public static Code LOCK = new Lock();

    private static class Lock implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            Object o = ctx.evaluationStackPop();
            ctx.pushLockObject(o);
            LockManager.MGR.acquire(ctx, o);
        }

        public String toString() {return "lock";}
    }

    public static Code RELEASE_LOCKS = new ReleaseLocks();

    private static class ReleaseLocks implements Code {
        public void execute(ExecutionContext ctx) throws ExecutionException {
            int lockCount = ctx.evaluationStackPopNumber().intValue();
            Object o = ctx.popLockObject(ctx);
            LockManager.MGR.release(ctx, o, lockCount);
        }

        public String toString() {return "release locks";}
    }

    /********
     * Stop *
     ********/
    public static Code STOP = new Stop();

    private static class Stop implements Code {
        public void execute(ExecutionContext ctx) {
            ctx.stop();
        }

        public String toString() {return "stop";}
    }
    
}