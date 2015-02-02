/*
 * SimpleJCompiler.g
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

header {
    package com.simplej.language.compiler;

    import com.simplej.language.vm.*;
}


class SimpleJCompiler extends TreeParser;

options {
    importVocab=SimpleJ;
}

{
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

compilationUnit returns [SimpleJCode code = null]
{CodeGenerator cg = new CodeGenerator(filename);}
    :   statementList[cg]
        {code = new SimpleJCode(cg.getCode(), filename);}
    ;

statementCompilationUnit returns [SimpleJCode code = null]
{CodeGenerator cg = new CodeGenerator(filename);}
    :   statement[cg]
        {code = new SimpleJCode(cg.getCode(), filename);}
    ;

exprCompilationUnit returns [SimpleJCode code = null]
{CodeGenerator cg = new CodeGenerator(filename);}
    :   expr[cg]
        {code = new SimpleJCode(cg.getCode(), filename);}
    ;

statementList[CodeGenerator cg]
    :   #(STMT_LIST (statement[cg])*)
    ;

statement[CodeGenerator cg]
    :   emptyStatement[cg]
    |   procedureDefStatement[cg]
    |   finalProcedureDefStatement[cg]
    |   exprStatement[cg]
    |   labeledStatement[cg]
    |   blockStatement[cg]
    |   whileStatement[cg, null]
    |   doWhileStatement[cg, null]
    |   forStatement[cg, null]
    |   ifStatement[cg]
    |   switchStatement[cg]
    |   returnStatement[cg]
    |   breakStatement[cg]
    |   continueStatement[cg]
    |   varDeclStatement[cg]
    |   finalVarDeclStatement[cg]
    |   synchronizedStatement[cg]
    ;

procedureDefStatement[CodeGenerator cg]
{int startLine;
 String procedureName;
 CodeGenerator mcg = new CodeGenerator(filename);}
    :   #(
            PROCEDURE_DEF
            n:IDENT
            {startLine = n.getLine();
             procedureName = n.getText();}
            procedureBody[mcg]
            {cg.checkLine(startLine);
             cg.pushProcedure(mcg,startLine,procedureName);
             cg.createLocalValue(procedureName);}
        )
    ;

finalProcedureDefStatement[CodeGenerator cg]
{int startLine;
 String procedureName;
 CodeGenerator mcg = new CodeGenerator(filename);}
    :   #(
            FINAL_PROCEDURE_DEF
            n:IDENT
            {startLine = n.getLine();
             procedureName = n.getText();}
            procedureBody[mcg]
            {cg.checkLine(startLine);
             cg.pushProcedure(mcg,startLine,procedureName);
             cg.createFinal(procedureName);}
        )
    ;

lambda[CodeGenerator cg]
{int startLine;
 CodeGenerator mcg = new CodeGenerator(filename);}
    :   #(
            l:"lambda" {startLine = l.getLine();}
            procedureBody[mcg]
            {cg.pushProcedure(mcg,startLine,"lambda");}
        )
    ;

procedureBody[CodeGenerator mcg]
    :   argumentList[mcg]
        statementList[mcg]
        {mcg.procedureEnd();}
    ;

argumentList[CodeGenerator mcg]
    {java.util.List args = new java.util.ArrayList();}
    :   #(
            ARG_LIST
            (n:IDENT {args.add(n.getText());})* // No line numbers here!
            {mcg.procedureStart(args);}
        )
    ;

emptyStatement[CodeGenerator cg]
    :   s:SEMICOLON {cg.checkLine(s);}
    ;

blockStatement[CodeGenerator cg]
    :   #(
            lb:LBRACE {cg.checkLine(lb);
                       cg.enterNewLocalEnv();}
            (statement[cg])*
            rb:RBRACE {cg.checkLine(rb);
                       cg.exitLocalEnv();}
        )
    ;

labeledStatement[CodeGenerator cg]
    {String label;}
    :   #(
            COLON
            n:IDENT {cg.checkLine(n);
                     label = n.getText();}
            (   whileStatement[cg,label]
            |   doWhileStatement[cg,label]
            |   forStatement[cg,label]
            )
        )
    ;

whileStatement[CodeGenerator cg, String loopLabel]
    :   #(
            w:"while" {cg.loopStart(loopLabel);
                       cg.checkLine(w);}
            expr[cg] {cg.toEndIfFalse();}
            statement[cg] {cg.whileLoopEnd();}
        )
    ;

doWhileStatement[CodeGenerator cg, String loopLabel]
    :   #(
            d:"do" {cg.loopStart(loopLabel);
                    cg.checkLine(d);}
            statement[cg]
            expr[cg] {cg.toStartIfTrue();
                      cg.doWhileLoopEnd();}
        )
    ;

forStatement[CodeGenerator cg, String loopLabel]
    :   #(
            f:"for" {cg.checkLine(f);
                     cg.forInit();}
            forInit[cg]
                {cg.forCond(loopLabel);}
            forCond[cg]
                {cg.forUpdate();}
            forUpdate[cg]
                {cg.forBody();}
            statement[cg]
                {cg.forEnd();}
        )
    ;

forInit[CodeGenerator cg]
    :   #(
            FOR_INIT
            (forInitStatement[cg])*
        )
    ;

forInitStatement[CodeGenerator cg]
    :   expr[cg] {cg.drop();}
    |   forVarDecl[cg]
    ;

forVarDecl[CodeGenerator cg]
    {String varName;}
    :   #(
            v:"var" {cg.checkLine(v);}
            n:IDENT {cg.checkLine(n);
                     varName = n.getText();}
            (expr[cg] {cg.createLocalValue(varName);})?
        )
    ;

forCond[CodeGenerator cg]
    :   #(
            FOR_COND
            (expr[cg])?
        )
    ;

forUpdate[CodeGenerator cg]
    :   #(
            FOR_UPDATE
            (expr[cg] {cg.drop();})*
        )
    ;

switchStatement[CodeGenerator cg]
    :   #(
            s:"switch" {cg.checkLine(s);}
            {cg.switchExprStart();}
            expr[cg]
            {cg.switchBodyStart();}
            (switchOption[cg])+
            {cg.switchBodyEnd();}
        )
    ;

switchOption[CodeGenerator cg]
    :   #(
            c:COLON
            {cg.switchOptionStart();
             cg.checkLine(c);}
            switchLabel[cg]
            {cg.switchOptionColon();}
            statementList[cg]
            {cg.switchOptionEnd();}
        )
    ;

switchLabel[CodeGenerator cg]
    :   #(
            c:"case" {cg.checkLine(c);}
            {cg.switchCaseExprStart();}
            expr[cg]
            {cg.switchCaseExprEnd();}
        )
    |   d:"default" {cg.checkLine(d);
                     cg.switchDefault();}
    ;

ifStatement[CodeGenerator cg]
    :   #(
            i1:"if" {cg.checkLine(i1);}
            expr[cg] {cg.ifStartTrue();}
            statement[cg] {cg.ifStartFalse();}
            (statement[cg])? {cg.ifEnd();}
        )
    ;

returnStatement[CodeGenerator cg]
    {boolean hasValue = false;}
    :   r:"return" {cg.checkLine(r);}
        (expr[cg] {hasValue = true;})? SEMICOLON
        {if (!hasValue) cg.pushVoid();
         cg.procedureReturn();}
    ;

breakStatement[CodeGenerator cg]
    {String label = null;}
    :   b:"break" {cg.checkLine(b);}
        (
            n:IDENT {cg.checkLine(n);
                     label = n.getText();}
        )? SEMICOLON
        {cg.breakLoop(label);}
    ;

continueStatement[CodeGenerator cg]
    {String label = null;}
    :   c:"continue" {cg.checkLine(c);}
        (
            n:IDENT {cg.checkLine(n);
                     label = n.getText();}
        )? SEMICOLON
        {cg.continueLoop(label);}
    ;

varDeclStatement[CodeGenerator cg]
    :   #(
            VAR_DECL
            (varDecl[cg])+
        )
    ;

varDecl[CodeGenerator cg]
    {String varName;}
    :   #(
            ASSIGN n:IDENT {cg.checkLine(n);
                            varName = n.getText();}
            expr[cg]
            {cg.createLocalValue(varName);}
        )
    |   nn:IDENT {cg.checkLine(nn);
                  cg.createLocal(nn.getText());}
    ;

finalVarDeclStatement[CodeGenerator cg]
    :   #(
            FINAL_VAR_DECL
            (finalVarDecl[cg])+
        )
    ;

finalVarDecl[CodeGenerator cg]
    {String varName;}
    :   #(
            ASSIGN n:IDENT {cg.checkLine(n);
                            varName = n.getText();}
            expr[cg]
            {cg.createFinal(varName);}
        )
    ;

synchronizedStatement[CodeGenerator cg]
    :   #(
            s:"synchronized" {cg.checkLine(s);}
            expr[cg] {cg.synchronizedStart();}
            statement[cg] {cg.synchronizedEnd();}
        )
    ;

exprStatement[CodeGenerator cg]
    :   expr[cg]
        {cg.drop();}
    ;

expr[CodeGenerator cg]
    {int count = 0;}
    :   #(
            COND_EXPR
            expr[cg] {cg.ifStartTrue();}
            expr[cg] {cg.ifStartFalse();}
            expr[cg] {cg.ifEnd();}
        )
    |   #(
            ASSIGN lvalue[cg] expr[cg]
            {cg.store();}
        )
    |   #(
            ASSIGN_ADD lvalue[cg] expr[cg]
            {cg.addStore();}
        )
    |   #(
            ASSIGN_SUB lvalue[cg] expr[cg]
            {cg.subStore();}
        )
    |   #(
            ASSIGN_MUL lvalue[cg] expr[cg]
            {cg.mulStore();}
        )
    |   #(
            ASSIGN_DIV lvalue[cg] expr[cg]
            {cg.divStore();}
        )
    |   #(
            ASSIGN_MOD lvalue[cg] expr[cg]
            {cg.modStore();}
        )
    |   #(
            ASSIGN_BITAND lvalue[cg] expr[cg]
            {cg.bitAndStore();}
        )
    |   #(
            ASSIGN_BITOR lvalue[cg] expr[cg]
            {cg.bitOrStore();}
        )
    |   #(
            ASSIGN_BITXOR lvalue[cg] expr[cg]
            {cg.bitXorStore();}
        )
    |   #(
            ASSIGN_SHIFTLEFT lvalue[cg] expr[cg]
            {cg.shiftLeftStore();}
        )
    |   #(
            ASSIGN_SHIFTRIGHT lvalue[cg] expr[cg]
            {cg.shiftRightStore();}
        )
    |   #(
            ASSIGN_SHIFTURIGHT lvalue[cg] expr[cg]
            {cg.shiftUnsignedRightStore();}
        )
    |   #(
            ADD expr[cg] expr[cg]
            {cg.add();}
        )
    |   #(
            SUB expr[cg] expr[cg]
            {cg.sub();}
        )
    |   #(
            MUL expr[cg] expr[cg]
            {cg.mul();}
        )
    |   #(
            DIV expr[cg] expr[cg]
            {cg.div();}
        )
    |   #(
            MOD expr[cg] expr[cg]
            {cg.mod();}
        )
    |   #(
            SHIFTLEFT expr[cg] expr[cg]
            {cg.shiftLeft();}
        )
    |   #(
            SHIFTRIGHT expr[cg] expr[cg]
            {cg.shiftRight();}
        )
    |   #(
            SHIFTURIGHT expr[cg] expr[cg]
            {cg.shiftUnsignedRight();}
        )
    |   #(
            GREATER expr[cg] expr[cg]
            {cg.greater();}
        )
    |   #(
            GREATER_EQUAL expr[cg] expr[cg]
            {cg.greaterOrEqual();}
        )
    |   #(
            LESS expr[cg] expr[cg]
            {cg.less();}
        )
    |   #(
            LESS_EQUAL expr[cg] expr[cg]
            {cg.lessOrEqual();}
        )
    |   #(
            EQUAL expr[cg] expr[cg]
            {cg.equal();}
        )
    |   #(
            NOT_EQUAL expr[cg] expr[cg]
            {cg.notEqual();}
        )
    |   #(
            OR expr[cg] {cg.orShortcutStart();}
            expr[cg] {cg.or(); cg.shortcutEnd();}
        )
    |   #(
            AND expr[cg] {cg.andShortcutStart();}
            expr[cg] {cg.and(); cg.shortcutEnd();}
        )
    |   #(
            BITOR expr[cg] expr[cg]
            {cg.bitOr();}
        )
    |   #(
            BITXOR expr[cg] expr[cg]
            {cg.bitXor();}
        )
    |   #(
            BITAND expr[cg] expr[cg]
            {cg.bitAnd();}
        )
    |   #(
            SET_SUPER expr[cg] expr[cg]
            {cg.setSuper();}
        )
    |   #(
            MINUS expr[cg]
            {cg.minus();}
        )
    |   #(
            BITNOT expr[cg]
            {cg.bitNot();}
        )
    |   #(
            NOT expr[cg]
            {cg.not();}
        )
    |   #(
            PRE_INCR lvalue[cg]
            {cg.preIncr();}
        )
    |   #(
            PRE_DECR lvalue[cg]
            {cg.preDecr();}
        )
    |   #(
            POST_INCR lvalue[cg]
            {cg.postIncr();}
        )
    |   #(
            POST_DECR lvalue[cg]
            {cg.postDecr();}
        )
    |   #(
            ARRAY_VALUES (expr[cg] {count++;})*
            {cg.createArrayValues(count);}
        )
    |   #(
            ARRAY_DIMENSIONS expr[cg] {count++;} (expr[cg] {count++;})*
            {cg.createArrayDimensions(count);}
        )
    |   #(
            ENVIRONMENT_VALUES (envPair[cg] {count++;})*
            {cg.createEnvironmentValues(count);}
        )
    |   #(
            PROCEDURE_CALL location[cg]
            {cg.fetch();}
            (expr[cg] {count++;})*
            {cg.procedureCall(count);}
        )
    |   #(
            LAMBDA_PROCEDURE_CALL lambda[cg]
            (expr[cg] {count++;})*
            {cg.procedureCall(count);}
        )
    |   lambda[cg]
    |   rvalue[cg]
    |   literal[cg]
    ;

envPair[CodeGenerator cg]
    :   n:IDENT {cg.checkLine(n);
                 cg.pushString(n.getText());}
        expr[cg]
    ;

lvalue[CodeGenerator cg]
    :   #(
            LVALUE
            location[cg]
        )
    ;

rvalue[CodeGenerator cg]
    :   #(
            FETCH location[cg]
            {cg.fetch();}
        )
    ;

location[CodeGenerator cg]
    :   (   variable[cg]
        |   thisEnv[cg]
        |   superEnv[cg]
        |   parenExpr[cg]
        )
        (locationSuffix[cg])*
    ;

locationSuffix[CodeGenerator cg]
    :   #(
            ARRAY_REF
            {cg.fetch();}
            expr[cg]
            {cg.arrayRef();}
        )
    |   #(
            ENV_REF
            {cg.fetch();}
            n:IDENT
            {cg.checkLine(n);
             cg.envRef(n.getText());}
        )
    ;

variable[CodeGenerator cg]
    : #(
            VARIABLE n:IDENT
            {cg.checkLine(n);
             cg.pushLocation(n.getText());}
        )
    ;

thisEnv[CodeGenerator cg]
    :   t:"this"
        {cg.checkLine(t);
         cg.pushThis();}
    ;

superEnv[CodeGenerator cg]
    :   s:"super"
        {cg.checkLine(s);
         cg.pushSuper();}
    ;

parenExpr[CodeGenerator cg]
    :   #(
            LPAREN expr[cg]
            {cg.makeLocation();}
        )
    ;

literal[CodeGenerator cg]
    :   cl:CHAR_LITERAL
        {cg.checkLine(cl);
         cg.pushChar(cl.getText().charAt(0));}
    |   slsl:SL_STRING_LITERAL
        {cg.checkLine(slsl);
         cg.pushString(slsl.getText());}
    |   mlsl:ML_STRING_LITERAL
        {cg.checkLine(mlsl);
         String s = mlsl.getText();
         cg.pushString(s.substring(2, s.length() - 2));}
    |   il:INT_LITERAL
        {cg.checkLine(il);
         String s = il.getText();
         if (s.startsWith("0x") || s.startsWith("0X"))
             cg.pushInt(Integer.parseInt(s.substring(2), 16));
         else if (s.startsWith("0") && s.length() > 1)
             cg.pushInt(Integer.parseInt(s.substring(1), 8));
         else
             cg.pushInt(Integer.parseInt(s));
        }
    |   fl:FLOAT_LITERAL
        {cg.checkLine(fl);
         cg.pushFloat(Double.parseDouble(fl.getText()));}
    |   t:"true"
        {cg.checkLine(t);
         cg.pushBoolean(true);}
    |   f:"false"
        {cg.checkLine(f);
         cg.pushBoolean(false);}
    |   n:"null"
        {cg.checkLine(n);
         cg.pushNull();}
    ;
