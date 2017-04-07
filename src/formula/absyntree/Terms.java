package formula.absyntree;
import java.util.ArrayList;

import pipe.dataLayer.Token;
import formula.parser.Visitor;
public class Terms {

	public Token Tok;
	int pos;
	public Term t;
	public TermRestList tr;
	public String str = "";
	public String placeName;
	
	//z3
	public ArrayList<String> z3TermList = new ArrayList<String>();
	
	
	public Terms(int p, Term t, TermRestList tr){
		this.pos = p;
		this.t = t;
		this.tr = tr;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
