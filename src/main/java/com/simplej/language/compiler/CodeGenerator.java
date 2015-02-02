/*
 * CodeGenerator.java
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

package com.simplej.language.compiler;

import com.simplej.language.vm.*;

import antlr.SemanticException;
import antlr.collections.AST;
import java.util.*;

public class CodeGenerator {

    private CodeChunk code = new CodeChunk();

    private CodeChunk savedCode;

    private String filename;

    private int currentLine = -1;

    private int currentColumn = 0;

    private List loops = new ArrayList();

    private List ifs = new ArrayList();

    private List shortcuts = new ArrayList();

    private Stack updateCodes = new Stack();

    private List switchInfos = new ArrayList();

    private int nestingLevel;

    private int lockCount;

    private boolean inProcedure;

    private List labels = new ArrayList();

    private String[] procedureArgNames;

    private static int labelCounter;

    private boolean errors;

    private class Label {
        private final String name = "L" + labelCounter++;

        private int target = -1;

        private List branches = new ArrayList();

        public void setTarget(int target) {
            this.target = target;
        }

        public void addBranch(Codes.Branch branch) {
            branches.add(branch);
        }

        public void updateBranches() throws SemanticException {
            if (target == -1 && branches.size() != 0) {
                errors = true;
                throw new SemanticException("undefined label " + name,
                                            filename, currentLine,
                                            currentColumn);
            }
            Iterator iter = branches.iterator();
            while (iter.hasNext()) {
                Codes.Branch branch = (Codes.Branch) iter.next();
                branch.setTarget(target);
            }
        }

        public boolean equals(Object o) {
            if (o == this)
                return true;
            if (!(o instanceof Label))
                return false;
            return ((Label) o).name == name;
        }

        public int hashCode() {
            return name.hashCode();
        }
    }

    private class LoopInfo {
        String name;

        Label startLabel = genLabel();

        Label continueLabel = genLabel();

        Label endLabel = genLabel();

        int level = nestingLevel;

        int locks = lockCount;

        boolean isSwitch;

        LoopInfo(String name) {
            this.name = name;
        }

        LoopInfo(boolean isSwitch) {
            this.isSwitch = isSwitch;
        }
    }

    private class IfInfo {
        Label falseLabel = genLabel();

        Label endLabel = genLabel();
    }

    private class ShortcutInfo {
        Label label = genLabel();
    }

    private class SwitchInfo {
        Label nextOptionStartLabel;

        Label nextOptionStatementsLabel;

        boolean hasDefault;
    }

    public CodeGenerator(String filename) {
        this.filename = filename;
    }

    private Label genLabel() {
        Label label = new Label();
        labels.add(label);
        return label;
    }

    private void updateBranches() throws SemanticException {
        Iterator iter = labels.iterator();
        while (iter.hasNext()) {
            Label label = (Label) iter.next();
            label.updateBranches();
        }
    }

    public boolean hasErrors() {
        return errors;
    }

    public void genGoto(Label label) {
        Codes.Branch branch = Codes.makeGoto();
        label.addBranch(branch);
        code.addCode(branch);
    }

    public void genGotoIfTrue(Label label) {
        Codes.Branch branch = Codes.makeGotoIfTrue();
        label.addBranch(branch);
        code.addCode(branch);
    }

    public void genGotoIfFalse(Label label) {
        Codes.Branch branch = Codes.makeGotoIfFalse();
        label.addBranch(branch);
        code.addCode(branch);
    }
        
    private void pushSwitchInfo() {
        switchInfos.add(new SwitchInfo());
    }

    private SwitchInfo getTopSwitchInfo() {
        return (SwitchInfo) switchInfos.get(switchInfos.size() - 1);
    }

    private void popSwitchInfo() {
        switchInfos.remove(switchInfos.size() - 1);
    }

    public void checkLine(int line) {
        if (line != currentLine) {
            code.addCode(Codes.makeSetLine(line));
            currentLine = line;
        }
    }

    public void checkLine(AST ast) {
        LinesAST la = (LinesAST) ast;
        int line = la.getLine();
        currentColumn = la.getColumn();
        if (line != currentLine) {
            code.addCode(Codes.makeSetLine(line));
            currentLine = line;
        }
    }

    public void forceLine() {
        currentLine = 0;
    }

    public CodeChunk getCode() throws SemanticException {
        code.addCode(Codes.STOP);
        updateBranches();
        return code;
    }

    public String getFilename() {
        return filename;
    }

    public void enterNewLocalEnv() {
        code.addCode(Codes.ENTER_NEW_LOCAL_ENV);
        nestingLevel++;
    }

    private void genExitLocalEnv() {
        code.addCode(Codes.EXIT_LOCAL_ENV);
    }

    public void exitLocalEnv() {
        genExitLocalEnv();
        nestingLevel--;
    }

    public void procedureStart(List args) {
        procedureArgNames = new String[args.size()];
        for (int i = 0; i < procedureArgNames.length; i++)
            procedureArgNames[i] = (String) args.get(i);
        inProcedure = true;
    }

    public void procedureEnd() throws SemanticException {
        code.addCode(Codes.PROCEDURE_END);
        inProcedure = false;
    }

    public void pushProcedure(CodeGenerator mcg, int startLine, String name)
        throws SemanticException {
        code.addCode(Codes.makeProcedurePush(mcg.getCode(), mcg.getFilename(),
                                             startLine, mcg.procedureArgNames,
                                             name));
    }

    public void loopStart(String name) {
        LoopInfo li = new LoopInfo(name);
        li.startLabel.setTarget(code.getSize());
        li.continueLabel.setTarget(code.getSize());
        loops.add(li);
    }

    public void whileLoopEnd() {
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        loops.remove(loops.size() - 1);
        genGoto(li.startLabel);
        li.endLabel.setTarget(code.getSize());
    }

    public void doWhileLoopEnd() {
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        loops.remove(loops.size() - 1);
        li.endLabel.setTarget(code.getSize());
    }

    public void toEndIfFalse() {
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        genGotoIfFalse(li.endLabel);
    }

    public void toStartIfTrue() {
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        genGotoIfTrue(li.startLabel);
    }

    public void forInit() {
        enterNewLocalEnv();
    }

    public void forCond(String name) {
        loopStart(name);
        forceLine();
    }

    public void forUpdate() {
        toEndIfFalse();
        savedCode = code;
        code = new CodeChunk();
    }

    public void forBody() {
        updateCodes.push(code);
        code = savedCode;
    }

    public void forEnd() {
        CodeChunk updateCode = (CodeChunk) updateCodes.pop();
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        li.continueLabel.setTarget(code.getSize());
        code.append(updateCode);
        whileLoopEnd();
        exitLocalEnv();
    }

    public void switchExprStart() {
        pushSwitchInfo();
    }

    public void switchBodyStart() {
        enterNewLocalEnv();
        LoopInfo li = new LoopInfo(true);
        loops.add(li);
        SwitchInfo si = getTopSwitchInfo();
        si.nextOptionStartLabel = genLabel();
        si.nextOptionStatementsLabel = genLabel();
    }

    public void switchBodyEnd() {
        SwitchInfo si = getTopSwitchInfo();
        si.nextOptionStartLabel.setTarget(code.getSize());
        drop();
        si.nextOptionStatementsLabel.setTarget(code.getSize());
        LoopInfo li = (LoopInfo) loops.get(loops.size() - 1);
        loops.remove(loops.size() - 1);
        li.endLabel.setTarget(code.getSize());
        exitLocalEnv();
        popSwitchInfo();
    }

    public void switchOptionStart() {
        SwitchInfo si = getTopSwitchInfo();
        si.nextOptionStartLabel.setTarget(code.getSize());
        si.nextOptionStartLabel = genLabel();
    }

    public void switchOptionColon() {
        SwitchInfo si = getTopSwitchInfo();
        si.nextOptionStatementsLabel.setTarget(code.getSize());
        si.nextOptionStatementsLabel = genLabel();
    }

    public void switchOptionEnd() {
        SwitchInfo si = getTopSwitchInfo();
        genGoto(si.nextOptionStatementsLabel);
    }

    public void switchCaseExprStart() {
        dup();
    }

    public void switchCaseExprEnd() throws SemanticException {
        SwitchInfo si = getTopSwitchInfo();
        if (si.hasDefault) {
            errors = true;
            throw new SemanticException("default must be the last option",
                                        filename, currentLine, currentColumn);
        }
        equal();
        genGotoIfFalse(si.nextOptionStartLabel);
        drop();
    }

    public void switchDefault() throws SemanticException {
        SwitchInfo si = getTopSwitchInfo();
        if (si.hasDefault) {
            errors = true;
            throw new SemanticException("switch statement can only have" +
                                        " one default",
                                        filename, currentLine, currentColumn);
        }
        si.hasDefault = true;
        drop();
    }

    public void breakLoop(String name) throws SemanticException {
        LoopInfo li = null;
        boolean found = false;
        int index = loops.size() - 1;
        while (!found && index >= 0) {
            li = (LoopInfo) loops.get(index--);
            if (name == null || name.equals(li.name))
                found = true;
        }
        if (!found) {
            errors = true;
            throw new SemanticException("break " + name + ": not found",
                                        filename, currentLine, currentColumn);
        }
        int levels = nestingLevel - li.level;
        for (int i = 0; i < levels; i++)
            genExitLocalEnv();
        releaseLocks(lockCount - li.locks);
        genGoto(li.endLabel);
    }

    public void continueLoop(String name) throws SemanticException {
        LoopInfo li = null;
        boolean found = false;
        int index = loops.size() - 1;
        while (!found && index >= 0) {
            li = (LoopInfo) loops.get(index--);
            if (!li.isSwitch && (name == null || name.equals(li.name)))
                found = true;
        }
        if (!found) {
            errors = true;
            throw new SemanticException("continue " + name + ": not found",
                                        filename, currentLine, currentColumn);
        }
        int levels = nestingLevel - li.level;
        for (int i = 0; i < levels; i++)
            genExitLocalEnv();
        releaseLocks(lockCount - li.locks);
        genGoto(li.continueLabel);
    }

    public void ifStartTrue() {
        IfInfo ii = new IfInfo();
        ifs.add(ii);
        genGotoIfFalse(ii.falseLabel);
    }

    public void ifStartFalse() {
        IfInfo ii = (IfInfo) ifs.get(ifs.size() - 1);
        genGoto(ii.endLabel);
        ii.falseLabel.setTarget(code.getSize());
    }

    public void ifEnd() {
        IfInfo ii = (IfInfo) ifs.get(ifs.size() - 1);
        ifs.remove(ifs.size() - 1);
        ii.endLabel.setTarget(code.getSize());
    }

    public void orShortcutStart() {
        ShortcutInfo si = new ShortcutInfo();
        shortcuts.add(si);
        dup();
        genGotoIfTrue(si.label);
    }

    public void andShortcutStart() {
        ShortcutInfo si = new ShortcutInfo();
        shortcuts.add(si);
        dup();
        genGotoIfFalse(si.label);
    }

    public void shortcutEnd() {
        ShortcutInfo si = (ShortcutInfo) shortcuts.get(shortcuts.size() - 1);
        shortcuts.remove(shortcuts.size() - 1);
        si.label.setTarget(code.getSize());
    }

    public void synchronizedStart() {
        code.addCode(Codes.LOCK);
        lockCount++;
    }

    private void releaseLocks(int count) {
        if (count != 0) {
            pushInt(count);
            code.addCode(Codes.RELEASE_LOCKS);
        }
    }

    public void synchronizedEnd() {
        releaseLocks(1);
        lockCount--;
    }

    public void procedureReturn() throws SemanticException {
        if (!inProcedure) {
            errors = true;
            throw new SemanticException("return outside a procedure",
                                        filename, currentLine, currentColumn);
        }
        for (int i = 0; i < nestingLevel; i++)
            genExitLocalEnv();
        releaseLocks(lockCount);
        code.addCode(Codes.RETURN);
    }

    public void createLocalValue(String varName) {
        code.addCode(Codes.makeCreateLocalValue(varName));
    }

    public void createLocal(String varName) {
        code.addCode(Codes.makeCreateLocal(varName));
    }

    public void createFinal(String varName) {
        code.addCode(Codes.makeCreateFinal(varName));
    }

    public void drop() {
        code.addCode(Codes.DROP);
    }

    public void dup() {
        code.addCode(Codes.DUP);
    }

    public void store() {
        code.addCode(Codes.STORE);
    }

    public void addStore() {
        code.addCode(Codes.STORE_ADD);
    }

    public void subStore() {
        code.addCode(Codes.STORE_SUB);
    }

    public void mulStore() {
        code.addCode(Codes.STORE_MUL);
    }

    public void divStore() {
        code.addCode(Codes.STORE_DIV);
    }

    public void modStore() {
        code.addCode(Codes.STORE_MOD);
    }

    public void bitAndStore() {
        code.addCode(Codes.STORE_BITAND);
    }

    public void bitOrStore() {
        code.addCode(Codes.STORE_BITOR);
    }

    public void bitXorStore() {
        code.addCode(Codes.STORE_BITXOR);
    }

    public void shiftLeftStore() {
        code.addCode(Codes.STORE_SHIFTLEFT);
    }

    public void shiftRightStore() {
        code.addCode(Codes.STORE_SHIFTRIGHT);
    }

    public void shiftUnsignedRightStore() {
        code.addCode(Codes.STORE_SHIFTURIGHT);
    }

    public void add() {
        code.addCode(Codes.ADD);
    }

    public void sub() {
        code.addCode(Codes.SUB);
    }

    public void mul() {
        code.addCode(Codes.MUL);
    }

    public void div() {
        code.addCode(Codes.DIV);
    }

    public void mod() {
        code.addCode(Codes.MOD);
    }

    public void shiftLeft() {
        code.addCode(Codes.SHIFTLEFT);
    }

    public void shiftRight() {
        code.addCode(Codes.SHIFTRIGHT);
    }

    public void shiftUnsignedRight() {
        code.addCode(Codes.SHIFTURIGHT);
    }

    public void greater() {
        code.addCode(Codes.GREATER);
    }

    public void greaterOrEqual() {
        code.addCode(Codes.GREATER_EQUAL);
    }

    public void less() {
        code.addCode(Codes.LESS);
    }

    public void lessOrEqual() {
        code.addCode(Codes.LESS_EQUAL);
    }

    public void equal() {
        code.addCode(Codes.EQUAL);
    }

    public void notEqual() {
        code.addCode(Codes.NOT_EQUAL);
    }

    public void or() {
        code.addCode(Codes.OR);
    }

    public void and() {
        code.addCode(Codes.AND);
    }

    public void bitOr() {
        code.addCode(Codes.BITOR);
    }

    public void bitXor() {
        code.addCode(Codes.BITXOR);
    }

    public void bitAnd() {
        code.addCode(Codes.BITAND);
    }

    public void setSuper() {
        code.addCode(Codes.SET_SUPER);
    }

    public void minus() {
        code.addCode(Codes.MINUS);
    }

    public void bitNot() {
        code.addCode(Codes.BITNOT);
    }

    public void not() {
        code.addCode(Codes.NOT);
    }

    public void preIncr() {
        code.addCode(Codes.PRE_INCR);
    }

    public void preDecr() {
        code.addCode(Codes.PRE_DECR);
    }

    public void postIncr() {
        code.addCode(Codes.POST_INCR);
    }

    public void postDecr() {
        code.addCode(Codes.POST_DECR);
    }

    public void createArrayValues(int count) {
        pushInt(count);
        code.addCode(Codes.ARRAY_VALUES);
    }

    public void createArrayDimensions(int count) {
        pushInt(count);
        code.addCode(Codes.ARRAY_DIMENSIONS);
    }

    public void createEnvironmentValues(int count) {
        pushInt(count);
        code.addCode(Codes.ENVIRONMENT_PAIRS);
    }

    public void fetch() {
        code.addCode(Codes.FETCH);
    }

    public void procedureCall(int argCount) {
        code.addCode(Codes.makeProcedureCall(argCount));
    }

    public void arrayRef() {
        code.addCode(Codes.ARRAY_REF);
    }

    public void envRef(String key) {
        code.addCode(Codes.makeEnvRef(key));
    }

    public void pushLocation(String varName) {
        code.addCode(Codes.makeVariable(varName));
    }

    public void pushThis() {
        code.addCode(Codes.THIS);
    }

    public void pushSuper() {
        code.addCode(Codes.SUPER);
    }

    public void makeLocation() {
        code.addCode(Codes.MAKE_LOCATION);
    }

    public void pushString(String s) {
        code.addCode(Codes.makeConstant(s));
    }

    public void pushChar(char c) {
        code.addCode(Codes.makeConstant(new Integer(c)));
    }

    public void pushInt(int i) {
        code.addCode(Codes.makeConstant(new Integer(i)));
    }

    public void pushFloat(double f) {
        code.addCode(Codes.makeConstant(new Double(f)));
    }

    public void pushBoolean(boolean b) {
        code.addCode(Codes.makeConstant(Boolean.valueOf(b)));
    }

    public void pushNull() {
        code.addCode(Codes.makeConstant(null));
    }
    
    public void pushVoid() {
        code.addCode(Codes.makeConstant(Uninitialized.VALUE));
    }

}
