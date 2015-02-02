/*
 * ExecutionContext.java
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
import java.util.*;

public class ExecutionContext implements Serializable {

    private final EvaluationStack evaluationStack = new EvaluationStack();

    private final ExecutionStack executionStack = new ExecutionStack();

    private final Stack lockedObjects = new Stack();

    private ExecutionFrame currentFrame;

    private boolean running;

    private boolean freezed;

    private List listeners = new ArrayList();

    private String lastSourceName;

    private int instructionCounter;

    public synchronized void addListener(ExecutionContextListener ecl) {
        if (ecl == null)
            throw new NullPointerException("listener is null");
        listeners.add(ecl);
    }

    public synchronized void removeListener(ExecutionContextListener ecl) {
        listeners.remove(ecl);
    }

    public synchronized void sourceLineChanged() throws ExecutionException {
        int n = listeners.size();
        String sourceName = getSourceName();
        if (lastSourceName == null || !lastSourceName.equals(sourceName)) {
            for (int i = 0; i < n; i++) {
                ExecutionContextListener ecl =
                    (ExecutionContextListener) listeners.get(i);
                ecl.sourceLineChanged(this, getSourceLine(), sourceName);
            }
            lastSourceName = sourceName;
        } else
            for (int i = 0; i < n; i++) {
                ExecutionContextListener ecl =
                    (ExecutionContextListener) listeners.get(i);
                ecl.sourceLineChanged(this, getSourceLine());
            }
    }

    public synchronized void executionFinished() {
        int n = listeners.size();
        for (int i = 0; i < n; i++) {
            ExecutionContextListener ecl =
                (ExecutionContextListener) listeners.get(i);
            ecl.executionFinished(this);
        }
    }

    public synchronized void willExecute(Code code) {
        int n = listeners.size();
        for (int i = 0; i < n; i++) {
            ExecutionContextListener ecl =
                (ExecutionContextListener) listeners.get(i);
            ecl.willExecute(this, code);
        }
    }

    public synchronized void modifiedLocation(Location location) {
        int n = listeners.size();
        for (int i = 0; i < n; i++) {
            ExecutionContextListener ecl =
                (ExecutionContextListener) listeners.get(i);
            ecl.modifiedLocation(this, location);
        }
    }

    public synchronized void createdLocation(Location location) {
        int n = listeners.size();
        for (int i = 0; i < n; i++) {
            ExecutionContextListener ecl =
                (ExecutionContextListener) listeners.get(i);
            ecl.createdLocation(this, location);
        }
    }

    public void execute(SimpleJCode code, Environment env)
        throws ExecutionException {
        execute(code.getCode(), env, code.getSourceFileName(), 0);
    }

    public void execute(CodeChunk codeChunk, Environment environment,
                        String sourceFileName, int sourceFileLine)
        throws ExecutionException {
        currentFrame = new ExecutionFrame(codeChunk, environment,
                                          sourceFileName, sourceFileLine);
        evaluationStack.reset();
        executionStack.reset();
        sourceLineChanged();
        run();
    }

    public void run() throws ExecutionException {
        running = true;
        Thread currentThread = Thread.currentThread();
        while (running) {
            synchronized (this) {
                while (freezed)
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        freezed = false;
                        currentThread.interrupt();
                    }
            }
            executeOneInstruction();
            if (currentThread.isInterrupted()) {
                running = false;
                LockManager.MGR.releaseAllLocksForThread(this);
            }
        }
    }

    public void executeOneInstruction() throws ExecutionException {
        Code code =
            currentFrame.codeChunk.getCodeAt(currentFrame.codePtr++, this);
        willExecute(code);
        try {
            code.execute(this);
            instructionCounter++;
        } catch (ExecutionException e) {
            LockManager.MGR.releaseAllLocksForThread(this);
            throw e;
        }
    }

    public void stop() {
        running = false;
    }

    public void restart() {
        running = true;
    }

    public synchronized void unfreeze() {
        freezed = false;
        notify();
    }

    public synchronized void freeze() {
        freezed = true;
    }

    public void executionStackPush(CodeChunk codeChunk, Environment environment,
                                   String sourceFileName, int sourceFileLine)
        throws ExecutionException {
        executionStack.push(currentFrame);
        currentFrame = new ExecutionFrame(codeChunk, environment,
                                          sourceFileName, sourceFileLine);
        sourceLineChanged();
    }

    public void executionStackPop() throws ExecutionException {
        currentFrame = executionStack.pop(this);
        if (currentFrame != null)
            sourceLineChanged();
        else
            executionFinished();
    }

    public boolean isRunning() {
        return running;
    }

    public Environment getCurrentEnvironment() {
        return currentFrame.environment;
    }

    public void setCurrentEnvironment(Environment env) {
        currentFrame.environment = env;
    }

    public void setCodePtr(int codePtr) {
        currentFrame.codePtr = codePtr;
    }

    public int getCodePtr() {
        return currentFrame.codePtr;
    }

    public void setSourceLine(int line) throws ExecutionException {
        if (line != currentFrame.sourceFileLine) {
            currentFrame.sourceFileLine = line;
            sourceLineChanged();
        }
    }

    public int getSourceLine() {
        return currentFrame.sourceFileLine;
    }

    public String getSourceName() {
        return currentFrame.sourceFileName;
    }

    public void evaluationStackPush(Object o) {
        evaluationStack.push(o);
    }

    public Object evaluationStackPop()
        throws EvaluationStackUnderflowException {
        return evaluationStack.pop(this);
    }

    public Location evaluationStackPopLocation()
        throws NotALocationException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Location))
            throw new NotALocationException(this);
        return (Location) o;
    }

    public Number evaluationStackPopNumber()
        throws NotANumberException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Number))
            throw new NotANumberException(this);
        return (Number) o;
    }

    public Location evaluationStackPopNumberLocation()
        throws NotANumberException, NotALocationException,
               EvaluationStackUnderflowException {
        Location loc = evaluationStackPopLocation();
        if (!(loc.getValue() instanceof Number))
            throw new NotANumberException(this);
        return loc;
    }

    public String evaluationStackPopString()
        throws NotAStringException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof String))
            throw new NotAStringException(this);
        return (String) o;
    }

    public Boolean evaluationStackPopBoolean()
        throws NotABooleanException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Boolean))
            throw new NotABooleanException(this);
        return (Boolean) o;
    }

    public Comparable evaluationStackPopComparable()
        throws NotAComparableException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Comparable))
            throw new NotAComparableException(this);
        return (Comparable) o;
    }

    public Location[] evaluationStackPopArray()
        throws NotAnArrayException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Location[]))
            throw new NotAnArrayException(this);
        return (Location[]) o;
    }

    public Environment evaluationStackPopEnvironment()
        throws NotAnEnvironmentException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof Environment))
            throw new NotAnEnvironmentException(this);
        return (Environment) o;
    }

    public ProcedureRef evaluationStackPopProcedureRef()
        throws NotAProcedureException, EvaluationStackUnderflowException {
        Object o = evaluationStackPop();
        if (!(o instanceof ProcedureRef))
            throw new NotAProcedureException(this);
        return (ProcedureRef) o;
    }

    public void pushLockObject(Object o) {
        lockedObjects.push(o);
    }

    public Object popLockObject(ExecutionContext ctx)
        throws IllegalLockUsageException {
        if (lockedObjects.isEmpty())
            throw new IllegalLockUsageException(ctx);
        return lockedObjects.pop();
    }

    public ExecutionFrame[] getFrames() {
        ExecutionFrame[] frames = executionStack.getFrames();
        if (frames.length == 0)
            return frames;
        System.arraycopy(frames, 1, frames, 0, frames.length - 1);
        frames[frames.length - 1] = currentFrame;
        return frames;
    }

    public Object evalStackPeek(int offset) {
        return evaluationStack.peek(offset);
    }

    public int getInstructionCounter() {
        return instructionCounter;
    }

}