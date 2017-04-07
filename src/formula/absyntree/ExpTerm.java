package formula.absyntree;

import java.util.ArrayList;

import pipe.dataLayer.abToken;
import formula.parser.Visitor;

public class ExpTerm extends Term {
  public Exp e;
  public boolean bool_val;
  public double int_val;
  public abToken abTok;
  public String strPre = "";
  public String strPost = "";
  public boolean postcond = false;
  public String varName;
  public boolean isUserVariable = false;

	//z3
  public String z3str = "";
  public boolean isPostCond;
  public boolean isValidClause;
  public ArrayList<String> z3TermList = new ArrayList<String>();

  public ExpTerm(int p, Exp e) {
    this.pos = p;
    this.e = e;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

}
