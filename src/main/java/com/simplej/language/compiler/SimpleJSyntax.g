/*
 * SimpleJSyntax.g
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
}

// Based on java.g from the ANTLR distribution
class SimpleJParser extends Parser;

options {
    exportVocab=SimpleJ;        // Call the vocabulary "SimpleJ"
    buildAST=true;
}

tokens {
    STMT_LIST;
    PROCEDURE_DEF;
    FINAL_PROCEDURE_DEF;
    ARG_LIST;
    FOR_INIT;
    FOR_COND;
    FOR_UPDATE;
    VAR_DECL;
    FINAL_VAR_DECL;
    LVALUE;
    COND_EXPR;
    FETCH;
    ARRAY_REF;
    ENV_REF;
    VARIABLE;
    MINUS;
    PRE_INCR;
    PRE_DECR;
    POST_INCR;
    POST_DECR;
    ARRAY_VALUES;
    ARRAY_DIMENSIONS;
    ENVIRONMENT_VALUES;
    PROCEDURE_CALL;
    LAMBDA_PROCEDURE_CALL;
}

compilationUnit
    :   statementList EOF!
    ;

statementCompilationUnit
    :   statement
    ;

exprCompilationUnit
    :   expr EOF!
    ;

statementList
    :   (statement)*
        {#statementList = #([STMT_LIST, "STMT_LIST"], #statementList);}
    ;

statement
    :   emptyStatement
    |   (name argumentList LBRACE) => procedureDefStatement
    |   ("final" name argumentList LBRACE) => finalProcedureDefStatement
    |   (expr SEMICOLON) => exprStatement
    |   blockStatement
    |   labeledStatement
    |   whileStatement
    |   doWhileStatement
    |   forStatement
    |   switchStatement
    |   ifStatement
    |   returnStatement
    |   breakStatement
    |   continueStatement
    |   varDeclStatement
    |   finalVarDeclStatement
    |   synchronizedStatement
    ;

procedureDefStatement
    :   name procedureBody
        {#procedureDefStatement = #([PROCEDURE_DEF, "PROCEDURE_DEF"],
                #procedureDefStatement);}
    ;

finalProcedureDefStatement
    :   "final"! name procedureBody
        {#finalProcedureDefStatement = #([FINAL_PROCEDURE_DEF,
                                          "FINAL_PROCEDURE_DEF"],
            #finalProcedureDefStatement);}
    ;

procedureBody
    :   argumentList LBRACE! statementList RBRACE!
    ;

argumentList
    :   LPAREN! (name (COMMA! name)*)? RPAREN!
        {#argumentList = #([ARG_LIST, "ARG_LIST"], #argumentList);}
    ;

emptyStatement
    :   SEMICOLON
    ;

blockStatement
    :   LBRACE^ (statement)* RBRACE
    ;

labeledStatement
    :   name COLON^ (whileStatement | doWhileStatement | forStatement)
    ;

whileStatement
    :   "while"^ LPAREN! expr RPAREN! statement
    ;

doWhileStatement
    :   "do"^ statement "while"! LPAREN! expr RPAREN! SEMICOLON!
    ;

forStatement
    :   "for"^ LPAREN! forInit SEMICOLON! forCond SEMICOLON! forUpdate RPAREN!
        statement
    ;

forInit
    :   (forInitStatement (COMMA! forInitStatement)*)?
        {#forInit = #([FOR_INIT, "FOR_INIT"], #forInit);}
    ;

forInitStatement
    :   expr
    |   forVarDecl
    ;

forVarDecl
    :   "var"^ name (ASSIGN! expr)?
    ;

forCond
    :   (expr)?
        {#forCond = #([FOR_COND, "FOR_COND"], #forCond);}
    ;

forUpdate
    :   (expr (COMMA! expr)*)?
        {#forUpdate = #([FOR_UPDATE, "FOR_UPDATE"], #forUpdate);}
    ;

switchStatement
    :   "switch"^ LPAREN! expr RPAREN! LBRACE!
        (switchOption)+
        RBRACE!
    ;

switchOption
    :   switchLabel COLON^ statementList
    ;

switchLabel
    :   "case"^ expr
    |   "default"^
    ;

ifStatement
    :   "if"^ LPAREN! expr RPAREN! statement
        (options {warnWhenFollowAmbig=false;}
        : "else"! statement)?
    ;

returnStatement
    :   "return" (expr)? SEMICOLON
    ;

breakStatement
    :   "break" (name)? SEMICOLON
    ;

continueStatement
    :   "continue" (name)? SEMICOLON
    ;

varDeclStatement
    :   "var"! varDecl (COMMA! varDecl)* SEMICOLON!
        {#varDeclStatement = #([VAR_DECL, "VAR_DECL"], #varDeclStatement);}
    ;

varDecl
    :   name (ASSIGN^ rvalue)?
    ;

finalVarDeclStatement
    :   "final"! finalVarDecl (COMMA! finalVarDecl)* SEMICOLON!
        {#finalVarDeclStatement = #([FINAL_VAR_DECL, "FINAL_VAR_DECL"],
                #finalVarDeclStatement);}
    ;

finalVarDecl
    :   name ASSIGN^ rvalue
    ;

synchronizedStatement
    :   "synchronized"^ LPAREN! expr RPAREN! statement
    ;

exprStatement
    :   expr SEMICOLON!
    ;

expr
    :   (lvalue
            (   ASSIGN
            |   ASSIGN_ADD
            |   ASSIGN_SUB
            |   ASSIGN_MUL
            |   ASSIGN_DIV
            |   ASSIGN_MOD
            |   ASSIGN_BITAND
            |   ASSIGN_BITOR
            |   ASSIGN_BITXOR
            |   ASSIGN_SHIFTLEFT
            |   ASSIGN_SHIFTRIGHT
            |   ASSIGN_SHIFTURIGHT
            )
        ) => assignmentExpr
    |   rvalue
    ;

assignmentExpr
    :   lvalue
        (   ASSIGN^
        |   ASSIGN_ADD^
        |   ASSIGN_SUB^
        |   ASSIGN_MUL^
        |   ASSIGN_DIV^
        |   ASSIGN_MOD^
        |   ASSIGN_BITAND^
        |   ASSIGN_BITOR^
        |   ASSIGN_BITXOR^
        |   ASSIGN_SHIFTLEFT^
        |   ASSIGN_SHIFTRIGHT^
        |   ASSIGN_SHIFTURIGHT^
        )
        expr
    ;

lvalue
    :   location
        {#lvalue = #([LVALUE, "LVALUE"], #lvalue);}
    ;

rvalue
    :   conditionalExpr
    ;

conditionalExpr
    :   setSuperExpr
        (QUESTION! expr COLON! setSuperExpr
         {#conditionalExpr = #([COND_EXPR, "COND_EXPR"], #conditionalExpr);}
        )?
    ;

setSuperExpr
    :   conditionalOrExpr (SET_SUPER^ setSuperExpr)?
    ;

conditionalOrExpr
    :   conditionalAndExpr (OR^ conditionalAndExpr)*
    ;

conditionalAndExpr
    :   bitOrExpr (AND^ bitOrExpr)*
    ;

bitOrExpr
    :   bitXorExpr (BITOR^ bitXorExpr)*
    ;

bitXorExpr
    :   bitAndExpr (BITXOR^ bitAndExpr)*
    ;

bitAndExpr
    :   equalExpr (BITAND^ equalExpr)*
    ;

equalExpr
    :   relExpr ((EQUAL^ | NOT_EQUAL^) relExpr)*
    ;

relExpr
    :   shiftExpr (
            ( GREATER^
            | GREATER_EQUAL^
            | LESS^
            | LESS_EQUAL^
            ) shiftExpr
        )*
    ;

shiftExpr
    :   term ((SHIFTLEFT^ | SHIFTRIGHT^ | SHIFTURIGHT^) term)*
    ;

term
    :   factor ((ADD^ | SUB^) factor)*
    ;

factor
    :   unary ((MUL^ | DIV^ | MOD^) unary)*
    ;

unary
    :   SUB! element
        {#unary = #([MINUS, "MINUS"], #unary);}
    |   BITNOT^ element
    |   NOT^ element
    |   INCR! lvalue
        {#unary = #([PRE_INCR, "PRE_INCR"], #unary);}
    |   DECR! lvalue
        {#unary = #([PRE_DECR, "PRE_DECR"], #unary);}
    |   (lvalue INCR) => lvalue INCR!
        {#unary = #([POST_INCR, "POST_INCR"], #unary);}
    |   (lvalue DECR) => lvalue DECR!
        {#unary = #([POST_DECR, "POST_DECR"], #unary);}
    |   element
    |   array
    |   environment
    ;

element
    :   (location LPAREN) => procedureCall
    |   (lambda LPAREN) => lambdaProcedureCall
    |   location
        {#element = #([FETCH, "FETCH"], #element);}
    |   lambda
    |   literal
    ;

location
    :   (
            variable
        |   "this"
        |   "super"
        |   parenExpr
        )
        (locationSuffix)*
    ;

parenExpr
    :   LPAREN^ expr RPAREN!
    ;

locationSuffix
    :   arrayRef
    |   envRef
    ;

arrayRef
    :   LBRACK! expr RBRACK!
        {#arrayRef = #([ARRAY_REF, "ARRAY_REF"], #arrayRef);}
    ;

envRef
    :   DOT! name
        {#envRef = #([ENV_REF, "ENV_REF"], #envRef);}
    ;

lambda
    :   "lambda"^ procedureBody
    ;

array
    :   LBRACK! (expr (COMMA! expr)*)? RBRACK!
        {#array = #([ARRAY_VALUES, "ARRAY_VALUES"], #array);}
    |   "new"! "array"! LBRACK! expr RBRACK! (LBRACK! expr RBRACK!)*
        {#array = #([ARRAY_DIMENSIONS, "ARRAY_DIMENSIONS"], #array);}
    ;

environment
    :   LBRACE! (envPair (COMMA! envPair)*)? RBRACE!
        {#environment = #([ENVIRONMENT_VALUES, "ENVIRONMENT_VALUES"],
                #environment);}
    ;

envPair
    :   name COLON! expr
    ;

procedureCall
    :   location LPAREN! (expr (COMMA! expr)*)? RPAREN!
        {#procedureCall = #([PROCEDURE_CALL, "PROCEDURE_CALL"],
                #procedureCall);}
    ;

lambdaProcedureCall
    :   lambda LPAREN! (expr (COMMAN! EXPR)*)? RPAREN!
        {#lambdaProcedureCall = #([LAMBDA_PROCEDURE_CALL,
                                   "LAMBDA_PROCEDURE_CALL"],
                #lambdaProcedureCall);}
    ;

variable
    :  name
        {#variable = #([VARIABLE, "VARIABLE"], #variable);} 
    ;

literal
    : charLiteral
    | stringLiteral
    | intLiteral
    | floatLiteral
    | booleanLiteral
    | nullLiteral
    ;

charLiteral
    : CHAR_LITERAL
    ;

stringLiteral
    : SL_STRING_LITERAL | ML_STRING_LITERAL
    ;

intLiteral
    : INT_LITERAL
    ;

floatLiteral
    : FLOAT_LITERAL
    ;

booleanLiteral
    : "true" | "false"
    ;

nullLiteral
    : "null"
    ;

name
    : IDENT
    ;

class SimpleJLexer extends Lexer;

options {
    exportVocab=SimpleJ;        // Call the vocabulary "SimpleJ"
    testLiterals=false;         // Don't automatically test for literals
    k=4;                        // Four characters of lookahead
}

// Whitespace -- ignored
WS
    options {paraphrase="white space";} 
    :   (   ' '
        |   '\t'
        |   ( options {generateAmbigWarnings=false;}
            : "\r\n"    // Evil DOS/Windows
            | '\r'      // Macintosh
            | '\n'      // Unix (the right way)
            )
            {newline();}
        )+
        {$setType(Token.SKIP);}
    ;

// Single-line comments
SL_COMMENT
    options {paraphrase="a to-end-of-line comment";} 
    :   "//"
        (~('\n' | '\r'))*
        ('\n' | '\r'('\n')?)?
        {$setType(Token.SKIP); newline();}
    ;

// Multiple-line comments
ML_COMMENT
    options {paraphrase="a multiline comment";} 
    :   "/*"
        ( options {generateAmbigWarnings=false;}
        :   { LA(2) != '/' }? '*'
        |   "\r\n"  {newline();}
        |   '\r'    {newline();}
        |   '\n'    {newline();}
        |   ~('*'|'\r'|'\n')
        )*
        "*/"
        {$setType(Token.SKIP);}
    ;

// An identifier.  Note that testLiterals is set to true!  This means
// that after we match the rule, we look in the literals table to see
// if it's a literal or really an identifer
IDENT
    options {testLiterals=true; paraphrase="an identifier";}
    :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')*
    ;

// Character literals
CHAR_LITERAL
    options {paraphrase="a character constant";} 
    : '\''! (ESCAPE | ~('\''|'\\'|'\n'|'\r'|'\t')) '\''!
    ;

// Single-line string literals
SL_STRING_LITERAL
    options {paraphrase="a single line string";} 
    :   '"'! (ESCAPE | ~('"'|'\\'|'\n'|'\r'|'\t'))* '"'!
    ;

// Multiple-line string literals
ML_STRING_LITERAL
    options {paraphrase="a multiline string";} 
    : "`["
        ( options {generateAmbigWarnings=false;}
        :  ML_STRING_LITERAL
        |  {LA(2)!='`'}? ']'
        |  "\r\n" {newline();}
        |  '\r'   {newline();}
        |  '\n'   {newline();}
        |  ESCAPE
        |  ~(']'|'\r'|'\n')
        )*
        "]`"
    ;

// Escape sequence -- note that this is protected; it can only be called
// from another lexer rule -- it will not ever directly return a token to
// the parser
protected
ESCAPE
    options {paraphrase="an escape sequence";} 
    :   '\\'
        ( 'n' {$setText("\n");}
        | 'r' {$setText("\r");}
        | 't' {$setText("\t");}
        | '"' {$setText("\"");}
        | '\\' {$setText("\\");}
        )
    ;

// A numeric literal
INT_LITERAL
    options {paraphrase="a number";} 
	{boolean isDecimal=false;}
    :   '.' {$setType(DOT);}
        (	('0'..'9')+ (EXPONENT)?
            {$setType(FLOAT_LITERAL);}
        )?

	|	(	'0' {isDecimal = true;} // special case for just '0'
			(	('x'|'X')
				(											// hex
					// the 'e'|'E' and float suffix stuff look
					// like hex digits, hence the (...)+ doesn't
					// know when to stop: ambig.  ANTLR resolves
					// it correctly by matching immediately.  It
					// is therefor ok to hush warning.
					options {
						warnWhenFollowAmbig=false;
					}
				:	HEX_DIGIT
				)+

			|	//float or double with leading zero
				(('0'..'9')+ ('.'|EXPONENT)) => ('0'..'9')+

			|	('0'..'7')+									// octal
			)?
		|	('1'..'9') ('0'..'9')*  {isDecimal=true;}		// non-zero decimal
		)
		(
            // only check to see if it's a float if looks like decimal so far
			{isDecimal}?
            (   '.' ('0'..'9')* (EXPONENT)?
            |   EXPONENT
            )
            {$setType(FLOAT_LITERAL);}

        )?
	;

// Floating point exponent -- protected -- it will not ever directly return
// a token to the parser
protected
EXPONENT
    options {paraphrase="an exponent";} 
    :   ('e'|'E') ('+'|'-')? ('0'..'9')+
    ;

// hexadecimal digit (again, note it's protected!)
protected
HEX_DIGIT
    options {paraphrase="an hexadecimal digit";}
	:	('0'..'9'|'A'..'F'|'a'..'f')
	;
ASSIGN              options {paraphrase="'='";} :   '='     ;
ADD                 options {paraphrase="'+'";} :   '+'     ;
ASSIGN_ADD          options {paraphrase="'+='";} :   "+="    ;
SUB                 options {paraphrase="'-'";} :   '-'     ;
ASSIGN_SUB          options {paraphrase="'-='";} :   "-="    ;
MUL                 options {paraphrase="'*'";} :   '*'     ;
ASSIGN_MUL          options {paraphrase="'*='";} :   "*="    ;
DIV                 options {paraphrase="'/'";} :   '/'     ;
ASSIGN_DIV          options {paraphrase="'/='";} :   "/="    ;
MOD                 options {paraphrase="'%'";} :   '%'     ;
ASSIGN_MOD          options {paraphrase="'%='";} :   "%="    ;
INCR                options {paraphrase="'++'";} :   "++"    ;
DECR                options {paraphrase="'--'";} :   "--"    ;
BITAND              options {paraphrase="'&'";} :   '&'     ;
ASSIGN_BITAND      options {paraphrase="'&='";} :   "&="    ;
BITOR               options {paraphrase="'|'";} :   '|'     ;
ASSIGN_BITOR        options {paraphrase="'|='";} :   "|="    ;
BITXOR              options {paraphrase="'^'";} :   '^'     ;
ASSIGN_BITXOR       options {paraphrase="'^='";} :   "^="    ;
BITNOT              options {paraphrase="'~'";} :   '~'     ;
SHIFTLEFT           options {paraphrase="'<<'";} :   "<<"    ;
ASSIGN_SHIFTLEFT    options {paraphrase="'<<='";} :   "<<="   ;
SHIFTRIGHT          options {paraphrase="'>>'";} :   ">>"    ;
ASSIGN_SHIFTRIGHT   options {paraphrase="'>>='";} :   ">>="   ;
SHIFTURIGHT         options {paraphrase="'>>>'";} :   ">>>"   ;
ASSIGN_SHIFTURIGHT  options {paraphrase="'>>>='";} :   ">>>="  ;
AND                 options {paraphrase="'&&'";} :   "&&"    ;
OR                  options {paraphrase="'||'";} :   "||"    ;
NOT                 options {paraphrase="'!'";} :   '!'     ;
GREATER             options {paraphrase="'>'";} :   '>'     ;
GREATER_EQUAL       options {paraphrase="'>='";} :   ">="    ;
LESS                options {paraphrase="'<'";} :   '<'     ;
LESS_EQUAL          options {paraphrase="'<='";} :   "<="    ;
EQUAL               options {paraphrase="'=='";} :   "=="    ;
NOT_EQUAL           options {paraphrase="'!='";} :   "!="    ;
SET_SUPER           options {paraphrase="'->'";} :   "->"    ;
// DOT              :   "."     ; recognized inside NUM_INT
QUESTION            options {paraphrase="'?'";} :   '?'     ;
COLON               options {paraphrase="':'";} :  ':'     ;
COMMA               options {paraphrase="','";} :   ','     ;
SEMICOLON           options {paraphrase="';'";} :   ';'     ;
LPAREN              options {paraphrase="'('";} :   '('     ;
RPAREN              options {paraphrase="')'";} :   ')'     ;
LBRACK              options {paraphrase="'['";} :   '['     ;
RBRACK              options {paraphrase="']'";} :   ']'     ;
LBRACE              options {paraphrase="'{'";} :   '{'     ;
RBRACE              options {paraphrase="'}'";} :   '}'     ;
