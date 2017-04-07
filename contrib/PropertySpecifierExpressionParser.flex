package pipe.core.expression.statespec.parser;

import formula.parser.ErrorMsg;

import java_cup.runtime.Symbol;

%%

%cup
%line
%unicode
%column


%function next_token
%type java_cup.runtime.Symbol
%char

%state STRING

%{
private String strContent = "";
private int strStart = 0;

private ErrorMsg mErrorCollector;

private Symbol tok(int kind, Object value) {
  return new Symbol(kind, yychar, yychar+yylength(), value);
}

public Yylex(final java.io.InputStream pInputStream, final ErrorMsg pErrorCollector) {
  this(new java.io.InputStreamReader(pInputStream), pErrorCollector);
}

public Yylex(final java.io.Reader pReader, final ErrorMsg pErrorCollector) {
  this(pReader);
  mErrorCollector = pErrorCollector;
}

%}

%eofval{
{
  return tok(sym.EOF, null);
}
%eofval}

%%

<YYINITIAL>" "			{}
<YYINITIAL>\t			{}
<YYINITIAL>"\n" | "\r"		{mErrorCollector.error(yychar,"no multilines supported.");}

<YYINITIAL>"\u2227"		{return tok(sym.AND,null);}
<YYINITIAL>"\u2228"		{return tok(sym.OR,null);}
<YYINITIAL>"\u00AC"		{return tok(sym.NOT,null);}
<YYINITIAL>")"			{return tok(sym.RPAREN,null);}
<YYINITIAL>"("			{return tok(sym.LPAREN,null);}
<YYINITIAL>[a-zA-Z0-9_,*]+		{return tok(sym.STR, yytext());}

<YYINITIAL>.	{mErrorCollector.error(yychar, "unmatched input: " + yytext());}

