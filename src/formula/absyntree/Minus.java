package formula.absyntree;

import formula.parser.Visitor;

public class Minus extends ArithExp {

  public Minus(int p, Term t1, Term t2) {
    super(p, t1, t2);
  }

  public void accept(Visitor v) {
    v.visit(this);
  }
}
