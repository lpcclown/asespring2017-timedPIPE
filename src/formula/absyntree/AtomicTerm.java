package formula.absyntree;

import formula.parser.Visitor;

public class AtomicTerm extends AtomicFormula{
	public Term t;

	//z3
	public boolean isPostCond;
	public boolean isValidClause;

	public AtomicTerm(int p, Term t){
		this.pos = p;
		this.t = t;
	}
	
	public void accept(Visitor v){
		v.visit(this);
	}
}
