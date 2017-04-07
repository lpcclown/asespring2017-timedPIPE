package ltlparser;
import ltlparser.errormsg.*;

import java.io.InputStreamReader;

%%

%unicode
%column

%implements java_cup.runtime.Scanner
%function next_token
%type java_cup.runtime.Symbol
%char

%state STRING

%{
private ErrorMsg errorMsg;
private String strContent = "";
private int strStart = 0;

private java_cup.runtime.Symbol tok(int kind, Object value) {
  return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

public Yylex(java.io.InputStream s, ErrorMsg e) {
  this(new InputStreamReader(s));
  errorMsg = e;
}
public Yylex(java.io.Reader s, ErrorMsg e) {
  this(s);
  errorMsg = e;
}
%}

%eofval{
{
//	if ( yy_lexical_state == STRING ) {
	if ( zzLexicalState == STRING ) {
		errorMsg.error(strStart, "unclosed string");
		yybegin(YYINITIAL);
		return new java_cup.runtime.Symbol(sym.STR, strStart, yychar + 1, strContent);
	} else
		return tok(sym.EOF, null);
}
%eofval}

%%

<YYINITIAL>" "			{}
<YYINITIAL>\t			{}
<YYINITIAL>"\n" | "\r"		{errorMsg.error(yychar,"no multilines supported.");}

// LTL symbols
<YYINITIAL>"\u25A1"		{return tok(sym.FLTL_ALWAYS,null);}
<YYINITIAL>"\u25CA"		{return tok(sym.FLTL_SOMETIMES,null);} // Eventually
<YYINITIAL>"\u25CB"		{return tok(sym.FLTL_NEXT,null);} // Next
<YYINITIAL>"\uFA63"		{return tok(sym.FLTL_UNTIL,null);} // Until
<YYINITIAL>"\uFA64"		{return tok(sym.FLTL_WKUNTIL,null);} // Weak Until

// FOL symbols and other related.
<YYINITIAL>"\u2227"		{return tok(sym.AND,null);}
<YYINITIAL>"\u2228"		{return tok(sym.OR,null);}
<YYINITIAL>"\u00AC"		{return tok(sym.NOT,null);}
<YYINITIAL>"\u2192"		{return tok(sym.IMP,null);}
<YYINITIAL>"\u2194"		{return tok(sym.EQUIV,null);}

<YYINITIAL>"="			  {return tok(sym.EQ,null);}
<YYINITIAL>"\u2260"		{return tok(sym.NEQ,null);}
<YYINITIAL>">"			  {return tok(sym.GT,null);}
<YYINITIAL>"<"			  {return tok(sym.LT,null);}
<YYINITIAL>"\u2265"		{return tok(sym.GEQ,null);}
<YYINITIAL>"\u2264"		{return tok(sym.LEQ,null);}


<YYINITIAL>"+"			{return tok(sym.PLUS,null);}
<YYINITIAL>"-"			{return tok(sym.MINUS,null);}
<YYINITIAL>"*"			{return tok(sym.MULT,null);}
<YYINITIAL>"%"			{return tok(sym.MOD,null);}
<YYINITIAL>"/"			{return tok(sym.DIV,null);}

<YYINITIAL>"\u2200"		{return tok(sym.FORALL,null);}
<YYINITIAL>"\u2203"		{return tok(sym.EXISTS,null);}
<YYINITIAL>"\u2204"		{return tok(sym.NEXISTS,null);}
<YYINITIAL>"\u2208"		{return tok(sym.IN,null);}
<YYINITIAL>"\u2209"		{return tok(sym.NIN,null);}
<YYINITIAL>"\u222A"		{return tok(sym.UNION,null);}
<YYINITIAL>"\u2216"		{return tok(sym.DIFF,null);}

<YYINITIAL>"\uFA43"		{return tok(sym.SCOPE,null);}
<YYINITIAL>"\uFA52"		{return tok(sym.EMPTY,null);}
<YYINITIAL>"\uFA53"		{return tok(sym.SUBSET,null);}
<YYINITIAL>"\uFA54"		{return tok(sym.NSUBSET,null);}
<YYINITIAL>"\uFA55"		{return tok(sym.SUBEQSET,null);}

<YYINITIAL>"]"			{return tok(sym.RBRACK,null);}
<YYINITIAL>"["			{return tok(sym.LBRACK,null);}
<YYINITIAL>")"			{return tok(sym.RPAREN,null);}
<YYINITIAL>"("			{return tok(sym.LPAREN,null);}
//<YYINITIAL>"'"			{return tok(sym.PRIME,null);} this can be at the end of a variable name
<YYINITIAL>"{"			{return tok(sym.LBRACE,null);}
<YYINITIAL>"}"			{return tok(sym.RBRACE,null);}
<YYINITIAL>","			{return tok(sym.COMMA,null);}
<YYINITIAL>"\u22C5"			{return tok(sym.DOT,null);}
<YYINITIAL>"true"		{return tok(sym.BOOL, new Boolean(yytext()));}
<YYINITIAL>"false"		{return tok(sym.BOOL, new Boolean(yytext()));}
<YYINITIAL>[A-Z][a-zA-Z_0-9_']*		{return tok(sym.ID_UPCASE,yytext());}
<YYINITIAL>[a-z][a-zA-Z_0-9_']*		{return tok(sym.ID_LOWCASE,yytext());}
<YYINITIAL>[0-9]+"." [0-9]+ | "-"[0-9]+"." [0-9]+ | [0-9]+ | "-"[0-9]+	{return tok(sym.NUM,yytext());}

<YYINITIAL>\"			{strContent = ""; strStart = yychar; yybegin(STRING);}
<STRING>\"			{yybegin(YYINITIAL);
					return new java_cup.runtime.Symbol(sym.STR, strStart, yychar + 1, strContent);}
<STRING>\n	{errorMsg.error(yychar, "no multiple lines allowed: " + yytext());}
<STRING>\r	{errorMsg.error(yychar, "no multiple lines allowed: " + yytext());}
<STRING>.	{strContent = strContent + yytext();}

<YYINITIAL>.	{errorMsg.error(yychar, "unmatched input: " + yytext());}

