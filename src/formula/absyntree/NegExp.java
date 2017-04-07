package formula.absyntree;

import formula.parser.Visitor;

public class NegExp extends ArithExp {
  public Term t;

  //z3
  public String z3str = "";

  public NegExp(int p, Term t) {
    super(p, t);
    this.t = t;
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
