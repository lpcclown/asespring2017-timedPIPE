package formula.absyntree;

import java.util.ArrayList;

import pipe.dataLayer.abToken;
import formula.parser.Visitor;

public class SExp extends Exp{
	public SetExp se;
	public abToken abTok;
	public String placeName;
	public String varName;
	public boolean isUserVariable = false;
	
	//z3
	public String z3str = "";
	public boolean isPostCond = false;
	public boolean isValidClause;
	public ArrayList<String> z3TermList = new ArrayList<String>();
	
	
//	public String str;
	public SExp(int p, SetExp se){
		this.pos = p;
		this.se = se;
	}
	public void accept(Visitor v){
		v.visit(this);
	}
}
