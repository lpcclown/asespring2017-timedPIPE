package formula.absyntree;

import formula.parser.Visitor;
import pipe.dataLayer.abToken;

public class Setdef extends SetExp {
  public Term u;
  public Variable v;
  public Formula sf;

  public Setdef(int p, Term u, Variable v, Formula sf) {
    this.pos = p;
    this.u = u;
    this.v = v;
    this.sf = sf;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }

}



