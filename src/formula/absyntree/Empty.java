package formula.absyntree;

import formula.parser.Visitor;

public class Empty {
	public int pos;
	public Empty(int p){
		this.pos = p;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
