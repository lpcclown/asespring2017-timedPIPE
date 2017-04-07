package formula.absyntree;

import formula.parser.Visitor;

public class AExp extends SingleValuedExp<Number> {
	private final boolean parenthesized;
	public ArithExp ae;

	public AExp(int p, ArithExp ae){
		this(p, ae, false);
	}

	public AExp(final int p, final ArithExp pAe, final boolean pIsParenthesized) {
		pos = p;
		ae = pAe;
		parenthesized = pIsParenthesized;
	}

	public boolean isParenthesized() {
		return parenthesized;
	}

	public void accept(Visitor v){
		v.visit(this);
	}
}
