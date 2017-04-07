package formula.absyntree;

import formula.parser.Visitor;

public class RExp extends SingleValuedExp<Boolean> {
	private final boolean parenthesized;
	public RelExp re;
	public String strPre = "";
	public String strPost = "";
	
	//z3
	public boolean isPostCond = false;

	public RExp(int p, RelExp re){
		this(p, re, false);
	}

	public RExp(int p, RelExp re, final boolean isParenthesized){
		this.pos = p;
		this.re = re;
		parenthesized = isParenthesized;
	}

	public boolean isParenthesized() {
		return parenthesized;
	}
	
	public void accept(Visitor v){
		v.visit(this);
	}
}
