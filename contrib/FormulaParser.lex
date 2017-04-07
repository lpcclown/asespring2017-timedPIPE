package formula.parser;

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
private ErrorMsg errorMsg;
private String strContent = "";
private int strStart = 0;

private java_cup.runtime.Symbol tok(int kind, Object value) {
  return new java_cup.runtime.Symbol(kind, yychar, yychar+yylength(), value);
}

public Yylex(java.io.Reader s, ErrorMsg e) {
  this(s);
  errorMsg = e;
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
<YYINITIAL>"\n" | "\r"		{errorMsg.error(yychar,"no multilines supported.");}

<YYINITIAL>"\u2227"		{return tok(sym.AND,null);}
<YYINITIAL>"\u2228"		{return tok(sym.OR,null);}
<YYINITIAL>"\u00AC"		{return tok(sym.NOT,null);}
<YYINITIAL>"\u2192"		{return tok(sym.IMP,null);}
<YYINITIAL>"\u2194"		{return tok(sym.EQUIV,null);}
<YYINITIAL>"="			{return tok(sym.EQ,null);}
<YYINITIAL>"\u2260"		{return tok(sym.NEQ,null);}
<YYINITIAL>">"			{return tok(sym.GT,null);}
<YYINITIAL>"<"			{return tok(sym.LT,null);}
<YYINITIAL>"\u2265"		{return tok(sym.GEQ,null);}
<YYINITIAL>"\u2264"		{return tok(sym.LEQ,null);}
<YYINITIAL>"+"			{return tok(sym.PLUS,null);}
<YYINITIAL>"-"			{return tok(sym.MINUS,null);}
<YYINITIAL>"~"			{return tok(sym.UMINUS,null);}
<YYINITIAL>"*"			{return tok(sym.MUL,null);}
<YYINITIAL>"%"			{return tok(sym.MOD,null);}
<YYINITIAL>"/"			{return tok(sym.DIV,null);}
//<YYINITIAL>"!"			{return tok(sym.INPUT,null);}
//<YYINITIAL>"?"			{return tok(sym.OUTPUT,null);}
<YYINITIAL>"\u2200"		{return tok(sym.FORALL,null);}
<YYINITIAL>"\u2203"		{return tok(sym.EXISTS,null);}
<YYINITIAL>"\u2204"		{return tok(sym.NEXISTS,null);}
<YYINITIAL>"\u2208"		{return tok(sym.IN,null);}
<YYINITIAL>"\u2209"		{return tok(sym.NIN,null);}
<YYINITIAL>"\u222A"		{return tok(sym.UNION,null);}
<YYINITIAL>"\u2216"		{return tok(sym.DIFF,null);}
<YYINITIAL>"|"          {return tok(sym.SETDEF,null);}
<YYINITIAL>"]"			{return tok(sym.RBRACK,null);}
<YYINITIAL>"["			{return tok(sym.LBRACK,null);}
<YYINITIAL>")"			{return tok(sym.RPAREN,null);}
<YYINITIAL>"("			{return tok(sym.LPAREN,null);}
//<YYINITIAL>"'"			{return tok(sym.PRIME,null);} this can be at the end of a variable name
<YYINITIAL>"{"			{return tok(sym.LBRACE,null);}
<YYINITIAL>"}"			{return tok(sym.RBRACE,null);}
<YYINITIAL>","			{return tok(sym.COMMA,null);}
<YYINITIAL>":"			{return tok(sym.COLON,null);}
<YYINITIAL>"\u22C5"			{return tok(sym.DOT,null);}
<YYINITIAL>"true"		{return tok(sym.TRUE, null);}
<YYINITIAL>"false"		{return tok(sym.FALSE, null);}
<YYINITIAL>"\u2205"		{return tok(sym.EMPTY, null);}
<YYINITIAL> [a-zA-Z][a-zA-Z0-9_']*		{return tok(sym.ID, yytext());}
//<YYINITIAL>[a-z][a-zA-Z_0-9_']*		{return tok(sym.ID_LOWCASE,yytext());}
<YYINITIAL>[0-9]+"." [0-9]+ | [0-9]+ 	{return tok(sym.NUM,yytext());}

<YYINITIAL>\"			{strContent = ""; strStart = yychar; yybegin(STRING);}
<STRING>\"			{yybegin(YYINITIAL);
					return new java_cup.runtime.Symbol(sym.STR, strStart, yychar + 1, strContent);}
<STRING>\n	{errorMsg.error(yychar, "no multiple lines allowed: " + yytext());}
<STRING>\r	{errorMsg.error(yychar, "no multiple lines allowed: " + yytext());}
<STRING>.	{strContent = strContent + yytext();}

<YYINITIAL>.	{errorMsg.error(yychar, "unmatched input: " + yytext());}

