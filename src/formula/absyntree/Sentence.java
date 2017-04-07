package formula.absyntree;

import formula.parser.Visitor;

public class Sentence {
  public int pos;
  public Formula f;
  public boolean bool_val = false;
  public String strPre = "";
  public String strPost = "";

  //z3
  public String z3str = "";

  public Sentence(int p, Formula f) {
    this.pos = p;
    this.f = f;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
